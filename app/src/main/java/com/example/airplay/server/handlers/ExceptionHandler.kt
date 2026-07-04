package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.rtsp.RtspResponseStatuses

class ExceptionHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        try {
            ctx.fireChannelRead(msg)
        } catch (e: Exception) {
            AirPlayLogger.e("❌ ❌ ❌ ExceptionHandler: CAUGHT UNCAUGHT EXCEPTION!")
            AirPlayLogger.e("Exception type: ${e.javaClass.name}")
            AirPlayLogger.e("Exception message: ${e.message}")
            AirPlayLogger.e("Full stack trace:")
            e.stackTrace.forEach { element ->
                AirPlayLogger.e("  at $element")
            }
            e.printStackTrace()
            
            // 尝试发送错误响应
            try {
                if (msg is FullHttpRequest) {
                    val response = DefaultFullHttpResponse(
                        msg.protocolVersion(),
                        RtspResponseStatuses.INTERNAL_SERVER_ERROR
                    )
                    response.headers().set("CSeq", msg.headers().get("CSeq", "1"))
                    ctx.writeAndFlush(response)
                    AirPlayLogger.i("ExceptionHandler: Sent error response to client")
                }
            } catch (e2: Exception) {
                AirPlayLogger.e("❌ ExceptionHandler: Failed to send error response: ${e2.message}")
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        AirPlayLogger.e("❌ ❌ ❌ ExceptionHandler: exceptionCaught!")
        AirPlayLogger.e("Exception type: ${cause.javaClass.name}")
        AirPlayLogger.e("Exception message: ${cause.message}")
        AirPlayLogger.e("Full stack trace:")
        cause.stackTrace.forEach { element ->
            AirPlayLogger.e("  at $element")
        }
        cause.printStackTrace()
    }
}
