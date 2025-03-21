import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

plugins {
    id("java")
    id("xyz.wagyourtail.unimined") version("1.3.13")
}

group = "dev.rdh"
version = "0.1"
base.archivesName = project.name.lowercase()

setupSourceSets()

val ap: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
    compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
    compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")


    ap("systems.manifold:manifold-exceptions:${"manifold_version"()}")
    ap("systems.manifold:manifold-rt:${"manifold_version"()}")

    "modernImplementation"(fabricApi.fabricModule("fabric-key-binding-api-v1", "modern_fabricapi_version"()))
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

val mergeJars by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("")
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

tasks.withType<RemapJarTask> {
    mixinRemap {
        disableRefmap()
    }
}

tasks.assemble {
    dependsOn(mergeJars)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

// region unimined
unimined.minecraft(sourceSets.neoforge) {
    combineWith(sourceSets.main)
    version = "neoforge_minecraft_version"()
    neoForge { loader("neoforge_version"()) }

    runs.config("server") { enabled = false }
    runs.all { jvmArgs("-Dmixin.debug.export=true") }

    mappings {
        mojmap()
        parchment(version = "neoforge_parchment_version"())
    }
}

unimined.minecraft(sourceSets.modern) {
    combineWith(sourceSets.main)
    version = "modern_minecraft_version"()
    fabric { loader("modern_fabricloader_version"()) }

    runs.config("server") { enabled = false }
    runs.all { jvmArgs("-Dmixin.debug.export=true") }

    mappings {
        mojmap()
        parchment(version = "modern_parchment_version"())
    }

    mods.remap(configurations.getByName("modernImplementation"))
}

unimined.minecraft(sourceSets.lexforge) {
    combineWith(sourceSets.main)
    version = "lexforge_minecraft_version"()
    minecraftForge {
        loader("lexforge_version"())
        mixinConfig("omnilook.mixins.json")
    }

    runs.config("server") { enabled = false }
    runs.all { jvmArgs("-Dmixin.debug.export=true") }

    mappings {
        mojmap()
        parchment(version = "lexforge_parchment_version"())
    }
}
// endregion

// region funny kotlin stuff
val SourceSetContainer.main get() = getByName("main")
val SourceSetContainer.neoforge get() = maybeCreate("neoforge")
val SourceSetContainer.modern get() = maybeCreate("modern")
val SourceSetContainer.lexforge get() = maybeCreate("lexforge")

// makes sure the source sets are created
fun setupSourceSets() {
    sourceSets.neoforge
    sourceSets.modern
    sourceSets.lexforge
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")
// endregion
