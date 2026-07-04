package com.example.airplay.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

class AudioPlayer {

    private var audioTrack: AudioTrack? = null
    private val alacDecoder = AlacDecoder()
    private var sampleRate = 44100
    private var channels = 2
    private var bufferSize = 0

    fun configure(sampleRate: Int, channels: Int) {
        release()
        this.sampleRate = sampleRate
        this.channels = channels

        val channelConfig = if (channels == 1) AudioFormat.CHANNEL_OUT_MONO
        else AudioFormat.CHANNEL_OUT_STEREO

        bufferSize = AudioTrack.getMinBufferSize(
            sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT
        ) * 4

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    fun playAudio(encodedData: ByteArray) {
        val pcmBuffer = ShortArray(encodedData.size)
        val samples = alacDecoder.decodeFrame(encodedData, pcmBuffer)

        if (samples > 0) {
            audioTrack?.write(pcmBuffer, 0, samples)
        }
    }

    fun pause() {
        audioTrack?.pause()
    }

    fun play() {
        audioTrack?.play()
    }

    fun release() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        alacDecoder.release()
    }
}
