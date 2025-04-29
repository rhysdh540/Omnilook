import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

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

}

fun constructClass(
    version: Int = V1_8,
    name: String,
    access: Int = ACC_PUBLIC + ACC_SUPER,
    superName: String = "java/lang/Object",
    interfaces: Array<String> = emptyArray(),
    action: ClassConstructor.() -> Unit
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

inline fun <reified T> typeOf(): Type {
    return Type.getType(T::class.java)
}