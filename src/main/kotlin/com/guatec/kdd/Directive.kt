package com.guatec.kdd

import java.lang.reflect.Method

class Directive(val name: String, val description: String, val returnType: Class<*>) {
    override fun toString(): String {
        return description
    }

    override fun hashCode(): Int {
        return description.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other?.hashCode()
    }

    companion object {
        fun build(type: DirectiveType, method: Method, altName: String? = null) : Directive {
            fun getName(): String = altName ?: method.name
            fun getSymbol() = when (type.isExtension()) {
                true -> "+"
                false -> "-"
            }
            fun getTypeInfo(): String {
                val paramTypes = method.parameters.filterNot { it.type.canonicalName.startsWith("kotlin.jvm.functions.Function") }
                        .map { it.type.canonicalName }
                if (type.isProp()) {
                    return ": ${method.parameterTypes[0].canonicalName}"
                }
                else if (paramTypes.isEmpty()) {
                    return ": ${method.returnType.canonicalName}"
                }
                else {
                    return " (${paramTypes.joinToString(",")}): ${method.returnType.canonicalName}"
                }
            }
            return Directive(getName(), getSymbol() + " " + getName() + getTypeInfo(), method.returnType)
        }
    }
}