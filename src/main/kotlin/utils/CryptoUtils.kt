package utils

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val TAG_LENGTH = 128

    private val secretKey: SecretKey by lazy {
        try {
            val kg = KeyGenerator.getInstance("AES")
            kg.init(KEY_SIZE)
            kg.generateKey()
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate secret key", e)
        }
    }

    fun encrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM) // Fixed variable name
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            val parameterSpec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val encryptedData = cipher.doFinal(plainText.toByteArray())

            val combined = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            throw RuntimeException("Encryption failed", e)
        }
    }

    fun decrypt(encryptedText: String): String {
        try {
            val combined = Base64.getDecoder().decode(encryptedText)

            val iv = ByteArray(12)
            val encryptedData = ByteArray(combined.size - iv.size)

            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, encryptedData, 0, encryptedData.size)

            val parameterSpec = GCMParameterSpec(TAG_LENGTH, iv)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            val decryptedData = cipher.doFinal(encryptedData)
            return String(decryptedData) // Added return statement
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }
}