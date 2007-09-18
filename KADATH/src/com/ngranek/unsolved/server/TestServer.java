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
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer extends Thread {
	private ServerSocket server = null;

	public class Client extends Thread {
		private Socket s = null;
		
		private DataOutputStream out = null;
		private DataInputStream in = null;

		public Client(Socket s) throws Exception {
			this.s = s;
			
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
		}

		public void run() {
			try {
				System.out.println("*** BEGIN ***");
				
				for (int i = 0 ; i < 4 ; i++) {
					byte b = in.readByte();
				}
				System.out.println("*** END ***");
			} catch (Exception _e) {
				_e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new TestServer().start();
	}

	public void run() {
		try {
			server = new ServerSocket(9876);
		} catch (Exception _e) {
			_e.printStackTrace();
		}
		
		while (true) {
			try {
				Socket s = server.accept();
				new Client(s).start();
			} catch (Exception _e) {
				_e.printStackTrace();
			}
		}
	}
}
