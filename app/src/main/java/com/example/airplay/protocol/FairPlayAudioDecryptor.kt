package com.example.airplay.protocol

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

class FairPlayAudioDecryptor(aesKey: ByteArray, aesIV: ByteArray, sharedSecret: ByteArray) {
    private val aesIv: ByteArray
    private val eaesKey: ByteArray
    private val aesCbcDecrypt: Cipher

    init {
        this.aesIv = aesIV

        val sha512Digest = MessageDigest.getInstance("SHA-512")
        sha512Digest.update(aesKey)
        sha512Digest.update(sharedSecret)
        eaesKey = sha512Digest.digest().copyOfRange(0, 16)

        aesCbcDecrypt = Cipher.getInstance("AES/CBC/NoPadding")
    }

    @Throws(Exception::class)
    fun decrypt(audio: ByteArray, audioLength: Int) {
        aesCbcDecrypt.init(Cipher.DECRYPT_MODE, SecretKeySpec(eaesKey, "AES"), IvParameterSpec(aesIv))
        aesCbcDecrypt.update(audio, 0, audioLength / 16 * 16, audio, 0)
    }
}
