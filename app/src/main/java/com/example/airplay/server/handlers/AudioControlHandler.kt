package com.example.airplay.server.handlers

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket

class AudioControlHandler : SimpleChannelInboundHandler<DatagramPacket>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val content = msg.content()
        val contentLength = content.readableBytes()
        val contentBytes = ByteArray(contentLength)
        content.readBytes(contentBytes)
        val type = contentBytes[1].toInt() and 0x7F.inv()
        // Audio control timing packets - just acknowledge them
    }
}
