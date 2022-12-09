import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.extractMethod"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.7.21" apply true
    id("org.jetbrains.intellij") version "1.10.0" apply true
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
    }

    repositories {
        mavenCentral()
    }

    val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        // Plugin utilities modules
        dependencies {
            implementation("$utilitiesProjectName:plugin-utilities-core") {
                version {
                    branch = "main"
                }
            }
        }
        implementation("com.google.code.gson:gson:2.9.0")
    }

    intellij {
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
        downloadSources.set(properties("platformDownloadSources").toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
        }
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }
}