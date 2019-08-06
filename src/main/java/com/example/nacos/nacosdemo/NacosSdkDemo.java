package com.example.nacos.nacosdemo;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author Pop
 * @date 2019/8/6 0:22
 */
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
            //对这个配置进行监听。
            configService.addListener(dataId, groupId, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println(configInfo);
                }
            });

        } catch (NacosException e) {
            e.printStackTrace();
        }


    }

}
