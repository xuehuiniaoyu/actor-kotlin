# actor-kotlin
Java动态伪装工具可以伪装成对象接口的接口副本，从而实现对象的动态代理(The Java dynamic disguise tool can disguise as an interface copy of the object interface, thus realizing the dynamic proxy of the object)


纯Kotlin开发，使用简单但功能强大，可用于组件化开发或插件化项目开发。

Android Demo: [https://github.com/xuehuiniaoyu/actor-demo]()

```
implementation 'io.github.xuehuiniaoyu:actor-kotlin:2.0.2'
```

#### 1. ActorBean
ActorBean：对象反射工具，不仅有get/set方法，还能把对象抽象到接口，方便集成。

```
data class Student(val name: String, var studentStatus: String? = null)
```

1.1. 直接获取属性值

```
val student = Student("张三")
val actorStudent = ActorBean(student)
val name = actorStudent("name")
```

1.2. 通过接口获取属性值
```
Interface StudentProxy {
    @GET("name") fun getName()
}

val actorStudent = ActorBean(student).agent(StudentProxy::class.java)
val name = actorStudent.getName()
```

1.3. 直接赋值

```
val actorStudent = ActorBean(student)
actorStudent.set("studentStatus", "学籍")
```

1.4. 通过接口给属性赋值

```

Interface StudentProxy {
    @SET("studentStatus") fun setStudentStatus(status: String)
}

val actorStudent = ActorBean(student).agent(StudentProxy::class.java)
actorStudent.setStudentStatus("学籍")
```

#### 2. ActorInterface
ActorInterface 接口动态伪装工具，通过Interface2伪装成Interface1
```
interface Interface1 {
    fun log(log: String)
}

```

```
interface Interface2 {
    fun log(log: Any)
}
```

```
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
```


#### 3. @DynamicImplementation
表示：伪装类需要动态实现


###### 完整代码：
```
interface Interface1 {
    fun log(log: String)
}

interface Interface2 {
    fun log(log: Any)
}
```
```
class Kit {
    fun call(key: String, interface1: Interface1) {
        interface1.log("$key -------> hello world from call...")
    }
}
```

```
interface KitProxy {
    fun call(key: String, @DynamicImplementation data: Any)
}
```

利用Actor让KitProxy伪装成Kit对象

```
val callback = object: Interface2 {
    override fun log(log: Any) {
        println("log: $log")
    }
}

val proxy = Actor(Kit()).imitate(KitProxy::class.java)
proxy.call("hello", callback)
```
