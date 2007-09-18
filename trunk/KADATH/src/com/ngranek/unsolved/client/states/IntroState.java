package com.ngranek.unsolved.client.states;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jme.input.MouseInput;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.state.LightState;
import com.jmex.awt.swingui.JMEDesktop;
import com.ngranek.unsolved.server.messages.ConnectionRequest;
import com.ngranek.unsolved.client.Main;

public class IntroState extends BaseState {
	private static IntroState instance = null;
	
	private IntroState() {

	}
	
	public static IntroState getInstance() {
		if (instance == null) {
			instance = new IntroState();
		}

		return instance;
	}
	
	private Node guiNode = null;
	
	private JLabel infoLabel = null;

	public void cleanup() {
	}

	public void doInit() {
		guiNode = new Node("gui");
		guiNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);

		int width = Main.getInstance().getDisplaySystem().getWidth();
		int height = Main.getInstance().getDisplaySystem().getHeight();

		// create the desktop Quad
		final JMEDesktop desktop = new JMEDesktop("desktop", 350, 280, Main.getInstance()
				.getInputHandler());
		// and attach it to the gui node
		guiNode.attachChild(desktop);
		// center it on screen
		desktop.getLocalTranslation().set(width/2, height/2, 0);
		
		final JMEDesktop infoDesktop = new JMEDesktop("infodesktop", width, 80, Main.getInstance()
				.getInputHandler());
		guiNode.attachChild(infoDesktop);
		infoDesktop.getLocalTranslation().set(width/2, height - 100, 0);

		final Color color = new Color(1, 1, 1, 0.8f);
		infoLabel = new JLabel("");
		infoLabel.setForeground(color);
		infoDesktop.getJDesktop().setLayout(new FlowLayout());
		infoDesktop.getJDesktop().add(infoLabel);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				desktop.getJDesktop().setBackground(new Color(0, 0, 0.2f, 0.8f));
				
				JLabel loginLabel = new JLabel("Login");
				desktop.getJDesktop().add(loginLabel);
				loginLabel.setLocation(50, 50);
				loginLabel.setForeground(color);
				loginLabel.setSize(loginLabel.getPreferredSize());
				
				final JTextField loginText = new JTextField("bigjocker");
				desktop.getJDesktop().add(loginText);
				loginText.setLocation(160, 45);
				loginText.setSize(150, 25);
				
				JLabel passwordLabel = new JLabel("Password");
				desktop.getJDesktop().add(passwordLabel);
				passwordLabel.setLocation(50, 85);
				passwordLabel.setForeground(color);
				passwordLabel.setSize(passwordLabel.getPreferredSize());
				
				final JPasswordField passwordText = new JPasswordField("test");
				desktop.getJDesktop().add(passwordText);
				passwordText.setLocation(160, 80);
				passwordText.setSize(150, 25);
				
				JLabel serverLabel = new JLabel("Server");
				desktop.getJDesktop().add(serverLabel);
				serverLabel.setLocation(50, 120);
				serverLabel.setForeground(color);
				serverLabel.setSize(serverLabel.getPreferredSize());
				
				final JTextField serverText = new JTextField("localhost");
				desktop.getJDesktop().add(serverText);
				serverText.setLocation(160, 115);
				serverText.setSize(150, 25);

				final JButton button = new JButton("Log in");
				desktop.getJDesktop().add(button);
				button.setLocation(240, 200);
				button.setSize(button.getPreferredSize());

				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setInfoText("Connecting ...");
						
						String login = loginText.getText();
						String password = new String(passwordText.getPassword());
						String serverName = serverText.getText();
						int port = 9876;
						
						try {
							Main.getInstance().getNetworkManager().setLogin(login);
							Main.getInstance().getNetworkManager().setPassword(password);
							
							try {
								Main.getInstance().getNetworkManager().init(serverName, port);
								ConnectionRequest request = new ConnectionRequest();
								request.setLogin(login);
								Main.getInstance().getNetworkManager().getClient().sendMessage(request);
							} catch (Exception _e) {
								setInfoText("Could not connect: " + _e.getMessage());
							}
						} catch (Exception _e) {
							_e.printStackTrace();
						}
					}
				});
				
				setInfoText("Welcome to Unsolved v0.8 (KADATH v2). Use your login and password to enter");
			}
		});

		guiNode.setCullMode(SceneElement.CULL_NEVER);
		guiNode.setLightCombineMode(LightState.OFF);
		guiNode.updateRenderState();
		guiNode.updateGeometricState(0, true);
		MouseInput.get().setCursorVisible(true);
	}
	
	public void setInfoText(String text) {
		infoLabel.setText(text);
	}

	public void update() {
		Main.getInstance().getDisplaySystem().getRenderer().draw(guiNode);
	}
}
