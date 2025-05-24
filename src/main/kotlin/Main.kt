import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Password Manager") {
        App()
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

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("Service") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Button(
                    onClick = { password = PasswordGenerator.generate() },
                    modifier = Modifier.padding(start = 8.dp)
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
                    }
                ) { Text("Add Entry") }
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search") },
                    modifier = Modifier.padding(start = 16.dp)
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

@Composable
fun PasswordTable(
    entries: List<PasswordEntry>,
    onCopyPassword: (String) -> Unit,
    onDelete: (PasswordEntry) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("Service", Modifier.weight(1f), style = MaterialTheme.typography.subtitle1)
            Text("Username", Modifier.weight(1f), style = MaterialTheme.typography.subtitle1)
            Text("Password", Modifier.weight(1f), style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.width(60.dp))
        }
        Divider()
        for (entry in entries) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Text(entry.service, Modifier.weight(1f))
                Text(entry.username, Modifier.weight(1f))
                Text("•".repeat(8), Modifier.weight(1f)) // Скрываем пароль
                Button(
                    onClick = { onCopyPassword(entry.password) },
                    modifier = Modifier.padding(end = 4.dp)
                ) { Text("Copy") }
                Button(
                    onClick = { onDelete(entry) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) { Text("Delete") }
            }
            Divider()
        }
    }
}