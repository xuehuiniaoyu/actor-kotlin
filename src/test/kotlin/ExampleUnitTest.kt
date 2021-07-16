import org.actor.Actor
import org.actor.ActorMe
import org.actor.ActorTrusteeship
import org.actor.MappingStrategy
import org.junit.Test

class ExampleUnitTest {
    class ApplicationEntity1(val name: String)
    class ApplicationEntity2(val name: String)
    class MyEntity1(val name: String)
    class MyEntity2(val name: String)

    class Entity1(val name: String)
    class Entity2(val name: String)


    class ApplicationBean {
        fun build(entity: Entity1) {
            println("my entity is ${entity.name}")
        }

        fun testMultiparameter(entity1: ApplicationEntity1, entity2: ApplicationEntity2) {
            println("--------------------${entity1.name}")
        }
    }

    interface ProxyInterface {
        fun build(entity: Entity2)
        fun build(arg: Int)
        fun testMultiparameter(entity1: MyEntity1, entity2: MyEntity2)
    }

    private val myMappings by lazy {
        mapOf<Class<*>, Class<*>>(
            Entity2::class.java to Entity1::class.java,
            MyEntity1:: class.java to ApplicationEntity1::class.java,
            MyEntity2::class.java to ApplicationEntity2::class.java
        )
    }

    private val myMappingStrategy by lazy {
        object: MappingStrategy {
            override fun onMapping(from: Any, expectedType: Class<*>): Any? {
                return if(from is Entity2) {
                    Entity1(from.name)
                } else if(from is MyEntity1) {
                    ApplicationEntity1(from.name)
                } else if(from is MyEntity2) {
                    ApplicationEntity2(from.name)
                }
                else null
            }
        }
    }

    @Test
    fun testActorTrusteeship() {
        val proxy: ProxyInterface = ActorTrusteeship().from(ApplicationBean()).to(ProxyInterface::class.java).apply {
            it.setMapping(Entity2::class.java, Entity1::class.java)
            it.setMappingStrategy(myMappingStrategy)
        }.proxy()

        proxy.build(Entity2("hello world"))
    }

    @Test
    fun testActorMe() {
        val proxy: ProxyInterface = object: ActorMe(ProxyInterface::class.java) {
            fun build(arg: Int) {
                println("arg is $arg")
            }
        }.proxy()
        proxy.build(1024)
    }

    @Test
    fun testActor() {
        val proxy: ProxyInterface = Actor.of(ApplicationBean())
            .setMapping(myMappings)
            .setMappingStrategy(myMappingStrategy)
            .proxyBy(ProxyInterface::class.java)
        proxy.build(Entity2("testActor -> hello world"))
    }

    @Test
    fun testMultiparameter() {
        val proxy: ProxyInterface = Actor.of(ApplicationBean())
                .setMapping(myMappings)
                .setMappingStrategy(myMappingStrategy)
                .proxyBy(ProxyInterface::class.java)
        proxy.build(Entity2("Ddddd"))
        proxy.testMultiparameter(MyEntity1("testActor -> hello world"), MyEntity2("arg2"))
        proxy.build(Entity2("vvvvv"))
    }
}