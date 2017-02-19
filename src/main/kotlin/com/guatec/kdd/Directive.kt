package com.guatec.kdd

import java.lang.reflect.Method

data class Directive(val name: String, val description: String, val returnType: Class<*>) {
    override fun toString(): String {
        return description
    }

    companion object {
        fun build(type: DirectiveType, method: Method, altName: String? = null) : Directive {
            fun getName(): String = altName ?: method.name
            fun getSymbol() = when (type.isExtension()) {
                true -> "+"
                false -> "-"
            }
            fun getTypeInfo(): String {
                val paramNames = method.parameters.filterNot { it.type.canonicalName.startsWith("kotlin.jvm.functions.Function") }
                        .map { it.type.canonicalName }
                if (type.isProp()) {
                    return ": ${method.returnType.canonicalName}"
                }
                else if (paramNames.isEmpty()) {
                    return ": ${method.returnType.canonicalName}"
                }
                else {
                    return " (${paramNames.joinToString(",")}): ${method.returnType.canonicalName}"
                }
            }
            return Directive(getName(), getSymbol() + " " + getName() + getTypeInfo(), method.returnType)
        }
    }
}