package com.ngranek.unsolved.server.console;

import com.ngranek.unsolved.server.ClientThread;

public interface ConsumerCallback {
	public void sendConsoleMessage(ClientThread client, String message);
}
