package com.bigProject_streaming;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class propsConsumer {
	public static Consumer<String,String>createConsumer(String topic) throws IOException
    {
		getProperties properties1 = new getProperties();		
		Properties project_props = properties1.readProperties();
    	Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, project_props.getProperty("BOOTSTRAP_SERVERS"));
		props.put(ConsumerConfig.GROUP_ID_CONFIG,project_props.getProperty("GROUP_ID_CONFIG"));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,Integer.parseInt(project_props.getProperty("MAX_POLL_RECORDS_CONFIG")));
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,project_props.getProperty("ENABLE_AUTO_COMMIT_CONFIG"));
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,project_props.getProperty("AUTO_OFFSET_RESET_CONFIG"));
		Consumer<String,String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(topic));
		return consumer;
    }
	
	public static HashMap<String, Object> kafkaParams() throws IOException{
		getProperties properties2 = new getProperties();		
		Properties project_props = properties2.readProperties();
		HashMap<String,Object> params = new HashMap<>();
	   	params.put("bootstrap.servers", project_props.getProperty("BOOTSTRAP_SERVERS"));
	   	params.put("key.deserializer", StringDeserializer.class.getName());
	   	params.put("value.deserializer", StringDeserializer.class.getName());
	   	params.put("group.id", "group2");
	   	params.put("auto.offset.reset", "latest");    
	   	params.put("enable.auto.commit",true);
	   	return params;
	}
}
