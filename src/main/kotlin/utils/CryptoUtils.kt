package utils

import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CryptoUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val TAG_LENGTH = 128
    private const val KEY_FILE = "master.key"

    private val secretKey: SecretKey by lazy {
        try {
            val keyFile = File(KEY_FILE)
            
            if (keyFile.exists()) {
                // Загрузка существующего ключа
                val keyBytes = Base64.getDecoder().decode(keyFile.readText())
                SecretKeySpec(keyBytes, "AES")
            } else {
                // Генерация нового ключа
                val kg = KeyGenerator.getInstance("AES")
                kg.init(KEY_SIZE)
                val key = kg.generateKey()
                
                // Сохранение ключа в файл
                keyFile.writeText(Base64.getEncoder().encodeToString(key.encoded))
                key
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate or load secret key", e)
        }
    }

    fun encrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
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
            return String(decryptedData)
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }
    
    // Функция для смены мастер-ключа и перешифрования всех паролей
    suspend fun <T> changeMasterKey(entries: List<T>, passwordExtractor: (T) -> String, entryUpdater: (T, String) -> T): List<T> {
        return withContext(Dispatchers.IO) {
            try {
                // Генерация нового ключа
                val kg = KeyGenerator.getInstance("AES")
                kg.init(KEY_SIZE)
                val newKey = kg.generateKey()
                
                // Расшифровка всех паролей со старым ключом и зашифровка новым
                val updatedEntries = entries.map { entry ->
                    val encryptedPassword = passwordExtractor(entry)
                    val decryptedPassword = decrypt(encryptedPassword)
                    
                    // Временно заменяем ключ на новый
                    val oldKey = secretKey
                    val keyField = CryptoUtils::class.java.getDeclaredField("secretKey")
                    keyField.isAccessible = true
                    keyField.set(CryptoUtils, newKey)
                    
                    // Шифруем пароль новым ключом
                    val newEncryptedPassword = encrypt(decryptedPassword)
                    
                    // Возвращаем старый ключ обратно
                    keyField.set(CryptoUtils, oldKey)
                    
                    // Создаем новую запись с перешифрованным паролем
                    entryUpdater(entry, newEncryptedPassword)
                }
                
                // Сохраняем новый ключ в файл
                val keyFile = File(KEY_FILE)
                keyFile.writeText(Base64.getEncoder().encodeToString(newKey.encoded))
                
                // Обновляем поле secretKey
                val keyField = CryptoUtils::class.java.getDeclaredField("secretKey")
                keyField.isAccessible = true
                keyField.set(CryptoUtils, newKey)
                
                updatedEntries
            } catch (e: Exception) {
                throw RuntimeException("Failed to change master key", e)
            }
        }
    }
    
    fun evaluatePasswordStrength(password: String): Int {
        if (password.isEmpty()) return 0
        
        var score = 0
        
        // Длина пароля (до 40 баллов)
        score += minOf(password.length * 4, 40)
        
        // Наличие разных типов символов
        val hasLower = password.any { it.lowercaseChar() in 'a'..'z' }
        val hasUpper = password.any { it.uppercaseChar() in 'A'..'Z' }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        // Бонусы за разнообразие символов (до 25 баллов)
        if (hasLower) score += 10
        if (hasUpper) score += 10
        if (hasDigit) score += 10
        if (hasSpecial) score += 15
        
        // Бонус за комбинацию разных типов символов (до 15 баллов)
        val typesCount = listOf(hasLower, hasUpper, hasDigit, hasSpecial).count { it }
        score += (typesCount - 1) * 5
        
        // Штраф за повторяющиеся символы
        val repeatedChars = password.groupingBy { it }.eachCount().count { it.value > 1 }
        score -= repeatedChars * 2
        
        // Ограничение итогового значения от 0 до 100
        return score.coerceIn(0, 100)
    }
}