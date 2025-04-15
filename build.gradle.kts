@file:Suppress("UnstableApiUsage", "VulnerableLibrariesLocal")

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.JsonShrinkingType
import org.taumc.gradle.compression.entryprocessing.EntryProcessors
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.compression.task.JarEntryModificationTask
import org.taumc.gradle.compression.util.toBytes
import xyz.wagyourtail.unimined.api.mapping.MappingsConfig
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
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
    maven("https://maven.wispforest.io")
}

// region unimined
mc(sourceSets.neoforge) {
    neoForge { loader("neoforge_version"()) }
}

mc(sourceSets.fabric) {
    fabric { loader("fabricloader_version"()) }
}

mc(sourceSets.babric, mappings = Mappings {
    mapping("me.alphamode:nostalgia:${minecraft.version}+build.${"babric_nostalgia_version"()}:v2", "nostalgia") {
        outputs("nostalgia", true) { listOf("intermediary") }
        mapNamespace("named", "nostalgia")
        sourceNamespace("intermediary")
        renest()
    }
}) {
    babric { loader("babric_loader_version"()) }
}

mc(sourceSets.legacyfabric, mappings = seargeMcp) {
    legacyFabric { loader("fabricloader_version"()) }

    mappings {
        searge()
        mcp(channel = "snapshot", version = "legacyfabric_mcp_version"())
    }
}

forge(sourceSets.lexforge)
forge(sourceSets.lexforge16)
forge(sourceSets.lexforge13, mappings = seargeMcp)
forge(sourceSets.lexforge12, mappings = seargeMcp)

mc(sourceSets.rift, mappings = seargeMcp) {
    minecraftData.metadataURL = uri("https://skyrising.github.io/mc-versions/manifest/f/f/8444b7446a793191e0c496bba07ac41ff17031/1.13.2.json")

    rift {}

    minecraftRemapper.config {
        ignoreConflicts(true)
    }
}

mc(sourceSets.liteloader, mappings = seargeMcp)

unimined.reIndev(sourceSets.reindev) {
    combineWith(sourceSets.main)
    version = "reindev_version"()
    side("client")
    runs.all { jvmArgs("-Dmixin.debug.export=true") }

    foxLoader {
        loader()
        modId = "omnilook"
    }
}
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

        legacyfabric.modImplementation(fabricApi.legacyFabricModule(
            "legacy-fabric-keybindings-api-v1-common",
            "legacyfabric_api_version"()
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

        // reindev might be a little broken
        reindev.implementation("org.semver4j:semver4j:5.3.0")
        reindev.implementation("net.fabricmc:sponge-mixin:0.15.0+mixin.0.8.7")
        reindev.implementation("io.github.llamalad7:mixinextras-common:0.4.0")
        reindev.implementation("org.apache.commons:commons-lang3:3.3.2")
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
    from(sourceSets.reindev.output)

    destinationDirectory = layout.buildDirectory.dir("libs")

    manifest.attributes(
        "MixinConfigs" to "omnilook.mixins.json",
        "Fabric-Loom-Mixin-Remap-Type" to "static",

        // reindev stuff
        "ModId" to "omnilook",
        "ModName" to "Omnilook",
        "ModVersion" to project.version,
        "ModDesc" to "funny look around mod",
        "ClientMod" to "dev.rdh.omnilook.FoxlookMod",
        "ClientMixin" to "omnilook.mixins.json",
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
val SourceSetContainer.babric get() = maybeCreate("babric")
val SourceSetContainer.reindev get() = maybeCreate("reindev")
val SourceSetContainer.stubs get() = maybeCreate("stubs")

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

class Mappings(private val action: MappingsConfig.(key: String) -> Unit) {
    operator fun invoke(key: String): MappingsConfig.() -> Unit = {
        action(key)
    }
}

val mojmap
    get() = Mappings {
        mojmap()
        parchment(version = "${it}_parchment_version"())
    }

val seargeMcp
    get() = Mappings {
        searge()
        val channel = project.findProperty("${it}_mcp_channel")?.toString() ?: "snapshot"
        mcp(channel, version = "${it}_mcp_version"())
    }


fun mc(sourceSet: SourceSet, mappings: Mappings = mojmap, block: MinecraftConfig.() -> Unit = {}) {
    val key = sourceSet.name.lowercase()
    unimined.minecraft(sourceSet) {
        combineWith(sourceSets.main)
        version = "${key}_minecraft_version"()
        runs.config("server") { enabled = false }
        runs.all { jvmArgs("-Dmixin.debug.export=true") }

        block()

        mappings {
            mappings(key)()
        }
    }
}

fun forge(sourceSet: SourceSet, mappings: Mappings = mojmap, block: MinecraftConfig.() -> Unit = {}) {
    val key = sourceSet.name.lowercase()
    mc(sourceSet, mappings) {
        block()

        minecraftForge {
            loader("${key}_version"())
            mixinConfig("omnilook.mixins.json")
        }
    }
}

fun SourceSet.configuration(name: String) = configurations.getByName(name.withSourceSet(this))
val SourceSet.implementation get() = configuration(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
val SourceSet.compileOnly get() = configuration(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
val SourceSet.modImplementation get() = configuration("modImplementation")
// endregion
