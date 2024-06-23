import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import de.honoka.gradle.buildsrc.MavenPublish.defineAarSourcesJarTask
import de.honoka.gradle.buildsrc.MavenPublish.setupAarVersionAndPublishing
import de.honoka.gradle.buildsrc.publishing

plugins {
    `maven-publish`
    alias(libs.plugins.dependency.management)
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "de.honoka.sdk.util.android"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.sdk.min.get().toInt()
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            @Suppress("UnstableApiUsage")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets["main"].java {
        srcDir("/patchSrc/main/java")
        val sourceDirSet = if(this is DefaultAndroidSourceDirectorySet) srcDirs else setOf()
        defineAarSourcesJarTask(sourceDirSet)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = sourceCompatibility
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }
}

@Suppress("GradleDependency")
//noinspection UseTomlInstead
dependencies {
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation(libs.kotlin.coroutines.android)
    listOf(
        libs.honoka.kotlin.utils,
        libs.honoka.framework.utils,
        "cn.hutool:hutool-all:5.8.18",
        "com.j256.ormlite:ormlite-android:5.1",
        libs.ktor.server.core
    ).forEach {
        implementation(it)
        api(it)
    }
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    compileOnly(libs.lombok.also {
        annotationProcessor(it)
        testCompileOnly(it)
        testAnnotationProcessor(it)
    })
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

publishing {
    repositories {
        mavenLocal()
    }
}

setupAarVersionAndPublishing(version.toString())
