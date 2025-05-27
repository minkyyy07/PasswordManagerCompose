import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import storage.PasswordStorage
import utils.CryptoUtils
import kotlin.random.Random

class PasswordManager {
    val entries = mutableStateListOf<PasswordEntry>()
    private var isInitialized = false

    suspend fun initialize() {
        if (isInitialized) return

        val loadedEntries = PasswordStorage.loadEntries()
        entries.clear()
        entries.addAll(loadedEntries)
        isInitialized = true
    }

    suspend fun addEntry(entry: PasswordEntry) {
        entries.add(entry)
        saveEntries()
    }

    suspend fun removeEntry(entry: PasswordEntry) {
        entries.remove(entry)
        saveEntries()
    }

    suspend fun updateEntry(oldEntry: PasswordEntry, newEntry: PasswordEntry) {
        val index = entries.indexOf(oldEntry)
        if (index >= 0) {
            entries[index] = newEntry
            saveEntries()
        }
    }

    suspend fun toggleFavorite(entry: PasswordEntry) {
        val index = entries.indexOf(entry)
        if (index >= 0) {
            entries[index] = entry.copy(isFavorite = !entry.isFavorite)
            saveEntries()
        }
    }

    suspend fun updateCategory(entry: PasswordEntry, newCategory: String) {
        val index = entries.indexOf(entry)
        if (index >= 0) {
            entries[index] = entry.copy(category = newCategory)
            saveEntries()
        }
    }

    suspend fun updateNotes(entry: PasswordEntry, notes: String) {
        val index = entries.indexOf(entry)
        if (index >= 0) {
            entries[index] = entry.copy(notes = notes)
            saveEntries()
        }
    }

    suspend fun changeMasterPassword() {
        val updatedEntries = CryptoUtils.changeMasterKey(
            entries = entries.toList(),
            passwordExtractor = { it.password },
            entryUpdater = { entry, newPassword -> entry.copy(password = newPassword) }
        )
        entries.clear()
        entries.addAll(updatedEntries)
        saveEntries()
    }

    private suspend fun saveEntries() {
        PasswordStorage.saveEntries(entries)
    }

    fun findEntries(query: String): List<PasswordEntry> =
        entries.filter {
            it.service.contains(query, true) || 
            it.username.contains(query, true) ||
            it.category.contains(query, true) ||
            it.notes.contains(query, true)
        }
        
    fun getFavorites(): List<PasswordEntry> =
        entries.filter { it.isFavorite }
        
    fun getByCategory(category: String): List<PasswordEntry> =
        entries.filter { it.category == category }
        
    fun getAllCategories(): Set<String> =
        entries.map { it.category }.toSet()
}

object PasswordGenerator {
    private const val LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?/"
    
    fun generate(
        length: Int = 12,
        useDigits: Boolean = true,
        useUpper: Boolean = true,
        useSpecial: Boolean = true
    ): String {
        var chars = LOWER_CHARS
        if (useUpper) chars += UPPER_CHARS
        if (useDigits) chars += DIGITS
        if (useSpecial) chars += SPECIAL_CHARS
        
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
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