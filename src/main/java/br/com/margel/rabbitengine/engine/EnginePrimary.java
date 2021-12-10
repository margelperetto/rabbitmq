package br.com.margel.rabbitengine.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import br.com.margel.rabbitengine.model.Job;
import br.com.margel.rabbitengine.rabbit.RabbitUtils;

class EnginePrimary extends EngineBase{

	@Override
	void initiate() {
		RabbitUtils.resetVHost(factory);
		initConnectionAndExchange();
		startSlaveReceiver();
		loadJobs();
	}

	@Override
	public void reloadQueue() throws IOException {
		startReload();
	}

	@Override
	void startConsumer(Channel channel, String queueName) throws IOException {
		startJobReceiver(channel, queueName);
	}

	private void startJobReceiver(Channel channel, String queueName) throws IOException {
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String messageBoby = new String(delivery.getBody(), StandardCharsets.UTF_8);
			jobReceived(messageBoby);
			boolean multiple = false;
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), multiple);
		};
		CancelCallback cancelCallback = consumerTag -> System.err.println("cancel "+queueName+": "+consumerTag);

		boolean autoAck = false;
		channel.basicQos(1); 
		channel.basicConsume(queueName, autoAck, deliverCallback, cancelCallback);
		channel.addShutdownListener(sig->channelShutdown(sig, queueName));
	}

	private void jobReceived(String oid) {
		try {
			dao.finishJob(oid, System.currentTimeMillis());
			System.out.println("job finished! "+oid);
		} catch (Exception e) {
			throw new RuntimeException("Error finish job", e);
		}
	}
	
	private void loadJobs() {
		Thread tr = new Thread(()->{
			try {
				System.out.println("Loading jobs...");
				List<Job> jobs = dao.getWaitingJobs();
				for (Job job : jobs) {
					sendToRabbit(job);
				}
				System.out.println("Jobs loaded! "+jobs.size());
			} catch (Exception e) {
				throw new RuntimeException("Error load jobs!", e);
			}
		});
		tr.setName("Queue jobs loader");
		tr.start();
	}

	private void startSlaveReceiver() {
		try {
			Channel channel = connection.createChannel();
			RabbitUtils.declareAndBindQueue(channel, EXCHANGE_NAME, SLAVE_QUEUE);
			synchronized (channels) {
				channels.put(SLAVE_QUEUE, channel);
			}

			DeliverCallback deliverCallback = (tag, delived) -> {
				String messageBoby = new String(delived.getBody(), StandardCharsets.UTF_8);
				if(RELOAD_MSG.equals(messageBoby)) {
					reloadQueue();
				} else {
					checkChannelAndStartConsumer(messageBoby);
				}
			};
			CancelCallback cancelCallback = tag -> System.err.println("cancel "+SLAVE_QUEUE+": "+tag);

			channel.basicQos(1); 
			channel.basicConsume(SLAVE_QUEUE, true, deliverCallback, cancelCallback);
			channel.addShutdownListener(sig->channelShutdown(sig, SLAVE_QUEUE));
		} catch (Exception e) {
			throw new RuntimeException("Error start slave receiver!", e);
		}
	}
}