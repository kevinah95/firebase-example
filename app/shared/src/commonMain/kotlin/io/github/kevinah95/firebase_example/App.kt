package io.github.kevinah95.firebase_example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.database.database
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Student(val name: String, val course: String)

object FirebaseHelper {
    private var isInitialized = false
    fun init() {
        if (isInitialized) return
        try {
            val host = getEmulatorHost()
            Firebase.auth.useEmulator(host, 9099)
            Firebase.firestore.useEmulator(host, 8080)
            Firebase.database.useEmulator(host, 9000)
            isInitialized = true
        } catch (e: Exception) {
            // Ya inicializado o error de red
        }
    }
}

@Composable
fun App() {
    FirebaseHelper.init()
    val scope = rememberCoroutineScope()
    
    // Auth State
    var currentUserUid by remember { mutableStateOf<String?>(null) }
    
    // Firestore State
    var studentName by remember { mutableStateOf("") }
    var studentCourse by remember { mutableStateOf("") }
    var studentsList by remember { mutableStateOf<List<Student>>(emptyList()) }
    
    // Realtime DB State
    var statusInput by remember { mutableStateOf("") }
    var currentStatus by remember { mutableStateOf("Desconocido") }

    // Listeners
    LaunchedEffect(Unit) {
        // Observe Auth State
        launch {
            try {
                Firebase.auth.authStateChanged.collect { user ->
                    currentUserUid = user?.uid
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }

        // Observe Firestore Students List
        launch {
            try {
                Firebase.firestore.collection("students")
                    .snapshots
                    .map { querySnapshot ->
                        querySnapshot.documents.map { doc ->
                            doc.data<Student>()
                        }
                    }
                    .collect { list ->
                        studentsList = list
                    }
            } catch (e: Exception) {
                // Manejar error
            }
        }

        // Observe Realtime DB Status Message
        launch {
            try {
                Firebase.database.reference("system_status").valueEvents.collect { snapshot ->
                    val value = try { snapshot.value<String>() } catch (e: Exception) { null }
                    if (value != null) {
                        currentStatus = value
                    }
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Firebase + KMP Demo 🚀",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- SECCIÓN AUTH ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔐 Autenticación de Firebase", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (currentUserUid != null) {
                        Text("Usuario Logueado (UID):")
                        Text(currentUserUid ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                try {
                                    Firebase.auth.signOut()
                                } catch (e: Exception) {
                                    // Manejar error
                                }
                            }
                        }) {
                            Text("Cerrar Sesión")
                        }
                    } else {
                        Text("No hay sesión iniciada (Ciertas operaciones bloqueadas)", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                try {
                                    Firebase.auth.signInAnonymously()
                                } catch (e: Exception) {
                                    // Manejar error
                                }
                            }
                        }) {
                            Text("Iniciar Sesión Anónima")
                        }
                    }
                }
            }

            // --- SECCIÓN REALTIME DATABASE ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚡ Realtime Database (Estado Global)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado actual: $currentStatus", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = statusInput,
                        onValueChange = { statusInput = it },
                        label = { Text("Nuevo Estado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    Firebase.database.reference("system_status").setValue(statusInput)
                                    statusInput = ""
                                } catch (e: Exception) {
                                    // Manejar error
                                }
                            }
                        },
                        enabled = currentUserUid != null
                    ) {
                        Text("Actualizar Estado")
                    }
                }
            }

            // --- SECCIÓN FIRESTORE ---
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔥 Cloud Firestore (Estudiantes)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Nombre del Estudiante") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = studentCourse,
                        onValueChange = { studentCourse = it },
                        label = { Text("Curso") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val newStudent = Student(studentName, studentCourse)
                                    Firebase.firestore.collection("students")
                                        .document(studentName.lowercase().replace(" ", "_"))
                                        .set(newStudent)
                                    studentName = ""
                                    studentCourse = ""
                                } catch (e: Exception) {
                                    // Manejar error
                                }
                            }
                        },
                        enabled = currentUserUid != null && studentName.isNotBlank() && studentCourse.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Estudiante")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Lista de Estudiantes:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(studentsList) { student ->
                            ListItem(
                                headlineContent = { Text(student.name) },
                                supportingContent = { Text("Curso: ${student.course}") }
                            )
                        }
                    }
                }
            }
        }
    }
}