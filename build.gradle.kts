plugins {
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

    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:e02a0f61d1")

    implementation("io.github.seggan:sf4k:0.8.0")
    library("org.msgpack:msgpack-core:0.9.8")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
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
        url("https://blob.build/dl/Slimefun4/Dev/1156")
        url("https://blob.build/dl/SlimeHUD/Dev/3")
        hangar("Multiverse-Core", "4.3.13")
    }
    maxHeapSize = "4G"
    minecraftVersion("1.20.6")
}