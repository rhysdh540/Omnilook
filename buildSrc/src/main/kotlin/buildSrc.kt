
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

    operator fun plus(next: Mappings) = Mappings { key ->
        this.action(key)
        next.action.invoke(this, key)
    }
}

val mojmap = Mappings {
    mojmap()
    project.propn("${it}_parchment_version")?.let { version ->
        parchment(version = version)
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

//https://github.com/p0t4t0sandwich/TaterLib/blob/dd900eaf9749af07374133237de775d4c464580e/versions/v1_12_2/build.gradle.kts#L53
fun featherForge1213Fix(srcName: String) = Mappings {
    stub.withMappings("searge", "intermediary") {
        // METHODs net/minecraft/unmapped/C_9482745/[m_9076954, getMaxSpeed]()D -> getMaxSpeed
        c(
            srcName,
            listOf(
                "net/minecraft/entity/item/EntityMinecart",
                "net/minecraft/entity/vehicle/MinecartEntity"
            )
        ) {
            m("getMaxSpeed", "()D", "m_9076954", "getMaxSpeedForge")
        }
    }
}

val featherForge112Fix = featherForge1213Fix("afe")
val featherForge113Fix = featherForge1213Fix("aph")

// https://github.com/p0t4t0sandwich/TaterLib/blob/dd900eaf9749af07374133237de775d4c464580e/versions/v1_7_10/build.gradle.kts#L48
val featherForge17Fix = Mappings {
    stub.withMappings("searge", "intermediary") {
        // METHODs cpw/mods/fml/common/registry/FMLControlledNamespacedRegistry/[net/minecraft/unmapped/C_7135514/m_1782140, get](Ljava/lang/String;)Ljava/lang/Object; -> get
        c(
            "cpw/mods/fml/common/registry/FMLControlledNamespacedRegistry", listOf()
        ) {
            m("get", "(Ljava/lang/String;)Ljava/lang/Object;", "net/minecraft/unmapped/C_7135514/m_1782140", "getObjectFromString")
        }
        // METHODs cpw/mods/fml/common/registry/FMLControlledNamespacedRegistry/[net/minecraft/unmapped/C_7135514/m_9381448, get](I)Ljava/lang/Object; -> get
        c(
            "cpw/mods/fml/common/registry/FMLControlledNamespacedRegistry", listOf()
        ) {
            m("get", "(I)Ljava/lang/Object;", "net/minecraft/unmapped/C_7135514/m_9381448", "getObjectFromInteger")
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
inline operator fun String.invoke() = prop(this)

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
