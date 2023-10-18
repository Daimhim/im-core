import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
}

group = "org.daimhim.im.core"
version = "1.2.5-SNAPSHOT"

kotlin {
    android(){
        publishLibraryVariants("release")
    }
    jvm("desktop") {
//        jvmToolchain(11)
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly("com.google.code.gson:gson:2.10.1")
                compileOnly("com.github.Daimhim:imc-core:1.0.3")
                compileOnly("com.squareup.okhttp3:okhttp:4.9.0")
                compileOnly("com.github.Daimhim.timber-multiple-platforms:timber:1.0.5")
                compileOnly("com.github.Daimhim:ContextHelper:1.0.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                compileOnly("com.github.kongqw:NetworkMonitor:1.2.0")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(33)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(22)
        targetSdkVersion(33)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    repositories {
        maven {
            url = uri("../repo")
        }
    }
}