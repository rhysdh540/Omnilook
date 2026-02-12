import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

class Platform internal constructor(val sourceSet: SourceSet) {
    val name: String
        get() = sourceSet.name

    val implementation: String
        get() = sourceSet.implementationConfigurationName
    val compileOnly: String
        get() = sourceSet.compileOnlyConfigurationName
    val modImplementation: String
        get() = "modImplementation".withSourceSet(sourceSet)

    val dep: Any
        get() = sourceSet.output
}

@Suppress("UNCHECKED_CAST")
abstract class PlatformContainer<T : PlatformContainer<T>>(val project: Project) {
    operator fun invoke(action: T.() -> Unit) = (this as T).action()

    fun init(): T {
        this::class.memberProperties.forEach {
            it.getter.call(this)
        }
        return this as T
    }

    protected fun creating(mappings: Mappings = mojmap, action: MinecraftConfig.() -> Unit = {}): ReadOnlyProperty<PlatformContainer<*>, Platform> {
        return PlatformHolder { sourceSet ->
            project.unimined.minecraft(sourceSet) {
                combineWith(project.rootProject.sourceSets["main"])
                version = project.prop("${sourceSet.name}_minecraft_version")
                runs.config("server") { enabled = false }
                runs.all { jvmArgs("-Dmixin.debug.export=true") }
                action()

                mappings {
                    mappings(sourceSet.name)
                }
            }
        }
    }

    protected fun forge(mappings: Mappings = mojmap, action: MinecraftConfig.() -> Unit = {}) = creating(mappings) {
        action()

        minecraftForge {
            loader(project.prop("${sourceSet.name}_version"))
            mixinConfig("omnilook.mixins.json")
        }
    }

    protected fun empty(action: (SourceSet) -> Unit = {}): ReadOnlyProperty<PlatformContainer<*>, Platform> {
        return PlatformHolder(action)
    }
}

private class PlatformHolder(val action: (SourceSet) -> Unit) : ReadOnlyProperty<PlatformContainer<*>, Platform> {
    private var cached: Platform? = null

    override fun getValue(thisRef: PlatformContainer<*>, property: KProperty<*>): Platform {
        if (cached != null) {
            if (cached!!.name != property.name) {
                throw IllegalStateException("PlatformHolder reuse detected: ${cached!!.name} vs ${property.name}")
            }
            return cached!!
        }

        val sourceSet = thisRef.project.sourceSets.maybeCreate(property.name)
        action(sourceSet)

        return Platform(sourceSet).also { cached = it }
    }
}
