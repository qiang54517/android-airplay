package com.example.airplay.server.handlers

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

class HeartBeatHandler : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        // Handle keep-alive / heartbeat POST requests from the client
        if ("POST" == request.method().toString() && request.uri() == "/feedback") {
            val response = createResponseForRequest(request)
            return sendResponse(ctx, request, response)
        }
        return false
    }
}
