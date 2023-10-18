plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

group = "org.daimhim.im.core"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation(project(":im-core"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.activity:activity-compose:1.5.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("com.github.Daimhim:imc-core:1.0.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.github.Daimhim.timber-multiple-platforms:timber:1.0.5")
    implementation ("androidx.fragment:fragment-ktx:1.5.7")
    implementation ("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.github.kongqw:NetworkMonitor:1.2.0")
    implementation("com.github.Daimhim:SimpleAdapter:1.1.0")
    implementation("com.github.Daimhim:ContextHelper:1.0.3")
}

android {
    compileSdkVersion(33)
    defaultConfig {
        applicationId = "org.daimhim.im.core.android"
        minSdkVersion(22)
        targetSdkVersion(33)
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        viewBinding = true
    }
}