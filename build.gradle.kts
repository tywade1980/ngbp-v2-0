plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("androidx.room") version "2.6.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
    // Screenshots of the real composables on the JVM (no phone or emulator): proof that a screen matches its design
    id("app.cash.paparazzi") version "1.3.1" apply false
}