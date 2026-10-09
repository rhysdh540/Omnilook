import com.google.gson.JsonParser
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.minecraft.patch.fabric.FabricLikePatcher
import xyz.wagyourtail.unimined.api.runs.RunConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.internal.minecraft.resolver.MinecraftDownloader
import xyz.wagyourtail.unimined.internal.minecraft.resolver.applyGameArgs
import xyz.wagyourtail.unimined.internal.minecraft.resolver.parseAllLibraries
import xyz.wagyourtail.unimined.internal.minecraft.resolver.parseArguments
import xyz.wagyourtail.unimined.util.MavenCoords
import xyz.wagyourtail.unimined.util.compareFlexVer
import xyz.wagyourtail.unimined.util.OSUtils
import xyz.wagyourtail.unimined.util.cachingDownload
import xyz.wagyourtail.unimined.util.execOps
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet
import java.net.URI
import java.io.File
import java.nio.file.Files
import java.util.jar.Manifest
import java.util.zip.ZipFile

fun Project.runProduction(
    name: String,
    modJar: Any,
    loader: String = "fabric",
    mods: List<String> = emptyList(),
    installer: String? = null,
    configure: MinecraftConfig.() -> Unit
): TaskProvider<RunConfig> {
    val sourceSet = sourceSets.create(name)
    sourceSet.java.setSrcDirs(emptyList<String>())
    sourceSet.resources.setSrcDirs(emptyList<String>())

    val modFiles = configurations.create("${name}Mods") {
        isCanBeConsumed = false
        isTransitive = false
    }
    mods.forEach { dependencies.add(modFiles.name, it) }
    val productionMods = files(modJar, modFiles)

    val action: MinecraftConfig.() -> Unit = action@{
        configure()
        val btaVanilla = if (loader == "bta") btaRuntime(production = true) else null
        side("client")
        defaultRemapJar = false
        defaultRemapSourcesJar = false
        (mcPatcher as? FabricLikePatcher)?.devMappings = null
        mappings { devNamespace("official") }
        unimined.legacyFabricMaven()
        replaceLibraryVersion("org.lwjgl.lwjgl", version = "2.9.4+legacyfabric.18")
        if (loader == "foxloader") replaceLibraryVersion("org.lwjgl", version = "2.9.4+legacyfabric.18")

        val lwjgl2 = loader in listOf("babric", "foxloader") ||
            (loader == "ornithe" && (version.startsWith("in-") || version.startsWith("inf-"))) ||
            version.compareFlexVer("1.13") < 0
        val metadata = (minecraftData as MinecraftDownloader).metadata
        val requiredJava = if (loader == "bta") JavaVersion.VERSION_17 else metadata.javaVersion
        val upgradeLwjgl3 = !lwjgl2 && requiredJava > JavaVersion.VERSION_1_8 &&
            OSUtils.oSId == "osx" && System.getProperty("os.arch") in listOf("aarch64", "arm64") &&
            metadata.libraries.any { it.name.startsWith("org.lwjgl:") && it.name.split(':')[2].compareFlexVer("3.3.0") < 0 }
        if (upgradeLwjgl3) {
            minecraftLibraries.withDependencies {
                val lwjgl = filter { it.group == "org.lwjgl" }
                removeAll(lwjgl.toSet())
                lwjgl.filter { (it as ModuleDependency).artifacts.none { artifact -> artifact.classifier?.startsWith("natives-") == true } }
                    .forEach {
                        add(project.dependencies.create("org.lwjgl:${it.name}:3.3.3"))
                        add(project.dependencies.create("org.lwjgl:${it.name}:3.3.3:natives-macos-arm64"))
                    }
            }
        }

        runs.config("client") {
            group = "runProd"
            description = "Launch Minecraft $version with the production mod jar"
            javaVersion = if (loader == "bta") JavaVersion.VERSION_17 else listOf(JavaVersion.VERSION_1_8, JavaVersion.VERSION_21, JavaVersion.VERSION_25)
                .first { it >= requiredJava }
            workingDir = project.file("run/production/$loader/$version")
            classpath = (sourceSet.runtimeClasspath - sourceSet.output - minecraft) +
                project.files(minecraftData.minecraftClientFile)
            if (btaVanilla != null) classpath += project.files(btaVanilla)
            dependsOn(productionMods)
            if (!lwjgl2) systemProperty("java.awt.headless", true)
            jvmArgs = jvmArgs.filterNot {
                it.startsWith("-Dfabric.development=") ||
                    it.startsWith("-Dfabric.remapClasspathFile=") ||
                    it.startsWith("-Dfabric.classPathGroups=") ||
                    it.startsWith("-Dloader.development=") ||
                    it.startsWith("-Dloader.remapClasspathFile=") ||
                    it.startsWith("-Dloader.classPathGroups=") ||
                    it.startsWith("-Dfoxloader.dev-mode=") ||
                    it.startsWith("-Dfoxloader.inject-mod=") ||
                    (lwjgl2 && it == "-XstartOnFirstThread") ||
                    (upgradeLwjgl3 && it.startsWith("-Djava.library.path="))
            }
            installer?.let { configureForgeProduction(this@action, it) }
            doFirst {
                check(classpath.files.all { it.isFile }) { "Production classpath contains missing files or directories" }
                check(sourceSet.output.files.none { it in classpath.files }) { "Development output is on the production classpath" }
                check(productionMods.files.all { it.isFile }) { "Production mods contain missing files or directories" }
                val modsDir = workingDir.resolve("mods")
                modsDir.mkdirs()
                val staged = productionMods.files.associateBy {
                    if (it in project.files(modJar).files) "omnilook.${if (loader == "liteloader") "litemod" else "jar"}"
                    else if (loader == "forge") "!${it.name}" else it.name
                }
                check(staged.size == productionMods.files.size) { "Production mods have duplicate filenames" }
                val manifest = workingDir.resolve(".omnilook-mods")
                if (manifest.exists()) manifest.readLines().filter { it !in staged && java.io.File(it).name == it }
                    .forEach { modsDir.resolve(it).delete() }
                staged.forEach { (filename, jar) ->
                    val target = modsDir.resolve(filename)
                    if (jar.canonicalFile != target.canonicalFile) jar.copyTo(target, overwrite = true)
                }
                manifest.writeText(staged.keys.joinToString("\n"))
                logger.lifecycle("Production $loader $version: ${staged.keys.joinToString()}")
            }
        }
    }
    if (loader == "foxloader") unimined.reIndev(sourceSet, action = action)
    else if (loader == "bta") unimined.bta(sourceSet, action = action)
    else unimined.minecraft(sourceSet, action = action)
    return tasks.named<RunConfig>("runClient".withSourceSet(sourceSet))
}

