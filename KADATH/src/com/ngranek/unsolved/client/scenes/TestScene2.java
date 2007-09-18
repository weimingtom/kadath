package com.ngranek.unsolved.client.scenes;

import javax.swing.ImageIcon;

import jmetest.effects.water.TestProjectedWater;
import jmetest.effects.water.TestSimpleQuadWater;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.math.Plane;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.RenderPass;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Skybox;
import com.jme.scene.Text;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Torus;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import com.jmex.effects.water.ProjectedGrid;
import com.jmex.effects.water.WaterHeightGenerator;
import com.jmex.effects.water.WaterRenderPass;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.HillHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;
import com.ngranek.unsolved.client.Main;

public class TestScene2 extends BaseScene {
	private WaterRenderPass waterEffectRenderPass;
	private Skybox skybox;
	private ProjectedGrid projectedGrid;
	private float farPlane = 10000.0f;

	public void cleanup() {
		waterEffectRenderPass.cleanup();
	}

	public void init() {
		Main.getInstance().getCamera().setFrustumPerspective(
				45.0f,
				(float) Main.getInstance().getDisplaySystem().getWidth()
						/ (float) Main.getInstance().getDisplaySystem().getHeight(), 1f, farPlane);
		Main.getInstance().getCamera().setLocation(new Vector3f(100, 50, 100));
		Main.getInstance().getCamera().lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
		Main.getInstance().getCamera().update();

		setupKeyBindings();

		setupFog();

		Node reflectedNode = new Node("reflectNode");

		buildSkyBox();
		reflectedNode.attachChild(skybox);
		reflectedNode.attachChild(createObjects());

		Main.getInstance().getRootNode().attachChild(reflectedNode);

		waterEffectRenderPass = new WaterRenderPass(Main.getInstance().getCamera(), 4, true, true);
		waterEffectRenderPass.setClipBias(0.5f);
		waterEffectRenderPass.setWaterMaxAmplitude(5.0f);
		// setting to default value just to show
		waterEffectRenderPass.setWaterPlane(new Plane(new Vector3f(0.0f, 1.0f, 0.0f), 0.0f));

		projectedGrid = new ProjectedGrid("ProjectedGrid", Main.getInstance().getCamera(), 100, 70, 0.01f,
				new WaterHeightGenerator());
		// or implement your own waves like this(or in a separate class)...
		// projectedGrid = new ProjectedGrid( "ProjectedGrid", cam, 50, 50,
		// 0.01f, new HeightGenerator() {
		// public float getHeight( float x, float z, float time ) {
		// return
		// FastMath.sin(x*0.05f+time*2.0f)+FastMath.cos(z*0.1f+time*4.0f)*2;
		// }
		// } );

		waterEffectRenderPass.setWaterEffectOnSpatial(projectedGrid);
		Main.getInstance().getRootNode().attachChild(projectedGrid);

		waterEffectRenderPass.setReflectedScene(reflectedNode);
		waterEffectRenderPass.setSkybox(skybox);
		Main.getInstance().getPassManager().add(waterEffectRenderPass);

		RenderPass rootPass = new RenderPass();
		rootPass.add(Main.getInstance().getRootNode());
		Main.getInstance().getPassManager().add(rootPass);

		RenderPass fpsPass = new RenderPass();
		fpsPass.add(Main.getInstance().getFPSNode());
		Main.getInstance().getPassManager().add(fpsPass);

		Main.getInstance().getRootNode().setCullMode(SceneElement.CULL_NEVER);
		Main.getInstance().getRootNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);
		Main.getInstance().getFPSNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);
		
		setupTerrain(Main.getInstance().getRootNode());
	}
	
	protected void setupTerrain(Node node) {
		/*
		 * CameraNode camNode = new CameraNode("Camera Node",
		 * Main.getInstance().getCamera()); camNode.setLocalTranslation(new
		 * Vector3f(0, 250, -20)); camNode.updateWorldData(0);
		 * Main.getInstance().getRootNode().attachChild(camNode);
		 */

		// Set basic render states
		CullState cs = Main.getInstance().getDisplaySystem().getRenderer().createCullState();
		cs.setCullMode(CullState.CS_BACK);
		cs.setEnabled(true);
		Main.getInstance().getRootNode().setRenderState(cs);

		// Some light
		DirectionalLight dl = new DirectionalLight();
		dl.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		dl.setDirection(new Vector3f(1, -0.5f, 1));
		dl.setEnabled(true);
		Main.getInstance().getLightState().attach(dl);

		// The terrain
		HillHeightMap heightMap = new HillHeightMap(129, 2000, 5.0f, 20.0f, (byte) 2);
		heightMap.setHeightScale(0.001f);
		Vector3f terrainScale = new Vector3f(1000, 100, 1000);
		TerrainPage terrain = new TerrainPage("Terrain", 33, heightMap.getSize(), terrainScale, heightMap
				.getHeightMap(), false);
		terrain.setDetailTexture(1, 16);
		node.attachChild(terrain);

		String dir = "/home/bigjocker/workspace/KADATH/testdata/texture/";

		// Some textures
		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(heightMap);
		pt.addTexture(new ImageIcon(dir + "/grassb.png"), -128, 0, 128);
		pt.addTexture(new ImageIcon(dir + "/dirt.jpg"), 0, 128, 255);
		pt.addTexture(new ImageIcon(dir + "/highest.jpg"), 128, 255, 384);

		pt.createTexture(256);

		TextureState ts = Main.getInstance().getDisplaySystem().getRenderer().createTextureState();
		ts.setEnabled(true);
		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(), Texture.MM_LINEAR_LINEAR,
				Texture.FM_LINEAR, true);
		ts.setTexture(t1, 0);

		Texture t2 = TextureManager.loadTexture(dir + "/Detail.jpg", Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
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
		node.setRenderState(ts);
	}

	public void update() {
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("f", false)) {
			projectedGrid.switchFreeze();
		}

		if (KeyBindingManager.getKeyBindingManager().isValidCommand("g", false)) {
			waterEffectRenderPass.setUseRefraction(!waterEffectRenderPass.isUseRefraction());
			waterEffectRenderPass.reloadShader();
		}

		skybox.getLocalTranslation().set(Main.getInstance().getCamera().getLocation());
		Main.getInstance().getCamera().update();
	}

	private void setupFog() {
		FogState fogState = Main.getInstance().getDisplaySystem().getRenderer().createFogState();
		fogState.setDensity(1.0f);
		fogState.setEnabled(true);
		fogState.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		fogState.setEnd(farPlane);
		fogState.setStart(farPlane / 10.0f);
		fogState.setDensityFunction(FogState.DF_LINEAR);
		fogState.setApplyFunction(FogState.AF_PER_VERTEX);
		Main.getInstance().getRootNode().setRenderState(fogState);
	}

	private void buildSkyBox() {
		skybox = new Skybox("skybox", 10, 10, 10);

		String dir = "jmetest/effects/water/data/";
		Texture north = TextureManager.loadTexture(
				TestProjectedWater.class.getClassLoader().getResource(dir + "1.jpg"), Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture south = TextureManager.loadTexture(
				TestProjectedWater.class.getClassLoader().getResource(dir + "3.jpg"), Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture east = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(dir + "2.jpg"),
				Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture west = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(dir + "4.jpg"),
				Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture up = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(dir + "6.jpg"),
				Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture down = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(dir + "5.jpg"),
				Texture.MM_LINEAR, Texture.FM_LINEAR);

		skybox.setTexture(Skybox.NORTH, north);
		skybox.setTexture(Skybox.WEST, west);
		skybox.setTexture(Skybox.SOUTH, south);
		skybox.setTexture(Skybox.EAST, east);
		skybox.setTexture(Skybox.UP, up);
		skybox.setTexture(Skybox.DOWN, down);
		skybox.preloadTextures();

		CullState cullState = Main.getInstance().getDisplaySystem().getRenderer().createCullState();
		cullState.setCullMode(CullState.CS_NONE);
		cullState.setEnabled(true);
		skybox.setRenderState(cullState);

		ZBufferState zState = Main.getInstance().getDisplaySystem().getRenderer().createZBufferState();
		zState.setEnabled(false);
		skybox.setRenderState(zState);

		FogState fs = Main.getInstance().getDisplaySystem().getRenderer().createFogState();
		fs.setEnabled(false);
		skybox.setRenderState(fs);

		skybox.setLightCombineMode(LightState.OFF);
		skybox.setCullMode(SceneElement.CULL_NEVER);
		skybox.setTextureCombineMode(TextureState.REPLACE);
		skybox.updateRenderState();

		skybox.lockBounds();
		skybox.lockMeshes();
	}

	private Node createObjects() {
		Node objects = new Node("objects");

		Torus torus = new Torus("Torus", 50, 50, 10, 20);
		torus.setLocalTranslation(new Vector3f(50, -5, 20));
		TextureState ts = Main.getInstance().getDisplaySystem().getRenderer().createTextureState();
		Texture t0 = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(
				"jmetest/data/images/Monkey.jpg"), Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		Texture t1 = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(
				"jmetest/data/texture/north.jpg"), Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		t1.setEnvironmentalMapMode(Texture.EM_SPHERE);
		ts.setTexture(t0, 0);
		ts.setTexture(t1, 1);
		ts.setEnabled(true);
		torus.setRenderState(ts);
		objects.attachChild(torus);

		ts = Main.getInstance().getDisplaySystem().getRenderer().createTextureState();
		t0 = TextureManager.loadTexture(TestProjectedWater.class.getClassLoader().getResource(
				"jmetest/data/texture/wall.jpg"), Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		t0.setWrap(Texture.WM_WRAP_S_WRAP_T);
		ts.setTexture(t0);

		Box box = new Box("box1", new Vector3f(-10, -10, -10), new Vector3f(10, 10, 10));
		box.setLocalTranslation(new Vector3f(0, -7, 0));
		box.setRenderState(ts);
		objects.attachChild(box);

		box = new Box("box2", new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5));
		box.setLocalTranslation(new Vector3f(15, 10, 0));
		box.setRenderState(ts);
		objects.attachChild(box);

		box = new Box("box3", new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5));
		box.setLocalTranslation(new Vector3f(0, -10, 15));
		box.setRenderState(ts);
		objects.attachChild(box);

		box = new Box("box4", new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5));
		box.setLocalTranslation(new Vector3f(20, 0, 0));
		box.setRenderState(ts);
		objects.attachChild(box);

		ts = Main.getInstance().getDisplaySystem().getRenderer().createTextureState();
		t0 = TextureManager.loadTexture(TestSimpleQuadWater.class.getClassLoader().getResource(
				"jmetest/data/images/Monkey.jpg"), Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		t0.setWrap(Texture.WM_WRAP_S_WRAP_T);
		ts.setTexture(t0);

		box = new Box("box5", new Vector3f(-50, -2, -50), new Vector3f(50, 2, 50));
		box.setLocalTranslation(new Vector3f(0, -15, 0));
		box.setRenderState(ts);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		objects.attachChild(box);

		return objects;
	}

	private void setupKeyBindings() {
		KeyBindingManager.getKeyBindingManager().set("f", KeyInput.KEY_F);
		KeyBindingManager.getKeyBindingManager().set("e", KeyInput.KEY_E);
		KeyBindingManager.getKeyBindingManager().set("g", KeyInput.KEY_G);

		Text t = new Text("Text", "F: switch freeze/unfreeze projected grid");
		t.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		t.setLightCombineMode(LightState.OFF);
		t.setLocalTranslation(new Vector3f(0, 20, 1));
		Main.getInstance().getFPSNode().attachChild(t);

		t = new Text("Text", "E: debug show/hide reflection and refraction textures");
		t.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		t.setLightCombineMode(LightState.OFF);
		t.setLocalTranslation(new Vector3f(0, 40, 1));
		Main.getInstance().getFPSNode().attachChild(t);
	}
}
