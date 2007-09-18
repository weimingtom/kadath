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

public class MessageIndexer {
	public static AbstractMessage createMessageForId(int id) {
		if (id == 0) {return new SampleMessage();}
if (id == 1) {return new ConnectionRequest();}
if (id == 2) {return new ConnectionResponse();}
if (id == 3) {return new LoginRequest();}
if (id == 4) {return new LoginResponse();}
if (id == 5) {return new CommandRequest();}
if (id == 6) {return new CommandResponse();}
if (id == 7) {return new SetSkyboxRequest();}

		return null;
	}
}