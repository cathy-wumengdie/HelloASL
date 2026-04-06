import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val appVersion = "1.0.0"
version = appVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.googleServices)
    kotlin("plugin.serialization") version "2.3.0"
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation(project.dependencies.platform("io.github.jan-tennert.supabase:bom:3.4.1"))
            implementation("io.github.jan-tennert.supabase:auth-kt")
            implementation("io.github.jan-tennert.supabase:postgrest-kt")
            implementation("io.github.jan-tennert.supabase:realtime-kt")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("io.github.jan-tennert.supabase:functions-kt")
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation("androidx.media3:media3-exoplayer:1.2.1")
            implementation("androidx.media3:media3-ui:1.2.1")
            implementation("io.ktor:ktor-client-android:3.0.0")
            implementation("com.squareup.retrofit2:retrofit:2.9.0")
            implementation("com.squareup.retrofit2:converter-gson:2.9.0")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.12.0"))
            implementation("com.google.firebase:firebase-messaging-ktx")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.ktor:ktor-client-cio:3.0.0")

            val osName = System.getProperty("os.name").lowercase()
            val osArch = System.getProperty("os.arch").lowercase()

            val javafxPlatform = when {
                osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm")) -> "mac-aarch64"
                osName.contains("mac") -> "mac"
                osName.contains("win") -> "win"
                else -> "linux"
            }

            implementation("org.openjfx:javafx-base:21.0.2:$javafxPlatform")
            implementation("org.openjfx:javafx-graphics:21.0.2:$javafxPlatform")
            implementation("org.openjfx:javafx-controls:21.0.2:$javafxPlatform")
            implementation("org.openjfx:javafx-media:21.0.2:$javafxPlatform")
            implementation("org.openjfx:javafx-swing:21.0.2:$javafxPlatform")
            implementation("org.bytedeco:javacv-platform:1.5.13")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        androidInstrumentedTest.dependencies {
            implementation("androidx.compose.ui:ui-test-junit4:1.7.3")
            implementation("androidx.test.ext:junit:1.1.5")
            implementation("androidx.test.espresso:espresso-core:3.5.1")
            implementation("androidx.test:runner:1.5.2")
            implementation("androidx.test:rules:1.5.0")
        }
    }
}

android {
    namespace = "ca.uwaterloo.helloasl"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "ca.uwaterloo.helloasl"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = appVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProps.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProps.getProperty("SUPABASE_ANON_KEY", "")}\""
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    dependencies {
        coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.3")
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-video:1.4.2")
}

compose.desktop {
    application {
        mainClass = "ca.uwaterloo.helloasl.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ca.uwaterloo.helloasl"
            packageVersion = appVersion

            macOS {
                bundleID = "ca.uwaterloo.helloasl"
                infoPlist {
                    extraKeysRawXml = """
                        <key>NSCameraUsageDescription</key>
                        <string>HelloASL uses the camera for ASL to English translation.</string>
                        
                        <key>NSCameraUseContinuityCameraDeviceType</key>
                        <true/>
                    """.trimIndent()
                }
            }
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    systemProperty("SUPABASE_URL", localProps.getProperty("SUPABASE_URL", ""))
    systemProperty("SUPABASE_ANON_KEY", localProps.getProperty("SUPABASE_ANON_KEY", ""))
}