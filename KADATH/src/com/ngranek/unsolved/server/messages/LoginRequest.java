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

package com.ngranek.unsolved.server.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class LoginRequest extends AbstractMessage {
	private int sessionId = -1;
	
	public int getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public static int getId() {
		return 3;
	}
	
	public void buildMessage(byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);

		int id = dis.readInt();
		sessionId = dis.readInt();
				login = dis.readUTF();
		password = dis.readUTF();

	}
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		dos.writeInt(getId());
		dos.writeInt(getSessionId());
				dos.writeUTF(login);
		dos.writeUTF(password);

		
		byte[] bt = baos.toByteArray();
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		dos.writeInt(bt.length);
		dos.write(bt);
		
		return baos.toByteArray();
	}

		private String login;
	public void setLogin(String login) {
	
this.login = login;
	}
	public String getLogin() {
		return login;
	}
	private String password;
	public void setPassword(String password) {
	
this.password = password;
	}
	public String getPassword() {
		return password;
	}

}
