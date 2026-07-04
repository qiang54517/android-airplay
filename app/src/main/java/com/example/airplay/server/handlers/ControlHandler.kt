package com.example.airplay.server.handlers

import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import io.netty.handler.codec.rtsp.RtspMethods
import io.netty.handler.codec.rtsp.RtspResponseStatuses
import io.netty.util.AsciiString
import java.nio.charset.StandardCharsets

abstract class ControlHandler : ChannelInboundHandlerAdapter() {

    protected abstract fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is FullHttpRequest) {
            try {
                // 记录所有的RTSP请求
                val headers = StringBuilder()
                msg.headers().forEach { header ->
                    headers.append("${header.key}: ${header.value}\n")
                }
                
                AirPlayLogger.logRequest(
                    method = msg.method().toString(),
                    uri = msg.uri(),
                    headers = headers.toString(),
                    contentLength = msg.content().readableBytes()
                )
                
                // 记录请求内容（如果是小包）
                if (msg.content().readableBytes() > 0 && msg.content().readableBytes() < 1024) {
                    val content = ByteArray(msg.content().readableBytes())
                    msg.content().getBytes(0, content)
                    AirPlayLogger.d("Request Content (hex): ${bytesToHex(content)}")
                    try {
                        val contentStr = String(content, StandardCharsets.UTF_8)
                        if (contentStr.isNotEmpty() && contentStr[0] != '\u0000') {
                            AirPlayLogger.d("Request Content (string): $contentStr")
                        }
                    } catch (e: Exception) {
                        // 二进制内容，忽略
                    }
                }

                val session = ctx.channel().attr(SESSION_KEY).get() ?: Session()
                ctx.channel().attr(SESSION_KEY).set(session)

                // 记录当前是哪个Handler在处理
                val handlerName = this.javaClass.simpleName
                AirPlayLogger.d("🔍 [$handlerName] About to call handleRequest for: ${msg.method()} ${msg.uri()}")
                
                // 检查msg是否可读
                if (msg.content().refCnt() <= 0) {
                    AirPlayLogger.e("❌ [$handlerName] ERROR: msg.content().refCnt() = ${msg.content().refCnt()}, content may be released!")
                }
                
                // 记录哪个Handler处理了请求
                val handled = handleRequest(ctx, session, msg)
                
                AirPlayLogger.d("🔍 [$handlerName] handleRequest returned: $handled for ${msg.method()} ${msg.uri()}")
                
                if (!handled) {
                    AirPlayLogger.d("⬇️ [$handlerName] Passing request to next handler in pipeline: ${msg.method()} ${msg.uri()}")
                    ctx.fireChannelRead(msg)
                    AirPlayLogger.d("⬆️ [$handlerName] Passed to next handler (fireChannelRead completed)")
                } else {
                    AirPlayLogger.d("✅ [$handlerName] Request handled successfully: ${msg.method()} ${msg.uri()}")
                }
            } catch (e: Exception) {
                AirPlayLogger.e("❌ Error in ControlHandler.channelRead: ${e.message}")
                AirPlayLogger.e("Exception type: ${e.javaClass.name}")
                AirPlayLogger.e("Stack trace: ${e.stackTrace.take(5).joinToString("\n  ") { it.toString() }}")
                e.printStackTrace()
                
                // 返回错误响应，防止连接挂起
                try {
                    val response = DefaultFullHttpResponse(
                        msg.protocolVersion(),
                        RtspResponseStatuses.INTERNAL_SERVER_ERROR
                    )
                    response.headers().set(AsciiString("CSeq"), msg.headers().get(AsciiString("CSeq"), "1"))
                    ctx.writeAndFlush(response)
                } catch (e2: Exception) {
                    AirPlayLogger.e("❌ Failed to send error response: ${e2.message}")
                }
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }
    
    protected fun sendResponse(
        ctx: ChannelHandlerContext,
        request: FullHttpRequest,
        response: FullHttpResponse
    ): Boolean {
        try {
            response.headers().set(AsciiString("CSeq"), request.headers().get(AsciiString("CSeq"), "1"))
            response.headers().set(HttpHeaderNames.SERVER, "AirTunes/220.68")
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
            // 如果没有设置Content-Type，默认设置为application/octet-stream
            if (response.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream")
            }
            
            // 记录响应
            val headers = StringBuilder()
            response.headers().forEach { header ->
                headers.append("${header.key}: ${header.value}\n")
            }
            
            AirPlayLogger.logResponse(
                status = response.status().toString(),
                headers = headers.toString(),
                contentLength = response.content().readableBytes()
            )
            
            // 记录响应内容（如果是小包）
            if (response.content().readableBytes() > 0 && response.content().readableBytes() < 1024) {
                val content = ByteArray(response.content().readableBytes())
                response.content().getBytes(0, content)
                AirPlayLogger.d("Response Content (hex): ${bytesToHex(content)}")
            }
            
            ctx.writeAndFlush(response)
            return true
        } catch (e: Exception) {
            AirPlayLogger.e("❌ Error in sendResponse: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    protected fun createResponseForRequest(request: FullHttpRequest): FullHttpResponse {
        return DefaultFullHttpResponse(
            request.protocolVersion(),
            RtspResponseStatuses.OK
        )
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02x".format(it) }
    }
    
    companion object {
        val SESSION_KEY = io.netty.util.AttributeKey.valueOf<Session>("session")
    }
}
