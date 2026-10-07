import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.runs.RunConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.internal.minecraft.patch.fabric.FabricLikeMinecraftTransformer
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet
import java.util.zip.ZipFile

fun Project.runProduction(
    name: String,
    modJar: Any,
    configure: MinecraftConfig.() -> Unit
): TaskProvider<RunConfig> {
    val sourceSet = sourceSets.create(name)
    sourceSet.java.setSrcDirs(emptyList<String>())
    sourceSet.resources.setSrcDirs(emptyList<String>())

    unimined.minecraft(sourceSet) {
        configure()
        side("client")
        defaultRemapJar = false
        defaultRemapSourcesJar = false
        val patcher = mcPatcher as? FabricLikeMinecraftTransformer
            ?: error("Production smoke runs currently support Fabric-like loaders")
        patcher.devMappings = null
        mappings { devNamespace("official") }

        val modFiles = project.files(modJar, project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName))
            .filter { file -> ZipFile(file).use { it.getEntry("fabric.mod.json") != null } }

        runs.config("client") {
            group = "runProd"
            description = "Launch Minecraft $version with the production mod jar"
            workingDir = project.file("run/production/$name/$version")
            classpath = (sourceSet.runtimeClasspath - sourceSet.output - minecraft) +
                project.files(minecraftData.minecraftClientFile)
            dependsOn(project.files(modJar))
            systemProperty("java.awt.headless", true)
            jvmArgs = jvmArgs!!.filterNot {
                it.startsWith("-Dfabric.development=") ||
                    it.startsWith("-Dfabric.remapClasspathFile=") ||
                    it.startsWith("-Dfabric.classPathGroups=")
            }
            doFirst {
                check(classpath.files.all { it.isFile }) { "Production classpath contains missing files or directories" }
                check(jvmArgs!!.none { it.startsWith("-Dfabric.development=") }) { "Fabric development mode is enabled" }
                systemProperty("fabric.addMods", modFiles.asPath)
                classpath -= modFiles
            }
        }
    }
    return tasks.named<RunConfig>("runClient".withSourceSet(sourceSet))
}
