package io.github.kevinah95.firebase_example

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform