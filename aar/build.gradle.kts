import de.honoka.gradle.buildsrc.Versions

plugins {
    `maven-publish`
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.android.library")
    kotlin("android")
}

version = "1.1.0-dev"

android {
    namespace = "de.honoka.sdk.util.android"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = sourceCompatibility
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }
}

@Suppress("GradleDependency")
dependencies {
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}")
    listOf(
        "de.honoka.sdk:honoka-kotlin-utils:1.0.0-dev",
        "de.honoka.sdk:honoka-framework-utils:1.0.4",
        "cn.hutool:hutool-all:5.8.18",
        "com.j256.ormlite:ormlite-android:5.1",
        "io.ktor:ktor-server-core:${Versions.ktor}"
    ).forEach {
        implementation(it)
        api(it)
    }
    implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.ktor}")
    implementation("io.ktor:ktor-server-cors:${Versions.ktor}")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    compileOnly("org.projectlombok:lombok:${Versions.lombok}".also {
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
        if(hasProperty("remoteMavenRepositoryUrl")) {
            maven(properties["remoteMavenRepositoryUrl"]!!)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = rootProject.name
            this.version = version
            pom.withXml {
                val apiDependencies = ArrayList<String>()
                project.configurations["api"].allDependencies.forEach {
                    apiDependencies.add("${it.group}:${it.name}")
                }
                asNode().appendNode("dependencies").run {
                    project.configurations.implementation.configure {
                        allDependencies.forEach {
                            val isInvalidDependency = it.group == null ||
                                it.name.lowercase() == "unspecified" ||
                                it.version == null
                            if(isInvalidDependency) return@forEach
                            val moduleName = "${it.group}:${it.name}"
                            appendNode("dependency").run {
                                val subNodes = hashMapOf(
                                    "groupId" to it.group,
                                    "artifactId" to it.name,
                                    "version" to it.version
                                )
                                if(!apiDependencies.contains(moduleName)) {
                                    subNodes["scope"] = "runtime"
                                }
                                subNodes.forEach { entry ->
                                    appendNode(entry.key, entry.value)
                                }
                            }
                        }
                    }
                }
            }
            afterEvaluate {
                val artifacts = listOf(
                    tasks["bundleReleaseAar"],
                    tasks["releaseSourcesJar"]
                )
                setArtifacts(artifacts)
            }
        }
    }
}

tasks.register("checkVersion") {
    group = "publishing"
    doLast {
        println("Versions:\n")
        //若project未设置version，则这里取到的version值为unspecified
        println("${project.name}=${project.version}")
        val passed = project.version.toString().lowercase().run {
            !(isEmpty() || this == "unspecified" || contains("dev"))
        }
        println("\nResults:\n")
        println("results.passed=$passed")
        println()
    }
}