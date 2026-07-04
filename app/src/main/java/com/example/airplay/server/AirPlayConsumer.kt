package com.example.airplay.server

import com.example.airplay.protocol.AudioStreamInfo
import com.example.airplay.protocol.VideoStreamInfo

interface AirPlayConsumer {
    fun onVideoFormat(videoStreamInfo: VideoStreamInfo)
    fun onVideo(bytes: ByteArray)
    fun onVideoSrcDisconnect()
    fun onAudioFormat(audioStreamInfo: AudioStreamInfo)
    fun onAudio(bytes: ByteArray)
    fun onAudioSrcDisconnect()
}
