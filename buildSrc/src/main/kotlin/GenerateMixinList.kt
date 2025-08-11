import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.defaultedMapOf
import xyz.wagyourtail.unimined.util.sourceSets

abstract class GenerateMixinList : DefaultTask() {
    init {
        notCompatibleWithConfigurationCache("gradle convention requires putting thought into your code")
    }

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val mixinList = defaultedMapOf<String, MutableList<String>> { mutableListOf() }

        @Suppress("UnstableApiUsage")
        for (sourceSet in project.unimined.minecrafts.keys) {
            for (root in sourceSet.java.srcDirs) {
                val mixinDir = root.resolve("dev/rdh/omnilook/mixin/${sourceSet.name}")
                if (!mixinDir.exists()) {
                    continue
                }

                for (file in mixinDir.listFiles()) {
                    assert(file.extension == "java") { "File ${file.name} is not a java file" }
                    val contents = file.readText()
                    assert(contents.contains("@Mixin")) { "File ${file.name} does not contain @Mixin" }
                    val className = file.nameWithoutExtension
                    mixinList[sourceSet.name].add(className)
                }
            }
        }


        outputFile.get().asFile.let {
            it.deleteRecursively()
            it.parentFile.mkdirs()
            it.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mixinList)))
        }
    }
}