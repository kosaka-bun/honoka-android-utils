plugins {
    alias(libs.plugins.android.gradle.plugin) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.honoka.android)
}

version = libs.versions.p.root.get()

allprojects {
    group = "de.honoka.sdk"
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "de.honoka.gradle.plugin.android")

    val libs = rootProject.libs

    dependencyManagement {
        imports {
            mavenBom(libs.kotlin.bom.get().toString())
        }
    }
}

honoka {
    basic {
        publishing {
            defineCheckVersionTask()
        }
    }
}
