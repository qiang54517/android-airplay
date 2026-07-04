package com.example.airplay.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sin

class ModifiedMD5 {
    private val shift = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    )

    fun modifiedMd5(originalBlockIn: ByteArray, keyIn: ByteArray, keyOut: ByteArray) {
        val blockIn = ByteArray(64)
        System.arraycopy(originalBlockIn, 0, blockIn, 0, 64)

        val keyWords = ByteBuffer.wrap(keyIn).order(ByteOrder.LITTLE_ENDIAN)
        var a = keyWords.int.toLong() and 0xffffffffL
        var b = keyWords.int.toLong() and 0xffffffffL
        var c = keyWords.int.toLong() and 0xffffffffL
        var d = keyWords.int.toLong() and 0xffffffffL

        for (i in 0 until 64) {
            val j = when {
                i < 16 -> i
                i < 32 -> (5 * i + 1) % 16
                i < 48 -> (3 * i + 5) % 16
                else -> 7 * i % 16
            }
            val input = ((blockIn[4 * j].toInt() and 0xFF) shl 24) or
                    ((blockIn[4 * j + 1].toInt() and 0xFF) shl 16) or
                    ((blockIn[4 * j + 2].toInt() and 0xFF) shl 8) or
                    (blockIn[4 * j + 3].toInt() and 0xFF)
            var z = a + input + ((1L shl 32) * abs(sin((i + 1).toDouble()))).toLong()
            z = when {
                i < 16 -> rol(z + f(b, c, d), shift[i].toLong())
                i < 32 -> rol(z + g(b, c, d), shift[i].toLong())
                i < 48 -> rol(z + h(b, c, d), shift[i].toLong())
                else -> rol(z + iMod(b, c, d), shift[i].toLong())
            }
            z += b
            val tmp = d
            d = c
            c = b
            b = z
            a = tmp
            if (i == 31) {
                swap(blockIn, 4 * (a.toInt() and 15), 4 * (b.toInt() and 15))
                swap(blockIn, 4 * (c.toInt() and 15), 4 * (d.toInt() and 15))
                swap(blockIn, 4 * ((a.toInt() and (15 shl 4)) shr 4), 4 * ((b.toInt() and (15 shl 4)) shr 4))
                swap(blockIn, 4 * ((a.toInt() and (15 shl 8)) shr 8), 4 * ((b.toInt() and (15 shl 8)) shr 8))
                swap(blockIn, 4 * ((a.toInt() and (15 shl 12)) shr 12), 4 * ((b.toInt() and (15 shl 12)) shr 12))
            }
        }
        val keyOutBuf = ByteBuffer.wrap(keyOut).order(ByteOrder.LITTLE_ENDIAN)
        keyOutBuf.putInt((keyWords.getInt(0).toLong() + a).toInt())
        keyOutBuf.putInt((keyWords.getInt(4).toLong() + b).toInt())
        keyOutBuf.putInt((keyWords.getInt(8).toLong() + c).toInt())
        keyOutBuf.putInt((keyWords.getInt(12).toLong() + d).toInt())
    }

    private fun f(b: Long, c: Long, d: Long) = (b and c) or (b.inv() and d)
    private fun g(b: Long, c: Long, d: Long) = (b and d) or (c and d.inv())
    private fun h(b: Long, c: Long, d: Long) = b xor c xor d
    private fun iMod(b: Long, c: Long, d: Long) = c xor (b or d.inv())

    private fun rol(input: Long, count: Long): Long =
        ((input shl count.toInt()) and 0xffffffffL) or ((input and 0xffffffffL) ushr (32 - count.toInt()))

    private fun swap(arr: ByteArray, idxA: Int, idxB: Int) {
        val wrap = ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN)
        val a = wrap.getInt(idxA)
        val b = wrap.getInt(idxB)
        wrap.putInt(idxB, a)
        wrap.putInt(idxA, b)
    }
}
