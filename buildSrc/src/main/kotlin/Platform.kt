import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.capitalized
import xyz.wagyourtail.unimined.util.sourceSets
import xyz.wagyourtail.unimined.util.withSourceSet
import kotlin.reflect.KProperty

class Platform internal constructor(
    private val project: Project,
    val sourceSet: SourceSet,
    val includeInOutput: Boolean
) {
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

    val supportsJarInJar by lazy {
        project.configurations.findByName("include".withSourceSet(sourceSet)) != null
    }

    val outputJar: Jar by lazy {
        (project.tasks.findByName("remap${name.capitalized()}Jar") ?: project.tasks.findByName(sourceSet.jarTaskName))
            as? Jar ?: error("No remap${name.capitalized()}Jar or ${sourceSet.jarTaskName} task found for platform ${name}")
    }

    class Provider internal constructor(
        private val include: Boolean = true,
        private val callback: (String, Platform) -> Unit,
        private val action: (SourceSet) -> Unit
    ) {
        operator fun provideDelegate(thisRef: PlatformContainer<*>, property: KProperty<*>): Delegate {
            val sourceSet = thisRef.project.sourceSets.maybeCreate(property.name)
            action(sourceSet)
            val platform = Platform(thisRef.project, sourceSet, include)
            callback(platform.name, platform)
            return Delegate(platform)
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
abstract class PlatformContainer<T : PlatformContainer<T>>(val project: Project): Iterable<Platform> {
    private val platforms = mutableMapOf<String, Platform>()

    override fun iterator(): Iterator<Platform> = platforms.values.iterator()

    operator fun invoke(action: T.() -> Unit) = (this as T).action()

    protected fun creating(mappings: Mappings = mojmap, include: Boolean = true, action: MinecraftConfig.() -> Unit = {}): Platform.Provider {
        return Platform.Provider(include, platforms::put) { sourceSet ->
            project.unimined.minecraft(sourceSet) {
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

    protected fun forge(mappings: Mappings = mojmap, include: Boolean = true, action: MinecraftConfig.() -> Unit = {}) = creating(mappings, include) {
        action()

        minecraftForge {
            loader(project.prop("${sourceSet.name}_version"))
            mixinConfig("omnilook.mixins.json")
        }
    }

    protected fun empty(include: Boolean = true, action: (SourceSet) -> Unit = {}) = Platform.Provider(include, platforms::put, action)
}

