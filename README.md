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



### 如果我们要实现一个配置中心，需要满足哪些条件

* 服务端的配置保存（持久化）
  * 数据库
* 服务器端提供访问api
  * rpc、http、openapi
* 数据变化之后如何通知到客户端
  * push（服务端主推送到客户端）、pull（客户端主动拉去数据）、
  * pull数据量大怎么办？
* 客户端如何去获得远程服务器的数据
* 安全性
* 刷盘（本地缓存）



### Nacos的源码分析

基于上面的猜想，作为一个注册中心可能会有以上的几种需要实现的功能。那么实际上是如何的呢？

我们回到使用sdk的那段代码，很明显starter包也只不过对这个进行了相对来说bean的注入。

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
            //从这里开始
            ConfigService configService=NacosFactory.createConfigService(properties);

            String content = configService.getConfig(dataId,groupId,3000);

            System.out.println(content);

        } catch (NacosException e) {
            e.printStackTrace();
        }


    }

}
```

其实我们发现，这段代码只不过是通过反射的方法，实例化了一个`NacosConfigService`

```java
public static ConfigService createConfigService(Properties properties) throws NacosException {
        try {
            Class<?> driverImplClass = Class.forName("com.alibaba.nacos.client.config.NacosConfigService");
            Constructor constructor = driverImplClass.getConstructor(Properties.class);
            ConfigService vendorImpl = (ConfigService) constructor.newInstance(properties);
            return vendorImpl;
        } catch (Throwable e) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
        }
    }
```

所以本质上，他返回的就是一个`NacosConfigService`的实例，这之后也就没有其他的代码了，只可能是在构造方法里发生了什么，所以我们进入他的构造方法中。

```java
public NacosConfigService(Properties properties) throws NacosException {
        String encodeTmp = properties.getProperty(PropertyKeyConst.ENCODE);
        if (StringUtils.isBlank(encodeTmp)) {
            encode = Constants.ENCODE;
        } else {
            encode = encodeTmp.trim();
        }
        initNamespace(properties);
    /*
    因为nacos本身也要读取配置文件，配置文件的路径位于
    ..\nacos-1.1.0\distribution\target\nacos-server-1.1.0\nacos\conf
    解析文件没什么好看的，下面才是重点。
    
    我们可以发现的是有一个http的字眼，而agent本身也有代理的意思
    所以返回的agent其实就是一个被装饰的代理类
    
    agent只不过会将http的地址保存起来，有一个serviceManger保存他们
    start方法将会启动一个定时任务去轮询他们。
    
    
    */
        agent = new MetricsHttpAgent(new ServerHttpAgent(properties));
        agent.start();
        worker = new ClientWorker(agent, configFilterChainManager, properties);
    }
```

重点回到`clientWorker`中

```java
public ClientWorker(final HttpAgent agent, final ConfigFilterChainManager configFilterChainManager, final Properties properties) {
        this.agent = agent;
        this.configFilterChainManager = configFilterChainManager;

        // Initialize the timeout parameter

        init(properties);
		
    /*
    
    这里的逻辑，初始化两个线程池
    并且启动了一个延迟线程
    
    */
    
        executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                //客户端的线程池，用于异步验证本地配置是否需要更新
                Thread t = new Thread(r);
                t.setName("com.alibaba.nacos.client.Worker." + agent.getName());
                t.setDaemon(true);
                return t;
            }
        });

        executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                //用于对服务发起长轮询的线程池，查看服务端的配置是否发生了变化
                Thread t = new Thread(r);
                t.setName("com.alibaba.nacos.client.Worker.longPolling." + agent.getName());
                t.setDaemon(true);
                return t;
            }
        });

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //每隔10s将会检查一次，所以我们进入这个方法。
                try {
                    checkConfigInfo();
                } catch (Throwable e) {
                    LOGGER.error("[" + agent.getName() + "] [sub-check] rotate check error", e);
                }
            }
        }, 1L, 10L, TimeUnit.MILLISECONDS);
    }
```

这里有一个缓存数过大的情况的处理

```java
public void checkConfigInfo() {
        // 分任务
        int listenerSize = cacheMap.get().size();
        // 向上取整为批数
        int longingTaskCount = (int) Math.ceil(listenerSize / ParamUtil.getPerTaskConfigSize());
    //启动不同的线程去按批次处理。
        if (longingTaskCount > currentLongingTaskCount) {
            for (int i = (int) currentLongingTaskCount; i < longingTaskCount; i++) {
                // 要判断任务是否在执行 这块需要好好想想。 任务列表现在是无序的。变化过程可能有问题
                executorService.execute(new LongPollingRunnable(i));
            }
            currentLongingTaskCount = longingTaskCount;
        }
    }
