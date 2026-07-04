package com.example.airplay.server.handlers

import com.example.airplay.protocol.AudioStreamInfo
import com.example.airplay.protocol.MediaStreamInfo
import com.example.airplay.protocol.VideoStreamInfo
import com.example.airplay.server.AirPlayConsumer
import com.example.airplay.server.AudioControlServer
import com.example.airplay.server.AudioReceiver
import com.example.airplay.server.VideoReceiver
import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.rtsp.RtspMethods
import java.io.ByteArrayOutputStream

class RTSPHandler(
    private val airTunesPort: Int,
    private val airPlayConsumer: AirPlayConsumer
) : ControlHandler() {

    override fun handleRequest(
        ctx: ChannelHandlerContext,
        session: Session,
        request: FullHttpRequest
    ): Boolean {
        val response = createResponseForRequest(request)

        if (RtspMethods.ANNOUNCE == request.method()) {
            // 处理ANNOUNCE请求 - 记录SDP信息，但不需要特殊处理
            val content = ByteArray(request.content().readableBytes())
            request.content().getBytes(0, content)
            val sdp = String(content, Charsets.UTF_8)
            AirPlayLogger.i("Handling ANNOUNCE request")
            AirPlayLogger.d("SDP content:\n$sdp")
            
            // AirPlay协议中，ANNOUNCE只是通知SDP信息，不需要特殊处理
            // 只需要返回200 OK即可
            AirPlayLogger.i("ANNOUNCE processed successfully (SDP logged)")
            
            return sendResponse(ctx, request, response)
        } else if (RtspMethods.SETUP == request.method()) {
            AirPlayLogger.i("Handling SETUP request")
            request.content().resetReaderIndex()
            val mediaStreamInfo = session.airPlay.rtspGetMediaStreamInfo(request.content().toInputStream())
            if (mediaStreamInfo == null) {
                AirPlayLogger.i("SETUP: Encryption setup")
                request.content().resetReaderIndex()
                session.airPlay.rtspSetupEncryption(request.content().toInputStream())
            } else {
                when (mediaStreamInfo.streamType) {
                    MediaStreamInfo.StreamType.AUDIO -> {
                        val audioInfo = mediaStreamInfo as AudioStreamInfo
                        airPlayConsumer.onAudioFormat(audioInfo)
                        AirPlayLogger.i("Setting up AUDIO stream: $audioInfo")

                        val audioHandler = AudioHandler(session.airPlay, airPlayConsumer)
                        val audioReceiver = AudioReceiver(audioHandler, this)
                        val audioThread = Thread(audioReceiver, "airplay-audio")
                        session.audioReceiverThread = audioThread
                        audioThread.start()
                        AirPlayLogger.d("AudioReceiver thread started, waiting for notification...")
                        synchronized(this) { waitForNotify() }
                        AirPlayLogger.d("AudioReceiver thread ready")

                        val audioControlServer = AudioControlServer(this)
                        val controlThread = Thread(audioControlServer, "airplay-audio-ctrl")
                        session.audioControlServerThread = controlThread
                        controlThread.start()
                        AirPlayLogger.d("AudioControlServer thread started, waiting for notification...")
                        synchronized(this) { waitForNotify() }
                        AirPlayLogger.d("AudioControlServer thread ready")

                        val out = ByteArrayOutputStream()
                        session.airPlay.rtspSetupAudio(out, audioReceiver.port, audioControlServer.port)
                        response.content().writeBytes(out.toByteArray())
                        AirPlayLogger.d("SETUP AUDIO response length: ${out.size()} bytes")
                    }
                    MediaStreamInfo.StreamType.VIDEO -> {
                        val videoInfo = mediaStreamInfo as VideoStreamInfo
                        airPlayConsumer.onVideoFormat(videoInfo)
                        AirPlayLogger.i("Setting up VIDEO stream: $videoInfo")

                        val videoHandler = VideoHandler(session.airPlay, airPlayConsumer)
                        val videoReceiver = VideoReceiver(videoHandler, this)
                        val videoThread = Thread(videoReceiver, "airplay-video")
                        session.videoReceiverThread = videoThread
                        videoThread.start()
                        AirPlayLogger.d("VideoReceiver thread started, waiting for notification...")
                        synchronized(this) { waitForNotify() }
                        AirPlayLogger.d("VideoReceiver thread ready")

                        val out = ByteArrayOutputStream()
                        session.airPlay.rtspSetupVideo(out, videoReceiver.port, airTunesPort, 7011)
                        response.content().writeBytes(out.toByteArray())
                        AirPlayLogger.d("SETUP VIDEO response length: ${out.size()} bytes")
                    }
                }
            }
            return sendResponse(ctx, request, response)
        } else if (RtspMethods.GET_PARAMETER == request.method()) {
            AirPlayLogger.d("Handling GET_PARAMETER request")
            response.content().writeBytes("volume: 1.000000\r\n".toByteArray(Charsets.US_ASCII))
            return sendResponse(ctx, request, response)
        } else if (RtspMethods.SET_PARAMETER == request.method()) {
            AirPlayLogger.d("Handling SET_PARAMETER request")
            return sendResponse(ctx, request, response)
        } else if (RtspMethods.RECORD == request.method()) {
            AirPlayLogger.i("Handling RECORD request - Starting playback!")
            response.headers().add("Audio-Latency", "11025")
            response.headers().add("Audio-Jack-Status", "connected; type=analog")
            return sendResponse(ctx, request, response)
        } else if ("FLUSH" == request.method().toString()) {
            AirPlayLogger.d("Handling FLUSH request")
            return sendResponse(ctx, request, response)
        } else if (RtspMethods.TEARDOWN == request.method()) {
            AirPlayLogger.i("Handling TEARDOWN request")
            request.content().resetReaderIndex()
            val mediaStreamInfo = session.airPlay.rtspGetMediaStreamInfo(request.content().toInputStream())
            if (mediaStreamInfo != null) {
                when (mediaStreamInfo.streamType) {
                    MediaStreamInfo.StreamType.AUDIO -> {
                        session.stopAudio()
                        airPlayConsumer.onAudioSrcDisconnect()
                        AirPlayLogger.i("TEARDOWN: Audio stream stopped")
                    }
                    MediaStreamInfo.StreamType.VIDEO -> {
                        session.stopVideo()
                        airPlayConsumer.onVideoSrcDisconnect()
                        AirPlayLogger.i("TEARDOWN: Video stream stopped")
                    }
                }
            } else {
                session.stopAudio()
                session.stopVideo()
                airPlayConsumer.onAudioSrcDisconnect()
                airPlayConsumer.onVideoSrcDisconnect()
                AirPlayLogger.i("TEARDOWN: All streams stopped")
            }
            return sendResponse(ctx, request, response)
        } else if ("POST" == request.method().toString() && request.uri() == "/audioMode") {
            AirPlayLogger.d("Handling /audioMode request")
            return sendResponse(ctx, request, response)
        }
        AirPlayLogger.w("Unhandled RTSP method: ${request.method()}")
        return false
    }
}
