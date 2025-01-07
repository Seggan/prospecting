@file:OptIn(ExperimentalPathApi::class)

import kotlin.io.path.ExperimentalPathApi

plugins {
    idea
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "io.github.seggan"
version = "MODIFIED"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    library(kotlin("stdlib"))
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    library(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:d12ae8580b")

    implementation("io.github.seggan:sf4k:0.8.1")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        javaParameters = true
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

val createResourceListingTask = tasks.register("createResourceListing") {
    dependsOn(tasks.generateBukkitPluginDescription)

    val dirs = project.sourceSets.main.get().resources.srcDirs
    for (dir in dirs) {
        inputs.dir(dir)
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }

    val output = layout.buildDirectory.dir("resource-listing")
    outputs.dir(output)
        .withPropertyName("output")

    doLast {
        val relative = dirs
            .filter(File::exists)
            .flatMap { dir ->
                dir.walk().map { it.relativeTo(dir) }
            }
            .map(File::toString)
            .filter(String::isNotBlank)
            .toSet()
        output.get().asFile.resolve("resources.txt").writeText(relative.joinToString("\n"))
    }
}

sourceSets {
    main {
        resources {
            srcDir(createResourceListingTask.map { it.outputs })
        }
    }
}

tasks.shadowJar {
    mergeServiceFiles()

    fun doRelocate(lib: String) {
        relocate(lib, "io.github.seggan.prospecting.shadowlibs.$lib")
    }

    doRelocate("org.bstats")
    doRelocate("co.aikar")
    doRelocate("io.github.seggan.sf4k")

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core"))
    }

    archiveBaseName = rootProject.name
}

bukkit {
    name = rootProject.name
    main = "io.github.seggan.prospecting.Prospecting"
    version = project.version.toString()
    author = "Seggan"
    apiVersion = "1.20"
    depend = listOf("Slimefun")
    loadBefore = listOf("Multiverse-Core")
}

tasks.runServer {
    downloadPlugins {
        url("https://blob.build/dl/Slimefun4/Dev/1157")
        url("https://blob.build/dl/SlimeHUD/Dev/3")
    }
    maxHeapSize = "4G"
    minecraftVersion("1.20.6")
}