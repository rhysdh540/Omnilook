@file:Suppress("UnstableApiUsage", "VulnerableLibrariesLocal")

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.JsonShrinkingType
import org.taumc.gradle.compression.entryprocessing.EntryProcessors
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.compression.task.JarEntryModificationTask
import org.taumc.gradle.compression.util.toBytes
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.util.OSUtils
import xyz.wagyourtail.unimined.util.withSourceSet


plugins {
    id("java")
    id("idea")
    id("xyz.wagyourtail.unimined") version ("1.3.14-SNAPSHOT")
    id("org.taumc.gradle.compression") version ("0.3.28")
}

group = "dev.rdh"
version = "0.1"
base.archivesName = project.name.lowercase()

idea.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

val ap: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

repositories {
    maven("https://maven.cleanroommc.com/") // mixinbooter
    unimined.modrinthMaven()
    unimined.fabricMaven()
    unimined.wagYourMaven("releases")
    unimined.spongeMaven()
    maven("https://repo.mumfrey.com/content/repositories/snapshots")
}

// region unimined
mc(sourceSets.neoforge) {
    neoForge { loader("neoforge_version"()) }
}

mc(sourceSets.fabric) {
    fabric { loader("fabricloader_version"()) }
}

mc(sourceSets.legacyfabric, mojmap = false) {
    legacyFabric { loader("fabricloader_version"()) }

    mappings {
        searge()
        mcp(channel = "snapshot", version = "legacyfabric_mcp_version"())
    }

    armNatives()
}

forge(sourceSets.lexforge)
forge(sourceSets.lexforge16)
forge(sourceSets.lexforge13, mojmap = false)
forge(sourceSets.lexforge12, mojmap = false) {
    armNatives()
}

mc(sourceSets.rift, mojmap = false) {
    minecraftData.metadataURL = uri("https://skyrising.github.io/mc-versions/manifest/f/f/8444b7446a793191e0c496bba07ac41ff17031/1.13.2.json")

    rift {}

    minecraftRemapper.config {
        ignoreConflicts(true)
    }
}

mc(sourceSets.liteloader, mojmap = false)
// endregion

dependencies {
    compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
    compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
    compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
    compileOnly("net.fabricmc:fabric-loader:${"fabricloader_version"()}")
    compileOnly(sourceSets.stubs.output)

    ap("systems.manifold:manifold-exceptions:${"manifold_version"()}")
    ap("systems.manifold:manifold-rt:${"manifold_version"()}")

    sourceSets {
        fabric.modImplementation(fabricApi.fabricModule(
            "fabric-key-binding-api-v1",
            "fabric_api_version"()
        ))

        fabric.implementation("ca.weblite:java-objc-bridge:1.1")

        lexforge12.modImplementation("zone.rong:mixinbooter:${"lexforge12_mixinbooter_version"()}")

        lexforge12.compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
        lexforge12.compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
        lexforge12.compileOnly("io.github.llamalad7:mixinextras-common:0.5.0-rc.1")

        lexforge13.compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
        lexforge13.compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
        lexforge13.compileOnly("io.github.llamalad7:mixinextras-common:0.3.6")

        liteloader.implementation("com.mumfrey:liteloader:${"liteloader_version"()}") // should be modImplementation but that gets rid of sources
        liteloader.implementation("net.minecraft:launchwrapper:1.12")
        liteloader.implementation("org.spongepowered:mixin:${"liteloader_mixin_version"()}")
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false

    destinationDirectory = layout.buildDirectory.dir("devlibs")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xplugin:Manifold no-bootstrap")
    options.annotationProcessorPath = options.annotationProcessorPath?.plus(ap) ?: ap
    options.release = 8
}

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
    inputs.property("version", project.version)

    filesMatching("**/*") {
        expand("version" to project.version)
    }
}

// region mergeJars and compression
val mergeJars by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier = "dev"
    from(sourceSets.main.output)

    destinationDirectory = layout.buildDirectory.dir("libs")

    manifest.attributes(
        "MixinConfigs" to "omnilook.mixins.json",
        "Fabric-Loom-Mixin-Remap-Type" to "static",
    )
}

afterEvaluate {
    mergeJars.configure {
        tasks.withType<RemapJarTask>().forEach { from(zipTree(it.asJar.archiveFile)) }
    }
}

