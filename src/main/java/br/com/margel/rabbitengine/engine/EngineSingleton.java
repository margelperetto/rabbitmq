package br.com.margel.rabbitengine.engine;

import br.com.margel.rabbitengine.RabbitEngineApp;

public class EngineSingleton {
	
	private static EngineBase engine;
	
	public static synchronized EngineBase getInstance() {
		if(engine == null) {
			engine = RabbitEngineApp.isPrimary() ? new EnginePrimary() : new EngineSecondary();
			System.out.println("Engine initiated!");
		}
		return engine;
	}
}
