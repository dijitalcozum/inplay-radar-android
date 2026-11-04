plugins {
    id("com.android.application")
}

android {
    namespace = "com.inplayradar.mobile"
    compileSdk = 35

    signingConfigs {
        create("persistent") {
            storeFile = rootProject.file("app/inplay-release.keystore")
            storePassword = System.getenv("INPLAY_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("INPLAY_KEY_ALIAS")
            keyPassword = System.getenv("INPLAY_KEY_PASSWORD")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.inplayradar.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("persistent")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("persistent")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("com.google.android.material:material:1.12.0")
}
