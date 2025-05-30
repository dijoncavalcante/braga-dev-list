plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
}

android {
    namespace = "com.bragadev.list"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bragadev.list"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

detekt {
    toolVersion = "1.23.8" // Use a mesma versão do plugin
    source.setFrom(files(project.projectDir.toString() + "/src/main/kotlin", project.projectDir.toString() + "/src/test/kotlin", project.projectDir.toString() + "/src/androidTest/kotlin")) // Garante que todos os source sets sejam incluídos
    config.setFrom(files("$projectDir/detekt-config.yml")) // Caminho para seu arquivo de configuração
    buildUponDefaultConfig = true // Usa a configuração padrão como base e sobrescreve com seu arquivo
//     reports { // Descomente e configure se quiser relatórios específicos
////         xml {
////             required.set(true)
////             outputLocation.set(file("build/reports/detekt/detekt.xml"))
////         }
////         html {
////             required.set(true)
////             outputLocation.set(file("build/reports/detekt/detekt.html"))
////         }
////         txt {
////             required.set(true)
////             outputLocation.set(file("build/reports/detekt/detekt.txt"))
////         }
////         sarif {
////             required.set(true)
////             outputLocation.set(file("build/reports/detekt/detekt.sarif"))
////         }
////     }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}