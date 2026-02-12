import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet
import kotlin.reflect.KProperty

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

    class Provider internal constructor(private val action: (SourceSet) -> Unit) {
        operator fun provideDelegate(thisRef: PlatformContainer<*>, property: KProperty<*>): Delegate {
            val sourceSet = thisRef.project.sourceSets.maybeCreate(property.name)
            action(sourceSet)
            return Delegate(Platform(sourceSet))
        }
    }

    class Delegate internal constructor(private val platform: Platform) {
        operator fun getValue(thisRef: PlatformContainer<*>, property: KProperty<*>): Platform {
            if (platform.name != property.name) {
                throw IllegalStateException("Platform delegate reuse: ${platform.name} and ${property.name}")
            }
            return platform
        }
    }
}

@Suppress("UNCHECKED_CAST")
abstract class PlatformContainer<T : PlatformContainer<T>>(val project: Project) {
    operator fun invoke(action: T.() -> Unit) = (this as T).action()

    protected fun creating(mappings: Mappings = mojmap, action: MinecraftConfig.() -> Unit = {}): Platform.Provider {
        return Platform.Provider { sourceSet ->
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

    protected fun empty(action: (SourceSet) -> Unit = {}) = Platform.Provider(action)
}

