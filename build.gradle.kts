import de.honoka.gradle.buildsrc.MavenPublish.defineCheckVersionOfProjectsTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.gradle.plugin) apply false
    alias(libs.plugins.kotlin.android) apply false
}

allprojects {
    group = "de.honoka.sdk"
    version = rootProject.libs.versions.root.get()
}

defineCheckVersionOfProjectsTask()