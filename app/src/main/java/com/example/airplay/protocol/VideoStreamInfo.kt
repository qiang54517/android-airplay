package com.example.airplay.protocol

class VideoStreamInfo(
    val streamConnectionID: String?
) : MediaStreamInfo() {
    override val streamType = StreamType.VIDEO
}
