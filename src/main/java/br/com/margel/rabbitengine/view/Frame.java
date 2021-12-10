package br.com.margel.rabbitengine.view;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import br.com.margel.rabbitengine.engine.EngineSingleton;
import br.com.margel.rabbitengine.model.Priority;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class Frame extends JFrame {
	private JTextField tfMsg = new JTextField("msg");
	private JTextField tfQueue = new JTextField("fila1");
	private JComboBox<Priority> cbPrio = new JComboBox<>(Priority.values());

	public Frame() {
		setLayout(new MigLayout(new LC().fill()));
		add(createAddjobPanel(), new CC().grow());
		add(createActionsPanel(), new CC().grow());

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	private Component createAddjobPanel() {
		JButton btnAddJob = new JButton("Adicionar");
		btnAddJob.addActionListener(evt->sendMessage());
		JPanel panel = new JPanel(new MigLayout(new LC().fillX()));
		panel.add(new JLabel("Mensagem"), new CC().wrap());
		panel.add(tfMsg, new CC().minWidth("150").growX().wrap());
		panel.add(new JLabel("Fila"), new CC().wrap());
		panel.add(tfQueue, new CC().growX().wrap());
		panel.add(new JLabel("Prioridade"), new CC().spanX().split());
		panel.add(cbPrio, new CC().wrap());
		panel.add(new JSeparator(), new CC().spanX().growX().wrap());
		panel.add(btnAddJob, new CC());
		return panel;
	}

	private Component createActionsPanel() {
		JButton btnReloadQueue = new JButton("Recarregar mensagens");
		btnReloadQueue.addActionListener(evt->reloadQueue());
		JPanel panel = new JPanel(new MigLayout(new LC().fillX()));
		panel.add(btnReloadQueue, new CC());
		return panel;
	}

	private void sendMessage() {
		try {
			String msg = tfMsg.getText();
			String queue = tfQueue.getText();
			Priority prio = cbPrio.getItemAt(cbPrio.getSelectedIndex());
			EngineSingleton.getInstance().addJob(queue, msg, prio);
			JOptionPane.showMessageDialog(this, "Mensagem adicionada!");
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Erro ao adicionar mensagem!", "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void reloadQueue() {
		try {
			EngineSingleton.getInstance().reloadQueue();
			JOptionPane.showMessageDialog(this, "Recarregado com sucesso!");
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Erro ao recarregar!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}