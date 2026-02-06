
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.api.mapping.MappingsConfig
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet

class Mappings(private val action: MappingsConfig<*>.(key: String) -> Unit) {
    operator fun invoke(key: String): MappingsConfig<*>.() -> Unit = {
        action(key)
    }

    operator fun plus(next: Mappings) = Mappings { key ->
        this.action(key)
        next.action.invoke(this, key)
    }
}

val mojmap = Mappings {
    if (!minecraft.obfuscated) {
        return@Mappings
    }
    mojmap()
    project.propn("${it}_parchment_version")?.let { version ->
        parchment(version = version)
    }

    if (minecraft.version == "1.21.11") {
        // unimined bug
        devNamespace("mojmap")
    }
}

val searge = Mappings { searge() }

val mcp = Mappings {
    val channel = project.propn("${it}_mcp_channel") ?: "snapshot"
    mcp(channel, version = project.prop("${it}_mcp_version"))
}

val feather = Mappings {
    calamus()
    val version = project.propn("${it}_feather_version") ?: project.prop("feather_version")
    feather(version)
}

val featherForge112Fix = Mappings {
    stubs("searge", "calamus") {
        c("net/minecraft/entity/item/EntityMinecart") {
            m("getMaxSpeed;()D", "getMaxSpeedForge")
        }
    }
}

val featherForge17Fix = Mappings {
    stubs("searge", "feather") {
        val cls = "cpw/mods/fml/common/registry/FMLControlledNamespacedRegistry"
        c(cls, cls) {
            m("get;(I)Ljava/lang/Object;", "getControlled")
            m("get;(Ljava/lang/String;)Ljava/lang/Object;", "getControlled")
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
// because you can't enable context parameters in buildscripts :(
context(p: Project)
@Suppress("NOTHING_TO_INLINE")
inline operator fun String.invoke() = p.prop(this)

// for when you're not in a Project context
fun Project.prop(name: String): String {
    return propn(name) ?: error("Property $name not found")
}

fun Project.propn(name: String): String? {
    return rootProject.properties[name] as? String
}

val SourceSet.implementation get() = this.implementationConfigurationName
val SourceSet.compileOnly get() = this.compileOnlyConfigurationName
val SourceSet.modImplementation get() = "modImplementation".withSourceSet(this)
