package com.guatec

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import java.lang.reflect.Method

val nonRoots = mutableListOf<String>()
val extensions = mutableMapOf<Class<*>, MutableList<Method>>()

fun main(args: Array<String>) {
    val results = FastClasspathScanner().enableMethodAnnotationIndexing().scan()

    val directiveClassNames = results.getNamesOfClassesWithMethodAnnotation("com.beust.kobalt.api.annotation.Directive")
    val directiveClasses = results.classNamesToClassRefs(directiveClassNames)
    val directiveMethodsByClass = directiveClasses.flatMap{
                it.methods.filter{it.isAnnotationPresent(com.beust.kobalt.api.annotation.Directive::class.java)}
            }
            .groupBy { it.declaringClass }

    directiveClasses.forEach { directiveClass ->
        println(directiveClass.canonicalName)
        directiveMethodsByClass[directiveClass]?.forEach { method ->
                            println("\t${method.name}: ${method.returnType.canonicalName} ${method}")
                            nonRoots.add(method.returnType.canonicalName)
                            if (method.parameterTypes.isNotEmpty() &&
                                !method.parameterTypes[0].isArray &&
                                method.parameterTypes.lastOrNull()?.canonicalName == "kotlin.jvm.functions.Function1") {

                                //it's an extension function
                                nonRoots.add(directiveClass.canonicalName)
                                println("\t\t[extends ${method.parameterTypes[0].name}] ${method.parameters.lastOrNull()?.parameterizedType}")
                                extensions.getOrPut(method.parameterTypes[0], {mutableListOf<Method>()}).add(method)
                            }
                        }
            }

    println("\n-----\n")
    val root = Class.forName("com.beust.kobalt.DirectivesKt")
    directiveMethodsByClass[root]?.forEach { method ->
        println(method.name)
        directiveMethodsByClass[method.returnType]?.forEach{
            println("\t${it.name}")
        }
        extensions[method.returnType]?.forEach{
            println("\t${it.name}")
        }
    }

    println("\n-----\n")
    fun printLn(level: Int, text: String) {
        for (i in 1..level) print("\t")
        println(text)
    }

    fun formatParams(paramNames: List<String>) =
        if (!paramNames.isEmpty()) {
            " (${paramNames.joinToString(",")})"
        }
        else {
            ""
        }

    fun printDirectives(clazz: Class<*>, level: Int) {
        val directives = directiveMethodsByClass[clazz]?.sortedBy { it.name }
        val exts = extensions[clazz]?.sortedBy{ it.name }

        if (level > 5 || (directives?.isEmpty() ?: true && exts?.isEmpty() ?: true))
            return

        //printLn(level, clazz.canonicalName)
        directives?.forEach { method ->
            val paramNames = method.parameters.filterNot { it.type.canonicalName == "kotlin.jvm.functions.Function1" }
                                              .map{it.type.canonicalName}
            printLn(level, "- ${method.name}${formatParams(paramNames)}: ${method.returnType.name}")
            printDirectives(method.returnType, level + 1)
        }
        exts?.forEach { method ->
            val paramNames = method.parameters.filterNot { it.type.canonicalName == "kotlin.jvm.functions.Function1" || it.type.canonicalName == clazz.canonicalName }
                                              .map{it.type.canonicalName}
            printLn(level, "+ ${method.name}${formatParams(paramNames)}: ${method.returnType.name}")
            printDirectives(method.returnType, level + 1)
        }
    }
    printDirectives(root, 0)
}
