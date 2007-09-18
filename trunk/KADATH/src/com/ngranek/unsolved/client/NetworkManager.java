package com.ngranek.unsolved.client;

import java.net.Socket;
import java.util.logging.Logger;

import com.ngranek.unsolved.server.ClientThread;
import com.ngranek.unsolved.server.MessagesConsumer;
import com.ngranek.unsolved.client.consumers.ClientConnectionConsumer;

public class NetworkManager extends MessagesConsumer {
	private Socket socket = null;
	private ClientThread client = null;
	
	private String login = null;
	private String password = null;
	
	public void init(String host, int port) throws Exception {
		socket = new Socket(host, port);
		
		registerConsumer(ClientConnectionConsumer.class);
		
		client = new ClientThread(socket, this);
		client.start();
	}
	
	public Logger getLogger() {
		return Logger.getLogger("jme");
	}
	
	public void kill() {
		client.kill();
	}

	public void killClient(ClientThread thread) {
		thread.kill();
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ClientThread getClient() {
		return client;
	}
}
