package br.com.margel.rabbitengine;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import br.com.margel.rabbitengine.engine.EngineSingleton;
import br.com.margel.rabbitengine.view.Frame;

public class RabbitEngineApp {
	private static boolean primary = false;

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Frame frame = new Frame();
		frame.setVisible(true);
		if(JOptionPane.showConfirmDialog(frame, "Servidor primário?", "Tipo de servidor", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
			primary = true;
		}
		System.out.println("PRIMARY: "+primary);
		frame.setTitle("Servidor "+(primary?"PRIMÁRIO":"SECUNDÁRIO"));
		EngineSingleton.getInstance();
	}
	
	public static boolean isPrimary() {
		return primary;
	}
}