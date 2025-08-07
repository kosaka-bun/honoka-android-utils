import de.honoka.gradle.plugin.android.ext.defaultAar
import de.honoka.gradle.plugin.android.ext.kotlinAndroid

plugins {
    alias(libs.plugins.android.gradle.plugin)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

version = rootProject.version

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

android {
    namespace = "de.honoka.sdk.util.android"
    compileSdk = libs.versions.a.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.a.min.sdk.get().toInt()
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets["main"].java {
        srcDir("/patchSrc/main/java")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
        }
    }
}

//noinspection UseTomlInstead
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    api(libs.honoka.kotlin.utils)
    api("cn.hutool:hutool-all:5.8.39")
    api("com.j256.ormlite:ormlite-android:5.1")
    api(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

honoka {
    basic {
        dependencies {
            kotlinAndroid()
            lombok()
        }

        publishing {
            defaultAar()
        }
    }
}
