# Ejemplo de Firebase en Kotlin Multiplatform (KMP)

¡Bienvenido al proyecto de ejemplo de Firebase en Kotlin Multiplatform (KMP)! Este repositorio está diseñado para enseñarte cómo integrar y utilizar las funciones de Firebase en una aplicación KMP dirigida a Android e iOS utilizando el [SDK de Firebase para Kotlin de GitLive](https://github.com/GitLiveApp/firebase-kotlin-sdk).

---

## 📁 Estructura del Repositorio

Este repositorio está dividido en dos partes principales:
*   **Configuraciones de Firebase ([`/firebase`](./firebase)):** Contiene los archivos de configuración de Firebase, reglas de seguridad y ajustes de los emuladores locales controlados por versiones.
*   **Aplicación Kotlin Multiplatform (`/app`):** El código base de KMP.
    *   [`/app/shared`](./app/shared): Código compartido multiplataforma. El SDK de Firebase para Kotlin se declara en el conjunto de fuentes (source set) `commonMain`.
    *   [`/app/androidApp`](./app/androidApp): Envoltorio (wrapper) de la aplicación nativa de Android.
    *   [`/app/iosApp`](./app/iosApp): Envoltorio (wrapper) de la aplicación nativa de iOS (proyecto de Xcode).

---

## ⚡ Atajos rápidos con Taskfile

Este proyecto incluye un archivo [`Taskfile.yml`](./Taskfile.yml) para facilitar la ejecución de tareas comunes si utilizas la herramienta [Task](https://taskfile.dev/):

*   **Iniciar el emulador de Firebase:**
    ```bash
    task emulator
    ```
*   **Compilar el proyecto KMP:**
    ```bash
    task build
    ```
*   **Ejecutar la app de Android en el emulador:**
    ```bash
    task run-android
    ```
*   **Abrir el proyecto de iOS en Xcode:**
    ```bash
    task run-ios
    ```
*   **Ejecutar pruebas unitarias de Android:**
    ```bash
    task test-android
    ```
*   **Limpiar compilaciones anteriores:**
    ```bash
    task clean
    ```

---

## 🚀 Parte 1: Firebase Local Emulator Suite (Recomendado para Desarrollo)

Para el desarrollo y pruebas locales, no necesitas crear un proyecto activo en la consola de Firebase. Puedes ejecutar el **Firebase Local Emulator Suite** en tu máquina de forma gratuita, sin conexión a Internet y sin ningún coste en la nube.

### 1. Requisitos Previos
Asegúrate de tener instalado **Node.js** en tu sistema.

### 2. Iniciar los Emuladores
Desde el directorio raíz de este repositorio, ejecuta:
```bash
cd firebase && npx -y firebase-tools@latest emulators:start
```

Esto iniciará los servicios locales en los siguientes puertos (configurados en [`firebase/firebase.json`](./firebase/firebase.json)):
*   **Emulador de Autenticación (Auth):** `9099`
*   **Emulador de Firestore (Base de datos NoSQL):** `8080`
*   **Emulador de Realtime Database:** `9000`
*   **Controlador de la Interfaz de Usuario (UI) del Emulador:** `4000` (¡Accede a esto a través de tu navegador web para ver/gestionar tus datos locales!)

Abre **[http://localhost:4000](http://localhost:4000)** en tu navegador para inspeccionar las entradas de la base de datos, gestionar usuarios de prueba y ver los registros de actividad.

---

## 🛠️ Parte 2: Conectar la App KMP a los Emuladores Locales

En tu código compartido de Kotlin Multiplatform, puedes instanciar los servicios de Firebase e indicarles que se conecten a tu emulador local en lugar de a los servidores de producción.

### 1. Configurar la IP del Host según la Plataforma
*   **Emulador de Android:** Se ejecuta en una máquina virtual y se refiere a tu ordenador (localhost) a través de la IP especial `10.0.2.2`.
*   **Simulador de iOS:** Comparte la red del host, por lo que puedes conectarte a `127.0.0.1` o `localhost`.

Aquí tienes un ejemplo de cómo configurar una clase de ayuda para inicializar y configurar los servicios de Firebase para que utilicen los emuladores locales:

```kotlin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.database.database

class FirebaseManager(private val isEmulator: Boolean = true) {
    
    // Determinar el host según la plataforma
    private val emulatorHost: String = getPlatformEmulatorHost()
    
    val auth by lazy { Firebase.auth }
    val firestore by lazy { Firebase.firestore }
    val database by lazy { Firebase.database }

    init {
        if (isEmulator) {
            // Apuntar el SDK a los emuladores locales en ejecución
            auth.useEmulator(emulatorHost, 9099)
            firestore.useEmulator(emulatorHost, 8080)
            database.useEmulator(emulatorHost, 9000)
        }
    }
}

// Helper expect/actual para resolver los bucles de retorno (loopbacks) de la plataforma
expect fun getPlatformEmulatorHost(): String
```

En tus carpetas específicas de plataforma:
*   **Android (`androidMain`):**
    ```kotlin
    actual fun getPlatformEmulatorHost(): String = "10.0.2.2"
    ```
*   **iOS (`iosMain`):**
    ```kotlin
    actual fun getPlatformEmulatorHost(): String = "127.0.0.1"
    ```

---

## ☁️ Parte 3: Conectar a un Proyecto de Firebase en Producción (Nube)

Cuando estés listo para desplegar en un proyecto real de Firebase en la nube, sigue estos pasos:

### 1. Crear un Proyecto de Firebase
1. Ve a la [Consola de Firebase](https://console.firebase.google.com/) y haz clic en **Añadir proyecto**.
2. Ejecuta `npx -y firebase-tools@latest login` en tu terminal para autenticar tu CLI.
3. Vincula esta carpeta con tu proyecto de Firebase:
   ```bash
   npx -y firebase-tools@latest use --add
   ```

### 2. Configurar la App de Android
1. Registra el nombre del paquete de tu app de Android `io.github.kevinah95.firebase_example` en la consola de tu proyecto de Firebase.
2. Descarga el archivo `google-services.json`.
3. Coloca `google-services.json` dentro del directorio [`/app/androidApp`](./app/androidApp).

### 3. Configurar la App de iOS
1. Habilita los objetivos de iOS en [`app/shared/build.gradle.kts`](./app/shared/build.gradle.kts) descomentando la configuración de los objetivos `ios`.
2. Registra el ID de paquete (Bundle ID) de tu app de iOS `io.github.kevinah95.firebase-example` (o el que corresponda) en la consola de Firebase.
3. Descarga el archivo `GoogleService-Info.plist`.
4. Coloca `GoogleService-Info.plist` dentro de [`/app/iosApp/iosApp`](./app/iosApp/iosApp) y añádelo/vincúlalo en Xcode.
5. In Xcode, añade la dependencia nativa del **SDK de Firebase para iOS** a través de Swift Package Manager (File -> Add Packages -> busca `https://github.com/firebase/firebase-ios-sdk`) y marca las librerías que necesites (Auth, Firestore, Database).
6. Inicializa Firebase en iOS dentro de tu `iosApp.swift` / AppDelegate:
   ```swift
   import SwiftUI
   import FirebaseCore

   @main
   struct iosApp: App {
       init() {
           FirebaseApp.configure()
       }
       var body: some Scene {
           WindowGroup {
               ContentView()
           }
       }
   }
   ```

---

## 📚 Parte 4: Ejemplos de Código del SDK de Firebase para Kotlin (GitLive)

### 1. Autenticación de Firebase (Firebase Authentication)
Gestiona usuarios, flujos de inicio de sesión y el estado de la sesión.

```kotlin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

// 1. Registrar un Usuario
suspend fun signUp(email: String, pass: String) {
    try {
        val result = Firebase.auth.createUserWithEmailAndPassword(email, pass)
        val user = result.user
        println("Usuario creado: ${user?.uid}")
    } catch (e: Exception) {
        println("Error al registrarse: ${e.message}")
    }
}

// 2. Iniciar Sesión de un Usuario
suspend fun signIn(email: String, pass: String) {
    try {
        Firebase.auth.signInWithEmailAndPassword(email, pass)
        println("Sesión iniciada con éxito!")
    } catch (e: Exception) {
        println("Error al iniciar sesión: ${e.message}")
    }
}

// 3. Escuchar los Cambios en el Estado de Autenticación
fun observeAuthState() {
    Firebase.auth.authStateChanged.subscribe { user ->
        if (user != null) {
            println("Sesión iniciada: ${user.uid}")
        } else {
            println("Sesión cerrada")
        }
    }
}
```

### 2. Cloud Firestore (Base de Datos NoSQL)
Almacena datos estructurados, realiza consultas y escucha cambios en tiempo real. Requiere Kotlin Serialization (`@Serializable`).

```kotlin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Serializable
data class Student(val name: String, val age: Int, val course: String)

// 1. Escribir Datos en Firestore
suspend fun addStudent(id: String, student: Student) {
    val document = Firebase.firestore.collection("students").document(id)
    document.set(student) // ¡Se serializa automáticamente!
}

// 2. Leer un Solo Documento
suspend fun getStudent(id: String): Student? {
    val snapshot = Firebase.firestore.collection("students").document(id).get()
    return if (snapshot.exists) {
        snapshot.data() // ¡Se deserializa automáticamente!
    } else {
        null
    }
}

// 3. Actualizaciones del Documento en Tiempo Real usando Flow
fun streamStudent(id: String): Flow<Student?> {
    return Firebase.firestore.collection("students").document(id)
        .snapshots
        .map { snapshot ->
            if (snapshot.exists) snapshot.data() else null
        }
}
```

### 3. Realtime Database (Base de Datos en Tiempo Real)
Almacena árboles de JSON simples con sincronización en tiempo real.

```kotlin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import kotlinx.serialization.Serializable

@Serializable
data class Message(val sender: String, val text: String)

// 1. Escribir Valor
suspend fun writeMessage(chatId: String, msgId: String, message: Message) {
    val reference = Firebase.database.reference("chats/$chatId/$msgId")
    reference.setValue(message)
}

// 2. Leer Valor Una Sola Vez
suspend fun readMessage(chatId: String, msgId: String): Message {
    val reference = Firebase.database.reference("chats/$chatId/$msgId")
    val snapshot = reference.value()
    return snapshot.value()
}
```
