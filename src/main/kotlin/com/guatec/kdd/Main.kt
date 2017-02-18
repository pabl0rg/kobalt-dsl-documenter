package com.guatec.kdd

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

val directivesByClass = mutableMapOf<String, MutableSet<Directive>>()

fun main(args: Array<String>) {
    val results = FastClasspathScanner().enableMethodAnnotationIndexing().scan()
    val directiveClassNames = results.getNamesOfClassesWithMethodAnnotation("com.beust.kobalt.api.annotation.Directive")
    val directiveClasses = results.classNamesToClassRefs(directiveClassNames)

    val otherClasses = mutableSetOf<String>()

    fun getDirectives(className: String) = directivesByClass.getOrPut(className, { mutableSetOf<Directive>() })

    fun Class<*>.mightHaveDirectives(): Boolean {
        return !this.canonicalName.startsWith("java.") && !this.equals(Void.TYPE)
    }

    directiveClasses.forEach { clazz ->
        clazz.methods.forEach { method ->
            if (method.isDirective()) {
                println("\n" + method.toString() + ": " + method.getTargetClassName())
                getDirectives(method.getTargetClassName()).add(method.toDirective()!!)

                if (method.returnType.mightHaveDirectives()) {
                    println("should also look for directives in ${method.returnType.canonicalName}")
                    otherClasses.add(method.returnType.canonicalName)
                }
            }
            else if (method.name.startsWith("set")) {
                println("\nprop: ${clazz.canonicalName} ${method.name.drop(3).decapitalize()}")
                getDirectives(clazz.canonicalName).add(Directive.build(DirectiveType.OTHER_PROP, method, method.name.drop(3).decapitalize()))
            }
        }
    }

    otherClasses.forEach(::println)
    otherClasses.forEach { clazzName ->
        try {
            val clazz = Class.forName(clazzName)
            clazz.methods.forEach { method ->
                if (method.isDirective()) {
                    println("\n" + method.toString() + ": " + method.getTargetClassName())
                    getDirectives(method.getTargetClassName()).add(method.toDirective()!!)

                    if (method.returnType.mightHaveDirectives()) {
                        println("should also look for directives in ${method.returnType.canonicalName}")
                        otherClasses.add(method.returnType.canonicalName)
                    }
                }
                else if (method.name.startsWith("set")) {
                    println("\nprop: ${clazz.canonicalName} ${method.name.drop(3).decapitalize()}")
                    getDirectives(clazz.canonicalName).add(Directive.build(DirectiveType.OTHER_PROP, method, method.name.drop(3).decapitalize()))
                }
            }
        }
        catch (ex: Exception) { println (ex.message)}
    }

    println(directivesByClass)

    println("\n-----\n")

    fun printLn(level: Int, text: String) {
        for (i in 1..level) print("\t")
        println(text)
    }

    val deDupe = mutableSetOf<String>()

    fun printDirectives(className: String, level: Int) {
        if (deDupe.contains(className)) {
            return
        } else {
            deDupe.add(className)
        }

        val directives = directivesByClass[className]?.sortedBy { it.name }

        if (level > 6 || directives?.isEmpty() ?: true )
            return

        //printLn(level, className)
        directives?.forEach { directive ->
            printLn(level, directive.toString())
            if (directive.returnType.mightHaveDirectives())
                printDirectives(directive.returnType.canonicalName, level + 1)
        }
    }
    printDirectives("com.beust.kobalt.DirectivesKt", 0)
}
