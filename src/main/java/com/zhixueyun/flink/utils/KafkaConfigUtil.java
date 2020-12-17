package com.zhixueyun.flink.utils;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * Kafka配置文件
 * */

public class KafkaConfigUtil {

    public static Properties buildKafkaProps(String filePath){

         Properties pro = PropertiesUtils.properties(filePath);

         String ZOOKEEPER_HOST = pro.getProperty("ZOOKEEPER_HOST");
         String KAFKA_BROKER = pro.getProperty("KAFKA_BROKER");
         String TRANSACTION_GROUP = pro.getProperty("TRANSACTION_GROUP");

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        properties.setProperty("zookeeper.connect", ZOOKEEPER_HOST);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, TRANSACTION_GROUP);//需要根据任务名字定义
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

      /**  earliest
        当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
                latest
        当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
                none
        topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常
        */
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        System.out.println("get kafka config, config map-> " + properties.toString());
        return properties;
    }


}
