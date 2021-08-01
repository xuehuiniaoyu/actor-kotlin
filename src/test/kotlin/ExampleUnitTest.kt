import org.junit.Test
import xuehuiniaoyu.github.io.actor.Actor
import xuehuiniaoyu.github.io.actor.ActorBean
import xuehuiniaoyu.github.io.actor.ActorInterface
import xuehuiniaoyu.github.io.actor.di.DynamicImplementation
import xuehuiniaoyu.github.io.actor.field.GET
import xuehuiniaoyu.github.io.actor.field.SET

class ExampleUnitTest {
    class ApplicationEntity1(val name: String)
    class ApplicationEntity2(val name: String)
    class MyEntity1(var name: String)
    class MyEntity2(val name: String)

    class Entity1(var name: String)
    class Entity2(val name: String)


    class ApplicationBean {
        fun build(fn: (Entity1) -> Unit) {
            fn(Entity1("nake"))
        }

        fun testMultiparameter(entity1: ApplicationEntity1, entity2: ApplicationEntity2) {
            println("--------------------${entity1.name}")
        }
    }

    interface ProxyInterface {
        fun build(fn: (Any) -> Unit)
        fun build(arg: Int)
        fun testMultiparameter(entity1: MyEntity1, entity2: MyEntity2)
    }

    interface ObjProperties {
        @GET("name")
        fun getName(): String

        @SET("name")
        fun setName(value: String)
    }
    @Test
    fun testActorTrusteeship() {
        val proxy = Actor(ApplicationBean()).imitate(ProxyInterface::class.java)
        proxy.build {
            val proxyObject = ActorBean(it).agent(ObjProperties::class.java)
            proxyObject.setName("哈哈")
            println(proxyObject.getName())
        }
    }

//    @Test
//    fun testActor() {
//        val proxy: ProxyInterface = Actor(ApplicationBean()).setMappingStrategy(object: MappingStrategy {
//            override fun createMapping(): Map<KClass<*>, KClass<*>> {
//                return mapOf<KClass<*>, KClass<*>>(
//                    Entity2::class to Entity1::class
//                )
//            }
//
//            override fun onMapping(from: Any, to: KClass<*>): Any? {
//                return Entity1("hello")
//            }
//        }).imitate(ProxyInterface::class.java)
//        proxy.build(Entity2("testActor -> hello world"))
//    }
//
//    @Test
//    fun testMultiparameter() {
//        val proxy: ProxyInterface = Actor(ApplicationBean()).imitate(ProxyInterface::class.java)
//        proxy.build(Entity2("Ddddd"))
//        proxy.testMultiparameter(MyEntity1("testActor -> hello world"), MyEntity2("arg2"))
//        proxy.build(Entity2("vvvvv"))
//    }


    @Test
    fun testReflect() {
        val entity1 = Entity1("hello world")
        val proxyEntity1 = ActorBean(entity1)
        proxyEntity1.set("name", "hello world111")
        val value = proxyEntity1.get<Any>("name")
        println(value)
    }



    interface Interface1 {
        fun log(log: String)
    }

    interface Interface2 {
        fun log(log: Any)
    }

    class Kit {
        fun call(key: String, interface1: Interface1) {
            interface1.log("$key -------> hello world from call...")
        }

        fun call() {
            println("hello world!")
        }
    }

    interface KitProxy {
        fun call(key: String, @DynamicImplementation data: Any)
        fun call()
    }

    @Test
    fun testActorInterface() {
        val actorInterface = ActorInterface(object: Interface2 {
            override fun log(log: Any) {
                println("log: $log")
            }
        })
        actorInterface.getImplement<Interface1> { impl ->
            impl?.log("hello world!")
        }
        actorInterface.bindInterface(Interface1::class.java)
        actorInterface.recovery()
    }

    @Test
    fun testDynamicImplementation() {
        val callback = object: Interface2 {
            override fun log(log: Any) {
                println("log: $log")
            }
        }

        val proxy = Actor(Kit()).imitate(KitProxy::class.java)
        proxy.call("hello", callback)
        proxy.call()
    }
}