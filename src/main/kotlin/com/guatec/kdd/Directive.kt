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

            fun Class<*>.shortName() = canonicalName.replace(Regex("java\\..*\\."), "")

            fun getTypeInfo(): String {
                val paramTypes = method.parameters.filterNot { it.type.canonicalName.startsWith("kotlin.jvm.functions.Function") }
                        .map { it.type.shortName() }
                var returnType = method.returnType.shortName()

                //parameter types (eg: E) are useless
                //if (method.returnType.typeParameters.isNotEmpty()) {
                //    returnType += "<${method.returnType.typeParameters.map{ it.name }.joinToString(", ")}>"
                //}

                if (type.isProp()) {
                    return ": $returnType"
                }
                else if (paramTypes.isEmpty()) {
                    return ": $returnType"
                }
                else {
                    return " (${paramTypes.joinToString(",")}): $returnType"
                }
            }

            return Directive(getName(), getSymbol() + " " + getName() + getTypeInfo(), method.returnType)
        }
    }
}