package com.example.airplay.protocol

sealed class MediaStreamInfo {
    abstract val streamType: StreamType

    enum class StreamType { AUDIO, VIDEO }
}
