import org.gradle.api.file.DirectoryProperty
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.lang.invoke.CallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

// I wish there was a better way to generate classes than like this
fun generateModMenuCompat(buildDir: DirectoryProperty) {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cw.visit(
        V1_8,
        ACC_PUBLIC + ACC_SUPER,
        "dev/rdh/omnilook/config/ModMenuCompat",
        null,
        "java/lang/Object",
        arrayOf("io/github/prospector/modmenu/api/ModMenuApi")
    )

    // constructor
    cw.visitMethod(
        ACC_PUBLIC,
        "<init>",
        "()V",
        null,
        null
    ).apply {
        visitCode()
        visitVarInsn(ALOAD, 0)
        visitMethodInsn(
            INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitInsn(RETURN)
        visitMaxs(1, 1)
        visitEnd()
    }

    // lambda
    cw.visitMethod(
        ACC_STATIC + ACC_PUBLIC + ACC_SYNTHETIC,
        "lambda",
        "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;",
        null,
        null
    ).apply {
        visitCode()
        val notEqual = Label()
        visitVarInsn(ALOAD, 1)
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;", false)
        visitLdcInsn("create")
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false)
        visitJumpInsn(IFEQ, notEqual)
        // equal
        visitVarInsn(ALOAD, 2)
        visitInsn(ICONST_0)
        visitInsn(AALOAD)
        visitMethodInsn(INVOKESTATIC, "dev/rdh/omnilook/config/ModMenuScreenProvider", "getScreen", "(Ljava/lang/Object;)Ljava/lang/Object;", true)
        visitInsn(ARETURN)

        // not equal
        visitLabel(notEqual)
        visitVarInsn(ALOAD, 1)
        visitVarInsn(ALOAD, 0)
        visitVarInsn(ALOAD, 2)
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false)
        visitInsn(ARETURN)

        visitMaxs(3, 3)
        visitEnd()
    }

    fun proxyShenanigans(csfType: Type): MethodVisitor.() -> Unit = {
        visitCode()
        visitVarInsn(ALOAD, 0)
        // getClass().getClassLoader()
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)

        // new Class[] { ConfigScreenFactory.class }
        visitInsn(ICONST_1)
        visitTypeInsn(ANEWARRAY, "java/lang/Class")
        visitInsn(DUP)
        visitInsn(ICONST_0) // index 0
        visitLdcInsn(csfType)
        visitInsn(AASTORE)

        // indy InvocationHandler
        visitInvokeDynamicInsn(
            "invoke",
            Type.getMethodDescriptor(
                Type.getType("Ljava/lang/reflect/InvocationHandler;")
            ),
            Handle(
                H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory", "metafactory",
                Type.getMethodDescriptor(
                    Type.getType(CallSite::class.java),
                    Type.getType(MethodHandles.Lookup::class.java),
                    Type.getType(String::class.java),
                    Type.getType(MethodType::class.java),

                    Type.getType(MethodType::class.java),
                    Type.getType(MethodHandle::class.java),
                    Type.getType(MethodType::class.java),
                ),
                false
            ),
            Type.getMethodType(
                Type.getType(Object::class.java),
                Type.getType(Object::class.java),
                Type.getType(Method::class.java),
                Type.getType("[Ljava/lang/Object;")
            ),
            Handle(
                H_INVOKESTATIC,
                "dev/rdh/omnilook/config/ModMenuCompat", "lambda",
                Type.getMethodDescriptor(
                    Type.getType(Object::class.java),
                    Type.getType(Object::class.java),
                    Type.getType(Method::class.java),
                    Type.getType("[Ljava/lang/Object;")
                ),
                false
            ),
            Type.getMethodType(
                Type.getType(Object::class.java),
                Type.getType(Object::class.java),
                Type.getType(Method::class.java),
                Type.getType("[Ljava/lang/Object;")
            )
        )
        visitMethodInsn(INVOKESTATIC,
            "java/lang/reflect/Proxy",
            "newProxyInstance",
            Type.getMethodDescriptor(
                Type.getType(Object::class.java),
                Type.getType(ClassLoader::class.java),
                Type.getType("[Ljava/lang/Class;"),
                Type.getType(InvocationHandler::class.java)
            ),
            false
        )
        visitTypeInsn(CHECKCAST, csfType.internalName)
        visitInsn(ARETURN)
        visitMaxs(1, 2)
        visitEnd()
    }

    // com.terraformersmc.modmenu.api.ConfigScreenFactory getModConfigScreenFactory()
    cw.visitMethod(
        ACC_PUBLIC,
        "getModConfigScreenFactory",
        "()Lcom/terraformersmc/modmenu/api/ConfigScreenFactory;",
        null,
        null
    ).apply(proxyShenanigans(Type.getType("Lcom/terraformersmc/modmenu/api/ConfigScreenFactory;")))

    // io.github.prospector.modmenu.api.ConfigScreenFactory getModConfigScreenFactory()
    cw.visitMethod(
        ACC_PUBLIC,
        "getModConfigScreenFactory",
        "()Lio/github/prospector/modmenu/api/ConfigScreenFactory;",
        null,
        null
    ).apply(proxyShenanigans(Type.getType("Lio/github/prospector/modmenu/api/ConfigScreenFactory;")))

    // java.lang.Function getConfigScreenFactory()
    cw.visitMethod(
        ACC_PUBLIC,
        "getConfigScreenFactory",
        "()Ljava/util/function/Function;",
        null,
        null
    ).apply {
        visitCode()
        visitInvokeDynamicInsn(
            "apply",
            "()Ljava/util/function/Function;",
            Handle(
                H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                false
            ),
            Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
            Handle(H_INVOKESTATIC,
                "dev/rdh/omnilook/config/ModMenuScreenProvider", "getScreen",
                "(Ljava/lang/Object;)Ljava/lang/Object;", true
            ),
            Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;")
        )
        visitInsn(ARETURN)
        visitMaxs(1, 2)
        visitEnd()
    }

    // String getModId()
    cw.visitMethod(
        ACC_PUBLIC,
        "getModId",
        "()Ljava/lang/String;",
        null,
        null
    ).apply {
        visitCode()
        visitFieldInsn(GETSTATIC,
            "dev/rdh/omnilook/Omnilook",
            "ID",
            "Ljava/lang/String;"
        )
        visitInsn(ARETURN)
        visitMaxs(1, 1)
        visitEnd()
    }

    // output
    val output = buildDir.get().asFile.resolve("dev/rdh/omnilook/config/ModMenuCompat.class")
    output.parentFile.mkdirs()
    output.writeBytes(cw.toByteArray())
}