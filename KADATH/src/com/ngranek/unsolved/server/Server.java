/***************************************************************************
 *   Copyright (C) 2007 by Francisco Andrades Grassi                       *
 *   bigjocker@gmail.com                                                   *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package com.ngranek.unsolved.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import com.nextj.util.logger.SingleLineFormatter;
import com.ngranek.unsolved.server.console.ConsoleConsumer;
import com.ngranek.unsolved.server.console.ConsumerCallback;
import com.ngranek.unsolved.server.consumers.ConnectionConsumer;
import com.ngranek.unsolved.server.messages.AbstractMessage;
import com.ngranek.unsolved.server.messages.CommandRequest;

public class Server extends MessagesConsumer implements ConsumerCallback, Runnable {
	private ServerSocket serverSocket = null;
	private int port = 0;
	private Vector<ClientThread> threads = null;

	private boolean running = false;

	private Logger logger = null;

	public static void main(String[] args) throws Exception {
		new Thread(new Server(9876)).start();
	}

	public Server(int port) throws IOException {
		logger = Logger.getLogger("unsolved");
		logger.setUseParentHandlers(false);

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new SingleLineFormatter());
		logger.addHandler(consoleHandler);

		init();
		threads = new Vector<ClientThread>();

		this.port = port;
		serverSocket = new ServerSocket(port);
	}

	private void init() {
		registerConsumer(ConnectionConsumer.class);
		ConsoleConsumer.getInstance().setCallback(this);
	}

	public void run() {
		getLogger().info("Starting UNSOLVED server in port '" + port + "'");
		running = true;
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				ClientThread thread = new ClientThread(socket, this);
				threads.addElement(thread);
				thread.start();
			} catch (Exception _e) {
				_e.printStackTrace();
			}
		}
	}

	public void killClient(ClientThread thread) {
		threads.remove(thread);
		thread.kill();
	}

	public void broadcastMessageToNearClients(AbstractMessage message,
			ClientThread thread, boolean toEnemies) throws IOException {
		// TODO: Only send the message to clients near to the sender
		// TODO: Respect the toEnemies flag (right now the message is sent to
		// everybody)

		for (ClientThread client : threads) {
			client.sendMessage(message);
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public void sendConsoleMessage(ClientThread client, String message) {
		CommandRequest request = new CommandRequest();
		request.setText(message);
		client.sendMessageSafe(request);
	}
}
