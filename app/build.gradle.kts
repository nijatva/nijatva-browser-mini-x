plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nijatva.mini.x"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nijatva.mini.x"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        resourceConfigurations += listOf("en")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signing config is supplied by the environment, never committed.
            // See README: signing keys stay out of the repository.
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf("META-INF/*.kotlin_module", "DebugProbesKt.bin")
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    // Nijatva signature: shared Appearance + Developer screens
    implementation("com.nijatva:signature:1.0.0")
    // Everything here is AndroidX or Material. There is deliberately no
    // com.google.android.gms, no Firebase, no Play Services of any kind.
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.webkit:webkit:1.12.1")

    // The one engine that reads a video page without scraping it by hand. MIT licensed,
    // a couple of megabytes, and no Python runtime inside the app.
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.25.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Apache-2.0, not GPL: a real player without the licence bill of the reference apps
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("com.google.android.material:material:1.12.0")
    // MainActivity wraps the WebView in a SwipeRefreshLayout for pull-to-refresh.
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // QR scanning, so a code on a poster opens in the browser
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // weekly refresh of the filter lists, on Wi-Fi only
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
