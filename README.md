# actor-kotlin
An open source dynamic agent Library

class Entity1(val name: String)
    class Entity2(val name: String)

    class ApplicationBean {
        fun build(entity: Entity1) {
            println("my entity is ${entity.name}")
        }
    }

    interface ProxyInterface {
        fun build(entity: Entity2)
        fun build(arg: Int)
    }

    private val myMappings by lazy {
        mapOf<Class<*>, Class<*>>(Entity2::class.java to Entity1::class.java)
    }

    private val myMappingStrategy by lazy {
        object: MappingStrategy {
            override fun onMapping(from: Any): Any? {
                return if(from is Entity2) {
                    Entity1(from.name)
                } else null
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
