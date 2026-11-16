// ISNscale - App-level Gradle build file

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.isnscale"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.isnscale"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }

    signingConfigs {
        // Stable signing key for every build (debug and release).
        //
        // The app is side-loaded, not published to Play, so a single key is
        // used for both variants. This keeps the signature identical between
        // CI runs, so a new APK installs over an older one instead of failing
        // with INSTALL_FAILED_UPDATE_INCOMPATIBLE.
        //
        // This key is a throwaway demo key and is intentionally public. Do not
        // reuse it for anything you actually distribute.
        create("demo") {
            storeFile = rootProject.file("keystore/isnscale-demo.jks")
            storePassword = "isnscale"
            keyAlias = "isnscale"
            keyPassword = "isnscale"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("demo")
        }
        release {
            isMinifyEnabled = false
            // Sign the release variant too, otherwise the artifact is
            // *unsigned* and Android refuses to install it.
            signingConfig = signingConfigs.getByName("demo")
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // ARCore - device tracking + real-world depth, so no reference object is needed
    implementation("com.google.ar:core:1.57.0")

    // Lifecycle / Activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    // Compose UI
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    // Debug tooling
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