```

在这里，有一个很关键的对象，`cacheMap`。

这个东西是`nacos`的本地缓存，

```java
/**
     * groupKey -> cacheData
     */
    private final AtomicReference<Map<String, CacheData>> cacheMap = new AtomicReference<Map<String, CacheData>>(
        new HashMap<String, CacheData>());
```

他的key是我们之前设置的groupId，然后对应的是cacheData。

cacheData中的有一些参数值得参考以后的开发。

```java
// ==================

    private final String name;
    private final ConfigFilterChainManager configFilterChainManager;
    public final String dataId;
    public final String group;
    public final String tenant;
    private final CopyOnWriteArrayList<ManagerListenerWrap> listeners;
// 监听的列表

    private volatile String md5;
    /**
     * whether use local config
     	是否使用本地配置
     */
    private volatile boolean isUseLocalConfig = false;
    /**
     * last modify time
     最后修改时间
     */
    private volatile long localConfigLastModified;
// 配置内容
    private volatile String content;
//线程任务id
    private int taskId;
    private volatile boolean isInitializing = true;
```

```java
public void checkConfigInfo() {
        // 分任务
        int listenerSize = cacheMap.get().size();
        // 向上取整为批数
    /*
    如果你的缓存设置是3000，以为这你可以允许3000个groupid的配置
		来了10000的个配置需要你验证，向上取整的结果为4，那么就会四次任务
    
    */
        int longingTaskCount = (int) Math.ceil(listenerSize / ParamUtil.getPerTaskConfigSize());
        if (longingTaskCount > currentLongingTaskCount) {
            for (int i = (int) currentLongingTaskCount; i < longingTaskCount; i++) {
                //由于执行了这个任务，所以我们可以直接看看这个方法里面有什么
                executorService.execute(new LongPollingRunnable(i));
            }
            currentLongingTaskCount = longingTaskCount;
        }
    }
```

进入这个的构造方法中。

```java
class LongPollingRunnable implements Runnable {
        private int taskId;
		/*
		我们看到了很有意思的一个属性，那就是taskId，因为我们在cacheData中也看到过相同的定义
		每一个被执行的长轮询任务都会被赋予一个id，这个很明显是向上取整获得的。
		*/
        public LongPollingRunnable(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
			//准备了一个cacheData列表，用于将相同的任务id，cache将会归于一组
            List<CacheData> cacheDatas = new ArrayList<CacheData>();
            //在初始化一个key的list
            List<String> inInitializingCacheList = new ArrayList<String>();
            try {
                // check failover config
                //我们会去本地缓存中拿到所有的CacheData
                for (CacheData cacheData : cacheMap.get().values()) {
                    //比对是否是一组的，也就是同一个定义任务里面的
                    if (cacheData.getTaskId() == taskId) {
                        //如果是，那就加进来
                        cacheDatas.add(cacheData);
                        try {
                            //触发检查
                            checkLocalConfig(cacheData);
                            if (cacheData.isUseLocalConfigInfo()) {
                                cacheData.checkListenerMd5();
                            }
                        } catch (Exception e) {
                            LOGGER.error("get local config info error", e);
                        }
                    }
                }
				/*
				但，由于一开始，cacheData里面什么东西都没有，所以上面的循环也不会成立
				将会直接从服务端取。
				*/
                
                // check server config
                /*
               	checkUpdateDataIds 方法会返回服务端可能改变的配置，这个每个string
               	都是凭借而来，大致是dataId+groupid 这样的组合
                
                */
                List<String> changedGroupKeys = checkUpdateDataIds(cacheDatas, inInitializingCacheList);

                for (String groupKey : changedGroupKeys) {
                    String[] key = GroupKey.parseKey(groupKey);
                    //解析并抽取关键文字
                    String dataId = key[0];
                    String group = key[1];
                    String tenant = null;
                    if (key.length == 3) {
                        tenant = key[2];
                    }
                    //请注意的是，这里并不会获得内容，只是获得可能改变的配置信息
                    try {
                        //接着，组装好的http请求，再去请求服务器，然后返回内容。
                        //并存到本地的文件中。
                        String content = getServerConfig(dataId, group, tenant, 3000L);
                        //开始刷内存缓存
                        CacheData cache = cacheMap.get().get(GroupKey.getKeyTenant(dataId, group, tenant));
                        cache.setContent(content);
                        LOGGER.info("[{}] [data-received] dataId={}, group={}, tenant={}, md5={}, content={}",
                            agent.getName(), dataId, group, tenant, cache.getMd5(),
                            ContentUtils.truncateContent(content));
                    } catch (NacosException ioe) {
                        String message = String.format(
                            "[%s] [get-update] get changed config exception. dataId=%s, group=%s, tenant=%s",
                            agent.getName(), dataId, group, tenant);
                        LOGGER.error(message, ioe);
                    }
                }
            	
                //检查是否需要更新内存缓存
                for (CacheData cacheData : cacheDatas) {
                    if (!cacheData.isInitializing() || inInitializingCacheList
                        .contains(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant))) {
                        cacheData.checkListenerMd5();
                        cacheData.setInitializing(false);
                    }
                }
                inInitializingCacheList.clear();
				
                //等待下一次执行
                executorService.execute(this);
			
            } catch (Throwable e) {

                // If the rotation training task is abnormal, the next execution time of the task will be punished
                LOGGER.error("longPolling error : ", e);
                executorService.schedule(this, taskPenaltyTime, TimeUnit.MILLISECONDS);
            }
        }
    }
