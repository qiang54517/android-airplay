package com.example.airplay.server.handlers

import com.example.airplay.protocol.AirPlay
import com.example.airplay.server.AirPlayConsumer
import com.example.airplay.utils.AirPlayLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.ReplayingDecoder

class VideoDecoder : ReplayingDecoder<VideoDecoder.DecoderState>(DecoderState.READ_HEADER) {
    enum class DecoderState { READ_HEADER, READ_PAYLOAD }

    private var payloadSize = 0
    private var payloadType: Short = 0
    private var packetCount = 0

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        when (state()) {
            DecoderState.READ_HEADER -> {
                val headerBuf = input.readSlice(128)
                payloadSize = headerBuf.readUnsignedIntLE().toInt()
                payloadType = (headerBuf.readUnsignedShortLE() and 0xff).toShort()
                
                if (packetCount % 100 == 0) {
                    AirPlayLogger.d("VideoDecoder: Read header, payloadSize=$payloadSize, payloadType=$payloadType, packetCount=$packetCount")
                }
                packetCount++
                
                checkpoint(DecoderState.READ_PAYLOAD)
            }
            DecoderState.READ_PAYLOAD -> {
                if (payloadType.toInt() == 0 || payloadType.toInt() == 1) {
                    val payloadBytes = ByteArray(payloadSize)
                    input.readBytes(payloadBytes)
                    checkpoint(DecoderState.READ_HEADER)
                    out.add(VideoPacket(payloadType, payloadSize, payloadBytes))
                } else {
                    input.skipBytes(payloadSize)
                    checkpoint(DecoderState.READ_HEADER)
                }
            }
        }
    }
}

class VideoPacket(
    val type: Short,
    val size: Int,
    val data: ByteArray
)

class VideoHandler(
    private val airPlay: AirPlay,
    private val dataConsumer: AirPlayConsumer
) : MessageToMessageDecoder<VideoPacket>() {
    
    private var frameCount = 0
    private var totalBytes = 0L
    
    override fun decode(ctx: ChannelHandlerContext, msg: VideoPacket, out: MutableList<Any>) {
        frameCount++
        totalBytes += msg.size
        
        // Log first 5 frames and every 100th frame with detail (BEFORE decrypt)
        if (frameCount <= 5 || frameCount % 100 == 0) {
            val beforeHex = if (msg.data.size >= 16) {
                (0 until 16).joinToString(" ") { "%02x".format(msg.data[it].toInt() and 0xFF) }
            } else {
                (0 until msg.data.size).joinToString(" ") { "%02x".format(msg.data[it].toInt() and 0xFF) }
            }
            AirPlayLogger.i("VideoHandler: Frame #$frameCount BEFORE decrypt: type=${msg.type}, size=${msg.size}, hex=[$beforeHex]")
        }
        
        // Decrypt video data
        try {
            airPlay.decryptVideo(msg.data)
        } catch (e: Exception) {
            AirPlayLogger.e("VideoHandler: decryptVideo FAILED on frame #$frameCount: ${e.javaClass.simpleName}: ${e.message}")
        }
        
        // Log AFTER decrypt for comparison (first 5 + every 100)
        if (frameCount <= 5 || frameCount % 100 == 0) {
            val afterHex = if (msg.data.size >= 16) {
                (0 until 16).joinToString(" ") { "%02x".format(msg.data[it].toInt() and 0xFF) }
            } else {
                (0 until msg.data.size).joinToString(" ") { "%02x".format(msg.data[it].toInt() and 0xFF) }
            }
            AirPlayLogger.i("VideoHandler: Frame #$frameCount AFTER  decrypt: size=${msg.size}, hex=[$afterHex]")
        }
        
        // Send to consumer for decoding + rendering
        dataConsumer.onVideo(msg.data)
    }
}
