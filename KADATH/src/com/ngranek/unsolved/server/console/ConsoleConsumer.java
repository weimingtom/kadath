package com.ngranek.unsolved.server.console;

import java.util.Hashtable;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.ngranek.unsolved.server.ClientThread;

public class ConsoleConsumer {
	private static ConsoleConsumer instance = null;

	private Hashtable<String, BaseConsumer> consumers = null;
	private ConsumerCallback callback = null;

	private CommandLineParser parser = null;

	private ConsoleConsumer() {
		consumers = new Hashtable<String, BaseConsumer>();
		parser = new PosixParser();

		// TODO: Send this to a property or XML file
		registerConsumer(new SetSkyboxConsumer());
	}

	public void registerConsumer(BaseConsumer consumer) {
		consumers.put(consumer.getCommand(), consumer);
	}

	public static ConsoleConsumer getInstance() {
		if (instance == null) {
			instance = new ConsoleConsumer();
		}

		return instance;
	}

	public void setCallback(ConsumerCallback callback) {
		ConsoleConsumer.getInstance().callback = callback;
	}

	public static ConsumerCallback getCallback() {
		return ConsoleConsumer.getInstance().callback;
	}

	public void consumeMessage(ClientThread client, String message) {
		try {
			String command = message;

			int idx1 = message.indexOf(' ');
			String args[] = null;
			if (idx1 != -1) {
				command = command.substring(0, idx1);
				args = message.substring(command.length()).trim().split(" ");
			}

			command = command.toLowerCase();

			BaseConsumer consumer = consumers.get(command);
			if (consumer != null) {
				try {
					consumer.consumeMessage(client, parser.parse(consumer
							.getOptions(), args));
				} catch (ParseException _e) {
					callback.sendConsoleMessage(client, _e.getMessage());
				}
			} else {
				callback.sendConsoleMessage(client, "No consumer for command /" + command);
			}
		} catch (Exception _e) {
			_e.printStackTrace();
		}
	}
}
