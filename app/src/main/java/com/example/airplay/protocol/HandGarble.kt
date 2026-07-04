package com.example.airplay.protocol

class HandGarble {
    private fun rol8(input: Byte, count: Int): Byte =
        (((input.toInt() shl count) and 0xff) or ((input.toInt() and 0xff) shr (8 - count))).toByte()

    private fun rol8x(input: Int, count: Int): Int =
        (input shl count) or (input shr (8 - count))

    private fun weirdRor8(input: Int, count: Int): Int {
        if (count == 0) return 0
        return ((input shr count) and 0xff) or ((input and 0xff) shl (8 - count))
    }

    private fun weirdRol8(input: Int, count: Int): Int {
        if (count == 0) return 0
        return ((input shl count) and 0xff) or ((input and 0xff) shr (8 - count))
    }

    private fun weirdRol32(input: Int, count: Int): Int {
        if (count == 0) return 0
        return (input shl count) xor (input shr (8 - count))
    }

    fun garble(buffer0: ByteArray, buffer1: ByteArray, buffer2: ByteArray, buffer3: ByteArray, buffer4: ByteArray) {
        var tmp: Int
        var tmp2: Int
        var tmp3: Int
        var A: Int
        var B: Int
        var C: Int
        var D: Int
        var E: Int
        var M: Int
        var J: Int
        var G: Int
        var F: Int
        var H: Int
        var K: Int
        var R: Int
        var S: Int
        var T: Int
        var U: Int
        var V: Int
        var W: Int
        var X: Int
        var Y: Int
        var Z: Int

        buffer2[12] = (0x14 + ((((buffer1[64].toInt() and 0xff) and 92) or (((buffer1[99].toInt() and 0xff) / 3) and 35))
                and (buffer4[rol8x((buffer4[((buffer1[206].toInt() and 0xff) % 21)].toInt() and 0xff), 4) % 21].toInt() and 0xff))).toByte()
        buffer1[4] = (((buffer1[99].toInt() and 0xff) / 5) * ((buffer1[99].toInt() and 0xff) / 5) * 2).toByte()
        buffer2[34] = 0xb8.toByte()
        buffer1[153] = (buffer1[153].toInt() xor ((buffer2[(buffer1[203].toInt() and 0xff) % 35].toInt() and 0xff) *
                (buffer2[(buffer1[203].toInt() and 0xff) % 35].toInt() and 0xff) *
                (buffer1[190].toInt() and 0xff))).toByte()
        buffer0[3] = (buffer0[3].toInt() - (((buffer4[(buffer1[205].toInt() and 0xff) % 21].toInt() shr 1) and 80) or 0xe6440)).toByte()
        buffer0[16] = 0x93.toByte()
        buffer0[13] = 0x62.toByte()
        buffer1[33] = (buffer1[33].toInt() - (buffer4[(buffer1[36].toInt() and 0xff) % 21].toInt() and 0xf6)).toByte()
        tmp2 = buffer2[(buffer1[67].toInt() and 0xff) % 35].toInt() and 0xff
        buffer2[12] = 0x07.toByte()
        tmp = buffer0[(buffer1[181].toInt() and 0xff) % 20].toInt() and 0xff
        buffer1[2] = (buffer1[2].toInt() - 3136).toByte()
        buffer0[19] = buffer4[(buffer1[58].toInt() and 0xff) % 21]
        buffer3[0] = (92 - (buffer2[(buffer1[32].toInt() and 0xff) % 35].toInt() and 0xff)).toByte()
        buffer3[4] = ((buffer2[(buffer1[15].toInt() and 0xff) % 35].toInt() and 0xff) + 0x9e).toByte()
        buffer1[34] = (buffer1[34].toInt() + ((buffer4[(((buffer2[(buffer1[15].toInt() and 0xff) % 35].toInt() and 0xff) + 0x9e) and 0xff) % 21].toInt() and 0xff) / 5)).toByte()
        buffer0[19] = ((buffer0[19].toInt() and 0xff) + 0xfffffee6 - (((buffer0[(buffer3[4].toInt() and 0xff) % 20].toInt() and 0xff) shr 1) and 102)).toByte()
        val _shift4 = ((buffer4[(buffer1[190].toInt() and 0xff) % 21].toInt() and 0xff) and 7)
        val _shift4sub1 = ((buffer4[(buffer1[190].toInt() and 0xff) % 21].toInt() and 0xff) - 1) and 7
        val _b72 = (buffer1[72].toInt() and 0xff)
        val _rotated = (_b72 shr _shift4) xor (_b72 shl (7 - _shift4sub1))
        val _b126 = (buffer4[(buffer1[126].toInt() and 0xff) % 21].toInt() and 0xff)
        buffer1[15] = ((3 * _rotated - (3 * _b126)) xor buffer1[15].toInt()).toByte()
        buffer0[15] = (buffer0[15].toInt() xor ((buffer2[(buffer1[181].toInt() and 0xff) % 35].toInt() and 0xff) *
                (buffer2[(buffer1[181].toInt() and 0xff) % 35].toInt() and 0xff) *
                (buffer2[(buffer1[181].toInt() and 0xff) % 35].toInt() and 0xff))).toByte()
        buffer2[4] = (buffer2[4].toInt() xor ((buffer1[202].toInt() and 0xff) / 3)).toByte()
        A = 92 - (buffer0[(buffer3[0].toInt() and 0xff) % 20].toInt() and 0xff)
        E = (A and 0xc6) or ((buffer1[105].toInt() and 0xff).inv() and 0xc6) or (A and (buffer1[105].toInt() and 0xff).inv())
        buffer2[1] = (buffer2[1].toInt() + (E * E * E)).toByte()
        buffer0[19] = (buffer0[19].toInt() xor (((224 or ((buffer4[(buffer1[92].toInt() and 0xff) % 21].toInt() and 0xff) and 27)) *
                (buffer2[(buffer1[41].toInt() and 0xff) % 35].toInt() and 0xff)) / 3)).toByte()
        buffer1[140] = (buffer1[140].toInt() + weirdRor8(92, (buffer1[5].toInt() and 0xff) and 7)).toByte()
        buffer2[12] = (buffer2[12].toInt() + (((((buffer1[4].toInt() and 0xff).inv() xor (buffer2[(buffer1[12].toInt() and 0xff) % 35].toInt() and 0xff)) or
                (buffer1[182].toInt() and 0xff)) and 192) or (((buffer1[4].toInt() and 0xff).inv() xor (buffer2[(buffer1[12].toInt() and 0xff) % 35].toInt() and 0xff)) and
                (buffer1[182].toInt() and 0xff)))).toByte()
        buffer1[36] = (buffer1[36].toInt() + 125).toByte()
        buffer1[124] = rol8x(((((74 and (buffer1[138].toInt() and 0xff)) or ((74 or (buffer1[138].toInt() and 0xff)) and (buffer0[15].toInt() and 0xff)))
                and (buffer0[(buffer1[43].toInt() and 0xff) % 20].toInt() and 0xff)) or (((74 and (buffer1[138].toInt() and 0xff)) or
                ((74 or (buffer1[138].toInt() and 0xff)) and (buffer0[15].toInt() and 0xff)) or
                (buffer0[(buffer1[43].toInt() and 0xff) % 20].toInt() and 0xff)) and 95)), 4).toByte()
        buffer3[8] = ((((((buffer0[(buffer3[4].toInt() and 0xff) % 20].toInt() and 0xff) and 95)) and
                (((buffer4[(buffer1[68].toInt() and 0xff) % 21].toInt() and 0xff) and 46) shl 1)) or 16) xor 92).toByte()
        A = (buffer1[177].toInt() and 0xff) + (buffer4[(buffer1[79].toInt() and 0xff) % 21].toInt() and 0xff)
        D = (((A shr 1) or ((3 * (buffer1[148].toInt() and 0xff)) / 5)) and (buffer2[1].toInt() and 0xff)) or ((A shr 1) and ((3 * (buffer1[148].toInt() and 0xff)) / 5))
        buffer3[12] = (-34 - D).toByte()
        A = 8 - (((buffer2[22].toInt() and 0xff) and 7))
        B = ((buffer1[33].toInt() and 0xff) shr (A and 7))
        C = (buffer1[33].toInt() and 0xff) shl ((buffer2[22].toInt() and 0xff) and 7)
        buffer2[16] = (buffer2[16].toInt() + (((buffer2[(buffer3[0].toInt() and 0xff) % 35].toInt() and 0xff) and 159) or
                (buffer0[(buffer3[4].toInt() and 0xff) % 20].toInt() and 0xff) or 8) - ((B xor C) or 128)).toByte()
        buffer0[14] = (buffer0[14].toInt() xor (buffer2[(buffer3[12].toInt() and 0xff) % 35].toInt() and 0xff)).toByte()
        A = weirdRol8((buffer4[(buffer0[(buffer1[201].toInt() and 0xff) % 20].toInt() and 0xff) % 21].toInt() and 0xff),
            (((buffer2[(buffer1[112].toInt() and 0xff) % 35].toInt() and 0xff) shl 1) and 7))
        D = (buffer0[(buffer1[208].toInt() and 0xff) % 20].toInt() and 131) or ((buffer0[(buffer1[164].toInt() and 0xff) % 20].toInt() and 0xff) and 124)
        buffer1[19] = (buffer1[19].toInt() + ((A and (D / 5)) or ((A or (D / 5)) and 37))).toByte()
        buffer2[8] = weirdRor8(140, (((buffer4[(buffer1[45].toInt() and 0xff) % 21].toInt() and 0xff) + 92) *
                ((buffer4[(buffer1[45].toInt() and 0xff) % 21].toInt() and 0xff) + 92)) and 7).toByte()
        buffer1[190] = 56.toByte()
        buffer2[8] = (buffer2[8].toInt() xor (buffer3[0].toInt() and 0xff)).toByte()
        buffer1[53] = ((buffer0[(buffer1[83].toInt() and 0xff) % 20].toInt() and 0xff) or 204).inv().let { (it / 5).toByte() }
        buffer0[13] = (buffer0[13].toInt() + (buffer0[(buffer1[41].toInt() and 0xff) % 20].toInt() and 0xff)).toByte()
        buffer0[10] = ((((buffer2[(buffer3[0].toInt() and 0xff) % 35].toInt() and 0xff) and (buffer1[2].toInt() and 0xff)) or
                (((buffer2[(buffer3[0].toInt() and 0xff) % 35].toInt() and 0xff) or (buffer1[2].toInt() and 0xff)) and
                        (buffer3[12].toInt() and 0xff))) / 15).toByte()
        A = (((56 or ((buffer4[(buffer1[2].toInt() and 0xff) % 21].toInt() and 0xff) and 68)) or
                (buffer2[(buffer3[8].toInt() and 0xff) % 35].toInt() and 0xff)) and 42) or
                ((((buffer4[(buffer1[2].toInt() and 0xff) % 21].toInt() and 0xff) and 68) or 56) and
                        (buffer2[(buffer3[8].toInt() and 0xff) % 35].toInt() and 0xff))
        buffer3[16] = ((A * A) + 110).toByte()
        buffer3[20] = (202 - (buffer3[16].toInt() and 0xff)).toByte()
        buffer3[24] = buffer1[151]
        buffer2[13] = (buffer2[13].toInt() xor (buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff)).toByte()
        B = (((buffer2[(buffer1[179].toInt() and 0xff) % 35].toInt() and 0xff) - 38) and 177) or ((buffer3[12].toInt() and 0xff) and 177)
        C = (((buffer2[(buffer1[179].toInt() and 0xff) % 35].toInt() and 0xff) - 38)) and (buffer3[12].toInt() and 0xff)
        buffer3[28] = (30 + ((B or C) * (B or C))).toByte()
        buffer3[32] = (buffer3[28].toInt() + 62).toByte()
        A = (((buffer3[20].toInt() and 0xff) + ((buffer3[0].toInt() and 0xff) and 74)) or
                (buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff).inv()) and 121
        B = (((buffer3[20].toInt() and 0xff) + ((buffer3[0].toInt() and 0xff) and 74)) and
                (buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff).inv())
        tmp3 = (A or B)
        C = ((((A or B) xor 0xffffffa6.toInt()) or (buffer3[0].toInt() and 0xff)) and 4) or
                (((A or B) xor 0xffffffa6.toInt()) and (buffer3[0].toInt() and 0xff))
        buffer1[47] = (((buffer2[(buffer1[89].toInt() and 0xff) % 35].toInt() and 0xff) + C) xor (buffer1[47].toInt() and 0xff)).toByte()
        buffer3[36] = (((rol8x(((tmp and 179) + 68).toByte().toInt(), 2) and (buffer0[3].toInt() and 0xff)) or
                (tmp2 and (buffer0[3].toInt() and 0xff).inv())) - 15).toByte()
        buffer1[123] = (buffer1[123].toInt() xor 221).toByte()
        A = (((buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff)) / 3) - (buffer2[(buffer3[4].toInt() and 0xff) % 35].toInt() and 0xff)
        C = (((buffer3[0].toInt() and 163) + 92) and 246) or (buffer3[0].toInt() and 92)
        E = ((C or buffer3[24].toInt()) and 54) or (C and buffer3[24].toInt())
        buffer3[40] = (A - E).toByte()
        buffer3[44] = (tmp3 xor 81 xor ((((buffer3[0].toInt() and 0xff) shr 1) and 101) + 26)).toByte()
        buffer3[48] = ((buffer2[(buffer3[4].toInt() and 0xff) % 35].toInt() and 0xff) and 27).toByte()
        buffer3[52] = 27.toByte()
        buffer3[56] = 199.toByte()
        buffer3[64] = ((buffer3[4].toInt() and 0xff) + ((((((((buffer3[40].toInt() and 0xff) or (buffer3[24].toInt() and 0xff)) and 177) or
                ((buffer3[40].toInt() and 0xff) and (buffer3[24].toInt() and 0xff)))
                and (((((buffer4[(buffer3[0].toInt() and 0xff) % 20].toInt() and 0xff) and 177) or 176)) or
                (((buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff)) and 3.inv())))
                or (((((buffer3[40].toInt() and 0xff) and (buffer3[24].toInt() and 0xff)) or
                (((buffer3[40].toInt() and 0xff) or (buffer3[24].toInt() and 0xff)) and 177)) and 199)
                or ((((((buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff) and 1) and 0xff) + 176) or
                        ((buffer4[(buffer3[0].toInt() and 0xff) % 21].toInt() and 0xff) and 3.inv()))
                        and (buffer3[56].toInt() and 0xff)))) and ((buffer3[52].toInt() and 0xff).inv())) or
                (buffer3[48].toInt() and 0xff))).toByte()
        buffer2[33] = (buffer2[33].toInt() xor buffer1[26].toInt()).toByte()
        buffer1[106] = (buffer1[106].toInt() xor (buffer3[20].toInt() xor 133)).toByte()
        buffer2[30] = ((((buffer3[64].toInt() and 0xff) / 3) - (275 or ((buffer3[0].toInt() and 0xff) and 247))) xor
                (buffer0[(buffer1[122].toInt() and 0xff) % 20].toInt() and 0xff)).toByte()
        buffer1[22] = (((buffer2[(buffer1[90].toInt() and 0xff) % 35].toInt() and 0xff) and 95) or 68).toByte()
        A = ((buffer4[(buffer3[36].toInt() and 0xff) % 21].toInt() and 0xff) and 184) or
                ((buffer2[(buffer3[44].toInt() and 0xff) % 35].toInt() and 0xff) and 184.inv())
        buffer2[18] = (buffer2[18].toInt() + ((A * A * A) shr 1)).toByte()
        buffer2[5] = (buffer2[5].toInt() - (buffer4[(buffer1[92].toInt() and 0xff) % 21].toInt() and 0xff)).toByte()
        A = ((((buffer1[41].toInt() and 0xff) and 24.inv()) or ((buffer2[(buffer1[183].toInt() and 0xff) % 35].toInt() and 0xff) and 24)) and
                ((buffer3[16].toInt() and 0xff) + 53)) or (buffer3[20].toInt() and
                (buffer2[(buffer3[20].toInt() and 0xff) % 35].toInt() and 0xff))
        B = ((buffer1[17].toInt() and 0xff) and ((buffer3[44].toInt() and 0xff).inv())) or
                ((buffer0[(buffer1[59].toInt() and 0xff) % 20].toInt() and 0xff) and (buffer3[44].toInt() and 0xff))
        buffer2[18] = (buffer2[18].toInt() xor (A * B)).toByte()
        A = weirdRor8((buffer1[11].toInt() and 0xff), (buffer2[(buffer1[28].toInt() and 0xff) % 35].toInt() and 0xff) and 7) and 7
        B = ((((buffer0[(buffer1[93].toInt() and 0xff) % 20].toInt() and 0xff) and (buffer0[14].toInt() and 0xff).inv()) or
                ((buffer0[14].toInt() and 0xff) and 150)) and 28.inv()) or ((buffer1[7].toInt() and 0xff) and 28)
        buffer2[22] = (((((B or weirdRol8((buffer2[(buffer3[0].toInt() and 0xff) % 35].toInt() and 0xff), A)) and
                (buffer2[33].toInt() and 0xff)) or (B and weirdRol8((buffer2[(buffer3[0].toInt() and 0xff) % 35].toInt() and 0xff), A))) + 74) and 0xff).toByte()
        A = buffer4[((buffer0[(buffer1[39].toInt() and 0xff) % 20].toInt() and 0xff) xor 217) % 21].toInt() and 0xff
        buffer0[15] = (buffer0[15].toInt() - (((((buffer3[20].toInt() and 0xff) or (buffer3[0].toInt() and 0xff)) and 214) or
                ((buffer3[20].toInt() and 0xff) and (buffer3[0].toInt() and 0xff))) and A) or
                (((((buffer3[20].toInt() and 0xff) or (buffer3[0].toInt() and 0xff)) and 214) or
                        ((buffer3[20].toInt() and 0xff) and (buffer3[0].toInt() and 0xff)) or A) and
                        (buffer3[32].toInt() and 0xff))).toByte()
        B = (((buffer2[(buffer1[57].toInt() and 0xff) % 35].toInt() and buffer0[(buffer3[64].toInt() and 0xff) % 20].toInt()) or
                ((buffer0[(buffer3[64].toInt() and 0xff) % 20].toInt() or buffer2[(buffer1[57].toInt() and 0xff) % 35].toInt()) and 95) or
                (buffer3[64].toInt() and 45) or 82) and 32)
        C = ((buffer2[(buffer1[57].toInt() and 0xff) % 35].toInt() and buffer0[(buffer3[64].toInt() and 0xff) % 20].toInt()) or
                ((buffer2[(buffer1[57].toInt() and 0xff) % 35].toInt() or buffer0[(buffer3[64].toInt() and 0xff) % 20].toInt()) and 95)) and
                ((buffer3[64].toInt() and 45) or 82)
        D = (((((buffer3[0].toInt() and 0xff) / 3) - ((buffer3[64].toInt() and 0xff) or (buffer1[22].toInt() and 0xff)))) xor
                ((buffer3[28].toInt() and 0xff) + 62) xor ((B or C)))
        T = (buffer0[(D and 0xff) % 20].toInt() and 0xff)
        buffer3[68] = (((buffer0[(buffer1[99].toInt() and 0xff) % 20].toInt() and 0xff) *
                (buffer0[(buffer1[99].toInt() and 0xff) % 20].toInt() and 0xff) *
                (buffer0[(buffer1[99].toInt() and 0xff) % 20].toInt() and 0xff) *
                (buffer0[(buffer1[99].toInt() and 0xff) % 20].toInt() and 0xff)) or
                (buffer2[(buffer3[64].toInt() and 0xff) % 35].toInt() and 0xff)).toByte()
        U = buffer0[(buffer1[50].toInt() and 0xff) % 20].toInt() and 0xff
        W = buffer2[(buffer1[138].toInt() and 0xff) % 35].toInt() and 0xff
        X = buffer4[(buffer1[39].toInt() and 0xff) % 21].toInt() and 0xff
        Y = buffer0[(buffer1[4].toInt() and 0xff) % 20].toInt() and 0xff
        Z = buffer4[(buffer1[202].toInt() and 0xff) % 21].toInt() and 0xff
        V = buffer0[(buffer1[151].toInt() and 0xff) % 20].toInt() and 0xff
        S = buffer2[(buffer1[14].toInt() and 0xff) % 35].toInt() and 0xff
        R = buffer0[(buffer1[145].toInt() and 0xff) % 20].toInt() and 0xff
        A = ((buffer2[(buffer3[68].toInt() and 0xff) % 35].toInt() and 0xff) and
                (buffer0[(buffer1[209].toInt() and 0xff) % 20].toInt() and 0xff)) or
                (((buffer2[(buffer3[68].toInt() and 0xff) % 35].toInt() and 0xff) or
                        (buffer0[(buffer1[209].toInt() and 0xff) % 20].toInt() and 0xff)) and 24)
        B = weirdRol8((buffer4[(buffer1[127].toInt() and 0xff) % 21].toInt() and 0xff),
            (buffer2[(buffer3[68].toInt() and 0xff) % 35].toInt() and 0xff) and 7)
        C = (A and (buffer0[10].toInt() and 0xff)) or (B and (buffer0[10].toInt() and 0xff).inv())
        D = 7 xor ((buffer4[(buffer2[(buffer3[36].toInt() and 0xff) % 35].toInt() and 0xff) % 21].toInt() and 0xff) shl 1)
        buffer3[72] = ((C and 71) or (D and 71.inv())).toByte()
        buffer2[2] = (buffer2[2].toInt() + ((((buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt() and 0xff) shl 1) and 159) or
                ((buffer4[(buffer1[190].toInt() and 0xff) % 21].toInt() and 0xff) and 159.inv())) and
                (((((buffer4[(buffer3[64].toInt() and 0xff) % 21].toInt() and 0xff) and 110) or
                        ((buffer0[(buffer1[25].toInt() and 0xff) % 20].toInt() and 0xff) and 110.inv())) and 150.inv()) or
                        ((buffer1[25].toInt() and 0xff) and 150))).toByte()
        buffer2[14] = (buffer2[14].toInt() - (((buffer2[(buffer3[20].toInt() and 0xff) % 35].toInt() and 0xff) and
                ((buffer3[72].toInt() and 0xff) xor (buffer2[(buffer1[100].toInt() and 0xff) % 35].toInt() and 0xff))) and 34.inv()) or
                ((buffer1[97].toInt() and 0xff) and 34)).toByte()
        buffer0[17] = 115.toByte()
        buffer1[23] = (buffer1[23].toInt() xor (((((((buffer4[(buffer1[17].toInt() and 0xff) % 21].toInt() and 0xff) or
                (buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt() and 0xff)) and (buffer3[72].toInt() and 0xff)) or
                ((buffer4[(buffer1[17].toInt() and 0xff) % 21].toInt() and 0xff) and
                        (buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt() and 0xff))) and
                ((buffer1[50].toInt() and 0xff) / 3)) or (((((buffer4[(buffer1[17].toInt() and 0xff) % 21].toInt() and 0xff) or
                (buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt() and 0xff)) and (buffer3[72].toInt() and 0xff)) or
                ((buffer4[(buffer1[17].toInt() and 0xff) % 21].toInt() and 0xff) and buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt()) or
                ((buffer1[50].toInt() and 0xff) / 3)) and 246)) shl 1)).toByte()
        buffer0[13] = (((((((buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff) or (buffer1[10].toInt() and 0xff)) and 82) or
                ((buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff) and (buffer1[10].toInt() and 0xff))) and 209) or
                (((buffer0[(buffer1[39].toInt() and 0xff) % 20].toInt() and 0xff) shl 1) and 46)) shr 1).toByte()
        buffer2[33] = (buffer2[33].toInt() - (buffer1[113].toInt() and 9)).toByte()
        buffer2[28] = (buffer2[28].toInt() - ((((2 or (buffer1[110].toInt() and 222)) shr 1) and 223.inv()) or
                (buffer3[20].toInt() and 223))).toByte()
        J = weirdRol8((V or Z), (U and 7))
        A = ((buffer2[16].toInt() and 0xff) and T) or (W and ((buffer2[16].toInt() and 0xff).inv()))
        B = ((buffer1[33].toInt() and 0xff) and 17) or (X and 17.inv())
        E = ((Y or ((A + B) / 5)) and 147) or (Y and ((A + B) / 5))
        M = ((buffer3[40].toInt() and 0xff) and
                (buffer4[(((buffer3[8].toInt() and 0xff) + J + E) and 0xff) % 21].toInt() and 0xff)) or
                (((buffer3[40].toInt() and 0xff) or (buffer4[(((buffer3[8].toInt() and 0xff) + J + E) and 0xff) % 21].toInt() and 0xff)) and
                        (buffer2[23].toInt() and 0xff))
        buffer0[15] = (((((buffer4[(buffer3[20].toInt() and 0xff) % 21].toInt() and 0xff) - 48) and
                ((buffer1[184].toInt() and 0xff).inv())) or (((buffer4[(buffer3[20].toInt() and 0xff) % 21].toInt() and 0xff) - 48) and 189) or
                (189 and (buffer1[184].toInt() and 0xff).inv())) and (M * M * M)).toByte()
        buffer2[22] = (buffer2[22].toInt() + (buffer1[183].toInt() and 0xff)).toByte()
        buffer3[76] = ((3 * (buffer4[(buffer1[1].toInt() and 0xff) % 21].toInt() and 0xff)) xor (buffer3[0].toInt() and 0xff)).toByte()
        A = buffer2[(((buffer3[8].toInt() and 0xff) + (J + E)) and 0xff) % 35].toInt() and 0xff
        F = ((((buffer4[(buffer1[178].toInt() and 0xff) % 21].toInt() and 0xff) and A) or
                (((buffer4[(buffer1[178].toInt() and 0xff) % 21].toInt() and 0xff) or A) and 209)) *
                (buffer0[(buffer1[13].toInt() and 0xff) % 20].toInt() and 0xff)) *
                ((buffer4[(buffer1[26].toInt() and 0xff) % 21].toInt() and 0xff) shr 1)
        G = (F + 0x733ffff9.toInt()) * 198 - (((F + 0x733ffff9.toInt()) * 396 + 212) and 212) + 85
        buffer3[80] = ((buffer3[36].toInt() and 0xff) + (G xor 148) + ((G xor 107) shl 1) - 127).toByte()
        buffer3[84] = ((((buffer2[(buffer3[64].toInt() and 0xff) % 35].toInt() and 0xff)) and 245) or
                ((buffer2[(buffer3[20].toInt() and 0xff) % 35].toInt() and 0xff) and 10)).toByte()
        A = (buffer0[(buffer3[68].toInt() and 0xff) % 20].toInt() and 0xff) or 81
        buffer2[18] = (buffer2[18].toInt() - (((A * A * A) and (buffer0[15].toInt() and 0xff).inv()) or
                (((buffer3[80].toInt() and 0xff) / 15) and (buffer0[15].toInt() and 0xff)))).toByte()
        val _tmp261_b8 = (buffer3[8].toInt() and 0xff) + J + E
        val _tmp261_b160 = (buffer0[(buffer1[160].toInt() and 0xff) % 20].toInt() and 0xff)
        val _tmp261_idx = (_tmp261_b8 and 255) % 20
        val _tmp261_b4 = (buffer4[(buffer0[_tmp261_idx].toInt() and 0xff) % 21].toInt() and 0xff) / 3
        buffer3[88] = (_tmp261_b8 - _tmp261_b160 + _tmp261_b4).toByte()
        B = ((R xor (buffer3[72].toInt() and 0xff)) and 198.inv()) or ((S * S) and 198)
        F = ((buffer4[(buffer1[69].toInt() and 0xff) % 21].toInt() and 0xff) and (buffer1[172].toInt() and 0xff)) or
                (((buffer4[(buffer1[69].toInt() and 0xff) % 21].toInt() and 0xff) or (buffer1[172].toInt() and 0xff)) and
                        (((buffer3[12].toInt() and 0xff) - B) + 77))
        buffer0[16] = (147 - (((buffer3[72].toInt() and 0xff) and ((F and 251) or 1)) or (((F and 250) or (buffer3[72].toInt() and 0xff)) and 198))).toByte()
        C = ((buffer4[(buffer1[168].toInt() and 0xff) % 21].toInt() and 0xff) and
                buffer0[(buffer1[29].toInt() and 0xff) % 20].toInt() and 7) or
                ((buffer4[(buffer1[168].toInt() and 0xff) % 21].toInt() or buffer0[(buffer1[29].toInt() and 0xff) % 20].toInt()) and 6)
        F = ((buffer4[(buffer1[155].toInt() and 0xff) % 21].toInt() and 0xff) and (buffer1[105].toInt() and 0xff)) or
                (((buffer4[(buffer1[155].toInt() and 0xff) % 21].toInt() and 0xff) or (buffer1[105].toInt() and 0xff)) and 141)
        buffer0[3] = (buffer0[3].toInt() - (buffer4[weirdRol32(F, C) % 21].toInt() and 0xff)).toByte()
        buffer1[5] = (weirdRor8((buffer0[12].toInt() and 0xff), (((buffer0[(buffer1[61].toInt() and 0xff) % 20].toInt() and 0xff) / 5) and 7)) xor
                (((buffer2[(buffer3[84].toInt() and 0xff) % 35].toInt() and 0xff).inv() and 0xffffffffL.toInt()) / 5)).toByte()
        buffer1[198] = (buffer1[198].toInt() + (buffer1[3].toInt() and 0xff)).toByte()
        A = (162 or (buffer2[(buffer3[64].toInt() and 0xff) % 35].toInt() and 0xff))
        buffer1[164] = (buffer1[164].toInt() + ((A * A) / 5)).toByte()
        G = weirdRor8(139, ((buffer3[80].toInt() and 0xff) and 7))
        C = (((buffer4[(buffer3[64].toInt() and 0xff) % 21].toInt() and 0xff) *
                (buffer4[(buffer3[64].toInt() and 0xff) % 21].toInt() and 0xff) *
                (buffer4[(buffer3[64].toInt() and 0xff) % 21].toInt() and 0xff)) and 95) or
                ((buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff) and 95.inv())
        buffer3[92] = ((G and 12) or ((buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt() and 0xff) and 12) or
                (G and (buffer0[(buffer3[20].toInt() and 0xff) % 20].toInt() and 0xff)) or C).toByte()
        buffer2[12] = (buffer2[12].toInt() + ((((buffer1[103].toInt() and 0xff) and 32) or
                ((buffer3[92].toInt() and 0xff) and ((buffer1[103].toInt() and 0xff) or 60))) or 16) / 3).toByte()
        buffer3[96] = buffer1[143]
        buffer3[100] = 27.toByte()
        buffer3[104] = (((((buffer3[40].toInt() and 0xff) and (buffer2[8].toInt() and 0xff).inv()) or
                ((buffer1[35].toInt() and 0xff) and (buffer2[8].toInt() and 0xff))) and (buffer3[64].toInt() and 0xff)) xor 119).toByte()
        buffer3[108] = (238 and (((((buffer3[40].toInt() and 0xff) and (buffer2[8].toInt() and 0xff).inv()) or
                ((buffer1[35].toInt() and 0xff) and (buffer2[8].toInt() and 0xff))) and (buffer3[64].toInt() and 0xff)) shl 1)).toByte()
        buffer3[112] = (((buffer3[64].toInt() and 0xff).inv() and ((buffer3[84].toInt() and 0xff) / 3)) xor 49).toByte()
        buffer3[116] = (98 and (((buffer3[64].toInt() and 0xff).inv() and ((buffer3[84].toInt() and 0xff) / 3)) shl 1)).toByte()
        A = ((buffer1[35].toInt() and 0xff) and (buffer2[8].toInt() and 0xff)) or ((buffer3[40].toInt() and 0xff) and (buffer2[8].toInt() and 0xff).inv())
        B = (A and buffer3[64].toInt()) or ((((buffer3[84].toInt() and 0xff) / 3) and (buffer3[64].toInt() and 0xff).inv()))
        val _b172_64 = ((buffer1[172].toInt() and 0xff) and 64) shr 1
        val _term1 = B and (86 + _b172_64)
        val _b172_65 = ((buffer1[172].toInt() and 0xff) and 65) shr 1
        val _b64_inv_b84_div3 = (buffer3[64].toInt() and 0xff).inv() and ((buffer3[84].toInt() and 0xff) / 3)
        val _b35_b40_b2_8 = (((buffer3[40].toInt() and 0xff) and (buffer2[8].toInt() and 0xff).inv()) or
                ((buffer1[35].toInt() and 0xff) and (buffer2[8].toInt() and 0xff))) and (buffer3[64].toInt() and 0xff)
        val _term2_inner = (_b172_65 xor 86) or _b64_inv_b84_div3 or _b35_b40_b2_8
        val _term2 = _term2_inner and (buffer3[100].toInt() and 0xff)
        buffer1[143] = ((buffer3[96].toInt() and 0xff) - (_term1 or _term2)).toByte()
        buffer2[29] = 162.toByte()
        A = (((((buffer4[(buffer3[88].toInt() and 0xff) % 21].toInt() and 0xff)) and 160) or
                ((buffer0[(buffer1[125].toInt() and 0xff) % 20].toInt() and 0xff) and 95)) shr 1)
        B = (buffer2[(buffer1[149].toInt() and 0xff) % 35].toInt() and 0xff) xor
                ((buffer1[43].toInt() and 0xff) * (buffer1[43].toInt() and 0xff))
        buffer0[15] = (buffer0[15].toInt() + ((B and A) or ((A or B) and 115))).toByte()
        buffer3[120] = ((buffer3[64].toInt() and 0xff) - (buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff)).toByte()
        buffer1[95] = buffer4[(buffer3[20].toInt() and 0xff) % 21]
        A = weirdRor8((buffer2[(buffer3[80].toInt() and 0xff) % 35].toInt() and 0xff),
            ((buffer2[(buffer1[17].toInt() and 0xff) % 35].toInt() and 0xff) *
                    (buffer2[(buffer1[17].toInt() and 0xff) % 35].toInt() and 0xff) *
                    (buffer2[(buffer1[17].toInt() and 0xff) % 35].toInt() and 0xff)) and 7)
        buffer0[7] = (buffer0[7].toInt() - (A * A)).toByte()
        buffer2[8] = ((buffer2[8].toInt() and 0xff) - (buffer1[184].toInt() and 0xff) +
                ((buffer4[(buffer1[202].toInt() and 0xff) % 21].toInt() and 0xff) *
                        (buffer4[(buffer1[202].toInt() and 0xff) % 21].toInt() and 0xff) *
                        (buffer4[(buffer1[202].toInt() and 0xff) % 21].toInt() and 0xff))).toByte()
        buffer0[16] = (((buffer2[(buffer1[102].toInt() and 0xff) % 35].toInt() and 0xff) shl 1) and 132).toByte()
        buffer3[124] = (((buffer4[(buffer3[40].toInt() and 0xff) % 21].toInt() and 0xff) shr 1) xor (buffer3[68].toInt() and 0xff)).toByte()
        buffer0[7] = (buffer0[7].toInt() - ((buffer0[(buffer1[191].toInt() and 0xff) % 20].toInt() and 0xff) -
                ((((buffer4[(buffer1[80].toInt() and 0xff) % 21].toInt() and 0xff) shl 1) and 177.inv()) or
                        ((buffer4[(buffer4[(buffer3[88].toInt() and 0xff) % 21].toInt() and 0xff) % 21].toInt() and 0xff) and 177)))).toByte()
        buffer0[6] = buffer0[(buffer1[119].toInt() and 0xff) % 20]
        A = (buffer4[(buffer1[190].toInt() and 0xff) % 21].toInt() and 209.inv()) or (buffer1[118].toInt() and 209)
        B = (buffer0[(buffer3[120].toInt() and 0xff) % 20].toInt() and 0xff) * (buffer0[(buffer3[120].toInt() and 0xff) % 20].toInt() and 0xff)
        buffer0[12] = ((buffer0[(buffer3[84].toInt() and 0xff) % 20].toInt() xor
                (buffer2[(buffer1[71].toInt() and 0xff) % 35].toInt() + buffer2[(buffer1[15].toInt() and 0xff) % 35].toInt())) and
                ((A and B) or ((A or B) and 27))).toByte()
        B = ((buffer1[32].toInt() and 0xff) and (buffer2[(buffer3[88].toInt() and 0xff) % 35].toInt() and 0xff)) or
                (((buffer1[32].toInt() and 0xff) or (buffer2[(buffer3[88].toInt() and 0xff) % 35].toInt() and 0xff)) and 23)
        D = ((((buffer4[(buffer1[57].toInt() and 0xff) % 21].toInt() and 0xff) * 231) and 169) or (B and 86))
        F = ((((buffer0[(buffer1[82].toInt() and 0xff) % 20].toInt() and 0xff) and 29.inv()) or
                ((buffer4[(buffer3[124].toInt() and 0xff) % 21].toInt() and 0xff) and 29)) and 190) or
                ((buffer4[(D / 5) % 21].toInt() and 0xff) and 190.inv())
        H = (buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff) *
                (buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff) *
                (buffer0[(buffer3[40].toInt() and 0xff) % 20].toInt() and 0xff)
        K = (H and (buffer1[82].toInt() and 0xff)) or (H and 92) or ((buffer1[82].toInt() and 0xff) and 92)
        buffer3[128] = (((F and K) or ((F or K) and 192)) xor (D / 5)).toByte()
        buffer2[25] = (buffer2[25].toInt() xor (((buffer0[(buffer3[120].toInt() and 0xff) % 20].toInt() and 0xff) shl 1) *
                (buffer1[5].toInt() and 0xff)) - (weirdRol8((buffer3[76].toInt() and 0xff),
            ((buffer4[(buffer3[124].toInt() and 0xff) % 21].toInt() and 0xff) and 7)) and
                ((buffer3[20].toInt() and 0xff) + 110))).toByte()
    }
}
