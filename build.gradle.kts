plugins {
    val versions = de.honoka.gradle.buildsrc.Versions
    //plugins
    id("com.android.library") version versions.libraryPlugin apply false
    kotlin("android") version versions.kotlin apply false
}

allprojects {
    group = "de.honoka.sdk"
}