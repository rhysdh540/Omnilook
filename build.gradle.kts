@file:Suppress("UnstableApiUsage", "VulnerableLibrariesLocal")

import org.gradle.api.internal.artifacts.configurations.ResolutionBackedFileCollection
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.JsonShrinkingType
import org.taumc.gradle.compression.entryprocessing.EntryProcessors
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.compression.task.JarEntryModificationTask
import org.taumc.gradle.compression.util.toBytes
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.internal.minecraft.task.GenSourcesTaskImpl
import xyz.wagyourtail.unimined.util.withSourceSet

plugins {
    id("idea")
    id("xyz.wagyourtail.unimined")
    id("org.taumc.gradle.compression")
}

group = "dev.rdh"
version = "mod_version"()
base.archivesName = project.name.lowercase()

//idea.module {
//    isDownloadSources = true
//    isDownloadJavadoc = true
//}

val ap: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

repositories {
    unimined.modrinthMaven()
    unimined.fabricMaven()
    unimined.wagYourMaven("releases")
    unimined.spongeMaven()
    exclusiveContent {
        forRepository { maven("https://repo.mumfrey.com/content/repositories/snapshots") }
        filter { includeModule("com.mumfrey", "liteloader") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.cleanroommc.com") }
        filter { includeGroup("zone.rong") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.wispforest.io") }
        filter { includeModule("me.alphamode", "nostalgia") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.terraformersmc.com/releases") }
        filter { includeGroup("com.terraformersmc") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.isxander.dev/releases") }
        filter { includeGroup("dev.isxander") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.shedaniel.me") }
        filter { includeGroupAndSubgroups("me.shedaniel") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.ornithemc.net/releases") }
        filter { includeGroup("net.ornithemc") }
    }
}

val SourceSetContainer.main by sourceSets.getting
val SourceSetContainer.stubs by sourceSets.creating

val SourceSetContainer.fabric by sourceSets.creating
val SourceSetContainer.legacyfabric by sourceSets.creating
val SourceSetContainer.ornithe by sourceSets.creating
val SourceSetContainer.babric by sourceSets.creating

val SourceSetContainer.neoforge by sourceSets.creating
val SourceSetContainer.lexforge by sourceSets.creating
val SourceSetContainer.lexforge20 by sourceSets.creating
val SourceSetContainer.lexforge16 by sourceSets.creating
val SourceSetContainer.lexforge13 by sourceSets.creating
val SourceSetContainer.lexforge12 by sourceSets.creating
val SourceSetContainer.lexforge7 by sourceSets.creating

val SourceSetContainer.rift by sourceSets.creating
val SourceSetContainer.liteloader by sourceSets.creating
val SourceSetContainer.reindev by sourceSets.creating

// region unimined
mc(sourceSets.neoforge) {
    neoForge { loader("neoforge_version"()) }
}

mc(sourceSets.fabric) {
    fabric { loader("fabricloader_version"()) }
}

// NOTE: currently trying to remap this to feather makes dependencies not remap
// but mcp works for now i guess
mc(sourceSets.legacyfabric, mappings = searge + mcp) {
    legacyFabric { loader("fabricloader_version"()) }
}

mc(sourceSets.ornithe, mappings = feather) {
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

forge(sourceSets.lexforge)
forge(sourceSets.lexforge20)
forge(sourceSets.lexforge16)
forge(sourceSets.lexforge13, mappings = feather + featherForge113Fix)
forge(sourceSets.lexforge12, mappings = feather + featherForge112Fix)
forge(sourceSets.lexforge7, mappings = feather + featherForge17Fix)

mc(sourceSets.rift, mappings = feather) {
    minecraftData.metadataURL = uri("https://skyrising.github.io/mc-versions/manifest/f/f/8444b7446a793191e0c496bba07ac41ff17031/1.13.2.json")

    rift {}

    minecraftRemapper.config {
        ignoreConflicts(true)
    }
}

mc(sourceSets.liteloader, mappings = searge + mcp)

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

(sourceSets - sourceSets.main).forEach {
    tasks.named(it.classesTaskName) {
        group = "platform"
    }

    tasks.findByName(it.jarTaskName)?.apply {
        group = "platform"
    }
}

dependencies {
    compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
    compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
    compileOnly("net.fabricmc:fabric-loader:${"fabricloader_version"()}")
    compileOnly(sourceSets.stubs.output)

    ap("systems.manifold:manifold-exceptions:${"manifold_version"()}")
    ap("systems.manifold:manifold-rt:${"manifold_version"()}")

    sourceSets {
        legacyfabric.modImplementation(fabricApi.legacyFabricModule(
            "legacy-fabric-keybindings-api-v1-common",
            "legacyfabric_api_version"()
        ))

        fabric.implementation("ca.weblite:java-objc-bridge:1.1")
        fabric.modImplementation("dev.isxander:yet-another-config-lib:3.6.1+1.21-fabric")
        fabric.modImplementation("me.shedaniel.cloth:cloth-config-fabric:15.0.140")
        fabric.modImplementation("com.terraformersmc:modmenu:11.0.3")

        neoforge.implementation("ca.weblite:java-objc-bridge:1.1")
        neoforge.modImplementation("dev.isxander:yet-another-config-lib:3.6.1+1.21-neoforge") {
            exclude("thedarkcolour", "kotlinforforge-neoforge")
        }
        neoforge.modImplementation("me.shedaniel.cloth:cloth-config-forge:15.0.140")

        lexforge20.modImplementation("me.shedaniel.cloth:cloth-config-forge:11.1.136")

        lexforge16.modImplementation("me.shedaniel.cloth:cloth-config-forge:4.17.132")

        lexforge12.modImplementation("zone.rong:mixinbooter:${"lexforge12_mixinbooter_version"()}")

        lexforge12.compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
        lexforge12.compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
        lexforge12.compileOnly("io.github.llamalad7:mixinextras-common:0.5.0-rc.1")

        lexforge13.compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
        lexforge13.compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
        lexforge13.compileOnly("io.github.llamalad7:mixinextras-common:0.3.6")

        rift.compileOnly(sourceSets.stubs.output)

        liteloader.modImplementation("com.mumfrey:liteloader:${"liteloader_version"()}") // should be modImplementation but that gets rid of sources
        liteloader.implementation("net.minecraft:launchwrapper:1.12")
        liteloader.implementation("org.spongepowered:mixin:${"liteloader_mixin_version"()}")

        // reindev might be a little broken
        reindev.implementation("org.semver4j:semver4j:5.3.0")
        reindev.implementation("net.fabricmc:sponge-mixin:0.15.0+mixin.0.8.7")
        reindev.implementation("io.github.llamalad7:mixinextras-common:0.4.0")
        reindev.implementation("org.apache.commons:commons-lang3:3.3.2")
    }
}

// intellij auto-download/find sources
gradle.taskGraph.whenReady {
    allTasks.filter { it.name.startsWith("ijDownloadArtifact") && it.javaClass.name.startsWith("IjDownloadTask") }.forEach { task ->
        // https://github.com/JetBrains/intellij-community/blob/00c6a47ee4f5aa01215d7bbdf9a07d5f30e8ab0f/plugins/gradle/tooling-extension-impl/resources/org/jetbrains/plugins/gradle/tooling/internal/init/downloadArtifact.gradle#L19
        @Suppress("UNCHECKED_CAST")
        val property = task.javaClass.getMethod("getCollectionProvider").invoke(task) as Property<FileCollection>
        val path = property.get() as ResolutionBackedFileCollection

        val name = path.resolutionHost.displayName.removePrefix("configuration ':").removeSuffix("'")
        val configuration = configurations.find { it.name == name } ?: error("no configuration $name")

        val dep = configuration.incoming.dependencies.singleOrNull() ?: return@forEach

        fun Dependency.isGameDep(): Boolean {
            if (this.group == "net.minecraft" && this.name.startsWith("minecraft+")) return true
            if (this.group == "net.silveros" && this.name.startsWith("reindev+")) return true
            return false
        }

        // run genSources for minecraft
        if (dep.isGameDep()) {
            val sourceSetName = dep.name.substringAfter("+")
            val sourceSet = sourceSets.findByName(sourceSetName) ?: error("no source set $sourceSetName")
            tasks.getByName<GenSourcesTaskImpl>("genSources".withSourceSet(sourceSet)).run()
        }

        // for remapped dependencies, disable the task (nothing to do for now)
        if (dep.group?.startsWith("remapped_") == true) {
            task.enabled = false
        }
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

val generatedOutput = layout.buildDirectory.dir("generated/resources")

tasks.compileJava {
    doLast {
        generateModMenuCompat(generatedOutput)
    }
}

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"

    val props = mapOf(
        "version" to project.version,
        "name" to "mod_name"(),
        "description" to "mod_description"(),
        "github" to "mod_github"(),
    )

    inputs.properties(props)

    filesMatching(listOf("*.json", "mcmod.info", "META-INF/*.toml")) {
        expand(props)
    }
}

val generateMixinList by tasks.registering(GenerateMixinList::class) {
    group = "build"

    outputFile = generatedOutput.map { it.dir("META-INF").file("mixinlist.json") }
}

tasks.processResources {
    dependsOn(generateMixinList)
    from(generatedOutput)
}

tasks.named<Jar>("lexforge12Jar") {
    exclude("cpw/mods/fml/**")
}

// region mergeJars and compression
val mergeJars by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier = "dev"
    from(sourceSets.main.output)
    from(sourceSets.reindev.output)

    destinationDirectory = layout.buildDirectory.dir("libs")

    from(file("LICENSE")) {
        into("META-INF")
    }

    manifest.attributes(
        "MixinConfigs" to "omnilook.mixins.json",
        "Fabric-Loom-Mixin-Remap-Type" to "static",

        // old forge stuff
        "FMLCorePlugin" to "dev.rdh.omnilook.MixinPlugin",
        "FMLCorePluginContainsFMLMod" to true,
        "ForceLoadAsMod" to true,
        "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",

        // reindev stuff
        "ModId" to "omnilook",
        "ModName" to "mod_name"(),
        "ModVersion" to project.version,
        "ModDesc" to "mod_description"(),
        "ClientMod" to "dev.rdh.omnilook.FoxlookMod",
        "ClientMixin" to "omnilook.mixins.json",
    )
}

allprojects {
    afterEvaluate {
        mergeJars.configure {
            tasks.withType<RemapJarTask>().forEach { from(zipTree(it.asJar.archiveFile)) }
        }
    }
}

val compressJar1 = tau.compression.compress<JarEntryModificationTask>(mergeJars, replaceOriginal = false) {
    group = "build"
    archiveClassifier = ""

    json(JsonShrinkingType.MINIFY) { it.endsWith(".json") || it.endsWith(".mcmeta") || it == "mcmod.info" }
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

operator fun String.invoke() = prop(this)
