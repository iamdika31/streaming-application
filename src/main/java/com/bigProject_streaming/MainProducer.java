package com.bigProject_streaming;

import java.io.IOException;


import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;

import java.util.logging.Logger;
import java.util.logging.Level;

public class MainProducer 
{
	//initialization logger
	static Logger logger = Logger.getLogger(MainProducer.class.getName());
	
	public static void PushTwittermessage(Producer<String, String> producer,String topic) throws InterruptedException, IOException {

		Properties props = new getProperties().readProperties();
  		Crawling_information getData = new Crawling_information();

		
		String consumerKey = props.getProperty("consumerKey");
		String consumerSecret = props.getProperty("consumerSecret");
		String token = props.getProperty("token");
		String secret = props.getProperty("secret");
		
		
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        
        //find tweets contains specific word
        endpoint.trackTerms(Lists.newArrayList("twitterapi", "spotify"));
        Authentication auth = new OAuth1(consumerKey,consumerSecret,token,secret);
        
        //create client builder
        Client client = new ClientBuilder()
        			   .name("client-1")
        			   .hosts(Constants.STREAM_HOST)
                       .endpoint(endpoint)
                       .authentication(auth)
                       .processor(new StringDelimitedProcessor(queue))
                       .build();
        
        //open connection
        client.connect();

        //loop to get tweet message
//        for (int msgRead=0 ; msgRead<100 ; msgRead++) {
        while(true) {
        	try {
        		//take message and save to string
                String message = queue.take();
                //convert message to JsonElement
                JsonElement jsonElement = new JsonParser().parse(message);
                //convert to jsonObject
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                
                //check if tweet have url value
                if(jsonObject.getAsJsonObject("entities").getAsJsonArray("urls").size() > 0 ) {
                	
                	//get expanded_url 
                    String spotify_url = jsonObject.getAsJsonObject("entities").getAsJsonArray("urls").get(0).getAsJsonObject().get("expanded_url").getAsString();
                    
                    // create regex to find url contains 'open.spotify.com'
                    String regex = "\\bopen.spotify.com\\b";
                    
                    //matcher function
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(spotify_url);
                    System.out.println(spotify_url);
                    
                    //check if matcher has match with given url and send to producer
                    if(matcher.find()) {
                        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, message);
                		producer.send(record);
                        logger.log(Level.INFO, "record success send to producer, data:"+record);
                    }
                    else {
                    	logger.log(Level.INFO, "record doesn't contains 'open.spotify.com'");
                    }
                }
                else {
                	logger.log(Level.INFO,"record doesn't have 'urls' object");
                }
             } 
        	catch (InterruptedException e) {
                e.getStackTrace();
                }
        	 
        }
//        producer.close(); 	
//        client.stop();
	}

    public static void main( String[] args ) throws IOException
    {
		Producer<String,String> producer = ProducerCreator.createProducerFe();
		final String topic="twitter-test2";

		try {
			PushTwittermessage(producer,topic);
		}
		catch(InterruptedException e) {
			logger.log(Level.WARNING,e.getMessage());
		}
    }
}
