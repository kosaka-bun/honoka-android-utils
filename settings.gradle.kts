@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        mavenCentral()
        mavenLocal()
        maven("https://mirrors.honoka.de/maven-repo")
        google()
    }
}

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "honoka-android-utils"

include(":aar")
