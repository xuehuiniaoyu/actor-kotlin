package xuehuiniaoyu.github.io.actor

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSupertypeOf

/**
 * @author Tang
 *
 * Like an actor,
 *
 * this class provides a simulation method as a actor of a real object
 * The concrete method of the real object is simulated through the interface method,
 *
 * and then
 *
 * the control of the real object is realized through the dynamic agent.
 *
 */
class Actor constructor(private val base: Any) {
    private var mClassLoader: ClassLoader? = null
    fun setClassLoader(classLoader: ClassLoader?): Actor {
        this.mClassLoader = classLoader
        return this
    }

    /**
     *
     * According to the interface, realizing the proxy of the real object,
     * Interface must contain methods of real objects
     */
    fun <T : Any> imitate(proxyInterface: Class<T>): T {
        val mProxyInvocationHandler = object : InvocationHandler {
            override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                val objClass = base::class
                val function: KFunction<*>? = objClass.functions.filter { it.name == method?.name }.find { functionChecking ->
                    val functionEstablished = proxyInterface.kotlin.functions.filterIndexed { index, proxyFunction ->
                        if (functionChecking.parameters.count() == proxyFunction.parameters.count()) {
                            // parameters[0] is obj
                            if (functionChecking.parameters[0].type == objClass && index > 0)
                                proxyFunction.parameters[index].type == functionChecking.parameters[index].type ||
                                proxyFunction.parameters[index].type.isSupertypeOf(functionChecking.parameters[index].type)
                            else true
                        } else false
                    }
                    functionEstablished.isNotEmpty()
                }
                if (function == null) {
                    val methodName = method?.name
                    val methodParameters = method?.parameterTypes?.joinToString(", ")
                    val error = NoSuchMethodError("$methodName($methodParameters)")
                    throw error
                } else {
                    val data = LinkedList(args?.asList() ?: arrayListOf()).also {
                        it.add(0, base)
                    }.toTypedArray()
                    return function?.call(*data)
                }
            }
        }
        return Proxy.newProxyInstance(
            mClassLoader ?: Thread.currentThread().contextClassLoader,
            arrayOf(proxyInterface),
            mProxyInvocationHandler
        ) as T
    }
}