package xuehuiniaoyu.github.io.actor

import xuehuiniaoyu.github.io.actor.di.DynamicImplementation
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.jvm.javaType

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
                val needDynamicImplementationNodes = hashMapOf<Int, Class<*>>()
                val function: KFunction<*>? =
                    objClass.functions.filter { it.name == method?.name && it.parameters.count()-1 == args?.count() ?: 0 }.find { functionChecking ->
                        val functionEstablished = proxyInterface.kotlin.functions.filter { it.name == method?.name && it.parameters.count()-1 == args?.count() ?: 0 }.filterIndexed { _, proxyFunction ->
                            checkParametersEq(proxyFunction, functionChecking) { index ->
                                val typeClass = (mClassLoader ?: Thread.currentThread().contextClassLoader).loadClass(functionChecking.parameters[index].type.javaType.typeName)
                                needDynamicImplementationNodes[index] = typeClass
                            }
                        }
                        functionEstablished.isNotEmpty()
                    }
                if (function == null) {
                    val methodName = method?.name
                    val methodParameters = method?.parameterTypes?.joinToString(", ")
                    val error = NoSuchMethodError("$objClass -> $methodName($methodParameters)")
                    throw error
                } else {
                    val data = LinkedList(args?.asList() ?: arrayListOf())
                    data.add(0, base)
                    needDynamicImplementationNodes.forEach { (key, value) ->
                        if (value.isInterface) {
                            val actorInterface = ActorInterface(data[key])
                            actorInterface.getImplement<Any> { impl ->
                                data[key] = impl
                            }
                            actorInterface.bindInterface(value)
                            actorInterface.recovery()
                        }
                    }
                    return try {
                        function?.call(*data.toTypedArray())
                    } catch (e: Exception) {
                        val methodName = method?.name
                        val methodParameters = method?.parameterTypes?.joinToString(", ")
                        throw(IllegalArgumentException("$objClass -> $methodName($methodParameters) === You can consider using annotation @DynamicImplementation"))
                    }
                }
            }
        }
        return Proxy.newProxyInstance(
            mClassLoader ?: Thread.currentThread().contextClassLoader,
            arrayOf(proxyInterface),
            mProxyInvocationHandler
        ) as T
    }

    private fun checkParametersEq(
        fun1: KFunction<*>,
        fun2: KFunction<*>,
        isDynamicImplementationAlso: (Int) -> Unit
    ): Boolean {
        return if (fun1.parameters.count() == fun2.parameters.count()) {
            val check1 = fun(index: Int, kParameter: KParameter) = kParameter.type == fun2.parameters[index].type
            val check2 = fun(index: Int, kParameter: KParameter) = (kParameter.annotations.find { it is DynamicImplementation } != null).also { isDynamicImplementation ->
                if (isDynamicImplementation) {
                    isDynamicImplementationAlso(index)
                }
            }
            val check3 = fun(index: Int, kParameter: KParameter) = kParameter.type.isSubtypeOf(fun2.parameters[index].type)
            val checkParams = fun1.parameters.filterIndexed { index, kParameter ->
                check1(index, kParameter) || check2(index, kParameter)  || check3(index, kParameter)
            }
            checkParams.count() == fun1.parameters.count()-1
        } else false
    }
}