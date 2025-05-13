import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.Properties

plugins {
    `kotlin-dsl`
    idea
}

// warning: do not move down, that breaks things
val gradleProperties by lazy {
    Properties().apply {
        load(rootDir.parentFile.resolve("gradle.properties").inputStream())
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.taumc.org/releases")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.wagyourtail.xyz/releases")
    maven("https://maven.wagyourtail.xyz/snapshots")
}

idea.module.isDownloadSources = true

kotlin {
    compilerOptions.languageVersion = KotlinVersion.KOTLIN_2_0
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

dependencies {
    implementation("org.ow2.asm:asm-tree:${"asm_version"()}")
    implementation("org.ow2.asm:asm-commons:${"asm_version"()}")
    implementation(group = "org.jetbrains", name = "annotations")

    implementation("xyz.wagyourtail.unimined.mapping:unimined-mapping-library-jvm:1.0.2")
    plugin(id = "xyz.wagyourtail.unimined", version = "unimined_version"())
    plugin(id = "org.taumc.gradle.compression", version = "taugradle_version"())
}

operator fun String.invoke() = gradleProperties.getProperty(this) ?: error("No property \"$this\"")

fun DependencyHandler.plugin(id: String, version: String) =
    implementation(group = id, name = "$id.gradle.plugin", version = version)