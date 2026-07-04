package com.example.airplay.player

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.example.airplay.utils.AirPlayLogger

class VideoDecoder(private val surface: Surface) {

    private var codec: MediaCodec? = null
    internal var configured = false
    private var totalFramesFed = 0
    private var totalFramesDecoded = 0
    private var totalErrors = 0
    
    // H.264 SPS/PPS cache for prepend to IDR frames
    private var spsPpsData: ByteArray? = null
    
    // NAL type counters for diagnostics
    private var countSPS = 0
    private var countPPS = 0
    private var countIDR = 0
    private var countNonIDR = 0
    private var countOtherNAL = 0
    private var lastHexDumpFrame = 0
    
    companion object {
        private const val TAG = "AirPlayVideo"
        // H.264 NAL unit start code
        private val NAL_START_CODE = byteArrayOf(0x00, 0x00, 0x00, 0x01.toByte())
        
        // NAL unit types
        private const val NAL_TYPE_SPS = 7
        private const val NAL_TYPE_PPS = 8
        private const val NAL_TYPE_IDR = 5
        
        init {
            AirPlayLogger.i("VideoDecoder: initialized class")
        }
    }

    fun configure(width: Int, height: Int) {
        release()
        
        AirPlayLogger.i("VideoDecoder: configure($width x $height), surface=$surface, isValid=${surface.isValid}")

        val format = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            width,
            height
        ).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, width * height * 30)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        try {
            codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            AirPlayLogger.i("VideoDecoder: MediaCodec created: ${codec?.name}")
            
            codec?.configure(format, surface, null, 0)
            codec?.start()
            configured = true
            totalFramesFed = 0
            totalFramesDecoded = 0
            totalErrors = 0
            countSPS = 0
            countPPS = 0
            countIDR = 0
            countNonIDR = 0
            countOtherNAL = 0
            
            AirPlayLogger.i("VideoDecoder: codec started successfully, ready for video data")
        } catch (e: Exception) {
            AirPlayLogger.e("VideoDecoder: FAILED to configure/start MediaCodec: ${e.message}")
            Log.e(TAG, "MediaCodec config error", e)
        }
    }

    /**
     * Feed raw H.264 data to the decoder.
     * Handles NAL unit reassembly and start-code prefixing.
     */
    fun feedData(data: ByteArray, timestamp: Long = 0) {
        if (!configured) {
            // Log once per 100 calls to avoid spam
            if (totalFramesFed % 100 == 0) {
                AirPlayLogger.w("VideoDecoder: feedData called but NOT configured! (totalFramesFed=$totalFramesFed)")
            }
            return
        }

        val codec = codec ?: run {
            if (totalErrors < 5) AirPlayLogger.e("VideoDecoder: codec is null")
            return
        }
        
        totalFramesFed++
        
        try {
            // Parse NAL units from the packet and assemble frames
            val nalUnits = extractNalUnits(data)
            
            for (nalUnit in nalUnits) {
                val nalType = (nalUnit[0].toInt() and 0x1F)
                
                when (nalType) {
                    NAL_TYPE_SPS, NAL_TYPE_PPS -> {
                        if (nalType == NAL_TYPE_SPS) countSPS++ else countPPS++
                        // Cache SPS/PPS for later prepend to IDR frames
                        AirPlayLogger.d("VideoDecoder: Got ${if (nalType == NAL_TYPE_SPS) "SPS" else "PPS"}, size=${nalUnit.size}")
                        appendToSpsPpsCache(nalUnit)
                    }
                    else -> {
                        // Track NAL types
                        when (nalType) {
                            NAL_TYPE_IDR -> countIDR++
                            in 1..4 -> countNonIDR++
                            else -> countOtherNAL++
                        }
                        
                        // Hex dump first few non-SPS/PPS NAL units for diagnosis
                        if (totalFramesFed <= 5 || (countOtherNAL + countNonIDR + countIDR) % 100 == 1) {
                            val hex = if (nalUnit.size >= 20) {
                                (0 until 20).joinToString(" ") { "%02x".format(nalUnit[it].toInt() and 0xFF) }
                            } else {
                                (0 until nalUnit.size).joinToString(" ") { "%02x".format(nalUnit[it].toInt() and 0xFF) }
                            }
                            AirPlayLogger.i("VideoDecoder: Feeding NAL type=$nalType (${when(nalType) {
                                NAL_TYPE_IDR -> "IDR"
                                in 1..4 -> "non-IDR"
                                else -> "other"
                            }}), size=${nalUnit.size}, hex first 20: [$hex]")
                        }
                        
                        // Build complete access unit with SPS/PPS + start codes
                        val accessUnit = buildAccessUnit(nalUnit, nalType == NAL_TYPE_IDR)
                        
                        // Feed to MediaCodec
                        feedToCodec(codec, accessUnit, timestamp)
                    }
                }
            }
        } catch (e: Exception) {
            totalErrors++
            if (totalErrors <= 10) {
                AirPlayLogger.e("VideoDecoder: Error in feedData (#$totalFramesFed): ${e.javaClass.simpleName}: ${e.message}")
            } else if (totalErrors == 11) {
                AirPlayLogger.e("VideoDecoder: Suppressing further error logs (total errors: $totalErrors)")
            }
            Log.e(TAG, "feedData error", e)
        }
    }
    
    /**
     * Extract individual NAL units from a byte array.
     * Handles both start-code-prefixed and length-prefixed formats.
     */
    private fun extractNalUnits(data: ByteArray): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        var i = 0
        
        while (i < data.size) {
            // Find start code (00 00 00 01 or 00 00 01)
            val startCodeLen = findStartCode(data, i)
            
            if (startCodeLen > 0) {
                i += startCodeLen
                
                // Find next start code or end of data
                var endPos = findNextStartCode(data, i)
                if (endPos < 0) endPos = data.size
                
                // Extract NAL unit (without start code, just the NAL header + payload)
                val nalUnit = data.copyOfRange(i, endPos)
                if (nalUnit.isNotEmpty()) {
                    result.add(nalUnit)
                }
                
                i = endPos
            } else {
                // No start code found - treat remaining data as one NAL unit
                if (i < data.size) {
                    result.add(data.copyOfRange(i, data.size))
                }
                break
            }
        }
        
        // If no NAL units were extracted with start codes, treat entire data as one unit
        if (result.isEmpty() && data.isNotEmpty()) {
            result.add(data)
        }
        
        return result
    }
    
    /**
     * Find H.264 start code at given position.
     * Returns length of start code (3 or 4), or 0 if not found.
     */
    private fun findStartCode(data: ByteArray, offset: Int): Int {
        if (offset + 3 > data.size) return 0
        if (data[offset].toInt() == 0 && data[offset + 1].toInt() == 0) {
            if (data[offset + 2].toInt() == 1) return 3
            if (offset + 3 < data.size && data[offset + 2].toInt() == 0 && data[offset + 3].toInt() == 1) return 4
        }
        return 0
    }
    
    /**
     * Find the next start code after the given position.
     */
    private fun findNextStartCode(data: ByteArray, offset: Int): Int {
        var i = offset
        while (i + 2 < data.size) {
            if (data[i].toInt() == 0 && data[i + 1].toInt() == 0) {
                if (data[i + 2].toInt() == 1) return i
                if (i + 3 < data.size && data[i + 2].toInt() == 0 && data[i + 3].toInt() == 1) return i
            }
            i++
        }
        return -1
    }
    
    private fun appendToSpsPpsCache(nalUnit: ByteArray) {
        val buf = ByteArrayOutputStream()
        spsPpsData?.let { buf.write(it) }
        buf.write(NAL_START_CODE)
        buf.write(nalUnit)
        spsPpsData = buf.toByteArray()
        AirPlayLogger.d("VideoDecoder: SPS/PPS cache updated, total size=${spsPpsData?.size ?: 0}")
    }
    
    /**
     * Build a complete H.264 access unit with proper start codes.
     */
    private fun buildAccessUnit(nalUnit: ByteArray, isIdr: Boolean): ByteArray {
        val buf = ByteArrayOutputStream()
        
        // Prepend SPS/PPS for IDR frames
        if (isIdr && spsPpsData != null) {
            buf.write(spsPpsData!!)
        }
        
        // Add start code + NAL unit
        buf.write(NAL_START_CODE)
        buf.write(nalUnit)
        
        return buf.toByteArray()
    }
    
    /**
     * Feed a prepared access unit to MediaCodec.
     */
    private fun feedToCodec(codec: MediaCodec, accessUnit: ByteArray, timestamp: Long) {
        val inputIndex = codec.dequeueInputBuffer(10_000)
        if (inputIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputIndex) ?: return
            inputBuffer.clear()
            
            if (accessUnit.size <= inputBuffer.capacity()) {
                inputBuffer.put(accessUnit)
                codec.queueInputBuffer(inputIndex, 0, accessUnit.size, timestamp, 0)
                
                // Pull decoded frames
                pullDecodedFrames(codec)
            } else {
                AirPlayLogger.w("VideoDecoder: Access unit too large (${accessUnit.size} > ${inputBuffer.capacity()})")
            }
        } else {
            AirPlayLogger.w("VideoDecoder: No input buffer available (timed out after 10s)")
        }
    }
    
    /**
     * Pull decoded frames from MediaCodec output.
     */
    private fun pullDecodedFrames(codec: MediaCodec) {
        val bufferInfo = MediaCodec.BufferInfo()
        try {
            var outputIndex = codec.dequeueOutputBuffer(bufferInfo, 5_000)
            while (outputIndex >= 0) {
                totalFramesDecoded++
                
                if (totalFramesDecoded <= 5 || totalFramesDecoded % 100 == 0) {
                    AirPlayLogger.i("VideoDecoder: Rendered frame #$totalFramesDecoded (size=${bufferInfo.size}, pts=${bufferInfo.presentationTimeUs})")
                }
                
                // Release to surface (render=true)
                codec.releaseOutputBuffer(outputIndex, true)
                outputIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            }
            
            // Handle special status codes
            when (outputIndex) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    AirPlayLogger.i("VideoDecoder: Output format changed: ${codec.outputFormat}")
                }
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    // Normal: no output available yet
                }
            }
        } catch (e: Exception) {
            totalErrors++
            if (totalErrors <= 5) {
                AirPlayLogger.e("VideoDecoder: Error pulling frames: ${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    fun release() {
        AirPlayLogger.i("VideoDecoder: release() called (fed=$totalFramesFed, decoded=$totalFramesDecoded, errors=$totalErrors, SPS=$countSPS, PPS=$countPPS, IDR=$countIDR, nonIDR=$countNonIDR, otherNAL=$countOtherNAL)")
        try {
            codec?.stop()
            codec?.release()
        } catch (_: Exception) {}
        codec = null
        configured = false
        spsPpsData = null
    }
}
