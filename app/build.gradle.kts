import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use { stream -> load(stream) }
    }
}

fun androidStringLiteral(value: String): String {
    val escapedValue = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$escapedValue\""
}

fun requireTrailingSlash(value: String): String {
    return if (value.endsWith("/")) value else "$value/"
}

val backendUrl = requireTrailingSlash(
    providers.gradleProperty("SOLARI_BACKEND_URL").orNull
        ?: providers.environmentVariable("SOLARI_BACKEND_URL").orNull
        ?: localProperties.getProperty("SOLARI_BACKEND_URL")
        ?: "http://10.0.2.2:3000/"
)
require(backendUrl.startsWith("http://") || backendUrl.startsWith("https://")) {
    "SOLARI_BACKEND_URL must start with http:// or https://"
}

val googleServerClientId =
    providers.gradleProperty("SOLARI_GOOGLE_SERVER_CLIENT_ID").orNull
        ?: providers.environmentVariable("SOLARI_GOOGLE_SERVER_CLIENT_ID").orNull
        ?: localProperties.getProperty("SOLARI_GOOGLE_SERVER_CLIENT_ID")
        ?: ""

android {
    namespace = "com.solari.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.solari.app"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SOLARI_BACKEND_URL", androidStringLiteral(backendUrl))
        buildConfigField("String", "SOLARI_GOOGLE_SERVER_CLIENT_ID", androidStringLiteral(googleServerClientId))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.extensions)
    
    // Coil
    implementation(libs.coil.compose)

    // Media playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // Networking & Serialization
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    // Authentication
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Local persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
