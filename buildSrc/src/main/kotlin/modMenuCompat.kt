@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.lang.invoke.CallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

// I wish there was a better way to generate classes than like this
fun generateModMenuCompat(buildDir: Provider<Directory>) = constructClass(
    access = ACC_PUBLIC + ACC_SUPER,
    name = "dev/rdh/omnilook/config/ModMenuCompat",
    interfaces = arrayOf("io/github/prospector/modmenu/api/ModMenuApi"),
) {
    constructor {
        visitVarInsn(ALOAD, 0)
        visitMethodInsn(
            INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitInsn(RETURN)
    }

    // lambda
    method(
        name = "lambda",
        access = ACC_STATIC + ACC_PUBLIC + ACC_SYNTHETIC,
        returnType = typeOf<Object>(),
        parameterTypes = arrayOf(
            typeOf<Object>(),
            typeOf<Method>(),
            typeOf<Array<Object>>()
        )
    ) {
        visitVarInsn(ALOAD, 1)
        callMethod<Method, String>(INVOKEVIRTUAL, "getName")
        ldc("create")
        callMethod<String, Boolean>(INVOKEVIRTUAL, "equals", typeOf<Object>())
        val notEqual = Label()
        visitJumpInsn(IFEQ, notEqual)
        // equal
        visitVarInsn(ALOAD, 2)
        visitInsn(ICONST_0)
        visitInsn(AALOAD)
        visitMethodInsn(
            INVOKESTATIC,
            "dev/rdh/omnilook/config/ModMenuScreenProvider",
            "getScreen",
            methodTypeOf<(Object) -> Object>().descriptor,
            true
        )
        visitInsn(ARETURN)

        // not equal
        visitLabel(notEqual)
        visitVarInsn(ALOAD, 1)
        visitVarInsn(ALOAD, 0)
        visitVarInsn(ALOAD, 2)
        callMethod<Method, Object>(INVOKEVIRTUAL, "invoke", typeOf<Object>(), typeOf<Array<Object>>())
        visitInsn(ARETURN)
    }

    // xxx.modmenu.api.ConfigScreenFactory getModConfigScreenFactory()
    for (csfName in arrayOf("com.terraformersmc", "io.github.prospector")) {
        val csfType = Type.getType("L${csfName.replace('.', '/')}/modmenu/api/ConfigScreenFactory;")
        method(
            name = "getModConfigScreenFactory",
            access = ACC_PUBLIC,
            returnType = csfType,
        ) {
            visitVarInsn(ALOAD, 0) // this
            // getClass().getClassLoader()
            callMethod<Object, Class<*>>(INVOKEVIRTUAL, "getClass")
            callMethod<Class<*>, ClassLoader>(INVOKEVIRTUAL, "getClassLoader")

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
                methodTypeOf<() -> InvocationHandler>().descriptor,
                Handle(
                    H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory", "metafactory",
                    methodTypeOf<(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType) -> CallSite>().descriptor,
                    false
                ),
                methodTypeOf<(Object, Method, Array<Object>) -> Object>(),
                Handle(
                    H_INVOKESTATIC,
                    "dev/rdh/omnilook/config/ModMenuCompat", "lambda",
                    methodTypeOf<(Object, Method, Array<Object>) -> Object>().descriptor,
                    false
                ),
                methodTypeOf<(Object, Method, Array<Object>) -> Object>().descriptor
            )
            callMethod<Proxy, Object>(INVOKESTATIC, "newProxyInstance",
                typeOf<ClassLoader>(),
                typeOf<Array<Class<*>>>(),
                typeOf<InvocationHandler>()
            )
            visitTypeInsn(CHECKCAST, csfType.internalName)
            visitInsn(ARETURN)
        }
    }

    // java.util.function.Function getConfigScreenFactory()
    method(
        name = "getConfigScreenFactory",
        access = ACC_PUBLIC,
        returnType = typeOf<java.util.function.Function<Any, Any>>(),
    ) {
        visitInvokeDynamicInsn(
            "apply",
            Type.getMethodDescriptor(
                typeOf<java.util.function.Function<Any, Any>>(),
            ),
            Handle(
                H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory", "metafactory",
                methodTypeOf<(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType) -> CallSite>().descriptor,
                false
            ),
            Type.getMethodType(typeOf<Object>(), typeOf<Object>()),
            Handle(
                H_INVOKESTATIC,
                "dev/rdh/omnilook/config/ModMenuScreenProvider", "getScreen",
                methodTypeOf<(Object) -> Object>().descriptor,
                true
            ),
            methodTypeOf<(Object) -> Object>().descriptor
        )
        visitInsn(ARETURN)
    }

    // String getModId()
    method(
        name = "getModId",
        access = ACC_PUBLIC,
        returnType = typeOf<String>(),
        signature = "()Ljava/lang/String;"
    ) {
        visitFieldInsn(
            GETSTATIC,
            "dev/rdh/omnilook/Omnilook",
            "ID",
            "Ljava/lang/String;"
        )
        visitInsn(ARETURN)
    }
}.let {
    val output = buildDir.get().asFile.resolve("dev/rdh/omnilook/config/ModMenuCompat.class")
    output.parentFile.mkdirs()
    output.writeBytes(it)
}

