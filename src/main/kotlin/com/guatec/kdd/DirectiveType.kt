package com.guatec.kdd

interface DirectiveType {
    fun isExtension(): Boolean
    fun isProp(): Boolean

    companion object {
        val OTHER_METHOD = object : DirectiveType {
            override fun isExtension() = false
            override fun isProp() = false
        }

        val OTHER_PROP = object : DirectiveType {
            override fun isExtension() = false
            override fun isProp() = true
        }

        val EXTENSION_METHOD = object : DirectiveType {
            override fun isExtension() = true
            override fun isProp() = false
        }

        val EXTENSION_PROP = object : DirectiveType {
            override fun isExtension() = true
            override fun isProp() = true
        }
    }
}