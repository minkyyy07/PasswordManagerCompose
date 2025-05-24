import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Password Manager",
        state = rememberWindowState(width = 900.dp, height = 700.dp)
    ) {
        // Центрируем интерфейс
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF201C24)),
            contentAlignment = Alignment.Center
        ) {
            App()
        }
    }
}

object PasswordGenerator {
    fun generate(): String {
        return (1..12)
            .map { ('a'..'z').random() }
            .joinToString("")
    }
}

@Composable
@Preview
fun App() {
    val manager = remember { PasswordManager() }
    var service by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }

    val shownEntries = if (search.isBlank()) manager.entries else manager.findEntries(search)

    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFF8E24AA),
            primaryVariant = Color(0xFF5C007A),
            secondary = Color(0xFF7C4DFF),
            background = Color(0xFF201C24),
            surface = Color(0xFF29243A),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFE1BEE7),
            onSurface = Color(0xFFE1BEE7),
            error = Color(0xFFD500F9)
        )
    ) {
        Surface(
            modifier = Modifier.widthIn(min = 500.dp, max = 600.dp).padding(32.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colors.surface),
            elevation = 16.dp,
            color = MaterialTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                Text(
                    text = "Password Manager",
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFCE93D8),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = service,
                        onValueChange = { service = it },
                        label = { Text("Service", color = Color(0xFFCE93D8)) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF8E24AA),
                            unfocusedBorderColor = Color(0xFF7C4DFF),
                            textColor = Color(0xFFE1BEE7),
                            cursorColor = Color(0xFF8E24AA)
                        )
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = Color(0xFFCE93D8)) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF8E24AA),
                            unfocusedBorderColor = Color(0xFF7C4DFF),
                            textColor = Color(0xFFE1BEE7),
                            cursorColor = Color(0xFF8E24AA)
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color(0xFFCE93D8)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF8E24AA),
                            unfocusedBorderColor = Color(0xFF7C4DFF),
                            textColor = Color(0xFFE1BEE7),
                            cursorColor = Color(0xFF8E24AA)
                        )
                    )
                    Button(
                        onClick = { password = PasswordGenerator.generate() },
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8E24AA), contentColor = Color.White)
                    ) { Text("Generate") }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (service.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                val encrypted = CryptoUtils.encrypt(password)
                                manager.addEntry(
                                    PasswordEntry(service.trim(), username.trim(), encrypted)
                                )
                                service = ""; username = ""; password = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7C4DFF), contentColor = Color.White)
                    ) { Text("Add Entry") }
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Search", color = Color(0xFFCE93D8)) },
                        modifier = Modifier.padding(start = 16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF8E24AA),
                            unfocusedBorderColor = Color(0xFF7C4DFF),
                            textColor = Color(0xFFE1BEE7),
                            cursorColor = Color(0xFF8E24AA)
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))
                PasswordTable(entries = shownEntries, onCopyPassword = { encrypted ->
                    val decrypted = CryptoUtils.decrypt(encrypted)
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(decrypted), null)
                }, onDelete = { entry ->
                    manager.removeEntry(entry)
                })
            }
        }
    }
}

@Composable
fun PasswordTable(
    entries: List<PasswordEntry>,
    onCopyPassword: (String) -> Unit,
    onDelete: (PasswordEntry) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("Service", Modifier.weight(1f), style = MaterialTheme.typography.subtitle1, color = Color(0xFFCE93D8))
            Text("Username", Modifier.weight(1f), style = MaterialTheme.typography.subtitle1, color = Color(0xFFCE93D8))
            Text("Password", Modifier.weight(1f), style = MaterialTheme.typography.subtitle1, color = Color(0xFFCE93D8))
            Spacer(Modifier.width(60.dp))
        }
        Divider(color = Color(0xFF8E24AA))
        for (entry in entries) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Text(entry.service, Modifier.weight(1f), color = Color(0xFFE1BEE7))
                Text(entry.username, Modifier.weight(1f), color = Color(0xFFE1BEE7))
                Text("•".repeat(8), Modifier.weight(1f), color = Color(0xFF7C4DFF)) // Скрываем пароль
                Button(
                    onClick = { onCopyPassword(entry.password) },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8E24AA), contentColor = Color.White)
                ) { Text("Copy") }
                Button(
                    onClick = { onDelete(entry) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD500F9), contentColor = Color.White)
                ) { Text("Delete") }
            }
            Divider(color = Color(0xFF8E24AA))
        }
    }
}

