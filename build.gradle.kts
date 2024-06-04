import de.honoka.gradle.buildsrc.MavenPublish.defineCheckVersionOfProjectsTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.gradle.plugin) apply false
    alias(libs.plugins.kotlin) apply false
}

version = libs.versions.root.get()

allprojects {
    group = "de.honoka.sdk"
}

defineCheckVersionOfProjectsTask()