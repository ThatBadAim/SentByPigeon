package com.hybrid.messaging.core.data.crypto

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class E2EECryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "e2ee_keys_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val identityKeyPair: KeyPair by lazy {
        getOrGenerateIdentityKeyPair()
    }

    private fun getOrGenerateIdentityKeyPair(): KeyPair {
        val pubKeyStr = prefs.getString("identity_pub", null)
        val privKeyStr = prefs.getString("identity_priv", null)

        if (pubKeyStr != null && privKeyStr != null) {
            val pubKey = KeyFactory.getInstance("EC").generatePublic(
                X509EncodedKeySpec(Base64.decode(pubKeyStr, Base64.NO_WRAP))
            )
            val privKey = KeyFactory.getInstance("EC").generatePrivate(
                PKCS8EncodedKeySpec(Base64.decode(privKeyStr, Base64.NO_WRAP))
            )
            return KeyPair(pubKey, privKey)
        }

        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(256, SecureRandom())
        val kp = kpg.generateKeyPair()

        prefs.edit()
            .putString("identity_pub", Base64.encodeToString(kp.public.encoded, Base64.NO_WRAP))
            .putString("identity_priv", Base64.encodeToString(kp.private.encoded, Base64.NO_WRAP))
            .apply()

        return kp
    }

    fun generatePreKeyBundle(): String {
        return Base64.encodeToString(identityKeyPair.public.encoded, Base64.NO_WRAP)
    }

    private fun getSharedSecret(remotePublicKeyStr: String?): ByteArray {
        // In a real Double Ratchet, this would establish shared secrets via X3DH
        // For this mock, we use static Diffie-Hellman or a mocked key if missing
        if (remotePublicKeyStr == null) {
            // Mock symmetric key for local testing if remote pubkey is missing
            val kpg = KeyPairGenerator.getInstance("EC")
            kpg.initialize(256, SecureRandom())
            val dummyKp = kpg.generateKeyPair()

            val ka = KeyAgreement.getInstance("ECDH")
            ka.init(identityKeyPair.private)
            ka.doPhase(dummyKp.public, true)
            return ka.generateSecret()
        }

        val remotePubKey = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(Base64.decode(remotePublicKeyStr, Base64.NO_WRAP))
        )
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(identityKeyPair.private)
        ka.doPhase(remotePubKey, true)
        return ka.generateSecret()
    }

    private fun getSessionKey(sharedSecret: ByteArray): SecretKeySpec {
        // We take the first 32 bytes (or pad) for AES-256
        val padded = sharedSecret.copyOf(32)
        return SecretKeySpec(padded, "AES")
    }

    fun encrypt(text: String, remotePublicKeyBase64: String? = null): String {
        val sessionKey = getSessionKey(getSharedSecret(remotePublicKeyBase64))

        val bytes = text.toByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(bytes)

        val result = iv + encryptedBytes
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    fun decrypt(encryptedString: String, remotePublicKeyBase64: String? = null): String {
        val sessionKey = getSessionKey(getSharedSecret(remotePublicKeyBase64))

        val decoded = Base64.decode(encryptedString, Base64.NO_WRAP)
        val iv = decoded.copyOfRange(0, 12)
        val encryptedBytes = decoded.copyOfRange(12, decoded.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, GCMParameterSpec(128, iv))

        return String(cipher.doFinal(encryptedBytes))
    }
}
