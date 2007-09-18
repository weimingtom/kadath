package com.ngranek.unsolved.client.test;

import javax.swing.ImageIcon;

import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.state.CullState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.FaultFractalHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;
import com.ngranek.unsolved.client.SkyDome;

/**
 * TestSkyDome.java
 * 
 * @author Highnik
 */
public class TestSkyDome extends SimpleGame {

	private SkyDome dome;
	private TerrainPage terrain;
	private Vector3f camPos = new Vector3f();

	/**
	 * Update time
	 */
	protected void simpleUpdate() {
		camPos.x = cam.getLocation().x;
		camPos.y = terrain.getHeight(cam.getLocation()) + 10;
		camPos.z = cam.getLocation().z;
		cam.setLocation(camPos);
		dome.update();
	}

	/**
	 * update colors
	 */
	protected void simpleRender() {
		dome.render();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jme.app.SimpleGame#initGame()
	 */
	protected void simpleInitGame() {
		display.setTitle("TestSkyDome");

		lightState.setTwoSidedLighting(true);

		setupTerrain();

		setupSkyDome();
	}

	/**
	 * add terrain
	 */
	private void setupTerrain() {

		CullState cs = DisplaySystem.getDisplaySystem().getRenderer().createCullState();
		cs.setCullMode(CullState.CS_BACK);
		cs.setEnabled(true);
		rootNode.setRenderState(cs);

		String dir = "/home/bigjocker/workspace/KADATH/testdata/texture/";

		FaultFractalHeightMap heightMap = new FaultFractalHeightMap(257, 32, 0, 255, 0.75f);
		Vector3f terrainScale = new Vector3f(10, 0.75f, 10);
		heightMap.setHeightScale(0.001f);
		terrain = new TerrainPage("Terrain", 33, heightMap.getSize(), terrainScale, heightMap.getHeightMap(), false);
		terrain.setDetailTexture(1, 128);
		rootNode.attachChild(terrain);

		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(heightMap);
		pt.addTexture(new ImageIcon(dir + "grassb.png"), -128, 0, 155);
		pt.addTexture(new ImageIcon(dir + "dirt.jpg"), 0, 155, 220);
		pt.addTexture(new ImageIcon(dir + "highest.jpg"), 155, 220, 512);
		pt.createTexture(512);

		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setEnabled(true);

		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(), Texture.MM_LINEAR_LINEAR,
				Texture.FM_LINEAR, true);
		ts.setTexture(t1, 0);

		Texture t2 = TextureManager.loadTexture(TestSkyDome.class.getClassLoader().getResource(
				"jmetest/data/texture/Detail.jpg"), Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		ts.setTexture(t2, 1);
		t2.setWrap(Texture.WM_WRAP_S_WRAP_T);

		t1.setApply(Texture.AM_COMBINE);
		t1.setCombineFuncRGB(Texture.ACF_MODULATE);
		t1.setCombineSrc0RGB(Texture.ACS_TEXTURE);
		t1.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
		t1.setCombineSrc1RGB(Texture.ACS_PRIMARY_COLOR);
		t1.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
		t1.setCombineScaleRGB(1.0f);

		t2.setApply(Texture.AM_COMBINE);
		t2.setCombineFuncRGB(Texture.ACF_ADD_SIGNED);
		t2.setCombineSrc0RGB(Texture.ACS_TEXTURE);
		t2.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
		t2.setCombineSrc1RGB(Texture.ACS_PREVIOUS);
		t2.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
		t2.setCombineScaleRGB(1.0f);
		terrain.setRenderState(ts);
	}

	/**
	 * Initialize SkyDome
	 */
	private void setupSkyDome() {
		dome = new SkyDome("skyDome", new Vector3f(0.0f, 0.0f, 0.0f), 11, 18, 850.0f);
		dome.setModelBound(new BoundingSphere());
		dome.updateModelBound();
		dome.updateRenderState();
		dome.setUpdateTime(5.0f);
		dome.setTimeWarp(180.0f);
		dome.setDay(267);
		dome.setLatitude(-22.9f);
		dome.setLongitude(-47.083f);
		dome.setStandardMeridian(-45.0f);
		dome.setSunPosition(5.75f); // 5:45 am
		dome.setTurbidity(2.0f);
		dome.setSunEnabled(true);
		dome.setExposure(true, 18.0f);
		dome.setOvercastFactor(0.0f);
		dome.setGammaCorrection(2.5f);
		dome.setRootNode(rootNode);
		dome.setIntensity(1.0f);
		// setup a target to LightNode, if you dont want terrain with light's
		// effect remove it.
		dome.setTarget(terrain);
		rootNode.attachChild(dome);
	}

	/**
	 * Entry point
	 */
	public static void main(String[] args) {
		TestSkyDome app = new TestSkyDome();
		app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
		app.start();
	}
}