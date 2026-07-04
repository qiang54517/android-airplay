package com.example.airplay.server.handlers

import com.example.airplay.protocol.AirPlay

data class Session(
    val airPlay: AirPlay = AirPlay(),
    var pairingDone: Boolean = false,
    var fairPlayDone: Boolean = false,
    var audioReceiverThread: Thread? = null,
    var videoReceiverThread: Thread? = null,
    var audioControlServerThread: Thread? = null
) {
    fun stopAudio() {
        audioReceiverThread?.interrupt()
        audioControlServerThread?.interrupt()
    }

    fun stopVideo() {
        videoReceiverThread?.interrupt()
    }
}
