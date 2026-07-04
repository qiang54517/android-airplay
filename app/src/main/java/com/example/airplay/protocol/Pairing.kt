package com.example.airplay.protocol

import com.dd.plist.BinaryPropertyListWriter
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSString
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.KeyPairGenerator
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.whispersystems.curve25519.Curve25519
import org.whispersystems.curve25519.Curve25519KeyPair
import java.io.InputStream
import java.io.OutputStream
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Pairing {
    private val keyPair: java.security.KeyPair
    private var edTheirs: ByteArray? = null
    private var ecdhOurs: ByteArray? = null
    private var ecdhTheirs: ByteArray? = null
    private var ecdhSecret: ByteArray? = null
    private var pairVerified = false

    init {
        keyPair = KeyPairGenerator().generateKeyPair()
    }

    fun info(width: Int, height: Int, fps: Int, out: OutputStream) {
        val audioFormat100 = NSDictionary()
        audioFormat100["audioInputFormats"] = NSNumber(67108860)
        audioFormat100["audioOutputFormats"] = NSNumber(67108860)
        audioFormat100["type"] = NSNumber(100)

        val audioFormat101 = NSDictionary()
        audioFormat101["audioInputFormats"] = NSNumber(67108860)
        audioFormat101["audioOutputFormats"] = NSNumber(67108860)
        audioFormat101["type"] = NSNumber(101)

        val audioFormats = NSArray(2)
        audioFormats.setValue(0, audioFormat100)
        audioFormats.setValue(1, audioFormat101)

        val audioLatency100 = NSDictionary()
        audioLatency100["audioType"] = NSString("default")
        audioLatency100["inputLatencyMicros"] = NSNumber(false)
        audioLatency100["type"] = NSNumber(100)

        val audioLatency101 = NSDictionary()
        audioLatency101["audioType"] = NSString("default")
        audioLatency101["inputLatencyMicros"] = NSNumber(false)
        audioLatency101["type"] = NSNumber(101)

        val audioLatencies = NSArray(2)
        audioLatencies.setValue(0, audioLatency100)
        audioLatencies.setValue(1, audioLatency101)

        val display = NSDictionary()
        display["features"] = NSNumber(14)
        display["height"] = NSNumber(height)
        display["heightPhysical"] = NSNumber(false)
        display["heightPixels"] = NSNumber(height)
        display["maxFPS"] = NSNumber(fps)
        display["overscanned"] = NSNumber(false)
        display["refreshRate"] = NSNumber(60)
        display["rotation"] = NSNumber(false)
        display["uuid"] = NSString("e5f7a68d-7b0f-4305-984b-974f677a150b")
        display["width"] = NSNumber(width)
        display["widthPhysical"] = NSNumber(false)
        display["widthPixels"] = NSNumber(width)

        val displays = NSArray(1)
        displays.setValue(0, display)

        val response = NSDictionary()
        response["audioFormats"] = audioFormats
        response["audioLatencies"] = audioLatencies
        response["displays"] = displays
        response["features"] = NSNumber(130367356919L)
        response["keepAliveSendStatsAsBody"] = NSNumber(1)
        response["model"] = NSString("AppleTV3,2")
        response["name"] = NSString("Android AirPlay")
        response["pi"] = NSString("b08f5a79-db29-4384-b456-a4784d9e6055")
        response["sourceVersion"] = NSString("220.68")
        response["statusFlags"] = NSNumber(68)
        response["vv"] = NSNumber(2)

        BinaryPropertyListWriter.write(response, out)
    }

    fun pairSetup(out: OutputStream) {
        out.write((keyPair.public as EdDSAPublicKey).abyte)
    }

    fun pairVerify(request: InputStream, response: OutputStream) {
        val flag = request.read()
        request.skip(3)

        if (flag > 0) {
            ecdhTheirs = ByteArray(32)
            edTheirs = ByteArray(32)
            request.read(ecdhTheirs)
            request.read(edTheirs)

            val curve25519 = Curve25519.getInstance(Curve25519.BEST)
            val curve25519KeyPair = curve25519.generateKeyPair()

            ecdhOurs = curve25519KeyPair.publicKey
            ecdhSecret = curve25519.calculateAgreement(ecdhTheirs, curve25519KeyPair.privateKey)

            val cipher = initCipher()

            val dataToSign = ByteArray(64)
            System.arraycopy(ecdhOurs, 0, dataToSign, 0, 32)
            System.arraycopy(ecdhTheirs, 0, dataToSign, 32, 32)

            val edDSAEngine = EdDSAEngine()
            edDSAEngine.initSign(keyPair.private)
            val signature = edDSAEngine.signOneShot(dataToSign)

            val encryptedSignature = cipher.doFinal(signature)

            val responseContent = ByteArray(ecdhOurs!!.size + encryptedSignature.size)
            System.arraycopy(ecdhOurs, 0, responseContent, 0, ecdhOurs!!.size)
            System.arraycopy(encryptedSignature, 0, responseContent, ecdhOurs!!.size, encryptedSignature.size)
            response.write(responseContent)
        } else {
            val signature = ByteArray(64)
            request.read(signature)

            val cipher = initCipher()

            val sigBuffer = ByteArray(64)
            cipher.update(sigBuffer)
            cipher.doFinal(signature, 0, signature.size, sigBuffer, 0)

            val sigMessage = ByteArray(64)
            System.arraycopy(ecdhTheirs, 0, sigMessage, 0, 32)
            System.arraycopy(ecdhOurs, 0, sigMessage, 32, 32)

            val edDSAPublicKey = EdDSAPublicKey(
                EdDSAPublicKeySpec(edTheirs, EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519))
            )

            val edDSAEngine = EdDSAEngine()
            edDSAEngine.initVerify(edDSAPublicKey)

            pairVerified = edDSAEngine.verifyOneShot(sigMessage, sigBuffer)
        }
    }

    fun isPairVerified() = pairVerified
    fun getSharedSecret() = ecdhSecret

    private fun initCipher(): Cipher {
        val sha512Digest = MessageDigest.getInstance("SHA-512")
        sha512Digest.update("Pair-Verify-AES-Key".toByteArray(Charsets.UTF_8))
        sha512Digest.update(ecdhSecret)
        val aesKey = sha512Digest.digest().copyOfRange(0, 16)

        sha512Digest.update("Pair-Verify-AES-IV".toByteArray(Charsets.UTF_8))
        sha512Digest.update(ecdhSecret)
        val aesIV = sha512Digest.digest().copyOfRange(0, 16)

        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"), IvParameterSpec(aesIV))
        return cipher
    }
}
