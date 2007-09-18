package com.ngranek.unsolved.client.states;

public abstract class BaseState {
	protected boolean initialized = false;

	protected BaseState() {
		initialized = false;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public final void init() {
		doInit();
		initialized = true;
	}

	abstract public void doInit();

	abstract public void cleanup();

	abstract public void update();

	abstract public void setInfoText(String text);
}
