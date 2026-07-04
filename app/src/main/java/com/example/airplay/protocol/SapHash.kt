package com.example.airplay.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

class SapHash {
    private val handGarble = HandGarble()

    private fun rol8(input: Byte, count: Int): Byte =
        (((input.toInt() shl count) and 0xff) or ((input.toInt() and 0xff) shr (8 - count))).toByte()

    fun sapHash(blockIn: ByteArray, keyOut: ByteArray) {
        val buffer0 = byteArrayOf(-106, 95, -58, 83, -8, 70, -52, 24, -33, -66, -78, -8, 56, -41, -20, 34, 3, -47, 32, -113)
        val buffer1 = ByteArray(210)
        val buffer2 = byteArrayOf(67, 84, 98, 122, 24, -61, -42, -77, -102, 86, -10, 28, 20, 63, 12, 29, 59, 54, -125, -79, 57, 81, 74, -86, 9, 62, -2, 68, -81, -34, -61, 32, -99, 66, 58)
        val buffer3 = ByteArray(132)
        val buffer4 = byteArrayOf(-19, 37, -47, -69, -68, 39, -97, 2, -94, -87, 17, 0, 12, -77, 82, -64, -67, -29, 27, 73, -57)
        val i0Index = intArrayOf(18, 22, 23, 0, 5, 19, 32, 31, 10, 21, 30)

        val blockWords = ByteBuffer.wrap(blockIn).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until 210) {
            val inWord = blockWords.getInt(((i % 64) shr 2) * 4)
            val inByte = (inWord shr ((3 - (i % 4)) shl 3)).toByte()
            buffer1[i] = inByte
        }

        for (i in 0 until 840) {
            val x = buffer1[((((i - 155) and 0xffffffffL.toInt()) % 210 + 210) % 210)]
            val y = buffer1[((((i - 57) and 0xffffffffL.toInt()) % 210 + 210) % 210)]
            val z = buffer1[((((i - 13) and 0xffffffffL.toInt()) % 210 + 210) % 210)]
            val w = buffer1[(i % 210)]
            buffer1[i % 210] = ((rol8(y, 5).toInt() + (rol8(z, 3).toInt() xor w.toInt()) - rol8(x, 7).toInt()) and 0xff).toByte()
        }

        handGarble.garble(buffer0, buffer1, buffer2, buffer3, buffer4)

        for (i in 0 until 16) {
            keyOut[i] = 0xE1.toByte()
        }

        for (i in 0 until 11) {
            if (i == 3) {
                keyOut[i] = 0x3d
            } else {
                keyOut[i] = ((keyOut[i].toInt() + buffer3[i0Index[i] * 4].toInt()) and 0xff).toByte()
            }
        }

        for (i in 0 until 20) {
            keyOut[i % 16] = (keyOut[i % 16].toInt() xor buffer0[i].toInt()).toByte()
        }

        for (i in 0 until 35) {
            keyOut[i % 16] = (keyOut[i % 16].toInt() xor buffer2[i].toInt()).toByte()
        }

        for (i in 0 until 210) {
            keyOut[i % 16] = (keyOut[i % 16].toInt() xor buffer1[i].toInt()).toByte()
        }

        for (j in 0 until 16) {
            for (i in 0 until 16) {
                val x = keyOut[((i - 7) % 16 + 16) % 16]
                val y = keyOut[i % 16]
                val z = keyOut[((i - 37) % 16 + 16) % 16]
                val w = keyOut[((i - 177) % 16 + 16) % 16]
                keyOut[i] = (rol8(x, 1).toInt() xor y.toInt() xor rol8(z, 6).toInt() xor rol8(w, 5).toInt()).toByte()
            }
        }
    }
}