val compressJar1 = tau.compression.compress<JarEntryModificationTask>(mergeJars, replaceOriginal = false) {
    group = "build"
    archiveClassifier = ""

    json(JsonShrinkingType.MINIFY) { it.endsWith(".json") || it == "mcmod.info" }
    process(EntryProcessors.minifyClass())
    process { name, bytes ->
        if (!name.endsWith(".class")) return@process bytes

        val cn = ClassNode().also {
            ClassReader(bytes).accept(it, ClassReader.SKIP_DEBUG)
        }

        cn.signature = null
        cn.methods.forEach { it.signature = null }
        cn.fields.forEach { it.signature = null }

        if (cn.invisibleAnnotations?.any { it.desc == "Lorg/spongepowered/asm/mixin/Mixin;" } == true) {
            cn.methods.removeAll { it.name == "<init>" && it.instructions.size() <= 3 }

            cn.fields.removeAll {
                it.visibleAnnotations?.any { it.desc == "Lorg/spongepowered/asm/mixin/Shadow;" } == true && it.visibleAnnotations.size == 1
            }

            cn.methods.removeAll {
                it.visibleAnnotations?.any { it.desc == "Lorg/spongepowered/asm/mixin/Shadow;" } == true && it.visibleAnnotations.size == 1
            }
        }

        cn.toBytes()
    }

    // TODO: make this more robust
    process { name, bytes ->
        if (!name.endsWith(".toml")) return@process bytes
        return@process String(bytes).lineSequence()
            .filterNot { it.isBlank() } // remove blank lines
            .map { it.substringBefore('#') } // remove comments
            .map { it.trim() } // remove leading and trailing whitespaces
            .map { it.replace(Regex("\\s*=\\s*"), "=") } // remove whitespaces around '='
            .joinToString("\n").toByteArray()
    }
}

val compressJar2 = tau.compression.compress<AdvzipTask>(compressJar1) {
    level = DeflateAlgorithm.INSANE
}

tasks.assemble {
    dependsOn(mergeJars, compressJar2)
}

// endregion

tasks.withType<RemapJarTask> {
    mixinRemap {
        disableRefmap()
        enableMixinExtra()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

// region helpers
val SourceSetContainer.main get() = getByName("main")
val SourceSetContainer.neoforge get() = maybeCreate("neoforge")
val SourceSetContainer.fabric get() = maybeCreate("fabric")
val SourceSetContainer.lexforge get() = maybeCreate("lexforge")
val SourceSetContainer.lexforge16 get() = maybeCreate("lexforge16")
val SourceSetContainer.lexforge13 get() = maybeCreate("lexforge13")
val SourceSetContainer.lexforge12 get() = maybeCreate("lexforge12")
val SourceSetContainer.legacyfabric get() = maybeCreate("legacyfabric")
val SourceSetContainer.rift get() = maybeCreate("rift")
val SourceSetContainer.liteloader get() = maybeCreate("liteloader")
val SourceSetContainer.stubs get() = maybeCreate("stubs")

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

fun mc(sourceSet: SourceSet, mojmap: Boolean = true, block: MinecraftConfig.() -> Unit = {}) {
    val key = sourceSet.name.lowercase()
    unimined.minecraft(sourceSet) {
        combineWith(sourceSets.main)
        version = "${key}_minecraft_version"()
        runs.config("server") { enabled = false }
        runs.all { jvmArgs("-Dmixin.debug.export=true") }

        block()

        if (mojmap) {
            mappings {
                mojmap()
                parchment(version = "${key}_parchment_version"())
            }
        } else {
            mappings {
                searge()
                mcp(channel = "snapshot", version = "${key}_mcp_version"())
            }
        }
    }
}

fun forge(sourceSet: SourceSet, mojmap: Boolean = true, block: MinecraftConfig.() -> Unit = {}) {
    val key = sourceSet.name.lowercase()
    mc(sourceSet, mojmap) {
        block()

        minecraftForge {
            loader("${key}_version"())
            mixinConfig("omnilook.mixins.json")
        }
    }
}

fun MinecraftConfig.armNatives() {
    if (OSUtils.oSId != OSUtils.OSX) return
    configurations.getByName("minecraftLibraries".withSourceSet(sourceSet)).resolutionStrategy{
        dependencySubstitution {
            substitute(module("org.lwjgl.lwjgl:lwjgl")).using(module("org.lwjgl.lwjgl:lwjgl:2.9.4+legacyfabric.8"))
            substitute(module("org.lwjgl.lwjgl:lwjgl_util")).using(module("org.lwjgl.lwjgl:lwjgl_util:2.9.4+legacyfabric.8"))
            substitute(module("com.mojang:text2speech:1.10.3")).using(module("com.mojang:text2speech:1.11.3"))
            force("org.lwjgl.lwjgl:lwjgl-platform:2.9.4+legacyfabric.8")
        }
    }
}

val SourceSet.implementation get() = configurations.getByName(implementationConfigurationName)
val SourceSet.compileOnly get() = configurations.getByName(compileOnlyConfigurationName)
val SourceSet.modImplementation get() = configurations.getByName("modImplementation".withSourceSet(this))
// endregion
