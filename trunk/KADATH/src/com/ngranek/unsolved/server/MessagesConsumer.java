package com.ngranek.unsolved.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;

import com.ngranek.unsolved.server.messages.AbstractMessage;
import com.ngranek.unsolved.server.messages.MessageIndexer;

public abstract class MessagesConsumer {
	protected HashMap<String, Method> consumers = null;
	
	public MessagesConsumer() {
		consumers = new HashMap<String, Method>();
	}
	
	public void consume(AbstractMessage message, ClientThread thread) throws Exception {
		String messageName = message.getClass().getName();
		messageName = messageName.substring(messageName.lastIndexOf('.') + 1);

		Method method = consumers.get(messageName);
		if (method != null) {
			getLogger().info("Found consumer for '" + messageName + "' (" + method + ")");
			method.invoke(null, new Object[] { this, thread, message });
		} else {
			getLogger().warning("No consumer for message '" + messageName + "'");
		}
	}

	

	public AbstractMessage buildMessage(byte[] messageBytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(messageBytes);
		DataInputStream dis = new DataInputStream(bais);

		String debugMessage = "[";
		for (int i = 0; i < messageBytes.length; i++) {
			debugMessage += messageBytes[i];
			if (i < messageBytes.length - 1) {
				debugMessage += ", ";
			}
		}
		debugMessage += "]";

		getLogger().info("Message bytes: " + debugMessage);

		int messageId = dis.readInt();
		int sessionId = dis.readInt();
		getLogger().info("Message code: " + messageId);
		getLogger().info("Session: " + sessionId);
		AbstractMessage message = MessageIndexer.createMessageForId(messageId);
		if (message != null) {
			message.setSessionId(sessionId);
			message.buildMessage(messageBytes);
		} else {
			getLogger().warning(
					"No message could be constructed (code = '" + messageId
							+ "')");
		}

		return message;
	}
	
	@SuppressWarnings("unchecked")
	public void registerConsumer(Class clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodName = methods[i].getName();
			if (methodName.startsWith("process")) {
				String messageName = methodName.substring(7);
				consumers.put(messageName, methods[i]);

				getLogger().info(
						"Registered method '" + clazz.getCanonicalName() + "."
								+ methods[i].getName() + "' for message '"
								+ messageName + "'");
			}
		}
	}

	public abstract void killClient(ClientThread thread);
	public abstract Logger getLogger();
}