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
