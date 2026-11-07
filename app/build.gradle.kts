plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.room")
    id("kotlinx-serialization")
}

android {
    namespace = "com.constructionmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wadecustomcarpentry.hocc"
        minSdk = 26
        targetSdk = 34
        versionCode = 32
        versionName = "0.7.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Firebase Web API key (audit B12): never in the source. From -PfirebaseApiKey=... or the FIREBASE_API_KEY
        // environment variable (a CI secret). Empty = the app builds and runs with cloud features failing visibly.
        val firebaseApiKey = (project.findProperty("firebaseApiKey") as String?) ?: System.getenv("FIREBASE_API_KEY") ?: ""
        buildConfigField("String", "FIREBASE_API_KEY", "\"" + firebaseApiKey.replace("\"", "") + "\"")
        
        room {
            schemaDirectory("$projectDir/schemas")
        }
    }

    signingConfigs {
        // No release key exists for this app. The "clean"/release builds below are signed OPENLY with the
        // standard Android debug keystore from this build machine (~/.android/debug.keystore) so the APK
        // carries the base applicationId with no .debug suffix and still installs. This is not a release key.
        create("debugKey") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // 0.5.2: shrink OFF - 0.5.1 installed but died at launch; delivery is the download link now, not Gmail
            signingConfig = signingConfigs.getByName("debugKey")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        // Whole-new build for Tyler's phone (Oct 26 2026): debug-shaped code, but the CLEAN package
        // com.constructionmanager (no .debug suffix) so it never collides with the .debug install.
        create("clean") {
            initWith(getByName("debug"))
            applicationIdSuffix = null
            signingConfig = signingConfigs.getByName("debugKey")
            // HOCC 0.5.0: shrink the shipped build (R8 + resource shrinking, proguard-rules.pro) - the
            // un-minified tree is 22 MB, too large to deliver as an email attachment (Gmail's 25 MB cap
            // counts the base64-encoded message, ~29.5 MB). Code stays debug-shaped otherwise.
            isMinifyEnabled = false  // 0.5.2: shrink OFF - 0.5.1 installed but died at launch; delivery is the download link now, not Gmail
            isShrinkResources = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Compose BOM and UI
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Lifecycle and ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")

    // In-app / OTA updates (Google Play In-App Updates + Kotlin coroutine extensions)
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Firebase (connected to the "nextgenbuildpro" project via manual FirebaseOptions init)
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.0")
    // The Caroline connector is tested against a local https server (MockWebServer + a throwaway certificate)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okhttp3:okhttp-tls:4.12.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}