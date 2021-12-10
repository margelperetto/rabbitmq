package br.com.margel.rabbitengine.engine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

import br.com.margel.rabbitengine.db.dao.JobDao;
import br.com.margel.rabbitengine.model.Job;
import br.com.margel.rabbitengine.model.Priority;
import br.com.margel.rabbitengine.rabbit.ConnectionFactoryBuilder;
import br.com.margel.rabbitengine.rabbit.RabbitUtils;

public abstract class EngineBase {

	private static final String VIRTUAL_HOST_NAME = "host1";
	private static final String CONNECTION_NAME = "connection1";
	static final String EXCHANGE_NAME = "exchange1";
	static final String SLAVE_QUEUE = "SLAVE_QUEUE";
	static final String RELOAD_MSG = "RELOAD_QUEUE_MSG";

	ConnectionFactory factory;
	Connection connection;
	Map<String, Channel> channels = new HashMap<>();

	final JobDao dao = new JobDao();
	private boolean reloading;

	EngineBase() {
		createFactory();
		initiate();
	}

	public abstract void reloadQueue() throws IOException;
	abstract void initiate();
	abstract void startConsumer(Channel channel, String queueName) throws IOException;

	public void addJob(String queue, String message, Priority prio) throws SQLException, IOException {
		Job job = new Job();
		job.setOid(UUID.randomUUID().toString());
		job.setCreated(System.currentTimeMillis());
		job.setQueue(queue);
		job.setMsg(message);
		job.setPriority(prio);
		dao.insert(job);
		sendToRabbit(job);
	}
	
	synchronized void startReload() {
		if(reloading) {
			System.err.println("Already reloading...");
			return;
		}
		reloading = true;
		closeChannelsAndConnection();
		Thread tr = new Thread(()->{
			int attempt = 0;
			do {
				try {
					Thread.sleep(1000);
					initiate();
				} catch (Exception e) {
					System.err.println("unable to recovery "+(++attempt)+" -> "+e.getMessage());
				}
			} while (connection==null);
			reloading = false;
			System.out.println("Queue engine reloaded!");
		});
		tr.setName("Queue reload (SETHREAD)");
		tr.start();
	}

	void sendToRabbit(Job job) throws IOException {
		sendToRabbit(job.getQueue(), job.getOid(), job.getPriority());
	}

	private void createFactory() {
		try {
			factory = ConnectionFactoryBuilder.build(VIRTUAL_HOST_NAME);
		} catch (Exception e) {
			throw new RuntimeException("Error create conn factory!", e);
		}
	}

	void sendToRabbit(String queue, String msg, Priority prio) throws IOException {
		if(queue==null || queue.trim().isEmpty()) {
			queue = "DEFAULT_QUEUE";
		}
		Channel channel = checkChannelAndStartConsumer(queue);
		RabbitUtils.sendMessage(channel, EXCHANGE_NAME, queue, msg, prio);
	}

	Channel checkChannelAndStartConsumer(String queue) throws IOException {
		synchronized (channels) {
			Channel channel = channels.get(queue);
			if(channel==null || !channel.isOpen()) {
				channels.remove(queue);
				if(connection==null) throw new RuntimeException("Could not create channel for "+queue+". Waiting connection");
				channel = connection.createChannel();
				channels.put(queue, channel);
				RabbitUtils.declareAndBindQueue(channel, EXCHANGE_NAME, queue);
				startConsumer(channel, queue);
				System.out.println("Created new channel for "+queue);
			}
			return channel;
		}
	}

	void initConnectionAndExchange() {
		try {
			connection = factory.newConnection(Executors.newFixedThreadPool(2), CONNECTION_NAME);
			System.out.println("Connection created");
			connection.addShutdownListener(sig->{
				System.err.println("Connection closed -> "+sig);
				startReload();
			});
			try(Channel channel = connection.createChannel()){
				channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error create rabbit connection", e);
		}
	}

	void channelShutdown(ShutdownSignalException sig, String queue) {
		synchronized (channels) {
			System.err.println("Channel shutdown "+queue+" -> "+sig);
			channels.remove(queue);
		}
	}

	private void closeChannelsAndConnection() {
		synchronized (channels) {
			for(Entry<String, Channel> entry : new HashSet<>(channels.entrySet())) {
				try {
					entry.getValue().close();
				} catch (Exception e) {
					System.err.println("Close channel fail! "+entry.getKey()+" -> "+e.getMessage());
				}
			}
			channels.clear();
		}
		if (connection!=null) {
			try {
				connection.close();
			} catch (Exception e) {
				System.err.println("Close connection fail! "+e.getMessage());
			}
		}
		connection = null;
	}
}