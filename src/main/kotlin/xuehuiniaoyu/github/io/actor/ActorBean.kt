package xuehuiniaoyu.github.io.actor

import xuehuiniaoyu.github.io.actor.field.GET
import xuehuiniaoyu.github.io.actor.field.SET
import java.lang.reflect.Proxy
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class ActorBean(private val instance: Any) {
    private val clazz = instance::class
    fun <T: Any> get(name: String): T? {
        val field = clazz.memberProperties.find { it.name == name }.let { it as KProperty1<Any, Any?> }
        return field?.get(instance) as? T
    }

    fun set(name: String, value: Any?) {
        val field = clazz.memberProperties.find { it.name == name }.let { it as KMutableProperty1<Any, Any?> }
        field?.set(instance, value)
    }

    fun <T: Any> agent(`interface`: Class<T>): T {
        return Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(`interface`)
        ) { _, method, args ->
            if (method?.annotations.isNullOrEmpty()) error("")
            when (val annotation = method!!.annotations.first()) {
                is GET -> get<Any>(annotation.name)
                is SET -> set(annotation.name, args!![0])
                else -> null
            }
        } as T
    }
}