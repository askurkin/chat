package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;


public class Main extends JFrame implements Runnable {
	protected JTextArea outTextArea;
	protected JPanel southPanel;
	protected JTextField inTextField;
	protected JButton inTextSendButton;
	protected boolean isOn;

	Network network;

	public Main(String title, Network network) throws HeadlessException {
		super(title);
		southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(2, 1, 10, 10));
		southPanel.add(inTextField = new JTextField());
		inTextField.setEditable(true);
		southPanel.add(inTextSendButton = new JButton("Send message"));
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(BorderLayout.CENTER, outTextArea = new JTextArea());
		outTextArea.setEditable(false);
		cp.add(BorderLayout.SOUTH, southPanel);

		this.network = network;

		ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = inTextField.getText();
				try {
					network.sendMeassage(text);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		};

		inTextSendButton.addActionListener(action);
		inTextField.addActionListener(action);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 500);
		setVisible(true);
		inTextField.requestFocus();
		(new Thread(this)).start();
		this.network.setCallback(new Callback() {
			@Override
			public void call(Object... args) {
				outTextArea.append((String) args[0] + "\n");
				inTextField.setText(null);
			}
		});
	}

	public static void main(String[] args) throws Exception {

		try (Network network = new Network()) {
			network.connect(8080);
			new Main("chat", network);
			Scanner scanner = new Scanner(System.in);
			while (true) {
				String msg = scanner.nextLine();
				network.sendMeassage(msg);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {

	}
}
