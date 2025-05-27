package storage

import PasswordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import utils.CryptoUtils
import java.io.File

data class SerializablePasswordEntry(
    val service: String,
    val username: String,
    val encryptedPassword: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

object PasswordStorage {
    private val dataFile = File("passwords.dat")
    private val json = Json { prettyPrint = true }

    suspend fun saveEntries(entries: List<PasswordEntry>) = withContext(Dispatchers.IO) {
        try {
            val serializableEntries = entries.map {
                SerializablePasswordEntry(
                    service = it.service,
                    username = it.username,
                    encryptedPassword = CryptoUtils.encrypt(it.password)
                )
            }

            dataFile.writeText(json.encodeToString(serializableEntries))
        } catch (e: Exception) {
            throw RuntimeException("Failed to save entries", e)
        }
    }

    suspend fun loadEntries(): List<PasswordEntry> = withContext(Dispatchers.IO) {
        if (!dataFile.exists()) return@withContext emptyList()

        try {
            val content = dataFile.readText()
            val serializableEntries: List<SerializablePasswordEntry> =
                json.decodeFromString(content)

            serializableEntries.map {
                PasswordEntry(
                    service = it.service,
                    username = it.username,
                    password = CryptoUtils.decrypt(it.encryptedPassword)
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to load entries", e)
        }
    }
}