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

package com.ngranek.unsolved.server.consumers;

import java.io.IOException;
import java.security.MessageDigest;

import com.nextj.util.security.PasswordAuthenticator;
import com.ngranek.unsolved.client.config.KADATHConfig;
import com.ngranek.unsolved.server.ClientThread;
import com.ngranek.unsolved.server.MessagesConsumer;
import com.ngranek.unsolved.server.Server;
import com.ngranek.unsolved.server.console.ConsoleConsumer;
import com.ngranek.unsolved.server.messages.AbstractMessage;
import com.ngranek.unsolved.server.messages.CommandRequest;
import com.ngranek.unsolved.server.messages.ConnectionRequest;
import com.ngranek.unsolved.server.messages.ConnectionResponse;
import com.ngranek.unsolved.server.messages.LoginRequest;
import com.ngranek.unsolved.server.messages.LoginResponse;
import com.ngranek.unsolved.server.messages.SampleMessage;

public class ConnectionConsumer {
	public static void processConnectionRequest(MessagesConsumer server,
			ClientThread thread, AbstractMessage message) throws Exception {
		ConnectionRequest request = (ConnectionRequest) message;
		ConnectionResponse response = new ConnectionResponse();

		String salt = PasswordAuthenticator.createSalt();
		thread.setProperty("login.salt", salt);
		server.getLogger().info("Sending salt '" + salt + "'");
		response.setSalt(salt);

		thread.sendMessage(response);
	}

	public static void processLoginRequest(MessagesConsumer server, ClientThread thread,
			AbstractMessage message) throws Exception {
		LoginRequest loginRequest = (LoginRequest) message;

		String salt = (String) thread.getProperty("login.salt");

		// TODO: Check the password from DB
		String toDigest = salt + "test";

		server.getLogger().info("To digest: '" + toDigest + "'");

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte bt[] = digest.digest(toDigest.getBytes());

		String localPassword = byteArrayToHexString(bt).toLowerCase();
		boolean ok = localPassword.equalsIgnoreCase(loginRequest.getPassword());

		server.getLogger().info("Digest: '" + localPassword + "'");
		server.getLogger().info(
				"Received: '" + loginRequest.getPassword() + "'");
		server.getLogger().info("Result = '" + ok + "'");
		
		LoginResponse response = new LoginResponse();
		
		if (ok) {
			thread.setSessionId((int) (Math.random()*10000000));
			thread.setLogin(loginRequest.getLogin());
			response.setScene(KADATHConfig.getProperty("com.ngranek.unsolved.test.level"));
			
		} else {
			thread.setSessionId(-1);
		}
		
		
		response.setSessionId(thread.getSessionId());
		thread.sendMessage(response);
	}

	public static void processSampleMessage(MessagesConsumer server, ClientThread thread,
			AbstractMessage message) throws IOException {
		SampleMessage m = (SampleMessage) message;
		server.getLogger().info("Session '" + m.getSessionId() + "'");
		server.getLogger().info("RCVDi '" + m.getIntVar() + "'");
		server.getLogger().info("RCVDs '" + m.getStringVar() + "'");
		thread.sendMessage(message);
	}
	
	public static void processCommandRequest(Server server, ClientThread thread,
			AbstractMessage message) throws IOException {
		
		CommandRequest request = (CommandRequest) message;
		request.setText(request.getText().trim());
		server.getLogger().info("Received command: '" + request.getText() + "'");
		
		if (!request.getText().trim().startsWith("/")) {
			request.setText(thread.getLogin() + ": " + request.getText().trim());
			server.broadcastMessageToNearClients(message, thread, false);
		} else {
			ConsoleConsumer.getInstance().consumeMessage(thread, request.getText());
		}
	}

	public static String byteArrayToHexString(byte in[]) {
		byte ch = 0x00;
		int i = 0;

		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };

		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(pseudo[(int) ch]);
			ch = (byte) (in[i] & 0x0F);
			out.append(pseudo[(int) ch]);
			i++;
		}

		String rslt = new String(out);
		return rslt;

	}
}
