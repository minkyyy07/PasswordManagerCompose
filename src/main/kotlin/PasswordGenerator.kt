import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

class PasswordManager {
    val entries = mutableStateListOf<PasswordEntry>()

    fun addEntry(entry: PasswordEntry) {
        entries.add(entry)
    }

    fun removeEntry(entry: PasswordEntry) {
        entries.remove(entry)
    }

    fun findEntries(query: String): List<PasswordEntry> =
        entries.filter {
            it.service.contains(query, true) || it.username.contains(query, true)
        }
}