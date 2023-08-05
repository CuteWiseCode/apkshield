# apkshield  
第二代加固及第三代加固用力产出中...

## 第一代加固   
原理分析 掘金博客地址：https://juejin.cn/spost/7255483407559442491

#### Usage
#### 1. 配置sdk 及ndk 环境
替换成自己的路径

```
sdk.dir=/Users/.../Library/Android/sdk
ndk.dir=/Users/.../Library/Android/sdk/ndk/21.1.6352462
```

#### 2.发布插件
配置发布的仓库地址
```
 mavenDeployer {
            if(useLocalMaven.toBoolean())
            {
                repository(url: mavenLocal().url) //定义本地maven仓库的地址
            }else{
                //可以配置远程仓库地址
            }

            pom {
                groupId="com.ck.plugin"
                artifactId="shield-dex"
                version="1.0.0"
            }
        }
```
其中，useLocalMaven 是gradle.properties 中配置的变量， 根据该变量控制使用本地还是远程仓库。
```
useLocalMaven = true
```

在AS 左侧 Gradle -> ShieldPlugin ->Tasks -> upload -> 点击uploadArchives

#### 3.编译
gradle.properties 下配置
```
useShield = true #true 表示开启加固
```

注意，需要执行sheild task 而不是普通的assemble 或者 generate apk

As -> Gradle -> app -> Tasks -> shield ->shieldxxx  XXX可以选择debug 或者 release 

编译完成后，会输出到 build -> outputs -> apk -> app-xxx.apk


