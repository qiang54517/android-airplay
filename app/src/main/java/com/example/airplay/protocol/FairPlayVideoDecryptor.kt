package com.example.airplay.protocol

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class FairPlayVideoDecryptor(aesKey: ByteArray, sharedSecret: ByteArray, streamConnectionID: String) {
    private val aesCtrDecrypt: Cipher
    private val og = ByteArray(16)
    private var nextDecryptCount = 0

    init {
        val sha512Digest = MessageDigest.getInstance("SHA-512")
        sha512Digest.update(aesKey)
        sha512Digest.update(sharedSecret)
        val eaesKey = sha512Digest.digest()

        val skey = ("AirPlayStreamKey$streamConnectionID").toByteArray(Charsets.UTF_8)
        sha512Digest.update(skey)
        sha512Digest.update(eaesKey, 0, 16)
        val hash1 = sha512Digest.digest()

        val siv = ("AirPlayStreamIV$streamConnectionID").toByteArray(Charsets.UTF_8)
        sha512Digest.update(siv)
        sha512Digest.update(eaesKey, 0, 16)
        val hash2 = sha512Digest.digest()

        val decryptAesKey = ByteArray(16)
        val decryptAesIV = ByteArray(16)
        System.arraycopy(hash1, 0, decryptAesKey, 0, 16)
        System.arraycopy(hash2, 0, decryptAesIV, 0, 16)

        aesCtrDecrypt = Cipher.getInstance("AES/CTR/NoPadding")
        aesCtrDecrypt.init(Cipher.DECRYPT_MODE, SecretKeySpec(decryptAesKey, "AES"), IvParameterSpec(decryptAesIV))
    }

    fun decrypt(video: ByteArray) {
        if (nextDecryptCount > 0) {
            for (i in 0 until nextDecryptCount) {
                video[i] = (video[i].toInt() xor og[(16 - nextDecryptCount) + i].toInt()).toByte()
            }
        }

        val encryptLen = ((video.size - nextDecryptCount) / 16) * 16
        aesCtrDecrypt.update(video, nextDecryptCount, encryptLen, video, nextDecryptCount)
        System.arraycopy(video, nextDecryptCount, video, nextDecryptCount, encryptLen)

        val restLen = (video.size - nextDecryptCount) % 16
        val restStart = video.size - restLen
        nextDecryptCount = 0
        if (restLen > 0) {
            Arrays.fill(og, 0.toByte())
            System.arraycopy(video, restStart, og, 0, restLen)
            aesCtrDecrypt.update(og, 0, 16, og, 0)
            System.arraycopy(og, 0, video, restStart, restLen)
            nextDecryptCount = 16 - restLen
        }
    }
}
