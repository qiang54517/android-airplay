package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

class FairPlayHandler : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        AirPlayLogger.d("FairPlayHandler: checking request ${request.method()} ${request.uri()}")
        
        val response = createResponseForRequest(request)

        try {
            when (request.uri()) {
                "/fp-setup" -> {
                    AirPlayLogger.i("✅ FairPlayHandler: Handling /fp-setup request")
                    val requestContent = ByteArray(request.content().readableBytes())
                    request.content().getBytes(0, requestContent)
                    AirPlayLogger.d("FairPlayHandler: /fp-setup request content (hex): ${requestContent.take(50).joinToString(" ") { "%02x".format(it) }}...")
                    
                    // TODO: 实现FairPlay setup逻辑
                    AirPlayLogger.w("⚠️ FairPlayHandler: /fp-setup not fully implemented, returning empty response")
                    
                    return sendResponse(ctx, request, response)
                }
                "/fp-getkey" -> {
                    AirPlayLogger.i("✅ FairPlayHandler: Handling /fp-getkey request")
                    // TODO: 实现FairPlay getkey逻辑
                    AirPlayLogger.w("⚠️ FairPlayHandler: /fp-getkey not implemented")
                    return sendResponse(ctx, request, response)
                }
            }
        } catch (e: Exception) {
            AirPlayLogger.e("❌ FairPlayHandler: Error handling ${request.uri()}: ${e.message}")
            AirPlayLogger.e("Exception type: ${e.javaClass.name}")
            e.printStackTrace()
            return sendResponse(ctx, request, response)
        }
        return false
    }
}
