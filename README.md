### Alibaba Nacos 

有服务注册发现的功能呢，但是我们使用他，还是比较喜欢用它的配置中心。

#### 服务注册

健康监测/服务的维护/服务地址变更的通知...

### 官网位置

https://nacos.io/zh-cn/docs/what-is-nacos.html

### 如何部署

我们可以从`github`下载安装包，有需要构建的源码包，也有已经可以直接启动的nacos工程

![1565018240100](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565018240100.png)

选择`releases`，然后有作者打包好的源码包，这里就是源码包。

下载完成后解压，用`idea`打开，后会需要构建一段时间，在等待完成后，cd到项目的根目录，并执行。

```
mvn -Prelease-nacos clean install -U  
```

中途会报几个错，但是没有关系，这是测试案例的服务器尝试连接。

然后等待构建完成，

![1565018386261](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565018386261.png)

构建完成后，在`distribution`模块下会生成`target`文件，里面就是可以启动服务

![1565018453890](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565018453890.png)

我们打开这个文件夹

![1565018498713](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565018498713.png)

支持linux和window两个版本的启动和终止。由于现在是window，所以我们cd到这个目录下，启动`nacos`

![1565018608738](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565018608738.png)

接着我们在url地址上输入`http://192.168.216.1:8848/nacos/index.html`，查看，

用户名密码都是`nacos`

![1565018736159](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565018736159.png)

### 开始构建一个Nacos项目

导入依赖

```xml
<dependency>
            <groupId>com.alibaba.boot</groupId>
            <artifactId>nacos-config-spring-boot-starter</artifactId>
            <version>0.2.2</version>
        </dependency>
```

我们在刚刚启动的Nacos服务随便配置点东西。

![1565019685740](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565019685740.png)

然后在`application.properties`配置些参数，目的是和Nacos建立连接。

```properties
#nacos
nacos.config.server-addr=localhost:8848
```

然后我们建立一个Controller，使用注解，和我们之前配置的东西关联上，

```java
package com.example.nacos.nacosdemo;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pop
 * @date 2019/8/5 23:39
 *
 * autoRefreshed 表示自动刷新
 */
@NacosPropertySource(dataId = "example",groupId = "DEFAULT_GROUP",autoRefreshed = true)
@RestController
public class NacosConfigController {

    /**
     * value中表示，会去example这个配置下，拿到auther：name：的这个值
     * 而nacos考虑到可用方案，在无法请求到数据的时候
     * 将会使用降级，拿到本地的默认值，就是我们设置的这个 “none auther”
     * 而避免拿到空值
     */
    @NacosValue(value = "${auther: none auther}",autoRefreshed = true)
    private String info;

    @GetMapping("getInfo")
    public String getInfo(){
        return info;
    }
}
```

![1565020845837](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565020845837.png)

![1565020854072](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565020854072.png)

并且你在`nacos`修改配置，web项目可以轻松感知到变化

![1565020975687](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565020975687.png)

![1565020987732](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565020987732.png)

### Nacos 的思考

* nacos server 中的配置是如何存储的
* 客户端是如何拿去远程服务数据
* 动态感知
* ....

这边来说一下，这里配置的dataId和groupid的意思

如果将我们的应用或者服务作为例子的话，一个服务，例如支付的服务

Pay-Service 可以看做一个groupId，因为他是支付功能这一组的集合。

而Pay-Service中拥有一些业务逻辑的部分，例如调用第三方接口的方法，还有服务层的部分，这里可以作为dataId用来区分，所以groupId和dataId可以说，表达的粒度不相同。

### 基于Nacos SDK的访问

Nacos提供了原生的开发包来操作，SDK（Software Development Kit）

首先引入依赖

```xml
<dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-client</artifactId>
            <version>1.1.1</version>
        </dependency>
```

```java
public class NacosSdkDemo {

    public static void main(String[] args) {
        //连接到目标服务器
        //指定dataid groupid
        String serviceAddr = "localhost:8848";
        String dataId = "example";
        String groupId="DEFAULT_GROUP";

        Properties properties = new Properties();
        properties.put("serverAddr",serviceAddr);

        try {
            ConfigService configService=NacosFactory.createConfigService(properties);

            String content = configService.getConfig(dataId,groupId,3000);

            System.out.println(content);

        } catch (NacosException e) {
            e.printStackTrace();
        }


    }

}
```

![1565022595679](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565022595679.png)

