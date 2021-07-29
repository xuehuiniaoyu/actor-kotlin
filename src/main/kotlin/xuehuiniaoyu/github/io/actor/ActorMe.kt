package xuehuiniaoyu.github.io.actor

open class ActorMe<T : Any>(private val `interface`: Class<T>) {
    fun imitate(classLoader: ClassLoader?): T {
        return Actor(this).setClassLoader(classLoader).imitate(`interface`)
    }
}