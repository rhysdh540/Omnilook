import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

interface ClassConstructor {
    fun field(
        name: String,
        type: Type,
        access: Int = ACC_PUBLIC,
        signature: String? = null,
        value: Any? = null,
        action: FieldConstructor.() -> Unit = {}
    )

    fun method(
        name: String,
        access: Int,
        returnType: Type,
        signature: String? = null,
        exceptions: Array<String> = emptyArray(),
        vararg parameterTypes: Type,
        action: MethodConstructor.() -> Unit
    )

    fun constructor(
        access: Int = ACC_PUBLIC,
        signature: String? = null,
        vararg parameterTypes: Type,
        action: MethodConstructor.() -> Unit
    ) = method(
        "<init>",
        access,
        Type.VOID_TYPE,
        signature,
        emptyArray(),
        *parameterTypes,
        action = action
    )
}

class FieldConstructor(parent: FieldVisitor) : FieldVisitor(ASM9, parent) {

}

class MethodConstructor(parent: MethodVisitor) : MethodVisitor(ASM9, parent) {
    inline fun <reified OType : Any, reified RType : Any> callMethod(opcode: Int, name: String, vararg parameterTypes: Type, isInterface: Boolean = false) =
        visitMethodInsn(
            opcode,
            typeOf<OType>().internalName,
            name,
            Type.getMethodDescriptor(typeOf<RType>(), *parameterTypes),
            isInterface
        )

    fun ldc(value: Any) = visitLdcInsn(value)
}

fun constructClass(
    version: Int = V1_8,
    name: String,
    access: Int = ACC_PUBLIC + ACC_SUPER,
    superName: String = "java/lang/Object",
    interfaces: Array<String> = emptyArray(),
    action: ClassConstructor.() -> Unit = {}
): ByteArray {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS)
    cw.visit(
        version,
        access,
        name,
        null,
        superName,
        interfaces
    )

    val impl = object : ClassConstructor {
        override fun field(name: String, type: Type, access: Int, signature: String?, value: Any?, action2: FieldConstructor.() -> Unit) {
            val fv = cw.visitField(
                access,
                name,
                type.descriptor,
                signature,
                value
            )

            FieldConstructor(fv).action2()
        }

        override fun method(
            name: String,
            access: Int,
            returnType: Type,
            signature: String?,
            exceptions: Array<String>,
            vararg parameterTypes: Type,
            action2: MethodConstructor.() -> Unit
        ) {
            cw.visitMethod(
                access,
                name,
                Type.getMethodDescriptor(returnType, *parameterTypes),
                signature,
                exceptions
            ).apply {
                visitCode()
                MethodConstructor(this).action2()
                visitMaxs(0, 0)
                visitEnd()
            }
        }
    }

    impl.action()
    return cw.toByteArray()
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified F : Any> methodTypeOf(): Type {
    val t = kotlin.reflect.typeOf<F>()

    require((t.classifier as? KClass<*>)?.isSubclassOf(Function::class) == true) {
        "Type $t is not a function type"
    }

    val args = t.arguments
    require(args.isNotEmpty()) { "Function type has no return type? $t" }

    val returnType = args.last().type?.asmType ?: error("Unknown return type for $t")

    return Type.getMethodType(returnType, *args.dropLast(1)
        .map { it.type?.asmType ?: error("Unknown param type for $t") }
        .toTypedArray())
}

val KType.asmType: Type
    get() = getType(this.classifier as? KClass<*> ?: error("KType must have a classifier"))

inline fun <reified T : Any> typeOf(): Type {
    return getType(T::class)
}

fun <T : Any> getType(clazz: KClass<T>): Type = when (clazz) {
    String::class -> Type.getType(java.lang.String::class.java)
    Unit::class -> Type.VOID_TYPE
    Nothing::class -> Type.VOID_TYPE
    else -> Type.getType(clazz.javaPrimitiveType ?: clazz.java)
}