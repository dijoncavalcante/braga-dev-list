import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("jacoco")
}

/** Upload key settings from the project root; null when the file is absent (e.g. CI, other devs). */
val keystoreProperties: Properties? = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { file -> Properties().apply { file.inputStream().use(::load) } }

android {
    namespace = "com.bragadev.fincheck"
    compileSdk = 36

    defaultConfig {
        // Permanent store identity of FinCheck: it can never change once published on Google Play.
        applicationId = "com.bragadev.fincheck"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Upload key for Google Play, read from keystore.properties (git-ignored, never committed).
        // See keystore.properties.example for the expected keys.
        if (keystoreProperties != null) {
            create("upload") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // R8: shrinks, optimizes and obfuscates the code; unused resources are removed too.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Without keystore.properties the release is signed with the debug key, only to be
            // tested on a device: Google Play rejects debug-signed uploads.
            signingConfig = if (keystoreProperties != null) {
                signingConfigs.getByName("upload")
            } else {
                logger.warn("keystore.properties not found: release signed with the DEBUG key (not uploadable).")
                signingConfigs.getByName("debug")
            }
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
        // BuildConfig.VERSION_NAME / VERSION_CODE for the "Sobre o app" screen.
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    // The exported database schemas feed MigrationTestHelper in the instrumented tests.
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}

room {
    // One JSON per database version, committed to git: the contract of every published version,
    // used to test that each migration produces exactly the schema the app expects.
    schemaDirectory("$projectDir/schemas")
}

detekt {
    toolVersion = "1.23.8" // Use a mesma versão do plugin
    source.setFrom(
        files(
            project.projectDir.toString() + "/src/main/kotlin",
            project.projectDir.toString() + "/src/test/kotlin",
            project.projectDir.toString() + "/src/androidTest/kotlin",
        ),
    ) // Garante que todos os source sets sejam incluídos
    config.setFrom(files("$projectDir/detekt-config.yml")) // Caminho para seu arquivo de configuração
    buildUponDefaultConfig = true // Usa a configuração padrão como base e sobrescreve com seu arquivo
}

ktlint {
    version.set("1.3.1") // Versão do Ktlint Core. Verifique a compatibilidade com o plugin em https://github.com/JLLeitschuh/ktlint-gradle
    verbose.set(true)
    android.set(true) // Aplica o estilo padrão do Android Kotlin Style Guide
    outputToConsole.set(true)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*\$Lambda\$*.*",
        "**/*\$inlined\$*.*",
        "**/di/**",
        "**/*Screen*.*",
        "**/*Preview*.*",
    )
    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/kotlin"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("outputs/unit_test_code_coverage/debugUnitTest/*.exec", "jacoco/testDebugUnitTest.exec")
        },
    )
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(kotlin("test"))

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
