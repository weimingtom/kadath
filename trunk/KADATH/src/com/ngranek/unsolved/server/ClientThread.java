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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Logger;

import com.ngranek.unsolved.server.messages.AbstractMessage;

public class ClientThread extends Thread {
	private Socket socket = null;
	private MessagesConsumer server = null;
	
	public boolean running = false;
	
	private DataInputStream in = null;
	private DataOutputStream out = null;
	
	private Hashtable<String, Object> properties = null;
	
	private int sessionId = -1;
	
	private String login = null;
	
	public ClientThread(Socket socket, MessagesConsumer server) throws Exception {
		this.socket = socket;
		this.server = server;
		
		properties = new Hashtable<String, Object>();
		
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		
		server.getLogger().info("New connection from '" + socket.getRemoteSocketAddress());
	}
	
	public void run() {
		running = true;
		while (running) {
			try {
				
				// Skip the first four bytes: the NL library sends 'NL' and an int16 with the message
				// length, but the length is already in our message.

				/*for (int i = 0 ; i < 4 ; i++) {
					byte bt = in.readByte();
					server.getLogger().info("Heading " + i + ": '" + bt + "' (" + ((char) bt) + ")");
				}*/
				
				int messageSize = in.readInt()/* - 4*/;
				server.getLogger().info("New message from '" + socket.getRemoteSocketAddress() + " with size '" + messageSize + "'");
				byte messageBytes[] = new byte[messageSize];
				int total = 0;
				while (total < messageSize) {
					int read = in.read(messageBytes, total, messageSize - total);
					server.getLogger().info("Read '" + read + "'");
					if (read != -1) {
						total += read;
					} else {
						break;
					}
				}
				
				// Read the trailing -1
				{
					//byte bt = in.readByte();
					//server.getLogger().info("Trailing: '" + bt + "' (" + ((char) bt) + ")");
				}
				
				AbstractMessage message = server.buildMessage(messageBytes);
				if (message != null) {
					server.consume(message, this);
				}
			} catch (Exception _e) {
				if (!(_e instanceof EOFException)) {
					_e.printStackTrace();
				}
				Logger.getLogger("unsolved").info("Client for '" + socket.getRemoteSocketAddress() + "' disconnected");
				server.killClient(this);
			}
		}
	}
	
	public void kill() {
		running = false;
		try {
			socket.close();
		} catch (Exception _e) {
			
		}
	}
	
	public void sendMessageSafe(AbstractMessage message) {
		try {
			sendMessage(message);
		} catch (Exception _e) {
			_e.printStackTrace();
		}
	}
	
	public void sendMessage(AbstractMessage message) throws IOException {
		byte[] bt = message.getBytes();
		
		try {
			Logger.getLogger("unsolved").info("Sending " + bt.length + " bytes (code = '" + (message.getClass().getMethod("getId", (Class[]) null).invoke(null, (Object[]) null)) + "')");
		} catch (Exception _e) {
			_e.printStackTrace();
		}
		
		String str = "[";
		for (int i = 0 ; i < bt.length ; i++) {
			str += (int) bt[i];
			if (i < bt.length - 1) {
				str += ", ";
			}
		}
		str += "]";
		Logger.getLogger("unsolved").info("Message to send: " + str);
		
		//out.writeByte('N');
		//out.writeByte('L');
		//out.writeShort((short) bt.length);
		out.write(bt);
		out.flush();
	}
	
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
