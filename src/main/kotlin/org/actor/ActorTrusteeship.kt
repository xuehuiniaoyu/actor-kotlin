package org.actor

open class ActorTrusteeship {
    private var configProxy: ((Actor) -> Unit)? = null
    private lateinit var interfaces: Array<Class<*>>
    private lateinit var target: Any

    fun from(target: Any): ActorTrusteeship {
        this.target = target
        return this
    }

    fun to(vararg interfaces: Class<*>): ActorTrusteeship {
        this.interfaces = interfaces.map { it }.toTypedArray()
        return this
    }

    fun join(target: Any, vararg interfaces: Class<*>): ActorTrusteeship {
        return from(target).to(*interfaces)
    }

    fun apply(proxy: (Actor) -> Unit): ActorTrusteeship {
        this.configProxy = proxy
        return this
    }

    open fun <T : Any> proxy(classLoader: ClassLoader? = null): T {
        return Actor.of(target).also { actor -> configProxy?.also { actor.also(it) } }.setClassLoader(classLoader).proxyBy(*interfaces)
    }
}