import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Define custom colors
val ProtonPurple = Color(0xFF6D4AFF)
val ProtonPurpleLight = Color(0xFF8E70FF)
val ProtonPurpleDark = Color(0xFF4A2EFF)
val ProtonBackground = Color(0xFF1C1B24)
val ProtonSurface = Color(0xFF2C2B35)
val ProtonCardBackground = Color(0xFF34323E)
val ProtonTextPrimary = Color(0xFFFFFFFF)
val ProtonTextSecondary = Color(0xFFB3B3B3)
val ProtonBorder = Color(0xFF444352)
val ProtonGreen = Color(0xFF1EA885)
val ProtonRed = Color(0xFFFF5252)

// Улучшенная генерация пароля
object PasswordGenerator {
    fun generate(length: Int = 12, useDigits: Boolean = true, useUpper: Boolean = true, useSpecial: Boolean = true): String {
        val lower = ('a'..'z').toList()
        val upper = if (useUpper) ('A'..'Z').toList() else emptyList()
        val digits = if (useDigits) ('0'..'9').toList() else emptyList()
        val special = if (useSpecial) listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=') else emptyList()
        val all = lower + upper + digits + special
        if (all.isEmpty()) return ""
        return (1..length).map { all.random() }.joinToString("")
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Proton Pass",
        state = rememberWindowState(width = 1100.dp, height = 700.dp)
    ) {
        App()
    }
}

enum class Screen {
    ALL_ITEMS, FAVORITES, LOGINS, SECURE_NOTES, CREDIT_CARDS, SETTINGS
}

@Composable
@Preview
fun App() {
    val manager = remember { PasswordManager() }
    var currentScreen by remember { mutableStateOf(Screen.ALL_ITEMS) }
    var search by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarText by remember { mutableStateOf("") }
    
    MaterialTheme(
        colors = darkColors(
            primary = ProtonPurple,
            primaryVariant = ProtonPurpleDark,
            secondary = ProtonPurpleLight,
            background = ProtonBackground,
            surface = ProtonSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = ProtonTextPrimary,
            onSurface = ProtonTextPrimary
        )
    ) {
        Surface(color = ProtonBackground) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                Sidebar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    onAddClick = { showAddDialog = true }
                )
                
                // Main content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar
                        SearchBar(search = search, onSearchChanged = { search = it })
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Content based on selected screen
                        when (currentScreen) {
                            Screen.ALL_ITEMS -> AllItemsScreen(
                                manager = manager,
                                search = search,
                                onEntrySelected = { selectedEntry = it }
                            )
                            Screen.LOGINS -> LoginsScreen(
                                manager = manager,
                                search = search,
                                onEntrySelected = { selectedEntry = it }
                            )
                            else -> AllItemsScreen(
                                manager = manager,
                                search = search,
                                onEntrySelected = { selectedEntry = it }
                            )
                        }
                    }
                    
                    // Detail panel when an entry is selected
                    selectedEntry?.let {
                        DetailPanel(
                            entry = it,
                            onClose = { selectedEntry = null },
                            onCopyPassword = { encrypted ->
                                val decrypted = CryptoUtils.decrypt(encrypted)
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(decrypted), null)
                                snackbarText = "Password copied to clipboard"
                                showSnackbar = true
                            },
                            onDelete = { entry ->
                                manager.removeEntry(entry)
                                selectedEntry = null
                                snackbarText = "Entry deleted"
                                showSnackbar = true
                            }
                        )
                    }
                    
                    // Add dialog
                    if (showAddDialog) {
                        AddEntryDialog(
                            onDismiss = { showAddDialog = false },
                            onAdd = { service, username, password ->
                                val encrypted = CryptoUtils.encrypt(password)
                                manager.addEntry(PasswordEntry(service, username, encrypted))
                                showAddDialog = false
                                snackbarText = "Entry added"
                                showSnackbar = true
                            }
                        )
                    }
                    
                    // Snackbar
                    if (showSnackbar) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Snackbar(
                                modifier = Modifier.padding(16.dp),
                                action = {
                                    TextButton(onClick = { showSnackbar = false }) {
                                        Text("Dismiss")
                                    }
                                }
                            ) {
                                Text(snackbarText)
                            }
                        }
                        
                        LaunchedEffect(snackbarText) {
                            kotlinx.coroutines.delay(3000)
                            showSnackbar = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Sidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(ProtonSurface)
            .padding(16.dp)
    ) {
        // Logo
        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Proton Pass",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ProtonTextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Screens
        Column {
            SidebarItem(
                icon = Icons.Default.List,
                text = "All Items",
                isSelected = currentScreen == Screen.ALL_ITEMS,
                onClick = { onScreenSelected(Screen.ALL_ITEMS) }
            )
            SidebarItem(
                icon = Icons.Default.Star,
                text = "Favorites",
                isSelected = currentScreen == Screen.FAVORITES,
                onClick = { onScreenSelected(Screen.FAVORITES) }
            )
            SidebarItem(
                icon = Icons.Default.Lock,
                text = "Logins",
                isSelected = currentScreen == Screen.LOGINS,
                onClick = { onScreenSelected(Screen.LOGINS) }
            )
            SidebarItem(
                icon = Icons.Default.Note,
                text = "Secure Notes",
                isSelected = currentScreen == Screen.SECURE_NOTES,
                onClick = { onScreenSelected(Screen.SECURE_NOTES) }
            )
            SidebarItem(
                icon = Icons.Default.CreditCard,
                text = "Credit Cards",
                isSelected = currentScreen == Screen.CREDIT_CARDS,
                onClick = { onScreenSelected(Screen.CREDIT_CARDS) }
            )
            SidebarItem(
                icon = Icons.Default.Settings,
                text = "Settings",
                isSelected = currentScreen == Screen.SETTINGS,
                onClick = { onScreenSelected(Screen.SETTINGS) }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Add button
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = ProtonPurple)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add", color = Color.White)
            }
        }
    }
}

