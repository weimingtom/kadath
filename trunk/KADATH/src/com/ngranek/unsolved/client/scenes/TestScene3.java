package com.ngranek.unsolved.client.scenes;

import java.nio.FloatBuffer;

import javax.swing.ImageIcon;

import com.jme.image.Texture;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.RenderPass;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Skybox;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.ImageBasedHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;
import com.ngranek.unsolved.client.Main;
import com.ngranek.unsolved.client.config.KADATHConfig;

public class TestScene3 extends BaseScene {

	private Skybox skybox;
	private Quad waterQuad;
	private float farPlane = 500000.0f;
	private float textureScale = 0.02f;

	private Vector3f camPos = new Vector3f();
	private TerrainPage terrain;

	public void init() {
		RenderPass rootPass = new RenderPass();
		rootPass.add(Main.getInstance().getRootNode());
		Main.getInstance().getPassManager().add(rootPass);

		Main.getInstance().getCamera().setFrustumPerspective(
				45.0f,
				(float) Main.getInstance().getDisplaySystem().getWidth()
						/ (float) Main.getInstance().getDisplaySystem().getHeight(), 1f, farPlane);
		Main.getInstance().getCamera().setLocation(new Vector3f(100, 200, 100));

		Main.getInstance().getCamera().lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
		Main.getInstance().getCamera().update();

		setupKeyBindings();
		setupFog();

		RenderPass fpsPass = new RenderPass();
		fpsPass.add(Main.getInstance().getFPSNode());
		Main.getInstance().getPassManager().add(fpsPass);

		Main.getInstance().getRootNode().setCullMode(SceneElement.CULL_NEVER);
		Main.getInstance().getRootNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);
		Main.getInstance().getFPSNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);

		buildSkyBox();
		Main.getInstance().getRootNode().attachChild(skybox);

		setupTerrain(Main.getInstance().getRootNode());
	}

	protected void setupTerrain(Node node) {
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
		//HillHeightMap heightMap = new HillHeightMap(129, 2000, 5.0f, 20.0f, (byte) 2);

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

	public void update() {
		if (Main.getInstance().getCamera().getLocation().y <= terrain.getHeight(Main.getInstance()
				.getCamera().getLocation()) + 30) {
			camPos.x = Main.getInstance().getCamera().getLocation().x;
			camPos.y = terrain.getHeight(Main.getInstance().getCamera().getLocation()) + 30;
			camPos.z = Main.getInstance().getCamera().getLocation().z;
			Main.getInstance().getCamera().setLocation(camPos);
		}

		if (skybox != null) {
			skybox.getLocalTranslation().set(Main.getInstance().getCamera().getLocation());
			skybox.updateGeometricState(0.0f, true);
		}
	}

	private void setVertexCoords(float x, float y, float z) {
		FloatBuffer vertBuf = waterQuad.getVertexBuffer(0);
		vertBuf.clear();

		vertBuf.put(x - farPlane).put(y).put(z - farPlane);
		vertBuf.put(x - farPlane).put(y).put(z + farPlane);
		vertBuf.put(x + farPlane).put(y).put(z + farPlane);
		vertBuf.put(x + farPlane).put(y).put(z - farPlane);
	}

	private void setTextureCoords(int buffer, float x, float y, float textureScale) {
		x *= textureScale * 0.5f;
		y *= textureScale * 0.5f;
		textureScale = farPlane * textureScale;
		FloatBuffer texBuf;
		texBuf = waterQuad.getTextureBuffer(0, buffer);
		texBuf.clear();
		texBuf.put(x).put(textureScale + y);
		texBuf.put(x).put(y);
		texBuf.put(textureScale + x).put(y);
		texBuf.put(textureScale + x).put(textureScale + y);
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

		String dir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir")
				+ KADATHConfig.getProperty("com.ngranek.unsolved.skybox.1.dir");
		
		Texture north = TextureManager.loadTexture(dir + "1.jpg", Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture south = TextureManager.loadTexture(dir + "3.jpg", Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture east = TextureManager.loadTexture(dir + "2.jpg", Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture west = TextureManager.loadTexture(dir + "4.jpg", Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture up = TextureManager.loadTexture(dir + "6.jpg", Texture.MM_LINEAR, Texture.FM_LINEAR);
		Texture down = TextureManager.loadTexture(dir + "5.jpg", Texture.MM_LINEAR, Texture.FM_LINEAR);

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

	private void setupKeyBindings() {
		KeyBindingManager.getKeyBindingManager().set("g", KeyInput.KEY_G);
		KeyBindingManager.getKeyBindingManager().set("e", KeyInput.KEY_E);

		KeyBindingManager.getKeyBindingManager().set("lower", KeyInput.KEY_H);
		KeyBindingManager.getKeyBindingManager().set("higher", KeyInput.KEY_Y);

		KeyBindingManager.getKeyBindingManager().set("1", KeyInput.KEY_1);
		KeyBindingManager.getKeyBindingManager().set("2", KeyInput.KEY_2);
	}

	public void cleanup() {
	}
}
