import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import utils.CryptoUtils

// Import PasswordEntry and PasswordGenerator
import PasswordEntry
import PasswordManager
import PasswordGenerator

// Цвета в стиле ProtonMail
val ProtonPurple = Color(0xFF6D4AFF)
val ProtonRed = Color(0xFFEC5858)
val ProtonSurface = Color(0xFF1C1B24)
val ProtonCardBackground = Color(0xFF2C2C34)
val ProtonBorder = Color(0xFF383846)
val ProtonTextPrimary = Color.White
val ProtonTextSecondary = Color(0xFFAFAFB3)

@Composable
fun DetailPanel(
    entry: PasswordEntry,
    onClose: () -> Unit,
    onCopyPassword: (String) -> Unit,
    onDelete: (PasswordEntry) -> Unit,
    onToggleFavorite: (PasswordEntry) -> Unit = {},
    onUpdateCategory: (PasswordEntry, String) -> Unit = { _, _ -> },
    onUpdateNotes: (PasswordEntry, String) -> Unit = { _, _ -> },
    categories: Set<String> = setOf("Общие", "Работа", "Личное", "Финансы", "Социальные сети")
) {
    var showPassword by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var editingNotes by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(entry.notes) }
    
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = ProtonSurface,
        border = BorderStroke(1.dp, ProtonBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок и кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProtonTextPrimary
                )
                
                Row {
                    // Кнопка избранного
                    IconButton(onClick = { onToggleFavorite(entry) }) {
                        Icon(
                            imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (entry.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            tint = if (entry.isFavorite) Color.Yellow else ProtonTextSecondary
                        )
                    }
                    
                    // Кнопка закрытия
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ProtonTextSecondary
                        )
                    }
                }
            }
            
            Divider(
                color = ProtonBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Информация о сервисе
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Service",
                    tint = ProtonPurple,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Service",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary
                    )
                    
                    Text(
                        text = entry.service,
                        fontSize = 16.sp,
                        color = ProtonTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Информация о пользователе
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Username",
                    tint = ProtonPurple,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Username",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary
                    )
                    
                    Text(
                        text = entry.username,
                        fontSize = 16.sp,
                        color = ProtonTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Кнопка копирования имени пользователя
                IconButton(
                    onClick = {
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(StringSelection(entry.username), null)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Username",
                        tint = ProtonTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Информация о пароле
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = ProtonPurple,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Password",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showPassword) CryptoUtils.decrypt(entry.password) else "••••••••••••",
                            fontSize = 16.sp,
                            color = ProtonTextPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Кнопка показать/скрыть пароль
                        IconButton(
                            onClick = { showPassword = !showPassword },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                tint = ProtonTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Кнопка копирования пароля
                IconButton(
                    onClick = { onCopyPassword(entry.password) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Password",
                        tint = ProtonTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Информация о категории
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Category",
                    tint = ProtonPurple,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary
                    )
                    
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCategoryDropdown = true }
                        ) {
                            Text(
                                text = entry.category,
                                fontSize = 16.sp,
                                color = ProtonTextPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change Category",
                                tint = ProtonTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier
                                .background(ProtonCardBackground)
                                .width(200.dp)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    onClick = {
                                        onUpdateCategory(entry, cat)
                                        showCategoryDropdown = false
                                    }
                                ) {
                                    Text(cat, color = ProtonTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Дата создания
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Created",
                    tint = ProtonPurple,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Created",
                        fontSize = 12.sp,
                        color = ProtonTextSecondary
                    )
                    
                    Text(
                        text = LocalDateTime.ofEpochSecond(entry.createdAt / 1000, 0, java.time.ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                        fontSize = 16.sp,
                        color = ProtonTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Заметки
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "Notes",
                            tint = ProtonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Notes",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary
                        )
                    }
                    
                    IconButton(
                        onClick = { editingNotes = !editingNotes },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (editingNotes) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (editingNotes) "Save Notes" else "Edit Notes",
                            tint = ProtonTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (editingNotes) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { 
                                notesText = entry.notes
                                editingNotes = false 
                            }
                        ) {
                            Text("Cancel", color = ProtonTextSecondary)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { 
                                onUpdateNotes(entry, notesText)
                                editingNotes = false 
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = ProtonPurple)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        text = if (entry.notes.isBlank()) "No notes" else entry.notes,
                        fontSize = 14.sp,
                        color = if (entry.notes.isBlank()) ProtonTextSecondary else ProtonTextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = ProtonCardBackground,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка удаления
            Button(
                onClick = { onDelete(entry) },
                colors = ButtonDefaults.buttonColors(backgroundColor = ProtonRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Entry", color = Color.White)
            }
        }
    }
}

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onAddEntry: (PasswordEntry) -> Unit,
    categories: Set<String> = setOf("Общие", "Работа", "Личное", "Финансы", "Социальные сети")
) {
    var service by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Общие") }
    var notes by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    val passwordStrength = PasswordGenerator.evaluatePasswordStrength(password)
    val passwordStrengthColor = when {
        passwordStrength < 40 -> Color.Red
        passwordStrength < 70 -> Color(0xFFFFAA00) // Amber
        else -> Color.Green
    }
    
    val isFormValid = service.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ProtonSurface,
            border = BorderStroke(1.dp, ProtonBorder),
            modifier = Modifier
                .width(420.dp)
                .height(480.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Заголовок и кнопки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add New Entry",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProtonTextPrimary
                        )
                        
                        Row {
                            // Маленькая кнопка Add
                            Button(
                                onClick = {
                                    if (isFormValid) {
                                        onAddEntry(
                                            PasswordEntry(
                                                service = service,
                                                username = username,
                                                password = CryptoUtils.encrypt(password),
                                                category = category,
                                                notes = notes
                                            )
                                        )
                                        onDismiss()
                                    }
                                },
                                enabled = isFormValid,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ProtonPurple,
                                    disabledBackgroundColor = ProtonPurple.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(30.dp)
                            ) {
                                Text(
                                    text = "Add",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Кнопка закрытия
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = ProtonTextSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Поле сервиса
                    OutlinedTextField(
                        value = service,
                        onValueChange = { service = it },
                        label = { Text("Service") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Service",
                                tint = ProtonPurple
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ProtonPurple,
                            unfocusedBorderColor = ProtonBorder,
                            focusedLabelColor = ProtonPurple,
                            cursorColor = ProtonPurple,
                            textColor = ProtonTextPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Поле имени пользователя
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username",
                                tint = ProtonPurple
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ProtonPurple,
                            unfocusedBorderColor = ProtonBorder,
                            focusedLabelColor = ProtonPurple,
                            cursorColor = ProtonPurple,
                            textColor = ProtonTextPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Поле пароля
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = ProtonPurple
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    password = PasswordGenerator.generate()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Generate Password",
                                    tint = ProtonPurple
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ProtonPurple,
                            unfocusedBorderColor = ProtonBorder,
                            focusedLabelColor = ProtonPurple,
                            cursorColor = ProtonPurple,
                            textColor = ProtonTextPrimary
                        )
                    )
                    
                    // Индикатор силы пароля
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        color = ProtonCardBackground,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width((passwordStrength * 0.01f * 100).dp)
                                        .background(
                                            color = passwordStrengthColor,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = when {
                                    passwordStrength < 40 -> "Weak"
                                    passwordStrength < 70 -> "Medium"
                                    else -> "Strong"
                                },
                                fontSize = 12.sp,
                                color = passwordStrengthColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Поле категории
                    Box {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { },
                            label = { Text("Category") },
                            singleLine = true,
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Category",
                                    tint = ProtonPurple
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCategoryDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Category",
                                        tint = ProtonTextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = ProtonPurple,
                                unfocusedBorderColor = ProtonBorder,
                                focusedLabelColor = ProtonPurple,
                                cursorColor = ProtonPurple,
                                textColor = ProtonTextPrimary
                            )
                        )
                        
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier
                                .background(ProtonCardBackground)
                                .width(200.dp)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    onClick = {
                                        category = cat
                                        showCategoryDropdown = false
                                    }
                                ) {
                                    Text(cat, color = ProtonTextPrimary)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Поле заметок
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Note,
                                contentDescription = "Notes",
                                tint = ProtonPurple
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ProtonPurple,
                            unfocusedBorderColor = ProtonBorder,
                            focusedLabelColor = ProtonPurple,
                            cursorColor = ProtonPurple,
                            textColor = ProtonTextPrimary
                        )
                    )
                    
                    if (!isFormValid) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please fill in all required fields",
                            fontSize = 12.sp,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Основная кнопка добавления
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onAddEntry(
                                    PasswordEntry(
                                        service = service,
                                        username = username,
                                        password = CryptoUtils.encrypt(password),
                                        category = category,
                                        notes = notes
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ProtonPurple,
                            disabledBackgroundColor = ProtonPurple.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Entry", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainApp() {
    val passwordManager = PasswordManager()
    
    MaterialTheme(
        colors = darkColors(
            primary = ProtonPurple,
            primaryVariant = ProtonPurple,
            secondary = ProtonPurple,
            background = ProtonSurface,
            surface = ProtonSurface
        )
    ) {
        var showAddDialog by remember { mutableStateOf(false) }
        var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var showFavorites by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            passwordManager.initialize()
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ProtonSurface
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Левая панель (список паролей)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // Заголовок и кнопка добавления
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password Manager",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProtonTextPrimary
                        )
                        
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = ProtonPurple)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New", color = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Поиск
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ProtonTextSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ProtonPurple,
                            unfocusedBorderColor = ProtonBorder,
                            cursorColor = ProtonPurple,
                            textColor = ProtonTextPrimary,
                            placeholderColor = ProtonTextSecondary
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Фильтры
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Избранное
                        OutlinedButton(
                            onClick = { showFavorites = !showFavorites },
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = if (showFavorites) ProtonPurple.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (showFavorites) ProtonPurple else ProtonTextSecondary
                            ),
                            border = BorderStroke(1.dp, if (showFavorites) ProtonPurple else ProtonBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorites",
                                tint = if (showFavorites) ProtonPurple else ProtonTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Favorites")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Категории
                        Box {
                            var showCategoryDropdown by remember { mutableStateOf(false) }
                            val categories = remember(passwordManager.entries) { passwordManager.getAllCategories() }
                            
                            OutlinedButton(
                                onClick = { showCategoryDropdown = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = if (selectedCategory != null) ProtonPurple.copy(alpha = 0.2f) else Color.Transparent,
                                    contentColor = if (selectedCategory != null) ProtonPurple else ProtonTextSecondary
                                ),
                                border = BorderStroke(1.dp, if (selectedCategory != null) ProtonPurple else ProtonBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Categories",
                                    tint = if (selectedCategory != null) ProtonPurple else ProtonTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(selectedCategory ?: "Categories")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Category",
                                    tint = if (selectedCategory != null) ProtonPurple else ProtonTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false },
                                modifier = Modifier.background(ProtonCardBackground)
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        selectedCategory = null
                                        showCategoryDropdown = false
                                    }
                                ) {
                                    Text("All Categories", color = ProtonTextPrimary)
                                }
                                
                                Divider(color = ProtonBorder)
                                
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        onClick = {
                                            selectedCategory = category
                                            showCategoryDropdown = false
                                        }
                                    ) {
                                        Text(category, color = ProtonTextPrimary)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Список паролей
                    val filteredEntries = remember(
                        passwordManager.entries,
                        searchQuery,
                        selectedCategory,
                        showFavorites
                    ) {
                        var result = if (searchQuery.isNotEmpty()) {
                            passwordManager.findEntries(searchQuery)
                        } else {
                            passwordManager.entries
                        }
                        
                        if (selectedCategory != null) {
                            result = result.filter { it.category == selectedCategory }
                        }
                        
                        if (showFavorites) {
                            result = result.filter { it.isFavorite }
                        }
                        
                        result
                    }
                    
                    if (filteredEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "No Results",
                                    tint = ProtonTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "No entries found",
                                    fontSize = 16.sp,
                                    color = ProtonTextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredEntries) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedEntry = entry },
                                    backgroundColor = if (selectedEntry == entry) ProtonPurple.copy(alpha = 0.2f) else ProtonCardBackground,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selectedEntry == entry) ProtonPurple else ProtonBorder
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Иконка сервиса
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    color = ProtonPurple.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = entry.service.take(1).uppercase(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ProtonPurple
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        // Информация о записи
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = entry.service,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = ProtonTextPrimary
                                                )
                                                
                                                if (entry.isFavorite) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Favorite",
                                                        tint = Color.Yellow,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Text(
                                                text = entry.username,
                                                fontSize = 14.sp,
                                                color = ProtonTextSecondary
                                            )
                                        }
                                        
                                        // Категория
                                        Chip(
                                            onClick = { },
                                            colors = ChipDefaults.chipColors(
                                                backgroundColor = ProtonPurple.copy(alpha = 0.1f),
                                                contentColor = ProtonPurple
                                            ),
                                            border = BorderStroke(1.dp, ProtonPurple.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = entry.category,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Правая панель (детали)
                if (selectedEntry != null) {
                    DetailPanel(
                        entry = selectedEntry!!,
                        onClose = { selectedEntry = null },
                        onCopyPassword = { encryptedPassword ->
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            clipboard.setContents(
                                StringSelection(CryptoUtils.decrypt(encryptedPassword)),
                                null
                            )
                        },
                        onDelete = {
                            runBlocking {
                                passwordManager.removeEntry(it)
                            }
                            selectedEntry = null
                        },
                        onToggleFavorite = {
                            runBlocking {
                                passwordManager.toggleFavorite(it)
                            }
                        },
                        onUpdateCategory = { entry, newCategory ->
                            runBlocking {
                                passwordManager.updateCategory(entry, newCategory)
                            }
                        },
                        onUpdateNotes = { entry, notes ->
                            runBlocking {
                                passwordManager.updateNotes(entry, notes)
                            }
                        },
                        categories = passwordManager.getAllCategories()
                    )
                } else {
                    // Пустая правая панель
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(320.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Select Entry",
                                tint = ProtonTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Select an entry to view details",
                                fontSize = 16.sp,
                                color = ProtonTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        // Диалог добавления новой записи
        if (showAddDialog) {
            AddEntryDialog(
                onDismiss = { showAddDialog = false },
                onAddEntry = {
                    runBlocking {
                        passwordManager.addEntry(it)
                    }
                    showAddDialog = false
                },
                categories = passwordManager.getAllCategories()
            )
        }
    }
}

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Password Manager",
            state = rememberWindowState(
                width = 900.dp,
                height = 600.dp
            )
        ) {
            MainApp()
        }
    }
}
