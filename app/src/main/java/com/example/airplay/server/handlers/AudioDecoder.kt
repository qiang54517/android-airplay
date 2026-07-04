package com.example.airplay.server.handlers

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

class AudioDecoder : MessageToMessageDecoder<ByteBuf>() {
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val headerBytes = ByteArray(12)
        msg.readBytes(headerBytes)

        val flag = headerBytes[0].toInt() and 0xFF
        val type = headerBytes[1].toInt() and 0x7F
        val seqNumber = ((headerBytes[2].toInt() and 0xFF) shl 8) or (headerBytes[3].toInt() and 0xFF)
        val timestamp = ((headerBytes[7].toInt() and 0xFF).toLong() or
                ((headerBytes[6].toInt() and 0xFF).toLong() shl 8) or
                ((headerBytes[5].toInt() and 0xFF).toLong() shl 16) or
                ((headerBytes[4].toInt() and 0xFF).toLong() shl 24))
        val ssrc = ((headerBytes[11].toInt() and 0xFF).toLong() or
                ((headerBytes[10].toInt() and 0xFF).toLong() shl 8) or
                ((headerBytes[9].toInt() and 0xFF).toLong() shl 16) or
                ((headerBytes[8].toInt() and 0xFF).toLong() shl 24))

        val encodedAudioSize = msg.readableBytes()
        val packet = AudioPacket(
            flag = flag,
            type = type,
            sequenceNumber = seqNumber,
            timestamp = timestamp,
            ssrc = ssrc,
            encodedAudioSize = encodedAudioSize
        )
        msg.readBytes(packet.encodedAudio, 0, encodedAudioSize)
        out.add(packet)
    }
}
