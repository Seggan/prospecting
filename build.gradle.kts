import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.2"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
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
    paperLibrary(kotlin("stdlib"))
    paperLibrary("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    paperLibrary(kotlin("reflect"))

    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:e02a0f61d1")

    implementation("io.github.seggan:sf4k:0.7.2")

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
        freeCompilerArgs = listOf("-Xjvm-default=all", "-Xcontext-receivers")
    }
}

tasks.shadowJar {
    fun doRelocate(from: String) {
        val last = from.split(".").last()
        relocate(from, "io.github.seggan.prospecting.shadowlibs.$last")
    }

    mergeServiceFiles()
    // Relocate if true or not set, always relocate bstats
    doRelocate("org.bstats")
    if (System.getenv("RELOCATE") != "false") {
        doRelocate("io.github.seggan.sf4k")
        doRelocate("co.aikar.commands")
        doRelocate("co.aikar.locales")
    } else {
        archiveClassifier = "unrelocated"
    }

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

paper {
    name = rootProject.name
    main = "io.github.seggan.prospecting.Prospecting"
    loader = "io.github.seggan.prospecting.shadowlibs.sf4k.PluginYmlLoader"
    bootstrapper = "io.github.seggan.prospecting.ProspectingBootstrapper"
    version = project.version.toString()
    author = "Seggan"
    apiVersion = "1.20"
    generateLibrariesJson = true

    serverDependencies {
        register("Slimefun") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("Multiverse-Core") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
        }
    }
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