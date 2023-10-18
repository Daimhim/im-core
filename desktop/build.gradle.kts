import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "org.daimhim.im.core"
version = "1.0-SNAPSHOT"


kotlin {
    jvm {
//        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("com.google.code.gson:gson:2.10.1")
                implementation("com.github.Daimhim:imc-core:1.0.3")
                implementation("com.squareup.okhttp3:okhttp:4.9.0")
                implementation("com.github.Daimhim.timber-multiple-platforms:timber:1.0.5")
                implementation(project(":im-core"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "im-core-demo"
            packageVersion = "1.0.0"
        }
    }
}
