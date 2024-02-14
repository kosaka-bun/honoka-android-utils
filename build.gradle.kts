plugins {
    val versions = de.honoka.gradle.buildsrc.Versions
    //plugins
    id("com.android.library") version versions.libraryPluginVersion apply false
    kotlin("android") version versions.kotlinVersion apply false
}

allprojects {
    group = "de.honoka.sdk"
}