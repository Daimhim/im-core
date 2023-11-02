group = "org.daimhim.im.core"
version = "1.0-SNAPSHOT"
buildscript {
    repositories {
        mavenLocal()
    }
}
allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/releases")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")
        maven("https://devrepo.devcloud.cn-east-3.huaweicloud.com/artgalaxy/cn-east-3_74e793c6bfdf4b258360413fd0eba4c7_maven_2_2/"){
            credentials {
                username = "cn-east-3_74e793c6bfdf4b258360413fd0eba4c7_32540a29aa2e4157a4d3a0233c5b895b"
                password = "6l^EL-1aVx"
            }
        }
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
}