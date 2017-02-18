package com.guatec.kdd

import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType


fun Method.isDirective(): Boolean = this.isAnnotationPresent(com.beust.kobalt.api.annotation.Directive::class.java)

fun Method.isExtension(): Boolean =
        this.parameterTypes.isNotEmpty() &&
                !this.parameterTypes[0].isArray &&
                this.parameterTypes.size > 1 &&
                this.parameterTypes.lastOrNull()?.canonicalName?.startsWith("kotlin.jvm.functions.Function") ?: false

fun Method.getTargetClassName(): String {
    if (isExtension()) {
        return this.parameters.first().type.canonicalName
    }
    return this.declaringClass.canonicalName
}

fun Method.toDirective(): Directive {
    if (this.isExtension()) {
        println("\t\t[extends ${this.getTargetClassName()}] ${this.returnType}")
        return Directive.build(DirectiveType.EXTENSION_METHOD, this)
    }
    return Directive.build(DirectiveType.OTHER_METHOD, this)
}