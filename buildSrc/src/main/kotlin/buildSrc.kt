
import Mappings.Companion.mojmap
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.api.mapping.MappingsConfig
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet

class Mappings(private val action: MappingsConfig.(key: String) -> Unit) {
    operator fun invoke(key: String): MappingsConfig.() -> Unit = {
        action(key)
    }

    companion object {
        val mojmap
            get() = Mappings {
                mojmap()
                parchment(version = project.prop("${it}_parchment_version"))
            }

        val seargeMcp
            get() = Mappings {
                searge()
                val channel = project.findProperty("${it}_mcp_channel")?.toString() ?: "snapshot"
                mcp(channel, version = project.prop("${it}_mcp_version"))
            }
    }
}

fun Project.mc(sourceSet: SourceSet, key: String = sourceSet.name.lowercase(), mappings: Mappings = mojmap, block: MinecraftConfig.() -> Unit = {}) {
    unimined.minecraft(sourceSet) {
        combineWith(rootProject.sourceSets["main"])
        version = "${key}_minecraft_version"()
        runs.config("server") { enabled = false }
        runs.all { jvmArgs("-Dmixin.debug.export=true") }

        block()

        mappings {
            mappings(key)()
        }
    }
}

fun Project.forge(sourceSet: SourceSet, key: String = sourceSet.name.lowercase(), mappings: Mappings = mojmap, block: MinecraftConfig.() -> Unit = {}) {
    mc(sourceSet, key, mappings) {
        block()

        minecraftForge {
            loader("${key}_version"())
            mixinConfig("omnilook.mixins.json")
        }
    }
}

// note that this can't be used in buildscripts and must be copied:
// operator fun String.invoke() = prop(this)
// because you can't enable context receivers in buildscripts :(
context(Project)
@Suppress("NOTHING_TO_INLINE", "CONTEXT_RECEIVERS_DEPRECATED")
inline operator fun String.invoke(): String {
    return rootProject.properties[this] as? String ?: error("Property $this not found")
}

// for when you're not in a Project context
fun Project.prop(name: String): String {
    return name()
}

val SourceSet.implementation get() = this.implementationConfigurationName
val SourceSet.compileOnly get() = this.compileOnlyConfigurationName
val SourceSet.modImplementation get() = "modImplementation".withSourceSet(this)
