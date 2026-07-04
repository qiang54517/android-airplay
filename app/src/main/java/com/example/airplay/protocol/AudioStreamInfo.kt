package com.example.airplay.protocol

class AudioStreamInfo(
    val audioFormat: AudioFormat?,
    val compressionType: CompressionType?,
    val samplesPerFrame: Int?
) : MediaStreamInfo() {
    override val streamType = StreamType.AUDIO

    enum class AudioFormat(val code: Long) {
        ALAC(0x4000000L),
        AAC(0x4000001L),
        UNKNOWN(-1);

        companion object {
            fun fromCode(code: Long) = entries.find { it.code == code } ?: UNKNOWN
        }
    }

    enum class CompressionType(val code: Int) {
        ALAC(1),
        AAC_ELD(2),
        UNKNOWN(-1);

        companion object {
            fun fromCode(code: Int) = entries.find { it.code == code } ?: UNKNOWN
        }
    }
}
