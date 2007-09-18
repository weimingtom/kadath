package com.ngranek.unsolved.client.scenes;

import javax.swing.ImageIcon;

import com.jme.animation.AnimationController;
import com.jme.bounding.OrientedBoundingBox;
import com.jme.image.Texture;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jmex.effects.water.WaterRenderPass;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.ImageBasedHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;
import com.ngranek.unsolved.client.Main;
import com.ngranek.unsolved.client.config.KADATHConfig;
import com.ngranek.unsolved.utils.KADATHModelLoader;
import com.ngranek.unsolved.utils.KADATHRotatingController;

/**
 * @author Ing. William Anez (cucho)
 *
 */
public class CuchoScene extends BaseScene {

	private static final String BASE_DIR = "/home/cucho/workspace/KADATH/testdata/";

	private BasicPassManager passManager = Main.getInstance().getPassManager();
	private Camera camera = Main.getInstance().getCamera();
	private DisplaySystem display = Main.getInstance().getDisplaySystem();
	private LightState lightState = Main.getInstance().getLightState();
	private Node rootNode = Main.getInstance().getRootNode();
	private Node fpsNode = Main.getInstance().getFPSNode();
	private Node mainNode = new Node("mainNode");
	private Skybox skybox;
	private Timer timer = Main.getInstance().getTimer();

	private AnimationController ac;
	private WaterRenderPass waterEffectRenderPass;
	//private ProjectedGrid projectedGrid;
	private TerrainPage terrain;

	private float farPlane = 500000.0f;
	private float textureScale = 0.02f;
	private int throtle = 0;
	
	private OrientedBoundingBox ssBounding;

	@Override
	public void cleanup() {

		if (waterEffectRenderPass != null) {
			waterEffectRenderPass.cleanup();
		}

	}

