package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class FairPlayHandler : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        AirPlayLogger.d("FairPlayHandler: checking request ${request.method()} ${request.uri()}")

        val response = createResponseForRequest(request)
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream")

        try {
            when (request.uri()) {
                "/fp-setup" -> {
                    AirPlayLogger.i("✅ FairPlayHandler: Handling /fp-setup request")

                    val requestContent = ByteArray(request.content().readableBytes())
                    request.content().getBytes(0, requestContent)
                    AirPlayLogger.d("FairPlayHandler: /fp-setup request size: ${requestContent.size} bytes")
                    AirPlayLogger.d("FairPlayHandler: /fp-setup request content (hex): ${requestContent.take(50).joinToString(" ") { "%02x".format(it) }}...")

                    val out = ByteArrayOutputStream()
                    val inputStream = ByteArrayInputStream(requestContent)
                    session.airPlay.fairPlaySetup(inputStream, out)

                    val responseBytes = out.toByteArray()
                    AirPlayLogger.i("✅ /fp-setup response length: ${responseBytes.size} bytes")
                    if (responseBytes.isNotEmpty()) {
                        AirPlayLogger.d("/fp-setup response (hex): ${responseBytes.take(100).joinToString(" ") { "%02x".format(it) }}")
                    } else {
                        AirPlayLogger.w("⚠️ /fp-setup response is empty!")
                    }

                    response.content().writeBytes(responseBytes)
                    return sendResponse(ctx, request, response)
                }
                "/fp-getkey" -> {
                    AirPlayLogger.i("✅ FairPlayHandler: Handling /fp-getkey request")
                    AirPlayLogger.w("⚠️ FairPlayHandler: /fp-getkey not implemented")
                    return sendResponse(ctx, request, response)
                }
            }
        } catch (e: Exception) {
            AirPlayLogger.e("❌ FairPlayHandler: Error handling ${request.uri()}: ${e.message}")
            AirPlayLogger.e("Exception type: ${e.javaClass.name}")
            AirPlayLogger.e("Stack trace: ${e.stackTrace.take(5).joinToString("\n  ") { it.toString() }}")
            e.printStackTrace()
            return sendResponse(ctx, request, response)
        }
        return false
    }
}
