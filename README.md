# actor-kotlin
Java 动态伪代理，它使用伪装者接口为对象创建代理。(Java dynamic pseudo proxy, which uses the stand-in interface to create a proxy for the object.)

```
class A {
  fun call(data: String) {
    ...
  }
}
```

```
interface B {
  fun call(data: String)
}

interface C {
  fun call(data: Any)
}
```

B 和 C 都是 A 的伪装者接口

纯Kotlin开发，使用简单但功能强大，可用于组件化开发或插件化项目开发。

Android Demo: <br />
https://github.com/xuehuiniaoyu/actor-demo <br />
https://github.com/xuehuiniaoyu/actor-demo-componentization
