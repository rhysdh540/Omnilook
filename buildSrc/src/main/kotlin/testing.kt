/*
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.minecraft.resolver.MinecraftData
import xyz.wagyourtail.unimined.api.runs.RunConfig
import xyz.wagyourtail.unimined.internal.minecraft.MinecraftProvider
import xyz.wagyourtail.unimined.internal.minecraft.patch.AbstractMinecraftTransformer
import xyz.wagyourtail.unimined.internal.minecraft.patch.reindev.ReIndevProvider
import xyz.wagyourtail.unimined.internal.minecraft.resolver.MinecraftDownloader
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet

// VERY WIP!!!

private val MinecraftData.metadata get() = (this as MinecraftDownloader).metadata

inline fun RunConfig.minecraft(configure: MinecraftConfig.() -> Unit) = this.conf(::MinecraftProvider, configure)
inline fun RunConfig.reindev(configure: MinecraftConfig.() -> Unit) = this.conf(::ReIndevProvider, configure)

inline fun <T : MinecraftProvider> RunConfig.conf(constructor: (Project, SourceSet) -> T, configure: T.() -> Unit): Unit {
    val sourceSet = project.sourceSets.create("__internal_runProduction__${this.name}")
    sourceSet.java.destinationDirectory.set(this.temporaryDir.resolve("compileOutput"))
    sourceSet.output.setResourcesDir(this.temporaryDir.resolve("compileOutput"))
    val mc = constructor(project, sourceSet)
    mc.configure()

    mc.defaultRemapJar = false
    mc.defaultRemapSourcesJar = false
    mc.apply()
    project.tasks.named("genSources".withSourceSet(sourceSet)).configure {
        group = "runProd-internal"
    }
    project.tasks.named("exportMappings".withSourceSet(sourceSet)).configure {
        group = "runProd-internal"
    }
    (mc.mcPatcher as AbstractMinecraftTransformer).applyClientRunTransform(this)
}*/