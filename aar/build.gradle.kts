import de.honoka.gradle.plugin.android.ext.defaultAar
import de.honoka.gradle.plugin.android.ext.kotlinAndroid
import de.honoka.gradle.util.dsl.implementationApi

plugins {
    alias(libs.plugins.android.gradle.plugin)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

version = rootProject.version

android {
    namespace = "de.honoka.sdk.util.android"
    compileSdk = libs.versions.a.android.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.a.android.sdk.min.get().toInt()
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            @Suppress("UnstableApiUsage")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets["main"].java {
        srcDir("/patchSrc/main/java")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = sourceCompatibility
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }
}

//noinspection UseTomlInstead
dependencies {
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementationApi(libs.honoka.kotlin.utils)
    implementationApi(libs.honoka.framework.utils)
    implementationApi("cn.hutool:hutool-all:5.8.18")
    implementationApi("com.j256.ormlite:ormlite-android:5.1")
    implementationApi(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

honoka {
    basic {
        dependencies {
            kotlinAndroid()
            lombok()
            libs.versions.d.kotlin.coroutines
            libs.versions.d.lombok
        }

        publishing {
            defaultAar()
        }
    }
}
