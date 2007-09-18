package com.ngranek.unsolved.server.console;

import org.apache.commons.cli.CommandLine;

import com.ngranek.unsolved.server.ClientThread;
import com.ngranek.unsolved.server.messages.SetSkyboxRequest;

public class SetSkyboxConsumer extends BaseConsumer {

	public void consumeMessage(ClientThread client, CommandLine cli) {
		String name = cli.getOptionValue("name");
		
		if (name != null) {
			SetSkyboxRequest request = new SetSkyboxRequest();
			request.setName(name);
			client.sendMessageSafe(request);
		} else {
			sendHelp(client);
		}
	}

	public void init() {
		options.addOption("name", true, "the name of the skybox to set");
	}
	
	public String getCommand() {
		return "/setskybox";
	}
}
