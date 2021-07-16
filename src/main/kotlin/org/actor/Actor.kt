package org.actor

import java.lang.reflect.Method
import java.lang.reflect.Proxy

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
class Actor private constructor(private val obj: Any) {
    companion object {
        private val basePrimitiveMappers: Map<Class<*>, Class<*>> by lazy {
            mapOf<Class<*>, Class<*>>(
                    java.lang.Integer::class.java to Int::class.java,
                    java.lang.Long::class.java to Long::class.java,
                    java.lang.Float::class.java to Float::class.java,
                    java.lang.Double::class.java to Double::class.java,
                    java.lang.Character::class.java to Char::class.java,
                    java.lang.Byte::class.java to Byte::class.java,
                    java.lang.Boolean::class.java to Boolean::class.java
            )
        }

        /**
         *
         * User-defined mappers
         */
        private val customMadePrimitiveMappers: HashMap<Class<*>, Class<*>> by lazy {
            HashMap<Class<*>, Class<*>>()
        }

        fun getMappings() = HashMap(basePrimitiveMappers).also { it.putAll(customMadePrimitiveMappers) }

        /**
         *
         * Create the proxy object of the object
         */
        fun of(obj: Any): Actor = Actor(obj)
    }

    private var mClassLoader: ClassLoader? = null
    private var mMappingStrategy: MappingStrategy? = null

    fun setMapping(fn: () -> Map<Class<*>, Class<*>>): Actor {
        customMadePrimitiveMappers.putAll(fn())
        return this
    }

    fun setMapping(map: Map<Class<*>, Class<*>>): Actor {
        customMadePrimitiveMappers.putAll(map)
        return this
    }

    fun setMapping(from: Class<*>, to: Class<*>): Actor {
        customMadePrimitiveMappers[from] = to
        return this
    }

    /**
     *
     * Custom mapping logic
     */
    fun setMappingStrategy(strategy: MappingStrategy): Actor {
        this.mMappingStrategy = strategy
        return this
    }

    fun setClassLoader(classLoader: ClassLoader?): Actor {
        this.mClassLoader = classLoader
        return this
    }

    /**
     *
     * According to the interface, realizing the proxy of the real object,
     * Interface must contain methods of real objects
     */
    fun <T : Any> proxyBy(vararg interfaces: Class<*>): T {
        return Proxy.newProxyInstance(mClassLoader
                ?: Thread.currentThread().contextClassLoader, interfaces) { _, method, args ->
            method?.let {
                if (args == null) {
                    obj.javaClass.getDeclaredMethod(method.name)
                            .invoke(obj)
                } else {
                    val variableArgs = args.map { it }.toTypedArray()
                    findMethod(obj.javaClass, method.name, method.parameterTypes, variableArgs)?.invoke(obj, *variableArgs)
                }
            }
        } as T
    }

    /**
     *
     * find method of realObj by interface method
     *
     * @param clazz realObj class
     * @param name method name
     * @param types types of args result is [Class<*>]
     * @param args Specific parameters result is [Any]
     * @param tryCount <= args.count
     *
     */
    private fun findMethod(clazz: Class<*>, name: String, types: Array<Class<*>>, args: Array<Any>, tryCount: Int = types.count()): Method? {
        return try {
            return clazz.getDeclaredMethod(name, *types)
        } catch (ex: NoSuchMethodException) {
            if (tryCount == 0) {
                if(clazz.superclass != null && clazz.superclass != java.lang.Object::class.java) {
                    findMethod(clazz.superclass, name, types, args, types.size)
                }
                else {
                    error("$obj -> $ex")
                }
            } else {
                replaceTypeToMap(types, args)
                findMethod(clazz, name, types, args, tryCount - 1)
            }
        }
    }

    /**
     *
     *  Mapping...
     *
     */
    private fun replaceTypeToMap(types: Array<Class<*>>, args: Array<Any>) {
        val mappings = getMappings()
        types.filterIndexed { index, clazz ->
            return@filterIndexed if (mappings.containsKey(clazz)) {
                val destClass: Class<*> = mappings[clazz] ?: TODO()
                types[index] = destClass
                mMappingStrategy?.also {
                    val mappingStrategyResult = it.onMapping(args[index], destClass)
                    if(mappingStrategyResult != null) {
                        args[index] = mappingStrategyResult
                    }
                }
                true
            } else false
        }
    }
}