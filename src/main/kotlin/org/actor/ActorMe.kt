package org.actor

open class ActorMe(vararg interfaces: Class<*>): ActorTrusteeship() {
    private val interfaces = interfaces.map { it }.toTypedArray()
    override fun <T : Any> proxy(classLoader: ClassLoader?): T {
        to(*interfaces).from(this)
        return super.proxy(classLoader)
    }
}