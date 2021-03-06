package com.kubji.avro;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by kafka on 29/1/19.
 */
public class AvroPageViewConsumerV2 implements Runnable {

    private final KafkaConsumer<String, PageView> consumer;
    private final String topic;

    public AvroPageViewConsumerV2(String brokers, String groupId, String topic, String schemaRegistryUrl) {
        Properties prop = createConsumerConfig(brokers, groupId, schemaRegistryUrl);
        this.consumer = new KafkaConsumer<>(prop);
        this.topic = topic;
        this.consumer.subscribe(Arrays.asList(this.topic));
    }

    private static Properties createConsumerConfig(String brokers, String groupId, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        return props;
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, PageView> records = consumer.poll(100);
            for (ConsumerRecord<String, PageView> record : records) {
                PageView pageView = record.value();
                /*System.out.println("Receive message: " + record.value() + ", Partition: "
                        + record.partition() + ", Offset: " + record.offset() + ", by ThreadID: "
                        + Thread.currentThread().getId());*/
                System.out.println("Receive message: { " + " ip: " + pageView.getIp() + ", page: " + pageView.getPage() + ", time:"
                        + pageView.getTime() + ", referrer: " + pageView.getReferrer() + " }, Partition: " + record.partition() + ", Offset: " + record.offset() + ", by ThreadID: "
                        + Thread.currentThread().getId());
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Please provide command line arguments: groupID, topic & SchemaRegistryUrl");
            System.exit(-1);
        }
        String groupID = args[0];
        String topic = args[1];
        String schemaRegistryUrl = args[2];

        AvroPageViewConsumerV2 consumer = new AvroPageViewConsumerV2("localhost:9092", groupID, topic,schemaRegistryUrl);
        Thread t1 = new Thread(consumer);
        t1.start();
    }
}