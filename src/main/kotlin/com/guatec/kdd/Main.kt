package com.guatec.kdd

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

val directivesByClass = mutableMapOf<String, MutableSet<Directive>>()

fun main(args: Array<String>) {
    val results = FastClasspathScanner().enableMethodAnnotationIndexing().scan()
    val directiveClassNames = results.getNamesOfClassesWithMethodAnnotation("com.beust.kobalt.api.annotation.Directive")
    val directiveClasses = results.classNamesToClassRefs(directiveClassNames)

    val otherClasses = mutableSetOf<Class<*>>()

    fun getDirectives(className: String) = directivesByClass.getOrPut(className, { mutableSetOf<Directive>() })

    fun Class<*>.mightHaveDirectives(): Boolean {
        return !this.canonicalName.startsWith("java.") && !this.equals(Void.TYPE)
    }

    fun findDirectives(clazz: Class<*>) {
        println("\n$clazz")
        try {
            clazz.kotlin.memberProperties.forEach { prop ->
                val isVar = prop is KMutableProperty<*>
                println(prop.toString() + " isVar: $isVar")
                if (prop.annotations.isNotEmpty() || isVar) {
                    prop.annotations.forEach { println(it.annotationClass.simpleName) }
                    val getter = prop.javaGetter!!
                    getDirectives(clazz.canonicalName).add(Directive.build(DirectiveType.OTHER_PROP, getter, prop.name))
                }
            }
        }
        catch (ex: Exception) {
            System.err.println("failed to get props of ${clazz}")
            ex.printStackTrace()
        }
        clazz.methods.forEach { method ->
            if (method.isDirective()) {
                println(method.toString() + ": " + method.getTargetClassName())
                getDirectives(method.getTargetClassName()).add(method.toDirective()!!)

                if (method.returnType.mightHaveDirectives() && !directiveClasses.contains(method.returnType)) {
                    println("should also look for directives in ${method.returnType.canonicalName}")
                    otherClasses.add(method.returnType)
                }
            }
        }
    }

    directiveClasses.forEach(::findDirectives)
    otherClasses.forEach(::findDirectives)

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
