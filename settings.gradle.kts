@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        maven("https://mirrors.honoka.de/maven-repo/release")
        maven("https://mirrors.honoka.de/maven-repo/development")
    }
}

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "honoka-android-utils"

include(":aar")
