package com.ngranek.unsolved.client.states;

import com.jme.input.MouseInput;
import com.ngranek.unsolved.client.Main;
import com.ngranek.unsolved.client.scenes.BaseScene;

public class PlayState extends BaseState {
	private static PlayState instance = null;

	private PlayState() {

	}

	public static PlayState getInstance() {
		if (instance == null) {
			instance = new PlayState();
		}

		return instance;
	}

	public void cleanup() {
		if (Main.getInstance().getCurrentScene() != null) {
			Main.getInstance().getCurrentScene().cleanup();
		}
	}

	public void doInit() {
		MouseInput.get().setCursorVisible(false);
	}
	
	public void setScene(String sceneName) {
		try {
			Class clazz = Class.forName(sceneName);
			BaseScene scene = (BaseScene) clazz.newInstance();
			Main.getInstance().setScene(scene);
		} catch (Exception _e) {
			_e.printStackTrace();
		}
	}

	public void setInfoText(String text) {
	}

	public void update() {
		if (Main.getInstance().getCurrentScene() != null) {
			Main.getInstance().getCurrentScene().update();
		}
	}
}
