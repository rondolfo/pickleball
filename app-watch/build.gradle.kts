plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.kriptobr.placar.watch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kriptobr.placar.watch"
        minSdk = 30
        targetSdk = 35
        versionCode = 7
        versionName = "0.7-quadra"
    }

    signingConfigs {
        create("estavel") {
            // Chave fixa, versionada junto com o projeto.
            // Sem isso, cada build do GitHub gera uma chave nova, o Android
            // recusa a atualizacao e a unica saida vira desinstalar, o que
            // apaga jogadores, fotos e historico.
            storeFile = rootProject.file("chave/placar.jks")
            storePassword = "placar2026"
            keyAlias = "placar"
            keyPassword = "placar2026"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("estavel")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("estavel")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    sourceSets["main"].java.srcDirs("src/main/kotlin")

    packaging {
        resources.excludes += setOf(
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.websockets)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