	@Override
	public void init() {

		RenderPass rootPass = new RenderPass();
		rootPass.add(rootNode);
		passManager.add(rootPass);

		camera.setFrustumPerspective(45.0f, (float) display.getWidth() / (float) display.getHeight(), 1f,
				farPlane);
		camera.setLocation(new Vector3f(10, 71000, 10));
		camera.lookAt(new Vector3f(0, 70000, 0), Vector3f.UNIT_Y);
		camera.update();

		setupKeyBindings();
		//setupFog(mainNode);

		buildSkyBox();
		mainNode.attachChild(skybox);
		rootNode.attachChild(mainNode);

		setupSpaceship(rootNode);

		//buildWaterEffect(mainNode);
		buildTerrain(mainNode);

		rootNode.updateWorldBound();
		
		RenderPass fpsPass = new RenderPass();
		fpsPass.add(fpsNode);
		passManager.add(fpsPass);

		rootNode.setCullMode(SceneElement.CULL_NEVER);
		rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
		fpsNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);

	}

	@Override
	public void update() {

		if (skybox != null) {
			skybox.getLocalTranslation().set(Main.getInstance().getCamera().getLocation());
			skybox.updateGeometricState(0.0f, true);
			skybox.getLocalTranslation().set(camera.getLocation());
			camera.update();
		}

		//		if (KeyBindingManager.getKeyBindingManager().isValidCommand("f", false)) {
		//			//projectedGrid.switchFreeze();
		//			System.out.println("Pressing F");
		//		}
		//
		//		if (KeyBindingManager.getKeyBindingManager().isValidCommand("g", false)) {
		//			waterEffectRenderPass.setUseRefraction(!waterEffectRenderPass.isUseRefraction());
		//			waterEffectRenderPass.reloadShader();
		//		}

		float tpf = Main.getInstance().getTimer().getTimePerFrame();
		Spatial ss = (Spatial) rootNode.getChild("sf-cucho");

		if (KeyBindingManager.getKeyBindingManager().isValidCommand("up", true)) {
			if (ss != null) {
				KADATHRotatingController.rotateXSpatial(ss, tpf);
			}
		}
		
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("down", true)) {
			if (ss != null) {
				KADATHRotatingController.rotateXSpatial(ss, -tpf);
			}
		}
		
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("rleft", true)) {
			if (ss != null) {
				KADATHRotatingController.rotateYSpatial(ss, tpf);
			}
		}
		
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("rright", true)) {
			if (ss != null) {
				KADATHRotatingController.rotateYSpatial(ss, -tpf);
			}
		}
		
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("increaseThrotle", false)) {
			throtle = 1;
		}

		if (KeyBindingManager.getKeyBindingManager().isValidCommand("decreaseThrotle", false)) {
			throtle = 0;
		}

		if (throtle == 1 && ss != null) {
			
			Vector3f center = ssBounding.getCenter();
			System.out.println("Center: " + center);
			
			Vector3f v = new Vector3f();
			ss.localToWorld(new Vector3f(0, -0.01f, 0), v);
			ss.setLocalTranslation(v);
			
		}
		
		rootNode.updateWorldBound();
		
	}

	protected void setupSpaceship(Node node) {

		Vector3f startPoint = new Vector3f(0, 70000, 0);

		Spatial model = KADATHModelLoader.loadColladaModel("sf-cucho", KADATHConfig
				.getProperty("com.ngranek.unsolved.models.dir"));
		ssBounding = new OrientedBoundingBox();

		model.updateGeometricState(0, true);
		model.setLocalScale(100.0f);
		model.setName("sf-cucho");
		model.setModelBound( ssBounding );
		model.setLocalTranslation(startPoint);
		model.updateModelBound();
		
		
		
		node.attachChild(model);

	}

	protected void buildTerrain(Node node) {

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
		String dataDir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir")
				+ KADATHConfig.getProperty("com.ngranek.unsolved.data.dir");

		ImageBasedHeightMap heightMap = new ImageBasedHeightMap(new ImageIcon(dataDir + "/heightmap1.png")
				.getImage());

		heightMap.setHeightScale(0.01f);
		Vector3f terrainScale = new Vector3f(10000, 400, 10000);
		terrain = new TerrainPage("Terrain", 33, heightMap.getSize(), terrainScale, heightMap.getHeightMap(),
				false);
		terrain.setDetailTexture(1, 400);
		node.attachChild(terrain);

		String dir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir")
				+ KADATHConfig.getProperty("com.ngranek.unsolved.textures.dir");

		// Some textures
		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(heightMap);
		pt.addTexture(new ImageIcon(dir + "grassb.png"), -128, 0, 128);
		pt.addTexture(new ImageIcon(dir + "dirt.jpg"), 0, 128, 255);
		pt.addTexture(new ImageIcon(dir + "highest.jpg"), 128, 255, 384);

		pt.createTexture(256);

		TextureState ts = Main.getInstance().getDisplaySystem().getRenderer().createTextureState();
		ts.setEnabled(true);
		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(), Texture.MM_LINEAR_LINEAR,
				Texture.FM_LINEAR, true);
		ts.setTexture(t1, 0);

		Texture t2 = TextureManager.loadTexture(dir + "/Detail.jpg", Texture.MM_LINEAR_LINEAR,
				Texture.FM_LINEAR);
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

	private void buildSkyBox() {
		skybox = new Skybox("skybox", 10, 10, 10);

		Texture north = TextureManager.loadTexture(BASE_DIR + "skybox1/1.jpg", Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture south = TextureManager.loadTexture(BASE_DIR + "skybox1/3.jpg", Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture east = TextureManager.loadTexture(BASE_DIR + "skybox1/2.jpg", Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture west = TextureManager.loadTexture(BASE_DIR + "skybox1/4.jpg", Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture up = TextureManager.loadTexture(BASE_DIR + "skybox1/6.jpg", Texture.MM_LINEAR,
				Texture.FM_LINEAR);
		Texture down = TextureManager.loadTexture(BASE_DIR + "skybox1/5.jpg", Texture.MM_LINEAR,
				Texture.FM_LINEAR);

		skybox.setTexture(Skybox.NORTH, north);
		skybox.setTexture(Skybox.WEST, west);
		skybox.setTexture(Skybox.SOUTH, south);
		skybox.setTexture(Skybox.EAST, east);
		skybox.setTexture(Skybox.UP, up);
		skybox.setTexture(Skybox.DOWN, down);
		skybox.preloadTextures();

		CullState cullState = display.getRenderer().createCullState();
		cullState.setCullMode(CullState.CS_NONE);
		cullState.setEnabled(true);
		skybox.setRenderState(cullState);

		ZBufferState zState = display.getRenderer().createZBufferState();
		zState.setEnabled(false);
		skybox.setRenderState(zState);

		FogState fs = display.getRenderer().createFogState();
		fs.setEnabled(false);
		skybox.setRenderState(fs);

		skybox.setLightCombineMode(LightState.OFF);
		skybox.setCullMode(SceneElement.CULL_NEVER);
		skybox.setTextureCombineMode(TextureState.REPLACE);
		skybox.updateRenderState();

		skybox.lockBounds();
		skybox.lockMeshes();
	}

	//	private void buildWaterEffect(Node node) {
	//
	//		waterEffectRenderPass = new WaterRenderPass(camera, 4, true, true);
	//		waterEffectRenderPass.setClipBias(0.5f);
	//		waterEffectRenderPass.setWaterMaxAmplitude(5.0f);
	//		waterEffectRenderPass.setWaterPlane(new Plane(new Vector3f(0.0f, 1.0f, 0.0f), 0.0f));
	//
	//		projectedGrid = new ProjectedGrid("ProjectedGrid", camera, 100, 60, 0.01f, new WaterHeightGenerator());
	//
	//		waterEffectRenderPass.setWaterEffectOnSpatial(projectedGrid);
	//		rootNode.attachChild(projectedGrid);
	//
	//		//waterEffectRenderPass.setUseRefraction(false);
	//		waterEffectRenderPass.setReflectedScene(node);
	//		waterEffectRenderPass.setSkybox(skybox);
	//
	//		passManager.add(waterEffectRenderPass);
	//
	//	}

	private void setupFog(Node node) {
		FogState fogState = display.getRenderer().createFogState();
		fogState.setDensity(1.0f);
		fogState.setEnabled(true);
		fogState.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		fogState.setEnd(farPlane);
		fogState.setStart(farPlane / 10.0f);
		fogState.setDensityFunction(FogState.DF_LINEAR);
		fogState.setApplyFunction(FogState.AF_PER_VERTEX);
		node.setRenderState(fogState);
	}

	private void setupKeyBindings() {

		KeyBindingManager.getKeyBindingManager().set("up", KeyInput.KEY_U);
		KeyBindingManager.getKeyBindingManager().set("down", KeyInput.KEY_J);
		KeyBindingManager.getKeyBindingManager().set("rleft", KeyInput.KEY_H);
		KeyBindingManager.getKeyBindingManager().set("rright", KeyInput.KEY_K);
		KeyBindingManager.getKeyBindingManager().set("increaseThrotle", KeyInput.KEY_1);
		KeyBindingManager.getKeyBindingManager().set("decreaseThrotle", KeyInput.KEY_0);

		Text t = new Text("Text", "F: switch freeze/unfreeze projected grid");
		t.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		t.setLightCombineMode(LightState.OFF);
		t.setLocalTranslation(new Vector3f(0, 20, 1));
		fpsNode.attachChild(t);

		t = new Text("Text", "E: debug show/hide reflection and refraction textures");
		t.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		t.setLightCombineMode(LightState.OFF);
		t.setLocalTranslation(new Vector3f(0, 40, 1));
		fpsNode.attachChild(t);
	}

}
