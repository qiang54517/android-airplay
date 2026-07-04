package com.example.airplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Surface
import androidx.core.app.NotificationCompat
import com.example.airplay.bonjour.AirPlayBonjour
import com.example.airplay.player.AudioPlayer
import com.example.airplay.player.VideoDecoder
import com.example.airplay.protocol.AirPlayLookupTables
import com.example.airplay.protocol.AudioStreamInfo
import com.example.airplay.protocol.VideoStreamInfo
import com.example.airplay.server.*

class AirPlayService : Service(), AirPlayConsumer {

    private val binder = LocalBinder()
    private var controlThread: Thread? = null
    private var bonjour: AirPlayBonjour? = null
    private var videoDecoder: VideoDecoder? = null
    private var audioPlayer: AudioPlayer? = null
    private var surface: Surface? = null

    var onStateChanged: ((String) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService() = this@AirPlayService
    }

    override fun onBind(intent: Intent?) = binder

    override fun onCreate() {
        super.onCreate()
        AirPlayLookupTables.init(this)
        createNotificationChannel()
    }

    fun startServer(surface: Surface, config: AirPlayConfig = AirPlayConfig()) {
        this.surface = surface
        stopServer()

        startForeground(1, createNotification(config.serverName))

        // 1. Start RTSP control server FIRST (so port is ready before Bonjour advertises it)
        controlThread = Thread(Runnable {
            ControlServer(config, this@AirPlayService).run()
        }, "airplay-control").apply { start() }

        // Small delay to let server bind
        Thread.sleep(500)

        // 2. Register mDNS/Bonjour AFTER server is listening
        bonjour = AirPlayBonjour(this)
        bonjour?.start(config.serverName, config.airtunesPort)

        onStateChanged?.invoke("AirPlay 服务已启动\n名称: ${config.serverName}\n端口: ${config.airtunesPort}\niPhone/iPad/Mac 可以发现此设备")
    }

    fun stopServer() {
        controlThread?.interrupt()
        controlThread = null
        bonjour?.stop()
        bonjour = null
        videoDecoder?.release()
        videoDecoder = null
        audioPlayer?.release()
        audioPlayer = null
        onStateChanged?.invoke("AirPlay 已停止")
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    // === AirPlayConsumer ===

    override fun onVideoFormat(videoStreamInfo: VideoStreamInfo) {
        // Initialize video decoder when client connects
        // Resolution will be negotiated; default 1920x1080
        videoDecoder?.release()
        videoDecoder = VideoDecoder(surface!!)
        videoDecoder?.configure(1920, 1080)
        onStateChanged?.invoke("视频流已连接")
    }

    override fun onVideo(bytes: ByteArray) {
        videoDecoder?.feedData(bytes)
    }

    override fun onVideoSrcDisconnect() {
        videoDecoder?.release()
        videoDecoder = null
        onStateChanged?.invoke("视频流已断开")
    }

    override fun onAudioFormat(audioStreamInfo: AudioStreamInfo) {
        audioPlayer?.release()
        audioPlayer = AudioPlayer()
        audioPlayer?.configure(44100, 2)
        onStateChanged?.invoke("音频流已连接 (${audioStreamInfo.audioFormat?.name ?: "Unknown"})")
    }

    override fun onAudio(bytes: ByteArray) {
        audioPlayer?.playAudio(bytes)
    }

    override fun onAudioSrcDisconnect() {
        audioPlayer?.release()
        audioPlayer = null
        onStateChanged?.invoke("音频流已断开")
    }

    // === Notification ===

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "airplay_channel",
                "AirPlay 服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "AirPlay 接收服务运行中"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(serverName: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "airplay_channel")
            .setContentTitle("AirPlay 接收运行中")
            .setContentText("设备名: $serverName")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }
}
