package io.github.kevinah95.firebase_example

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun getEmulatorHost(): String = "10.0.2.2"