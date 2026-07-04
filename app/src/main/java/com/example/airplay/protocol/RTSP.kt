package com.example.airplay.protocol

import com.dd.plist.BinaryPropertyListParser
import com.dd.plist.BinaryPropertyListWriter
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import java.io.InputStream
import java.io.OutputStream

class RTSP {
    private var streamConnectionID: String? = null
    private var encryptedAESKey: ByteArray? = null
    private var eiv: ByteArray? = null

    @Throws(Exception::class)
    fun getMediaStreamInfo(payload: InputStream): MediaStreamInfo? {
        val rtspSetup = BinaryPropertyListParser.parse(payload) as NSDictionary

        if (rtspSetup.containsKey("streams")) {
            val streams = rtspSetup["streams"]!!.toJavaObject()
            val stream = (streams as Array<*>)[0] as Map<*, *>
            val type = (stream["type"] as Number).toInt()

            return when (type) {
                110 -> {
                    if (stream.containsKey("streamConnectionID")) {
                        streamConnectionID = (stream["streamConnectionID"] as Number).toLong().toULong().toString()
                    }
                    VideoStreamInfo(streamConnectionID)
                }
                96 -> {
                    val compressionType = if (stream.containsKey("ct")) {
                        val ct = (stream["ct"] as Number).toInt()
                        AudioStreamInfo.CompressionType.fromCode(ct)
                    } else null

                    val audioFormat = if (stream.containsKey("audioFormat")) {
                        val afCode = (stream["audioFormat"] as Number).toLong()
                        AudioStreamInfo.AudioFormat.fromCode(afCode)
                    } else null

                    val samplesPerFrame = if (stream.containsKey("spf")) {
                        (stream["spf"] as Number).toInt()
                    } else null

                    AudioStreamInfo(audioFormat, compressionType, samplesPerFrame)
                }
                else -> null
            }
        }
        return null
    }

    @Throws(Exception::class)
    fun setup(request: InputStream) {
        val req = BinaryPropertyListParser.parse(request) as NSDictionary

        if (req.containsKey("ekey")) {
            encryptedAESKey = req["ekey"]!!.toJavaObject() as ByteArray
        }
        if (req.containsKey("eiv")) {
            eiv = req["eiv"]!!.toJavaObject() as ByteArray
        }
    }

    fun setupVideo(out: OutputStream, videoDataPort: Int, videoEventPort: Int, videoTimingPort: Int) {
        val streams = NSArray(1)
        val dataStream = NSDictionary()
        dataStream["dataPort"] = NSNumber(videoDataPort)
        dataStream["type"] = NSNumber(110)
        streams.setValue(0, dataStream)

        val response = NSDictionary()
        response["streams"] = streams
        response["eventPort"] = NSNumber(videoEventPort)
        response["timingPort"] = NSNumber(videoTimingPort)
        BinaryPropertyListWriter.write(response, out)
    }

    fun setupAudio(out: OutputStream, audioDataPort: Int, audioControlPort: Int) {
        val streams = NSArray(1)
        val dataStream = NSDictionary()
        dataStream["dataPort"] = NSNumber(audioDataPort)
        dataStream["type"] = NSNumber(96)
        dataStream["controlPort"] = NSNumber(audioControlPort)
        streams.setValue(0, dataStream)

        val response = NSDictionary()
        response["streams"] = streams
        BinaryPropertyListWriter.write(response, out)
    }

    fun getStreamConnectionID() = streamConnectionID
    fun getEncryptedAESKey() = encryptedAESKey
    fun getEiv() = eiv
}