```

我们回到第二次的有缓存的情况。

```java
// check failover config
                //我们会去本地缓存中拿到所有的CacheData
                for (CacheData cacheData : cacheMap.get().values()) {
                    //比对是否是一组的，也就是同一个定义任务里面的
                    if (cacheData.getTaskId() == taskId) {
                        //如果是，那就加进来
                        cacheDatas.add(cacheData);
                        try {
                            //触发检查
                            checkLocalConfig(cacheData);<-----
                            if (cacheData.isUseLocalConfigInfo()) {
                                cacheData.checkListenerMd5();
                            }
                        } catch (Exception e) {
                            LOGGER.error("get local config info error", e);
                        }
                    }
```

```java
private void checkLocalConfig(CacheData cacheData) {
        final String dataId = cacheData.dataId;
        final String group = cacheData.group;
        final String tenant = cacheData.tenant;
    //判断文件是否存在。
        File path = LocalConfigInfoProcessor.getFailoverFile(agent.getName(), dataId, group, tenant);
		/*
		 其实这里可以看出，nacos其实拥有本地的文件存储，也有自己的内存缓存cacheMap
		
		*/
        // 没有 -> 有 没有使用本地缓存，但是本地文件存在的情况，则说明长轮询异步线程已经获得了新的数据，但是内存缓存还没有更新，所以我们要说更新本地内存缓存
        if (!cacheData.isUseLocalConfigInfo() && path.exists()) {
            String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
            String md5 = MD5.getInstance().getMD5String(content);
            cacheData.setUseLocalConfigInfo(true);
            cacheData.setLocalConfigInfoVersion(path.lastModified());
            cacheData.setContent(content);

            LOGGER.warn("[{}] [failover-change] failover file created. dataId={}, group={}, tenant={}, md5={}, content={}",
                agent.getName(), dataId, group, tenant, md5, ContentUtils.truncateContent(content));
            return;
        }

        // 有 -> 没有。不通知业务监听器，从server拿到配置后通知。
        if (cacheData.isUseLocalConfigInfo() && !path.exists()) {
            cacheData.setUseLocalConfigInfo(false);
            LOGGER.warn("[{}] [failover-change] failover file deleted. dataId={}, group={}, tenant={}", agent.getName(),
                dataId, group, tenant);
            return;
        }

        // 有变更
    /*
    	使用了本地内存缓存，文件也存在，但是cache的最后修改时间改变了，所以我们要从本地文件中拿一次。生成md5吗重新构建以此。
    */
        if (cacheData.isUseLocalConfigInfo() && path.exists()
            && cacheData.getLocalConfigInfoVersion() != path.lastModified()) {
            String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
            String md5 = MD5.getInstance().getMD5String(content);
            cacheData.setUseLocalConfigInfo(true);
            cacheData.setLocalConfigInfoVersion(path.lastModified());
            cacheData.setContent(content);
            LOGGER.warn("[{}] [failover-change] failover file changed. dataId={}, group={}, tenant={}, md5={}, content={}",
                agent.getName(), dataId, group, tenant, md5, ContentUtils.truncateContent(content));
        }
    }
```

下一步，看看是否使用了本地缓存。本质上来说，能影响cacheData的有两种，就是在checkLocalConfig的时候所发生事情。

* 本地文件存在
  * 说明异步线程池已经拿到了服务器的配置信息，并且存储到了本地，但是内存缓存中还没有，所以回去本地文件中去拿，得到更新，设置成为已使用本地。
  * 最后修改时间不匹配，说明值已经发生了改变，想起之前nacos的ui界面我们点击确定的时候，那个更新操作估计会刷新文件的值，导致值已经更新，需要重新刷新内存，
* 本地文件不存在
  * 不存在的情况，说明文件已经被删除，这可能是意外情况，nacos对于这种情况选择了置之不理的态度，等一下轮询的时候自我创建。

```java
 try {
                            checkLocalConfig(cacheData);
                            if (cacheData.isUseLocalConfigInfo()) {
                                cacheData.checkListenerMd5();
                            }
                        } catch (Exception e) {
                            LOGGER.error("get local config info error", e);
                        }
```

然后呢，进行了进行md5的比较，由于字符的改变会导致最后生成md码不同，所以可以用来验证是否为相同。保证最新。

```java
void checkListenerMd5() {
    for (ManagerListenerWrap wrap : listeners) {
        if (!md5.equals(wrap.lastCallMd5)) {
            safeNotifyListener(dataId, group, content, md5, wrap);
        }
    }
}
```

这个就是我们在这个节点上加的监听了。如果添加了的话。将会发生回调。

```java
private void safeNotifyListener(final String dataId, final String group, final String content,
                                    final String md5, final ManagerListenerWrap listenerWrap) {
        final Listener listener = listenerWrap.listener;

        Runnable job = new Runnable() {
            @Override
            public void run() {
                ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader appClassLoader = listener.getClass().getClassLoader();
                try {
                    if (listener instanceof AbstractSharedListener) {
                        AbstractSharedListener adapter = (AbstractSharedListener) listener;
                        adapter.fillContext(dataId, group);
                        LOGGER.info("[{}] [notify-context] dataId={}, group={}, md5={}", name, dataId, group, md5);
                    }
                    // 执行回调之前先将线程classloader设置为具体webapp的classloader，以免回调方法中调用spi接口是出现异常或错用（多应用部署才会有该问题）。
                    Thread.currentThread().setContextClassLoader(appClassLoader);

                    ConfigResponse cr = new ConfigResponse();
                    cr.setDataId(dataId);
                    cr.setGroup(group);
                    cr.setContent(content);
                    configFilterChainManager.doFilter(null, cr);
                    String contentTmp = cr.getContent();
                    listener.receiveConfigInfo(contentTmp);//《----这个地方
                    listenerWrap.lastCallMd5 = md5;
                    LOGGER.info("[{}] [notify-ok] dataId={}, group={}, md5={}, listener={} ", name, dataId, group, md5,
                        listener);
                } catch (NacosException de) {
                    LOGGER.error("[{}] [notify-error] dataId={}, group={}, md5={}, listener={} errCode={} errMsg={}", name,
                        dataId, group, md5, listener, de.getErrCode(), de.getErrMsg());
                } catch (Throwable t) {
                    LOGGER.error("[{}] [notify-error] dataId={}, group={}, md5={}, listener={} tx={}", name, dataId, group,
                   //...
    }
```

到此，完毕，当然还有一个getConfig也差不多，大致也能猜到先去拿本地，本地拿不到就回去拿服务器那边。

#### 总结

从new 出NacosFactory开始的时候，就启动了三个线程池，其中一个线程池将会每隔10s，开启等待1s去服务器轮询，并闪存到本地，这之后nacos的内存缓存只会从本地的文件中去取，如果有变更，第一时间更新也是从文件开始取走。



### 本地存储改成mysql

其实nacos是支持mysql的。

![1565115290108](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565115290108.png)

我们新建一个mysql数据库。然后将sql文件导入。

![1565115349355](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565115349355.png)

由于要配置mysql，我们必须使用集群环境，但是为了偷懒，我们将三个全部设置成本地，将cluster.config.exmaple拷贝一份出来，并且重命名，并加入以下配置。

```properties
#it is ip
#example
#10.10.109.214
#11.16.128.34
#11.16.128.36
localhost
localhost
localhost
```

更改数据库配置。

```java
# nacos.naming.distro.taskDispatchPeriod=200
# nacos.naming.distro.batchSyncKeyCount=1000
# nacos.naming.distro.syncRetryDelay=5000
# nacos.naming.data.warmup=true
# nacos.naming.expireInstance=true

spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://localhost:3306/nacos-config
db.user=root
db.password=root
```

cd到目标目录下，输入

```
startup.cmd -m cluster
```

因为是集群环境，所以你要是不加参数，还是一个单机模式，是会报错的。

我们添加一个

![1565115557638](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565115557638.png)

![1565115596034](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565115596034.png)

![1565115613631](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565115613631.png)

刷新数据库。

![1565115639446](https://github.com/PopCandier/Nacos-Demo/blob/master/img/1565115639446.png)

可见数据库已经配置成功了，替换成mysql只是方便管理，你不使用mysql，nacos默认使用derby。