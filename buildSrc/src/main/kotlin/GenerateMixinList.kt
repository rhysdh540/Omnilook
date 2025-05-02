import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.util.sourceSets

abstract class GenerateMixinList : DefaultTask() {
    init {
        notCompatibleWithConfigurationCache("gradle convention requires putting thought into your code")
    }

    @TaskAction
    fun generateMixinList() {
        val mixinList = mutableMapOf<String, MutableList<String>>()

        project.sourceSets.forEach { sourceSet ->
            sourceSet.java.srcDirs.forEach a@{ root ->
                val mixinDir = root.resolve("dev/rdh/omnilook/mixin/${sourceSet.name}")
                if (!mixinDir.exists()) {
                    return@a
                }

                mixinDir.listFiles().forEach { file ->
                    assert(file.extension == "java") { "File ${file.name} is not a java file" }
                    val contents = file.readText()
                    assert(contents.contains("@Mixin")) { "File ${file.name} does not contain @Mixin" }
                    val className = file.nameWithoutExtension
                    mixinList.computeIfAbsent(sourceSet.name) { mutableListOf() }.add(className)
                }
            }
        }

        val outputFile = project.sourceSets["main"].resources.srcDirs.first().resolve("META-INF/mixinlist.json")

        outputFile.let {
            it.deleteRecursively()
            it.parentFile.mkdirs()
            it.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mixinList)))
        }
    }
}