package br.com.margel.rabbitengine.engine;

import java.io.IOException;

import com.rabbitmq.client.Channel;

import br.com.margel.rabbitengine.model.Priority;
import br.com.margel.rabbitengine.rabbit.RabbitUtils;

class EngineSecondary extends EngineBase{

	@Override
	void initiate() {
		initConnectionAndExchange();
	}

	@Override
	public void reloadQueue() throws IOException {
		sendToRabbit(SLAVE_QUEUE, RELOAD_MSG, Priority.MAX);
	}

	@Override
	void startConsumer(Channel channel, String queueName) throws IOException {
		RabbitUtils.sendMessage(channel, EXCHANGE_NAME, SLAVE_QUEUE, queueName, Priority.MIN);
	}

}
