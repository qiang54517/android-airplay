package com.example.airplay.protocol

import java.io.InputStream
import java.io.OutputStream

/**
 * Main AirPlay protocol handler.
 * Responds to pairing setup, fairplay setup requests, decrypts data.
 */
class AirPlay {
    private val pairing = Pairing()
    private val fairplay = FairPlay()
    private val rtsp = RTSP()

    private var fairPlayVideoDecryptor: FairPlayVideoDecryptor? = null
    private var fairPlayAudioDecryptor: FairPlayAudioDecryptor? = null

    fun info(width: Int, height: Int, fps: Int, out: OutputStream) {
        pairing.info(width, height, fps, out)
    }

    fun pairSetup(out: OutputStream) {
        pairing.pairSetup(out)
    }

    fun pairVerify(input: InputStream, out: OutputStream) {
        pairing.pairVerify(input, out)
    }

    fun isPairVerified() = pairing.isPairVerified()

    @Throws(Exception::class)
    fun fairPlaySetup(input: InputStream, out: OutputStream) {
        fairplay.fairPlaySetup(input, out)
    }

    @Throws(Exception::class)
    fun rtspGetMediaStreamInfo(input: InputStream) = rtsp.getMediaStreamInfo(input)

    @Throws(Exception::class)
    fun rtspSetupEncryption(input: InputStream) {
        rtsp.setup(input)
    }

    @Throws(Exception::class)
    fun rtspSetupVideo(out: OutputStream, videoDataPort: Int, videoEventPort: Int, videoTimingPort: Int) {
        rtsp.setupVideo(out, videoDataPort, videoEventPort, videoTimingPort)
    }

    @Throws(Exception::class)
    fun rtspSetupAudio(out: OutputStream, audioDataPort: Int, audioControlPort: Int) {
        rtsp.setupAudio(out, audioDataPort, audioControlPort)
    }

    fun getFairPlayAesKey() = fairplay.decryptAesKey(rtsp.getEncryptedAESKey() ?: ByteArray(0))

    fun isFairPlayVideoDecryptorReady(): Boolean {
        return pairing.getSharedSecret() != null &&
                rtsp.getEncryptedAESKey() != null &&
                rtsp.getStreamConnectionID() != null
    }

    fun isFairPlayAudioDecryptorReady(): Boolean {
        return pairing.getSharedSecret() != null &&
                rtsp.getEncryptedAESKey() != null &&
                rtsp.getEiv() != null
    }

    @Throws(Exception::class)
    fun decryptVideo(video: ByteArray) {
        if (fairPlayVideoDecryptor == null) {
            check(isFairPlayVideoDecryptorReady()) { "FairPlayVideoDecryptor not ready!" }
            fairPlayVideoDecryptor = FairPlayVideoDecryptor(
                getFairPlayAesKey(),
                pairing.getSharedSecret()!!,
                rtsp.getStreamConnectionID()!!
            )
        }
        fairPlayVideoDecryptor!!.decrypt(video)
    }

    @Throws(Exception::class)
    fun decryptAudio(audio: ByteArray, audioLength: Int) {
        if (fairPlayAudioDecryptor == null) {
            check(isFairPlayAudioDecryptorReady()) { "FairPlayAudioDecryptor not ready!" }
            fairPlayAudioDecryptor = FairPlayAudioDecryptor(
                getFairPlayAesKey(),
                rtsp.getEiv()!!,
                pairing.getSharedSecret()!!
            )
        }
        fairPlayAudioDecryptor!!.decrypt(audio, audioLength)
    }
}
