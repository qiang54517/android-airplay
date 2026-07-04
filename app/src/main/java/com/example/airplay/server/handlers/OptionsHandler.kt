package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.rtsp.RtspMethods
import io.netty.util.AsciiString

class OptionsHandler : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        AirPlayLogger.d("OptionsHandler: checking request ${request.method()} ${request.uri()}")
        
        // 只处理 OPTIONS 请求
        if (RtspMethods.OPTIONS != request.method()) {
            AirPlayLogger.d("OptionsHandler: not OPTIONS, passing to next handler")
            return false
        }
        
        AirPlayLogger.i("✅ OptionsHandler: Handling OPTIONS request")
        
        val response = createResponseForRequest(request)

        // Public header lists supported RTSP methods
        response.headers().set(
            AsciiString("Public"),
            AsciiString("ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, GET_PARAMETER, SET_PARAMETER, OPTIONS")
        )

        AirPlayLogger.d("OptionsHandler: sending OPTIONS response")
        
        return sendResponse(ctx, request, response)
    }
}
