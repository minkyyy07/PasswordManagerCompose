import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val scrollState = rememberScrollState()
    
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Сервис
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = ProtonCardBackground,
                    border = BorderStroke(1.dp, ProtonBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Service",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = entry.service,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ProtonTextPrimary
                        )
                    }
                }
                
                // Имя пользователя
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = ProtonCardBackground,
                    border = BorderStroke(1.dp, ProtonBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Username",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.username,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ProtonTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = {
                                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                    clipboard.setContents(StringSelection(entry.username), null)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Username",
                                    tint = ProtonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Пароль
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = ProtonCardBackground,
                    border = BorderStroke(1.dp, ProtonBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Password",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        var showPassword by remember { mutableStateOf(false) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showPassword) CryptoUtils.decrypt(entry.password) else "••••••••••••",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ProtonTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { showPassword = !showPassword }
                            ) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                    tint = ProtonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { onCopyPassword(entry.password) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Password",
                                    tint = ProtonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Категория
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = ProtonCardBackground,
                    border = BorderStroke(1.dp, ProtonBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Category",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        var showCategoryDropdown by remember { mutableStateOf(false) }
                        var currentCategory by remember { mutableStateOf(entry.category) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentCategory,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ProtonTextPrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showCategoryDropdown = true }
                            )
                            
                            IconButton(
                                onClick = { showCategoryDropdown = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Category",
                                    tint = ProtonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.background(ProtonCardBackground)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    onClick = {
                                        currentCategory = category
                                        onUpdateCategory(entry, category)
                                        showCategoryDropdown = false
                                    }
                                ) {
                                    Text(category, color = ProtonTextPrimary)
                                }
                            }
                        }
                    }
                }
                
                // Заметки
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = ProtonCardBackground,
                    border = BorderStroke(1.dp, ProtonBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notes",
                                fontSize = 12.sp,
                                color = ProtonTextSecondary
                            )
                            
                            var isEditing by remember { mutableStateOf(false) }
                            var editedNotes by remember { mutableStateOf(entry.notes) }
                            
                            if (isEditing) {
                                Row {
                                    IconButton(
                                        onClick = {
                                            onUpdateNotes(entry, editedNotes)
                                            isEditing = false
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save Notes",
                                            tint = ProtonPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            editedNotes = entry.notes
                                            isEditing = false
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel",
                                            tint = ProtonTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = { isEditing = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Notes",
                                        tint = ProtonPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        var isEditing by remember { mutableStateOf(false) }
                        var editedNotes by remember { mutableStateOf(entry.notes) }
                        
                        if (isEditing) {
                            TextField(
                                value = editedNotes,
                                onValueChange = { editedNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Transparent,
                                    cursorColor = ProtonPurple,
                                    focusedIndicatorColor = ProtonPurple,
                                    unfocusedIndicatorColor = ProtonBorder,
                                    textColor = ProtonTextPrimary
                                )
                            )
                        } else {
                            Text(
                                text = entry.notes.ifEmpty { "No notes" },
                                fontSize = 16.sp,
                                color = if (entry.notes.isEmpty()) ProtonTextSecondary else ProtonTextPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isEditing = true }
                            )
                        }
                    }
                }
                
                // Дата создания
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ProtonCardBackground,
                    border = BorderStroke(1.dp, ProtonBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Created",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = LocalDateTime.ofEpochSecond(
                                entry.createdAt / 1000,
                                0,
                                java.time.ZoneOffset.UTC
                            ).format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")),
                            fontSize = 16.sp,
                            color = ProtonTextPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Кнопка удаления
                OutlinedButton(
                    onClick = { onDelete(entry) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = ProtonRed
                    ),
                    border = BorderStroke(1.dp, ProtonRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ProtonRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
                
                // Кнопка избранного
                OutlinedButton(
                    onClick = { onToggleFavorite(entry) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = if (entry.isFavorite) Color.Yellow else ProtonTextSecondary
                    ),
                    border = BorderStroke(1.dp, if (entry.isFavorite) Color.Yellow else ProtonBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (entry.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = if (entry.isFavorite) Color.Yellow else ProtonTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (entry.isFavorite) "Unfavorite" else "Favorite")
                }
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
    val scrollState = rememberScrollState()
    
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
                    
                    // Поля ввода с возможностью прокрутки
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
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
                                            color = passwordStrengthColor.copy(alpha = 0.2f),
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
                                        passwordStrength < 70 -> "Good"
                                        else -> "Strong"
                                    },
                                    fontSize = 12.sp,
                                    color = passwordStrengthColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Поле категории
                        Box(modifier = Modifier.fillMaxWidth()) {
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
                                            tint = ProtonPurple
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
                                modifier = Modifier.background(ProtonCardBackground)
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
                            label = { Text("Notes (Optional)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Note,
                                    contentDescription = "Notes",
                                    tint = ProtonPurple
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = ProtonPurple,
                                unfocusedBorderColor = ProtonBorder,
                                focusedLabelColor = ProtonPurple,
                                cursorColor = ProtonPurple,
                                textColor = ProtonTextPrimary
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Подсказка о необходимости заполнения полей
                    if (!isFormValid) {
                        Text(
                            text = "Please fill in all required fields",
                            fontSize = 12.sp,
                            color = ProtonTextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Кнопка добавления
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
                        Text(
                            text = "Add Entry",
                            color = Color.White
                        )
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
