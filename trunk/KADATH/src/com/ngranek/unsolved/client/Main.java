package com.ngranek.unsolved.client;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import com.jme.app.SimplePassGame;
import com.jme.renderer.pass.RenderPass;
import com.nextj.util.logger.SingleLineFormatter;
import com.ngranek.unsolved.client.scenes.BaseScene;
import com.ngranek.unsolved.client.states.BaseState;
import com.ngranek.unsolved.client.states.IntroState;

public class Main extends KADATHSimplePassGame {
	private BaseState currentState = null;
	private BaseState nextState = null;
	
	private BaseScene currentScene = null;
	private BaseScene nextScene = null;

	private static Main instance = null;

	private NetworkManager networkManager = null;

	public static void main(String[] args) {
		Logger logger = Logger.getLogger("jme");
		logger.setUseParentHandlers(false);

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new SingleLineFormatter());
		logger.addHandler(consoleHandler);

		instance = new Main();
		instance.setDialogBehaviour(SimplePassGame.FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
		//instance.setDialogBehaviour(SimplePassGame.ALWAYS_SHOW_PROPS_DIALOG);
		instance.start();
	}

	public static Main getInstance() {
		return instance;
	}

	public void setState(BaseState state) {
		nextState = state;
	}
	
	public void setScene(BaseScene scene) {
		nextScene = scene;
	}

	private void doSetState() {
		if (currentState != null) {
			currentState.cleanup();
		}

		currentState = nextState;
		nextState = null;

		synchronized (currentState) {
			currentState.init();
		}
		
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		fpsNode.updateGeometricState(0.0f, true);
		fpsNode.updateRenderState();
	}
	
	private void doSetScene() {
		if (currentScene != null) {
			currentScene.cleanup();
		}

		currentScene = nextScene;
		nextScene = null;

		synchronized (currentScene) {
			currentScene.init();
		}
		
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		fpsNode.updateGeometricState(0.0f, true);
		fpsNode.updateRenderState();
	}

	public void cleanup() {
		if (currentState != null) {
			currentState.cleanup();
		}
		
		if (currentScene != null) {
			currentScene.cleanup();
		}
		super.cleanup();
	}

	public void simpleUpdate() {
		if (nextScene != null) {
			doSetScene();
		}
		
		if (currentState != null && currentState.isInitialized()) {
			synchronized (currentState) {
				if (nextState != null) {
					doSetState();
				}
				
				currentState.update();
			}
		}
	}

	public void simpleInitGame() {
		display.setTitle("Unsolved");

		RenderPass fpsPass = new RenderPass();
		fpsPass.add(fpsNode);
		pManager.add(fpsPass);

		networkManager = new NetworkManager();

		setState(IntroState.getInstance());
		doSetState();
	}

	public BaseState getCurrentState() {
		return currentState;
	}
	
	public BaseScene getCurrentScene() {
		return currentScene;
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}
}