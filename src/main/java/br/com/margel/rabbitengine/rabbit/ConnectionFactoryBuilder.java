package br.com.margel.rabbitengine.rabbit;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.rabbitmq.client.ConnectionFactory;

public class ConnectionFactoryBuilder {
	public static final String HOST = "localhost";
	public static final Integer PORT = 5672;
	public static final String USER = "admin";
	public static final String PASSWD = "111111";
	public static final String WEB_PROTOCOL = "http";
	public static final Integer WEB_PORT = 15672;
	public static final boolean USE_SSL = false;
	
	private ConnectionFactoryBuilder() {/**/}
	
	public static ConnectionFactory build(String virtualHost) throws KeyManagementException, NoSuchAlgorithmException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setVirtualHost(virtualHost);
		factory.setHost(HOST);
		factory.setPort(PORT);
		factory.setUsername(USER);
		factory.setPassword(PASSWD);
		if(USE_SSL) {
			factory.useSslProtocol();
		}
		return factory;
	}
	
}