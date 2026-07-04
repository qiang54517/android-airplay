package com.example.airplay.player

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import java.nio.ByteBuffer

class VideoDecoder(private val surface: Surface) {

    private var codec: MediaCodec? = null
    private var configured = false

    fun configure(width: Int, height: Int) {
        release()

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

        codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec?.configure(format, surface, null, 0)
        codec?.start()
        configured = true
    }

    fun feedData(data: ByteArray, timestamp: Long = 0) {
        if (!configured) return

        val codec = codec ?: return
        try {
            val inputIndex = codec.dequeueInputBuffer(10_000)
            if (inputIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputIndex) ?: return
                inputBuffer.clear()
                inputBuffer.put(data)
                codec.queueInputBuffer(inputIndex, 0, data.size, timestamp, 0)

                // Pull decoded frames
                val bufferInfo = MediaCodec.BufferInfo()
                var outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                while (outputIndex >= 0) {
                    codec.releaseOutputBuffer(outputIndex, true)
                    outputIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }
        } catch (_: Exception) {
            // Buffer full or codec error - skip frame
        }
    }

    fun release() {
        try {
            codec?.stop()
            codec?.release()
        } catch (_: Exception) {}
        codec = null
        configured = false
    }
}
