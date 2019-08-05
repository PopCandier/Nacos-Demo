package com.example.nacos.nacosdemo;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;

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

        } catch (NacosException e) {
            e.printStackTrace();
        }


    }

}