fun RunConfig.configureForgeProduction(minecraft: MinecraftConfig, notation: String) {
    val installer = project.configurations.create("${sourceSet.name}Installer") {
        isTransitive = false
        isCanBeConsumed = false
    }
    project.dependencies.add(installer.name, notation)
    doFirst {
        val coordinates = MavenCoords(notation)
        val installation = project.file(".gradle/production/${coordinates.artifact}/${coordinates.version}")
        val (profile, universalPath) = ZipFile(installer.singleFile).use { zip ->
            val json = zip.getInputStream(zip.getEntry("install_profile.json")).reader().use { JsonParser.parseReader(it).asJsonObject }
            if (json.has("versionInfo")) json.getAsJsonObject("versionInfo") to json.getAsJsonObject("install").get("filePath").asString
            else zip.getInputStream(zip.getEntry("version.json")).reader().use { JsonParser.parseReader(it).asJsonObject } to null
        }
        val id = profile.get("id").asString
        check(profile.get("inheritsFrom").asString == minecraft.version) { "$id targets a different Minecraft version than ${minecraft.version}" }
        val versionJson = installation.resolve("versions/$id/$id.json")
        if (!installation.resolve(".installed").exists()) {
            installation.mkdirs()
            installation.resolve("launcher_profiles.json").writeText("{\"profiles\":{}}")
            val vanilla = installation.resolve("versions/${minecraft.version}")
            vanilla.mkdirs()
            minecraft.minecraftData.minecraftClientFile.copyTo(vanilla.resolve("${minecraft.version}.jar"), overwrite = true)
            project.cachingDownload(minecraft.minecraftData.metadataURL).toFile()
                .copyTo(vanilla.resolve("${minecraft.version}.json"), overwrite = true)
            if (universalPath != null) {
                val libraries = parseAllLibraries(profile.getAsJsonArray("libraries")).filter { library -> library.rules.all { it.testRule() } }
                val external = libraries.filterNot { MavenCoords(it.name).group == coordinates.group && MavenCoords(it.name).artifact == coordinates.artifact }
                val resolved = project.configurations.detachedConfiguration(*external.map { project.dependencies.create(it.name) }.toTypedArray())
                    .apply { isTransitive = false }.resolvedConfiguration.resolvedArtifacts
                for (library in libraries) {
                    val dependency = MavenCoords(library.name)
                    val target = installation.resolve("libraries/${dependency.group!!.replace('.', '/')}/${dependency.artifact}/${dependency.version}/${dependency.fileName}")
                    target.parentFile.mkdirs()
                    if (dependency.group == coordinates.group && dependency.artifact == coordinates.artifact) {
                        ZipFile(installer.singleFile).use { zip -> zip.getInputStream(zip.getEntry(universalPath)).use { input ->
                            target.outputStream().use { input.copyTo(it) }
                        } }
                    } else {
                        resolved.single { it.moduleVersion.id.group == dependency.group && it.name == dependency.artifact && it.classifier == dependency.classifier }
                            .file.copyTo(target, overwrite = true)
                    }
                }
                versionJson.parentFile.mkdirs()
                versionJson.writeText(profile.toString())
            } else {
                val main = ZipFile(installer.singleFile).use { zip ->
                    zip.getInputStream(zip.getEntry("META-INF/MANIFEST.MF")).use { Manifest(it).mainAttributes.getValue("Main-Class") }
                }
                project.execOps.javaexec {
                    executable = javaLauncher.get().executablePath.asFile.absolutePath
                    classpath = installer
                    mainClass.set(main)
                    jvmArgs("-Djava.awt.headless=true")
                    args("--installClient", installation.absolutePath)
                    workingDir = installation
                }.assertNormalExitValue()
            }
            check(versionJson.isFile) { "Installer did not create $versionJson" }
        }
        val installed = versionJson.reader().use { JsonParser.parseReader(it).asJsonObject }
        val libraries = parseAllLibraries(installed.getAsJsonArray("libraries"))
            .filter { library -> library.rules.all { it.testRule() } }
        val installedFiles = libraries.map { library ->
            val path = library.downloads?.artifact?.path?.toString() ?: MavenCoords(library.name).let {
                "${it.group!!.replace('.', '/')}/${it.artifact}/${it.version}/${it.fileName}"
            }
            installation.resolve("libraries/$path")
        }
        check(installedFiles.all { it.isFile }) { "Installed production libraries are missing: ${installedFiles.filterNot { it.isFile }}" }
        installation.resolve(".installed").writeText(id)
        val replacedModules = libraries.map { MavenCoords(it.name).let { c -> Triple(c.group, c.artifact, c.classifier) } }
        val replacedFiles = minecraft.minecraftLibraries.resolvedConfiguration.resolvedArtifacts.filter {
            Triple(it.moduleVersion.id.group, it.name, it.classifier) in replacedModules
        }.map { it.file }
        classpath = project.files(installedFiles) + (classpath - project.files(replacedFiles))
        mainClass.set(installed.get("mainClass").asString)
        properties["library_directory"] = { installation.resolve("libraries").absolutePath }
        properties["version_name"] = { id }
        properties["classpath_separator"] = { java.io.File.pathSeparator }
        if (installed.has("minecraftArguments")) {
            args = applyGameArgs(installed.get("minecraftArguments").asString.split(' '),
                (minecraft.minecraftData as MinecraftDownloader).metadata.assetIndex?.id ?: "", id, "release")
        } else {
            val arguments = parseArguments(installed.getAsJsonObject("arguments"))
            args(arguments.game.filter { arg -> arg.rules.all { it.testRule() } }.flatMap { it.value })
            jvmArgs(arguments.jvm.filter { arg -> arg.rules.all { it.testRule() } }.flatMap { it.value }.map {
                if (it.startsWith("-DignoreList=")) "$it,${minecraft.minecraftData.minecraftClientFile.name}" else it
            })
        }
        check(jvmArgs.none { it.contains("deobfuscatedEnvironment=true") || it.contains("launchTarget=forgeclientuserdev") })
    }
}

