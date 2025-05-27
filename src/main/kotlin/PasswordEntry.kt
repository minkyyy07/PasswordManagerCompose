data class PasswordEntry(
    val service: String,
    val username: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val category: String = "Общие",
    val notes: String = ""
)