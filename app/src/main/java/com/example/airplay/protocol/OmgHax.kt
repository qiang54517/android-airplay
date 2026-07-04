package com.example.airplay.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

class OmgHax {
    private val modifiedMD5 = ModifiedMD5()
    private val sapHash = SapHash()

    fun decryptAesKey(message3: ByteArray, cipherText: ByteArray, keyOut: ByteArray) {
        val chunk1 = cipherText.copyOfRange(16, cipherText.size)
        val chunk2 = cipherText.copyOfRange(56, cipherText.size)
        val blockIn = ByteArray(16)
        val sapKey = ByteArray(16)
        val keySchedule = Array(11) { IntArray(4) }
        generateSessionKey(OmgHaxConst.defaultSap, message3, sapKey)
        generateKeySchedule(sapKey, keySchedule)
        zXor(chunk2, blockIn, 1)
        cycle(blockIn, keySchedule)
        for (i in 0 until 16) {
            keyOut[i] = (blockIn[i].toInt() xor chunk1[i].toInt()).toByte()
        }
        xXor(keyOut, keyOut, 1)
        zXor(keyOut, keyOut, 1)
    }

    fun generateKeySchedule(keyMaterial: ByteArray, keySchedule: Array<IntArray>) {
        val keyData = IntArray(4)
        for (i in 0..10) {
            keySchedule[i][0] = 0xdeadbeef.toInt()
            keySchedule[i][1] = 0xdeadbeef.toInt()
            keySchedule[i][2] = 0xdeadbeef.toInt()
            keySchedule[i][3] = 0xdeadbeef.toInt()
        }
        val buffer = ByteArray(16)
        var ti = 0
        tXor(keyMaterial, buffer)

        val wrap = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0..3) {
            keyData[i] = wrap.int
        }

