import xyz.wagyourtail.unimined.util.sourceSets

plugins {
    id("xyz.wagyourtail.unimined")
}

repositories {
    mavenCentral()
    unimined.fabricMaven()
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.terraformersmc.com/releases")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

val SourceSetContainer.fabric by sourceSets.creating

mc(sourceSets.fabric) {
    fabric { loader("fabricloader_version"()) }
}

dependencies {
    sourceSets.fabric.modImplementation("dev.isxander:yet-another-config-lib:3.6.1+1.21-fabric")
    sourceSets.fabric.modImplementation("com.terraformersmc:modmenu:11.0.3")
}

operator fun String.invoke() = prop(this)