fun MinecraftConfig.ornitheMetadata() {
    minecraftData.metadataURL = URI("https", "ornithemc.net", "/mc-versions/gen2/version/manifest/$version.json", null)
    val libraries = project.cachingDownload(
        URI("https", "meta.ornithemc.net", "/v3/versions/gen2/libraries/$version", null),
        cachePath = minecraftData.mcVersionFolder.resolve("ornithe-libraries.json")
    )
    Files.newBufferedReader(libraries).use { reader ->
        JsonParser.parseReader(reader).asJsonArray.forEach {
            project.dependencies.add(minecraftLibraries.name, it.asJsonObject["name"].asString)
        }
    }
}

fun MinecraftConfig.btaRuntime(production: Boolean = false): File? {
    val os = if (OSUtils.oSId == "osx") "macos" else OSUtils.oSId
    val arch = System.getProperty("os.arch")
    val suffix = when {
        arch in listOf("aarch64", "arm64") -> "-arm64"
        arch.startsWith("arm") -> "-arm32"
        os == "windows" && arch in listOf("x86", "i386", "i686") -> "-x86"
        else -> ""
    }
    listOf("lwjgl", "lwjgl-glfw", "lwjgl-openal", "lwjgl-opengl", "lwjgl-stb").forEach {
        project.dependencies.add(minecraftLibraries.name, "org.lwjgl:$it:3.3.3:natives-$os$suffix")
    }
    if (!production) listOf(
        "com.paulscode:soundsystem:20120107",
        "com.paulscode:codecjorbis:20101023",
        "com.paulscode:codecwav:20101023"
    ).forEach { project.dependencies.add(minecraftLibraries.name, it) }
    // BTA 8 bundles LWJGL 3, but its launcher manifest still lists LWJGL 2 libraries.
    minecraftLibraries.withDependencies {
        removeAll { it.group in setOf("org.lwjgl.lwjgl", "net.java.jinput", "net.java.jutils", "net.sf.jopt-simple") ||
            (it.group == "net.minecraft" && it.name == "launchwrapper") ||
            (it.group == "org.ow2.asm" && it.name == "asm-all") ||
            (production && it.group in setOf("org.jetbrains", "io.github.llamalad7")) }
    }
    return if (production) project.cachingDownload(
        URI("https://piston-data.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar"),
        sha1 = "43db9b498cb67058d2e12d394e6507722e71bb45",
        size = 1465375
    ).toFile() else null
}
