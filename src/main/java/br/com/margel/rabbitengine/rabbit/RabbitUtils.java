package br.com.margel.rabbitengine.rabbit;

import static br.com.margel.rabbitengine.rabbit.ConnectionFactoryBuilder.WEB_PORT;
import static br.com.margel.rabbitengine.rabbit.ConnectionFactoryBuilder.WEB_PROTOCOL;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

import br.com.margel.rabbitengine.model.Priority;

public class RabbitUtils {

	private RabbitUtils() {}
	
	public static void resetVHost(ConnectionFactory factory) {
		try {
			System.out.println("Reseting vHost "+factory.getVirtualHost());
			String address = WEB_PROTOCOL+"://"+factory.getHost()+":"+WEB_PORT+"/api/vhosts/" + factory.getVirtualHost();
			String credential = factory.getUsername()+":"+factory.getPassword();
			
			String encodedCredential = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
			Builder builder = ClientBuilder.newClient().target(address)
					.request().header("Authorization", "Basic " + encodedCredential);
			
			Response delResp = builder.delete();
			Response putResp = builder.put(Entity.json(""));
			
			System.out.println("DEL: "+delResp+"\nPUT: "+putResp);
		} catch (Exception e) {
			System.err.println("Error reset VHost: "+e.getMessage());
		}
	}
	
	public static void sendMessage(Channel channel, String exchange, String routingKey, String message, Priority prio) throws IOException {
		BasicProperties props = new BasicProperties.Builder().contentType("text/plain").priority(prio.ordinal()+1).build();
		channel.basicPublish(exchange, routingKey, props, message.getBytes());
		System.out.println("published: "+message+" prio: "+prio);
	}
	
	public static void declareAndBindQueue(Channel channel, String exchange, String queueName) throws IOException {
		boolean durable = true;
		boolean exclusive = false;
		boolean autoDelete = false;
		Map<String, Object> args = new HashMap<>();
		args.put("x-max-priority", Priority.values().length);
		channel.queueDeclare(queueName, durable, exclusive, autoDelete, args);
		channel.queueBind(queueName, exchange, queueName);
	}
}
