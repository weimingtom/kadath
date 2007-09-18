package com.ngranek.unsolved.server.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.ngranek.unsolved.server.ClientThread;

public abstract class BaseConsumer {
	protected Options options = null;
	
	public BaseConsumer() {
		options = new Options();
		init();
	}
	
	public Options getOptions() {
		return options; 
	}
	
	public void sendHelp(ClientThread client) {
		HelpFormatter formatter = new HelpFormatter();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		formatter.printHelp(pw, 80, getCommand(), "", options, 1, 4, "", true);
		pw.flush();
		pw.close();
		
		String message = new String(baos.toByteArray());
		ConsoleConsumer.getCallback().sendConsoleMessage(client, message);
	}
	
	public abstract void init();
	public abstract void consumeMessage(ClientThread client, CommandLine cli);
	public abstract String getCommand();
}
