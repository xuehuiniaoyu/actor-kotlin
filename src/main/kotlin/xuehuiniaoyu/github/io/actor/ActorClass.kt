package xuehuiniaoyu.github.io.actor

import xuehuiniaoyu.github.io.actor.field.GET
import xuehuiniaoyu.github.io.actor.field.SET
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

class ActorClass(private val clazz: KClass<*>) {
    fun <T: Any> get(name: String): T? {
        return try {
            clazz.java.getDeclaredField(name).let {
                it.isAccessible = true
                it.get(clazz) as? T
            }
        } catch (e: NoSuchFieldException) {
            var field = clazz.companionObject?.declaredMemberProperties?.find { it.name == name }
                ?.let { it as KProperty1<Any, Any?> }
                ?: throw e
            field.get(clazz.companionObjectInstance!!) as? T
        }
    }

    fun set(name: String, value: Any?) {
        try {
            clazz.java.getDeclaredField(name).let {
                it.isAccessible = true
                it.set(clazz, value)
            }
        } catch (e: NoSuchFieldException) {
            val field = clazz.companionObject?.declaredMemberProperties?.find { it.name == name }
                ?.let { it as KMutableProperty1<Any, Any?> }
                ?: throw e
            field.set(clazz.companionObjectInstance!!, value)
        }
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