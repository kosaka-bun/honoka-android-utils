plugins {
    alias(libs.plugins.android.gradle.plugin) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.honoka.android)
}

version = libs.versions.p.root.get()

allprojects {
    group = "de.honoka.sdk"
}

subprojects {
    apply(plugin = "de.honoka.gradle.plugin.android")
}

honoka {
    basic {
        publishing {
            defineCheckVersionTask()
        }
    }
}

libs.versions.d.kotlin.coroutines
libs.versions.d.lombok
