pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
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

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        kotlin("android").version(extra["kotlin.version"] as String)
        id("com.android.application").version(extra["agp.version"] as String)
        id("com.android.library").version(extra["agp.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

rootProject.name = "im-core"

include(":android", ":desktop", ":im-core")
