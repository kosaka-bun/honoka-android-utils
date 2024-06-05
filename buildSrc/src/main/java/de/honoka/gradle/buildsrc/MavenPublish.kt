package de.honoka.gradle.buildsrc

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import java.io.File
import java.nio.file.Paths

object MavenPublish {

    private lateinit var rootProject: Project

    private val projectsWillPublish = ArrayList<Project>()

    var sourceDirSet: Set<File> = setOf()

    fun Project.setupAarVersionAndPublishing(version: String) {
        val project = this
        this.version = version
        publishing {
            repositories {
                val isReleaseVersion = version.isReleaseVersion()
                val isDevelopmentRepository = properties["isDevelopmentRepository"]?.toString() == "true"
                if(isReleaseVersion == isDevelopmentRepository) return@repositories
                val remoteUrl = properties["remoteMavenRepositoryUrl"]?.toString() ?: return@repositories
                maven(remoteUrl)
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
                            tasks["sourcesJar"]
                        )
                        setArtifacts(artifacts)
                    }
                }
            }
        }
        projectsWillPublish.add(this)
    }

    fun Project.defineAarSourcesJarTask() {
        tasks.register("sourcesJar", Jar::class.java) {
            group = "build"
            destinationDirectory.set(Paths.get(buildDir.absolutePath, "libs").toFile())
            archiveFileName.set("${project.name}-$version-sources.jar")
            archiveClassifier.set("sources")
            from(*sourceDirSet.toTypedArray())
        }
    }

    fun Project.defineCheckVersionOfProjectsTask() {
        this@MavenPublish.rootProject = rootProject
        tasks.register("checkVersionOfProjects") {
            group = "publishing"
            doLast {
                checkVersionOfProjects()
            }
        }
    }

    private fun checkVersionOfProjects() {
        var passed = true
        val dependencies = HashSet<Dependency>()
        println("Versions:\n")
        listOf(rootProject, *projectsWillPublish.toTypedArray()).forEach {
            if(!passed) return@forEach
            //若project未设置version，则这里取到的version值为unspecified
            println("${it.name}=${it.version}")
            dependencies.addAll(it.rawDependencies)
            passed = it.version.isReleaseVersion()
        }
        if(passed) passed = checkVersionOfDependencies(dependencies)
        println("\nResults:\n")
        println("results.passed=$passed")
        println()
    }

    private fun Any?.isReleaseVersion(): Boolean = toString().lowercase().run {
        !(isEmpty() || this == "unspecified" || contains("dev"))
    }

    private fun checkVersionOfDependencies(dependencies: Set<Dependency>): Boolean {
        var passed = true
        println("\nDependencies:\n")
        dependencies.forEach {
            if(!passed) return@forEach
            println("${it.group}:${it.name}=${it.version}")
            passed = it.version.isReleaseVersion()
        }
        return passed
    }
}