package com.example.airplay.server.handlers

import com.example.airplay.protocol.AirPlay
import com.example.airplay.server.AirPlayConsumer
import com.example.airplay.utils.AirPlayLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class AudioHandler(
    private val airPlay: AirPlay,
    private val dataConsumer: AirPlayConsumer
) : ChannelInboundHandlerAdapter() {

    private val buffer = arrayOfNulls<AudioPacket>(512)
    private var prevSeqNum = 0
    private var packetsInBuffer = 0
    private var packetCount = 0

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        packetCount++
        
        if (packetCount % 100 == 0) {
            AirPlayLogger.i("AudioHandler: Received audio packet #$packetCount, buffer=$packetsInBuffer")
        }
        
        val packet = msg as AudioPacket
        val curSeqNum = packet.sequenceNumber
        if (curSeqNum <= prevSeqNum) return

        buffer[curSeqNum % buffer.size] = packet
        packetsInBuffer++

        var seq = curSeqNum
        while (seq - prevSeqNum == 1 || prevSeqNum == 0) {
            if (!dequeue(seq)) break
            seq++
        }
    }

    private fun dequeue(curSeqNo: Int): Boolean {
        if (curSeqNo - prevSeqNum == 1 || prevSeqNum == 0) {
            val audioPacket = buffer[curSeqNo % buffer.size]
            if (audioPacket != null && audioPacket.available) {
                airPlay.decryptAudio(audioPacket.encodedAudio, audioPacket.encodedAudioSize)
                val data = audioPacket.encodedAudio.copyOfRange(0, audioPacket.encodedAudioSize)
                dataConsumer.onAudio(data)
                audioPacket.available = false
                prevSeqNum = curSeqNo
                packetsInBuffer--
                return true
            }
        }
        return false
    }
}

class AudioPacket(
    val flag: Int,
    val type: Int,
    val sequenceNumber: Int,
    val timestamp: Long,
    val ssrc: Long,
    var available: Boolean = true,
    val encodedAudioSize: Int,
    val encodedAudio: ByteArray = ByteArray(encodedAudioSize + 1024)
)