        for (round in 0..10) {
            keySchedule[round][0] = keyData[0]
            val tbl1 = OmgHaxConst.tableIndex(ti)
            val tbl2 = OmgHaxConst.tableIndex(ti + 1)
            val tbl3 = OmgHaxConst.tableIndex(ti + 2)
            val tbl4 = OmgHaxConst.tableIndex(ti + 3)
            ti += 4

            buffer[0] = (buffer[0].toInt() xor (tbl1[buffer[0x0d].toInt() and 0xFF].toInt() xor OmgHaxConst.indexMangle[round].toInt())).toByte()
            buffer[1] = (buffer[1].toInt() xor tbl2[buffer[0x0e].toInt() and 0xFF].toInt()).toByte()
            buffer[2] = (buffer[2].toInt() xor tbl3[buffer[0x0f].toInt() and 0xFF].toInt()).toByte()
            buffer[3] = (buffer[3].toInt() xor tbl4[buffer[0x0c].toInt() and 0xFF].toInt()).toByte()

            keyData[0] = wrap.getInt(0)
            keySchedule[round][1] = keyData[1]
            keyData[1] = keyData[1] xor keyData[0]
            wrap.putInt(4, keyData[1])
            keySchedule[round][2] = keyData[2]
            keyData[2] = keyData[2] xor keyData[1]
            wrap.putInt(8, keyData[2])
            keySchedule[round][3] = keyData[3]
            keyData[3] = keyData[3] xor keyData[2]
            wrap.putInt(12, keyData[3])
        }
    }

    fun generateSessionKey(oldSap: ByteArray, messageIn: ByteArray, sessionKey: ByteArray) {
        val decryptedMessage = ByteArray(128)
        val newSap = ByteArray(320)

        decryptMessage(messageIn, decryptedMessage)

        System.arraycopy(OmgHaxConst.staticSource1, 0, newSap, 0, 0x11)
        System.arraycopy(decryptedMessage, 0, newSap, 0x11, 0x80)
        System.arraycopy(oldSap, 0x80, newSap, 0x91, 0x80)
        System.arraycopy(OmgHaxConst.staticSource2, 0, newSap, 0x111, 0x2f)
        System.arraycopy(OmgHaxConst.initialSessionKey, 0, sessionKey, 0, 16)

        for (round in 0 until 5) {
            val base = newSap.copyOfRange(round * 64, newSap.size)
            val md5 = ByteArray(16)
            modifiedMD5.modifiedMd5(base, sessionKey, md5)
            sapHash.sapHash(base, sessionKey)

            val md5Wrap = ByteBuffer.wrap(md5).order(ByteOrder.LITTLE_ENDIAN)
            val sessionKeyWrap = ByteBuffer.wrap(sessionKey).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0..3) {
                sessionKeyWrap.putInt(
                    i * 4,
                    ((sessionKeyWrap.getInt(i * 4).toLong() + md5Wrap.getInt(i * 4).toLong()) and 0xffffffffL).toInt()
                )
            }
        }

        for (i in 0 until 16 step 4) {
            var tmp = sessionKey[i]
            sessionKey[i] = sessionKey[i + 3]
            sessionKey[i + 3] = tmp
            tmp = sessionKey[i + 1]
            sessionKey[i + 1] = sessionKey[i + 2]
            sessionKey[i + 2] = tmp
        }

        for (i in 0 until 16) {
            sessionKey[i] = (sessionKey[i].toInt() xor 121).toByte()
        }
    }

    private fun decryptMessage(messageIn: ByteArray, decryptedMessage: ByteArray) {
        val buffer = ByteArray(16)
        val mode = messageIn[12].toInt()

        for (i in 0 until 8) {
            for (j in 0 until 16) {
                buffer[j] = if (mode == 3) {
                    messageIn[(0x80 - 0x10 * i) + j]
                } else {
                    messageIn[(0x10 * (i + 1)) + j]
                }
            }

            for (j in 0 until 9) {
                val base = 0x80 - 0x10 * j
                val tbl = OmgHaxConst.messageTableIndex(base)

                buffer[0x0] = (tbl[buffer[0x0].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x0].toInt()).toByte()
                buffer[0x4] = (tbl[buffer[0x4].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x4].toInt()).toByte()
                buffer[0x8] = (tbl[buffer[0x8].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x8].toInt()).toByte()
                buffer[0xc] = (tbl[buffer[0xc].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0xc].toInt()).toByte()

                var tmp = buffer[0x0d]
                buffer[0xd] = (tbl[buffer[0x9].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0xd].toInt()).toByte()
                buffer[0x9] = (tbl[buffer[0x5].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x9].toInt()).toByte()
                buffer[0x5] = (tbl[buffer[0x1].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x5].toInt()).toByte()
                buffer[0x1] = (tbl[tmp.toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x1].toInt()).toByte()

                tmp = buffer[0x02]
                buffer[0x2] = (tbl[buffer[0xa].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x2].toInt()).toByte()
                buffer[0xa] = (tbl[tmp.toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0xa].toInt()).toByte()
                tmp = buffer[0x06]
                buffer[0x6] = (tbl[buffer[0xe].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x6].toInt()).toByte()
                buffer[0xe] = (tbl[tmp.toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0xe].toInt()).toByte()

                tmp = buffer[0x3]
                buffer[0x3] = (tbl[buffer[0x7].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x3].toInt()).toByte()
                buffer[0x7] = (tbl[buffer[0xb].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0x7].toInt()).toByte()
                buffer[0xb] = (tbl[buffer[0xf].toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0xf].toInt()).toByte()
                buffer[0xf] = (tbl[tmp.toInt() and 0xFF].toInt() xor OmgHaxConst.messageKey[mode][base + 0xf].toInt()).toByte()

                val block = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                block.putInt(
                    OmgHaxConst.table9[0x000 + (buffer[0x0].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x100 + (buffer[0x1].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x200 + (buffer[0x2].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x300 + (buffer[0x3].toInt() and 0xFF)]
                )
                block.putInt(
                    OmgHaxConst.table9[0x000 + (buffer[0x4].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x100 + (buffer[0x5].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x200 + (buffer[0x6].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x300 + (buffer[0x7].toInt() and 0xFF)]
                )
                block.putInt(
                    OmgHaxConst.table9[0x000 + (buffer[0x8].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x100 + (buffer[0x9].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x200 + (buffer[0xa].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x300 + (buffer[0xb].toInt() and 0xFF)]
                )
                block.putInt(
                    OmgHaxConst.table9[0x000 + (buffer[0xc].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x100 + (buffer[0xd].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x200 + (buffer[0xe].toInt() and 0xFF)] xor
                            OmgHaxConst.table9[0x300 + (buffer[0xf].toInt() and 0xFF)]
                )
            }

            buffer[0x0] = OmgHaxConst.table10[(0x0 shl 8) + (buffer[0x0].toInt() and 0xFF)]
            buffer[0x4] = OmgHaxConst.table10[(0x4 shl 8) + (buffer[0x4].toInt() and 0xFF)]
            buffer[0x8] = OmgHaxConst.table10[(0x8 shl 8) + (buffer[0x8].toInt() and 0xFF)]
            buffer[0xc] = OmgHaxConst.table10[(0xc shl 8) + (buffer[0xc].toInt() and 0xFF)]

            var tmp = buffer[0x0d]
            buffer[0xd] = OmgHaxConst.table10[(0xd shl 8) + (buffer[0x9].toInt() and 0xFF)]
            buffer[0x9] = OmgHaxConst.table10[(0x9 shl 8) + (buffer[0x5].toInt() and 0xFF)]
            buffer[0x5] = OmgHaxConst.table10[(0x5 shl 8) + (buffer[0x1].toInt() and 0xFF)]
            buffer[0x1] = OmgHaxConst.table10[(0x1 shl 8) + (tmp.toInt() and 0xFF)]

            tmp = buffer[0x02]
            buffer[0x2] = OmgHaxConst.table10[(0x2 shl 8) + (buffer[0xa].toInt() and 0xFF)]
            buffer[0xa] = OmgHaxConst.table10[(0xa shl 8) + (tmp.toInt() and 0xFF)]
            tmp = buffer[0x06]
            buffer[0x6] = OmgHaxConst.table10[(0x6 shl 8) + (buffer[0xe].toInt() and 0xFF)]
            buffer[0xe] = OmgHaxConst.table10[(0xe shl 8) + (tmp.toInt() and 0xFF)]

            tmp = buffer[0x3]
            buffer[0x3] = OmgHaxConst.table10[(0x3 shl 8) + (buffer[0x7].toInt() and 0xFF)]
            buffer[0x7] = OmgHaxConst.table10[(0x7 shl 8) + (buffer[0xb].toInt() and 0xFF)]
            buffer[0xb] = OmgHaxConst.table10[(0xb shl 8) + (buffer[0xf].toInt() and 0xFF)]
            buffer[0xf] = OmgHaxConst.table10[(0xf shl 8) + (tmp.toInt() and 0xFF)]

            val xorResult = ByteArray(16)
            if (mode == 2 || mode == 1 || mode == 0) {
                if (i > 0) {
                    xorBlocks(buffer, messageIn.copyOfRange(0x10 * i, 0x10 * i + 16), xorResult)
                    System.arraycopy(xorResult, 0, decryptedMessage, 0x10 * i, 16)
                } else {
                    xorBlocks(buffer, OmgHaxConst.messageIV[mode], xorResult)
                    System.arraycopy(xorResult, 0, decryptedMessage, 0, 16)
                }
            } else {
                if (i < 7) {
                    xorBlocks(buffer, messageIn.copyOfRange(0x70 - 0x10 * i, (0x70 - 0x10 * i) + 16), xorResult)
                    System.arraycopy(xorResult, 0, decryptedMessage, 0x70 - 0x10 * i, 16)
                } else {
                    xorBlocks(buffer, OmgHaxConst.messageIV[mode], xorResult)
                    System.arraycopy(xorResult, 0, decryptedMessage, 0x70 - 0x10 * i, 16)
                }
            }
        }
    }

    fun cycle(block: ByteArray, keySchedule: Array<IntArray>) {
        val bWords = ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN)
        bWords.putInt(0, bWords.getInt(0) xor keySchedule[10][0])
        bWords.putInt(4, bWords.getInt(4) xor keySchedule[10][1])
        bWords.putInt(8, bWords.getInt(8) xor keySchedule[10][2])
        bWords.putInt(12, bWords.getInt(12) xor keySchedule[10][3])
        permuteBlock1(block)

        for (round in 0 until 9) {
            val key = ByteArray(16)
            val wrap = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0..3) {
                wrap.putInt(keySchedule[9 - round][i])
            }

            val ptr1 = OmgHaxConst.table5[(block[3].toInt() and 0xff) xor (key[3].toInt() and 0xff)]
            val ptr2 = OmgHaxConst.table6[(block[2].toInt() and 0xff) xor (key[2].toInt() and 0xff)]
            val ptr3 = OmgHaxConst.table8[(block[0].toInt() and 0xff) xor (key[0].toInt() and 0xff)]
            val ptr4 = OmgHaxConst.table7[(block[1].toInt() and 0xff) xor (key[1].toInt() and 0xff)]
            var ab = ptr1 xor ptr2 xor ptr3 xor ptr4
            bWords.putInt(0, ab)

            val rptr2 = OmgHaxConst.table5[(block[7].toInt() and 0xff) xor (key[7].toInt() and 0xff)]
            val rptr1 = OmgHaxConst.table6[(block[6].toInt() and 0xff) xor (key[6].toInt() and 0xff)]
            val rptr4 = OmgHaxConst.table7[(block[5].toInt() and 0xff) xor (key[5].toInt() and 0xff)]
            val rptr3 = OmgHaxConst.table8[(block[4].toInt() and 0xff) xor (key[4].toInt() and 0xff)]
            ab = rptr1 xor rptr2 xor rptr3 xor rptr4
            bWords.putInt(4, ab)

            bWords.putInt(8, OmgHaxConst.table5[(block[11].toInt() and 0xff) xor (key[11].toInt() and 0xff)] xor
                    OmgHaxConst.table6[(block[10].toInt() and 0xff) xor (key[10].toInt() and 0xff)] xor
                    OmgHaxConst.table7[(block[9].toInt() and 0xff) xor (key[9].toInt() and 0xff)] xor
                    OmgHaxConst.table8[(block[8].toInt() and 0xff) xor (key[8].toInt() and 0xff)])
            bWords.putInt(12, OmgHaxConst.table5[(block[15].toInt() and 0xff) xor (key[15].toInt() and 0xff)] xor
                    OmgHaxConst.table6[(block[14].toInt() and 0xff) xor (key[14].toInt() and 0xff)] xor
                    OmgHaxConst.table7[(block[13].toInt() and 0xff) xor (key[13].toInt() and 0xff)] xor
                    OmgHaxConst.table8[(block[12].toInt() and 0xff) xor (key[12].toInt() and 0xff)])
            permuteBlock2(block, 8 - round)
        }

        bWords.putInt(0, bWords.getInt(0) xor keySchedule[0][0])
        bWords.putInt(4, bWords.getInt(4) xor keySchedule[0][1])
        bWords.putInt(8, bWords.getInt(8) xor keySchedule[0][2])
        bWords.putInt(12, bWords.getInt(12) xor keySchedule[0][3])
    }

    private fun permuteBlock1(block: ByteArray) {
        block[0] = OmgHaxConst.table3[block[0].toInt() and 0xff]
        block[4] = OmgHaxConst.table3[0x400 + (block[4].toInt() and 0xff)]
        block[8] = OmgHaxConst.table3[0x800 + (block[8].toInt() and 0xff)]
        block[12] = OmgHaxConst.table3[0xc00 + (block[12].toInt() and 0xff)]

        var tmp = block[13]
        block[13] = OmgHaxConst.table3[0x100 + (block[9].toInt() and 0xff)]
        block[9] = OmgHaxConst.table3[0xd00 + (block[5].toInt() and 0xff)]
        block[5] = OmgHaxConst.table3[0x900 + (block[1].toInt() and 0xff)]
        block[1] = OmgHaxConst.table3[0x500 + (tmp.toInt() and 0xff)]

        tmp = block[2]
        block[2] = OmgHaxConst.table3[0xa00 + (block[10].toInt() and 0xff)]
        block[10] = OmgHaxConst.table3[0x200 + (tmp.toInt() and 0xff)]
        tmp = block[6]
        block[6] = OmgHaxConst.table3[0xe00 + (block[14].toInt() and 0xff)]
        block[14] = OmgHaxConst.table3[0x600 + (tmp.toInt() and 0xff)]

        tmp = block[3]
        block[3] = OmgHaxConst.table3[0xf00 + (block[7].toInt() and 0xff)]
        block[7] = OmgHaxConst.table3[0x300 + (block[11].toInt() and 0xff)]
        block[11] = OmgHaxConst.table3[0x700 + (block[15].toInt() and 0xff)]
        block[15] = OmgHaxConst.table3[0xb00 + (tmp.toInt() and 0xff)]
    }

    private fun permuteBlock2(block: ByteArray, round: Int) {
        block[0] = OmgHaxConst.permuteTable2(round * 16)[block[0].toInt() and 0xff]
        block[4] = OmgHaxConst.permuteTable2(round * 16 + 4)[block[4].toInt() and 0xff]
        block[8] = OmgHaxConst.permuteTable2(round * 16 + 8)[block[8].toInt() and 0xff]
        block[12] = OmgHaxConst.permuteTable2(round * 16 + 12)[block[12].toInt() and 0xff]

        var tmp = block[13]
        block[13] = OmgHaxConst.permuteTable2(round * 16 + 13)[block[9].toInt() and 0xff]
        block[9] = OmgHaxConst.permuteTable2(round * 16 + 9)[block[5].toInt() and 0xff]
        block[5] = OmgHaxConst.permuteTable2(round * 16 + 5)[block[1].toInt() and 0xff]
        block[1] = OmgHaxConst.permuteTable2(round * 16 + 1)[tmp.toInt() and 0xff]

        tmp = block[2]
        block[2] = OmgHaxConst.permuteTable2(round * 16 + 2)[block[10].toInt() and 0xff]
        block[10] = OmgHaxConst.permuteTable2(round * 16 + 10)[tmp.toInt() and 0xff]
        tmp = block[6]
        block[6] = OmgHaxConst.permuteTable2(round * 16 + 6)[block[14].toInt() and 0xff]
        block[14] = OmgHaxConst.permuteTable2(round * 16 + 14)[tmp.toInt() and 0xff]

        tmp = block[3]
        block[3] = OmgHaxConst.permuteTable2(round * 16 + 3)[block[7].toInt() and 0xff]
        block[7] = OmgHaxConst.permuteTable2(round * 16 + 7)[block[11].toInt() and 0xff]
        block[11] = OmgHaxConst.permuteTable2(round * 16 + 11)[block[15].toInt() and 0xff]
        block[15] = OmgHaxConst.permuteTable2(round * 16 + 15)[tmp.toInt() and 0xff]
    }

    private fun xorBlocks(a: ByteArray, b: ByteArray, out: ByteArray) {
        for (i in 0 until 16) {
            out[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        }
    }

    private fun zXor(input: ByteArray, out: ByteArray, blocks: Int) {
        for (j in 0 until blocks) {
            for (i in 0 until 16) {
                out[j * 16 + i] = (input[j * 16 + i].toInt() xor OmgHaxConst.zKey[i].toInt()).toByte()
            }
        }
    }

    private fun xXor(input: ByteArray, out: ByteArray, blocks: Int) {
        for (j in 0 until blocks) {
            for (i in 0 until 16) {
                out[j * 16 + i] = (input[j * 16 + i].toInt() xor OmgHaxConst.xKey[i].toInt()).toByte()
            }
        }
    }

    private fun tXor(input: ByteArray, out: ByteArray) {
        for (i in 0 until 16) {
            out[i] = (input[i].toInt() xor OmgHaxConst.tKey[i].toInt()).toByte()
        }
    }
}
