package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

class FallbackHandler : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        AirPlayLogger.e("🚨🚨🚨 FallbackHandler: UNHANDLED REQUEST! This is a BUG!")
        AirPlayLogger.e("🚨 FallbackHandler: Method=${request.method()}, URI=${request.uri()}")
        AirPlayLogger.e("🚨 FallbackHandler: This request was NOT handled by OptionsHandler, PairingHandler, FairPlayHandler, or RTSPHandler!")
        AirPlayLogger.e("🚨 FallbackHandler: Check the logs above to see which handler saw this request")
        
        // 打印pipeline中的所有handler
        val pipeline = ctx.channel().pipeline()
        AirPlayLogger.e("🚨 Pipeline handlers: ${pipeline.names()}")
        
        val response = createResponseForRequest(request)
        return sendResponse(ctx, request, response)
    }
}
