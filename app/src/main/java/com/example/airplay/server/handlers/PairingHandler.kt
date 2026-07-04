package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import com.example.airplay.server.AirPlayConfig
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import java.io.ByteArrayOutputStream

class PairingHandler(
    private val config: AirPlayConfig
) : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        AirPlayLogger.d("PairingHandler checking request: ${request.method()} ${request.uri()}")
        
        val response = createResponseForRequest(request)

        try {
            when (request.uri()) {
                "/info" -> {
                    AirPlayLogger.i("✅ Handling /info request")
                    val out = ByteArrayOutputStream()
                    try {
                        session.airPlay.info(config.width, config.height, config.fps, out)
                        AirPlayLogger.i("✅ /info response length: ${out.size()} bytes")
                        AirPlayLogger.d("/info response (hex): ${out.toByteArray().take(100).joinToString(" ") { "%02x".format(it) }}...")
                    } catch (e: Exception) {
                        AirPlayLogger.e("❌ Error in session.airPlay.info: ${e.message}")
                        AirPlayLogger.e("Exception type: ${e.javaClass.name}")
                        e.printStackTrace()
                        // 即使出错也返回响应
                    }
                    response.content().writeBytes(out.toByteArray())
                    return sendResponse(ctx, request, response)
                }
                "/pair-setup" -> {
                    AirPlayLogger.i("✅ Handling /pair-setup request")
                    val requestContent = ByteArray(request.content().readableBytes())
                    request.content().getBytes(0, requestContent)
                    AirPlayLogger.d("/pair-setup request content (hex): ${requestContent.joinToString(" ") { "%02x".format(it) }}")
                    
                    val out = ByteArrayOutputStream()
                    try {
                        session.airPlay.pairSetup(out)
                        AirPlayLogger.i("✅ /pair-setup response length: ${out.size()} bytes")
                    } catch (e: Exception) {
                        AirPlayLogger.e("❌ Error in session.airPlay.pairSetup: ${e.message}")
                        e.printStackTrace()
                    }
                    response.content().writeBytes(out.toByteArray())
                    return sendResponse(ctx, request, response)
                }
                "/pair-verify" -> {
                    AirPlayLogger.i("✅ Handling /pair-verify request")
                    val requestContent = ByteArray(request.content().readableBytes())
                    request.content().getBytes(0, requestContent)
                    AirPlayLogger.d("/pair-verify request content (hex): ${requestContent.joinToString(" ") { "%02x".format(it) }}")
                    
                    val out = ByteArrayOutputStream()
                    try {
                        session.airPlay.pairVerify(request.content().toInputStream(), out)
                        if (session.airPlay.isPairVerified()) {
                            session.pairingDone = true
                            AirPlayLogger.i("✅ ✅ Pairing verified successfully!")
                        } else {
                            AirPlayLogger.w("⚠️ Pairing verification failed!")
                        }
                    } catch (e: Exception) {
                        AirPlayLogger.e("❌ Error in session.airPlay.pairVerify: ${e.message}")
                        e.printStackTrace()
                    }
                    response.content().writeBytes(out.toByteArray())
                    AirPlayLogger.i("/pair-verify response length: ${out.size()} bytes")
                    return sendResponse(ctx, request, response)
                }
            }
        } catch (e: Exception) {
            AirPlayLogger.e("❌ ❌ Error handling ${request.uri()}: ${e.message}")
            AirPlayLogger.e("Exception type: ${e.javaClass.name}")
            e.printStackTrace()
            
            // 返回错误响应，防止连接挂起
            return sendResponse(ctx, request, response)
        }
        return false
    }
}
