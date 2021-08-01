package xuehuiniaoyu.github.io.actor

import java.util.*

class ActorInterface(private val impl: Any) {
    private val mObservable by lazy {
        object: Observable() {
            fun open() = setChanged()
        }
    }
    private var mObserver: Observer? = null
    private var recycled = false
    fun bindInterface(`interface`: Class<*>) {
        mObservable.open()
        mObservable.notifyObservers(Actor(impl).imitate(`interface`))
    }

    fun <T: Any> getImplement(fn: (T?) -> Unit) {
        if(mObserver != null) error("Cannot be implemented repeatedly!")
        mObserver = Observer { o, arg ->
            fn(arg as? T)
        }
        mObservable.addObserver(mObserver)
    }

    fun recovery() {
        if(mObserver != null) {
            mObservable.deleteObserver(mObserver)
            mObserver = null
        }
        recycled = true
    }
}