@Composable
fun SidebarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) ProtonPurple.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) ProtonPurple else ProtonTextSecondary
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun SearchBar(
    search: String,
    onSearchChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = search,
        onValueChange = onSearchChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Search in all vaults...", color = ProtonTextSecondary) },
        leadingIcon = { 
            Icon(
                imageVector = Icons.Default.Search, 
                contentDescription = "Search",
                tint = ProtonTextSecondary
            ) 
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = ProtonCardBackground,
            textColor = ProtonTextPrimary,
            cursorColor = ProtonPurple,
            focusedBorderColor = ProtonPurple,
            unfocusedBorderColor = ProtonBorder
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun AllItemsScreen(
    manager: PasswordManager,
    search: String,
    onEntrySelected: (PasswordEntry) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "All Items",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ProtonTextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Group by date
        Text(
            text = "Yesterday",
            fontSize = 14.sp,
            color = ProtonTextSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Entries
        manager.entries
            .filter { it.service.contains(search, ignoreCase = true) || it.username.contains(search, ignoreCase = true) }
            .forEach { entry ->
                EntryItem(
                    entry = entry,
                    onSelected = onEntrySelected
                )
            }
    }
}

@Composable
fun LoginsScreen(
    manager: PasswordManager,
    search: String,
    onEntrySelected: (PasswordEntry) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Logins",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ProtonTextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Entries
        manager.entries
            .filter { it.service.contains(search, ignoreCase = true) || it.username.contains(search, ignoreCase = true) }
            .forEach { entry ->
                EntryItem(
                    entry = entry,
                    onSelected = onEntrySelected
                )
            }
    }
}

@Composable
fun EntryItem(
    entry: PasswordEntry,
    onSelected: (PasswordEntry) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelected(entry) },
        color = ProtonCardBackground
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service icon (first letter in a circle)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ProtonPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.service.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = entry.service,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProtonTextPrimary
                )
                Text(
                    text = entry.username,
                    fontSize = 14.sp,
                    color = ProtonTextSecondary
                )
            }
        }
    }
}

@Composable
fun DetailPanel(
    entry: PasswordEntry,
    onClose: () -> Unit,
    onCopyPassword: (String) -> Unit,
    onDelete: (PasswordEntry) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    
    Dialog(onCloseRequest = onClose) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            color = ProtonSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with service name and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.service,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProtonTextPrimary
                    )
                    
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ProtonTextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Username
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Username",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.username,
                            fontSize = 16.sp,
                            color = ProtonTextPrimary
                        )
                        
                        IconButton(
                            onClick = {
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                                    StringSelection(entry.username), null
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Username",
                                tint = ProtonTextSecondary
                            )
                        }
                    }
                }
                
                Divider(color = ProtonBorder)
                
                // Password
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (showPassword) CryptoUtils.decrypt(entry.password) else "••••••••••••",
                            fontSize = 16.sp,
                            color = ProtonTextPrimary
                        )
                        
                        Row {
                            IconButton(
                                onClick = { showPassword = !showPassword }
                            ) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                    tint = ProtonTextSecondary
                                )
                            }
                            
                            IconButton(
                                onClick = { onCopyPassword(entry.password) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Password",
                                    tint = ProtonTextSecondary
                                )
                            }
                        }
                    }
                }
                
                Divider(color = ProtonBorder)
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Delete button
                Button(
                    onClick = { onDelete(entry) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = ProtonRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var service by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    Dialog(onCloseRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
            color = ProtonSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProtonTextPrimary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ProtonTextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Service
                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("Service", color = ProtonTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = ProtonCardBackground,
                        textColor = ProtonTextPrimary,
                        cursorColor = ProtonPurple,
                        focusedBorderColor = ProtonPurple,
                        unfocusedBorderColor = ProtonBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = ProtonTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = ProtonCardBackground,
                        textColor = ProtonTextPrimary,
                        cursorColor = ProtonPurple,
                        focusedBorderColor = ProtonPurple,
                        unfocusedBorderColor = ProtonBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = ProtonTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                tint = ProtonTextSecondary
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = ProtonCardBackground,
                        textColor = ProtonTextPrimary,
                        cursorColor = ProtonPurple,
                        focusedBorderColor = ProtonPurple,
                        unfocusedBorderColor = ProtonBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Generate password button
                OutlinedButton(
                    onClick = { password = PasswordGenerator.generate() },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = ProtonPurple
                    ),
                    border = BorderStroke(1.dp, ProtonPurple)
                ) {
                    Text("Generate Password")
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Add button
                Button(
                    onClick = { 
                        if (service.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                            onAdd(service, username, password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = ProtonPurple),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = service.isNotBlank() && username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Add", color = Color.White)
                }
            }
        }
    }
}
