package com.ngranek.unsolved.client;

import java.nio.FloatBuffer;

import com.jme.bounding.BoundingBox;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import com.jme.scene.batch.TriangleBatch;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Dome;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.geom.BufferUtils;
import com.jmex.effects.LensFlare;
import com.jmex.effects.LensFlareFactory;

/**
 * sky gradient based on "A practical analytic model for daylight" by A. J.
 * Preetham, Peter Shirley, Brian Smits (University of Utah)
 * 
 * @author Highnik
 */
public class SkyDome extends Node {
	public static final float INFINITY = 3.3e+38f;
	public static final float EPSILON = 0.000001f;

	private Dome dome;
	private Vector3f cameraPos = new Vector3f();

	// shading parameters
	private float thetaSun;
	private float phiSun;
	private float turbidity = 2.0f;
	private boolean isLinearExpControl;
	private float exposure = 18.0f;
	private float overcast;
	private float gammaCorrection = 2.5f;

	// time parameters
	private float timeOfDay = 0.0f;
	private float julianDay = 0.0f;
	private float latitude = 0.0f;
	private float longitude = 0.0f;
	private float stdMeridian = 0.0f;
	private float sunnyTime = 12.0f;
	private float solarDeclination = 0.0f;
	private float latitudeInRadian = 0.0f;
	private boolean isNight = false;
	// timer control.
	private Timer timer;
	private float currentTime;
	private float updateTime = 0.0f;
	private float timeWarp = 180.0f;
	private boolean renderRequired = true;

	// used at update color
	private float chi;
	private float zenithLuminance;
	private float zenithX;
	private float zenithY;
	private float[] perezLuminance;
	private float[] perezX;
	private float[] perezY;
	private Vector3f sunDirection = new Vector3f();
	private Vector3f sunPosition = new Vector3f();
	private ColorXYZ color;
	private ColorXYZ colorTemp;
	private TriangleBatch batch;
	private FloatBuffer colorBuf;
	private FloatBuffer normalBuf;
	private Vector3f vertex = new Vector3f();
	private float gamma;
	private float cosTheta;
	private float cosGamma2;
	private float x_value;
	private float y_value;
	private float yClear;
	private float yOver;
	private float _Y;
	private float _X;
	private float _Z;

	private DirectionalLight dr;
	private LightNode sun;
	private LensFlare flare;
	private boolean sunEnabled = true;

	/** Distribution coefficients for the luminance(Y) distribution function */
	private float distributionLuminance[][] = { // Perez distributions
	{ 0.17872f, -1.46303f }, // a = darkening or brightening of the horizon
			{ -0.35540f, 0.42749f }, // b = luminance gradient near the
										// horizon,
			{ -0.02266f, 5.32505f }, // c = relative intensity of the
										// circumsolar region
			{ 0.12064f, -2.57705f }, // d = width of the circumsolar region
			{ -0.06696f, 0.37027f } }; // e = relative backscattered light

	/** Distribution coefficients for the x distribution function */
	private float distributionXcomp[][] = { { -0.01925f, -0.25922f }, { -0.06651f, 0.00081f }, { -0.00041f, 0.21247f },
			{ -0.06409f, -0.89887f }, { -0.00325f, 0.04517f } };

	/** Distribution coefficients for the y distribution function */
	private float distributionYcomp[][] = { { -0.01669f, -0.26078f }, { -0.09495f, 0.00921f }, { -0.00792f, 0.21023f },
			{ -0.04405f, -1.65369f }, { -0.01092f, 0.05291f } };

	/** Zenith x value */
	private float zenithXmatrix[][] = { { 0.00165f, -0.00375f, 0.00209f, 0.00000f },
			{ -0.02903f, 0.06377f, -0.03202f, 0.00394f }, { 0.11693f, -0.21196f, 0.06052f, 0.25886f } };

	/** Zenith y value */
	private float zenithYmatrix[][] = { { 0.00275f, -0.00610f, 0.00317f, 0.00000f },
			{ -0.04214f, 0.08970f, -0.04153f, 0.00516f }, { 0.15346f, -0.26756f, 0.06670f, 0.26688f } };

	/** Creates a new instance of SkyDome */
	public SkyDome() {
		this("SkyDome", 11, 18, 100f);
	}

	public SkyDome(String name) {
		this(name, 11, 18, 100f);
	}

	public SkyDome(String name, int planes, int radialSamples, float radius) {
		this(name, new Vector3f(0, 0, 0), planes, radialSamples, radius);
	}

	public SkyDome(String name, Vector3f center, int planes, int radialSamples, float radius) {
		dome = new Dome(name, center, planes, radialSamples, radius, true);
		dome.setIsCollidable(false);
		dome.setSolidColor(ColorRGBA.black);
		attachChild(dome);
		timer = Timer.getTimer();
		currentTime = timer.getTimeInSeconds();

		solarDeclination = calc_solar_declination(julianDay);
		sunnyTime = calc_sunny_time(latitude, solarDeclination);

		// create a lens flare effects
		setupLensFlare();

		ZBufferState zbuff = DisplaySystem.getDisplaySystem().getRenderer().createZBufferState();
		zbuff.setWritable(false);
		zbuff.setEnabled(true);
		zbuff.setFunction(ZBufferState.CF_LEQUAL);
		setRenderState(zbuff);
		setCullMode(SceneElement.CULL_NEVER);
		setLightCombineMode(LightState.OFF);
		setTextureCombineMode(TextureState.REPLACE);
	}

	/**
	 * Set Sun's positon
	 */
	public void setSunPosition(Vector3f sunPos) {
		Vector3f pos = new Vector3f();
		pos = FastMath.cartesianToSpherical(sunPos, pos);
		thetaSun = pos.z;
		phiSun = pos.y;
	}

	/**
	 * Return Sun's position
	 */
	public Vector3f getSunPosition() {
		return sunPosition;
	}

	/**
	 * Convert time to sun position
	 * 
	 * @param time
	 *            Sets a time of day between 0 to 24 (6,25 = 6:15 hs)
	 */
	public void setSunPosition(float time) {
		float solarTime, solarAltitude, opp, adj, solarAzimuth, cosSolarDeclination, sinSolarDeclination, sinLatitude, cosLatitude;
		this.timeOfDay = time;

		sinLatitude = FastMath.sin(latitudeInRadian);
		cosLatitude = FastMath.cos(latitudeInRadian);
		sinSolarDeclination = FastMath.sin(solarDeclination);
		cosSolarDeclination = FastMath.cos(solarDeclination);

		// real time
		solarTime = time
				+ (0.170f * FastMath.sin(4f * FastMath.PI * (julianDay - 80f) / 373f) - 0.129f * FastMath
						.sin(FastMath.TWO_PI * (julianDay - 8f) / 355f)) + (stdMeridian - longitude) / 15;

		solarAltitude = FastMath.asin(sinLatitude * sinSolarDeclination - cosLatitude * cosSolarDeclination
				* FastMath.cos(FastMath.PI * solarTime / sunnyTime));

		opp = -cosSolarDeclination * FastMath.sin(FastMath.PI * solarTime / sunnyTime);

		adj = -(cosLatitude * sinSolarDeclination + sinLatitude * cosSolarDeclination
				* FastMath.cos(FastMath.PI * solarTime / sunnyTime));

		solarAzimuth = FastMath.atan(opp / adj);

		if (solarAltitude > 0.0f) {

			isNight = false;
			if ((opp < 0.0f && solarAzimuth < 0.0f) || (opp > 0.0f && solarAzimuth > 0.0f)) {
				solarAzimuth = FastMath.HALF_PI + solarAzimuth;
			} else {
				solarAzimuth = FastMath.HALF_PI - solarAzimuth;
			}
			phiSun = FastMath.TWO_PI - solarAzimuth;
			thetaSun = FastMath.HALF_PI - solarAltitude;

			sunDirection.x = dome.radius;
			sunDirection.y = phiSun;
			sunDirection.z = solarAltitude;
			sunPosition = FastMath.sphericalToCartesian(sunDirection, sunPosition);
			if (solarAzimuth < 0.0f) {
				sunPosition.x *= -1;
			}

			if (this.isSunEnabled())
				sun.setLocalTranslation(sunPosition);

		} else {
			isNight = true;
		}

	}

	/**
	 * Return if now is night
	 */
	public boolean isNight() {
		return isNight;
	}

	/**
	 * Set Day of year between 0 to 364
	 */
	public void setDay(float julianDay) {
		this.julianDay = clamp(julianDay, 0.0f, 365.0f);
		// Solar declination
		solarDeclination = calc_solar_declination(julianDay);
		sunnyTime = calc_sunny_time(latitude, solarDeclination);
	}

	/**
	 * Get Day of year
	 */
	public float getDay() {
		return julianDay;
	}

	/**
	 * Set latitude
	 */
	public void setLatitude(float latitude) {
		this.latitude = clamp(latitude, -90.0f, 90.0f);
		latitudeInRadian = FastMath.DEG_TO_RAD * latitude;
		sunnyTime = calc_sunny_time(latitudeInRadian, solarDeclination);
	}

	/**
	 * Get latitude
	 */
	public float getLatitude() {
		return latitude;
	}

	/**
	 * Set longitude
	 */
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	/**
	 * Get longitude
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * Set standar meridian
	 * 
	 * @param stdMeridian
	 *            TimeZone * 15
	 */
	public void setStandardMeridian(float stdMeridian) {
		this.stdMeridian = stdMeridian;
	}

	/**
	 * Get standar meridian
	 */
	public float getStandardMeridian() {
		return stdMeridian;
	}

	public void setTurbidity(float turbidity) {
		this.turbidity = clamp(turbidity, 1.0f, 512.0f);
	}

	/**
	 * Set Exposure factor
	 */
	public void setExposure(boolean isLinearExpControl, float exposure) {
		this.isLinearExpControl = isLinearExpControl;
		this.exposure = 1.0f / clamp(exposure, 1.0f, INFINITY);
	}

	/**
	 * Set Over Cast factor
	 */
	public void setOvercastFactor(float overcast) {
		this.overcast = clamp(overcast, 0.0f, 1.0f);
	}

	/**
	 * Set gamma correction factor
	 */
	public void setGammaCorrection(float gamma) {
		this.gammaCorrection = 1.0f / clamp(gamma, EPSILON, INFINITY);
	}

	/**
	 * Seconds to update
	 */
	public void setUpdateTime(float seconds) {
		this.updateTime = seconds;
	}

	public float getUpdateTime() {
		return updateTime;
	}

	/**
	 * if updateTime = 1 and timeWarp = 1, every seconds will be updated
	 */
	public void setTimeWarp(float timeWarp) {
		this.timeWarp = timeWarp;
	}

	public float getTimeWarp() {
		return timeWarp;
	}

	public void update() {
		if (updateTime > 0.0f) {
			if ((timer.getTimeInSeconds() - currentTime) >= updateTime) {
				currentTime = timer.getTimeInSeconds();
				timeOfDay += (updateTime * timeWarp / 3600f);
				setSunPosition(timeOfDay);
				renderRequired = true;
			}
		}
		cameraPos = DisplaySystem.getDisplaySystem().getRenderer().getCamera().getLocation();
		dome.setLocalTranslation(new Vector3f(cameraPos.x, 0.0f, cameraPos.z));
	}

	/**
	 * update Sky color
	 */
	public void render() {

		if (!renderRequired)
			return;

		if (isNight) {
			dome.setSolidColor(ColorRGBA.black);
			return;
		}

		// get zenith luminance
		chi = ((4.0f / 9.0f) - (turbidity / 120.0f)) * (FastMath.PI - (2.0f * thetaSun));
		zenithLuminance = ((4.0453f * turbidity) - 4.9710f) * FastMath.tan(chi) - (0.2155f * turbidity) + 2.4192f;
		if (zenithLuminance < 0.0f)
			zenithLuminance = -zenithLuminance;

		// get x / y zenith
		zenithX = getZenith(zenithXmatrix, thetaSun, turbidity);
		zenithY = getZenith(zenithYmatrix, thetaSun, turbidity);

		// get perez function parameters
		perezLuminance = getPerez(distributionLuminance, turbidity);
		perezX = getPerez(distributionXcomp, turbidity);
		perezY = getPerez(distributionYcomp, turbidity);

		// make some precalculation
		zenithX = perezFunctionO1(perezX, thetaSun, zenithX);
		zenithY = perezFunctionO1(perezY, thetaSun, zenithY);
		zenithLuminance = perezFunctionO1(perezLuminance, thetaSun, zenithLuminance);

		// build sun direction vector
		sunDirection.x = FastMath.cos(FastMath.HALF_PI - thetaSun) * FastMath.cos(phiSun);
		sunDirection.y = FastMath.sin(FastMath.HALF_PI - thetaSun);
		sunDirection.z = FastMath.cos(FastMath.HALF_PI - thetaSun) * FastMath.sin(phiSun);
		sunDirection.normalize();

		// trough all vertices
		for (int i = 0; i < dome.getBatchCount(); i++) {
			batch = dome.getBatch(i);

			normalBuf = batch.getNormalBuffer();
			colorBuf = batch.getColorBuffer();

			for (int j = 0; j < batch.getVertexCount(); j++) {

				BufferUtils.populateFromBuffer(vertex, normalBuf, j);

				// angle between sun and vertex
				gamma = FastMath.acos(vertex.dot(sunDirection));

				if (vertex.y < 0.05f)
					vertex.y = 0.05f;

				cosTheta = 1.0f / vertex.y;
				cosGamma2 = FastMath.sqr(FastMath.cos(gamma));

				// Compute x,y values
				x_value = perezFunctionO2(perezX, cosTheta, gamma, cosGamma2, zenithX);
				y_value = perezFunctionO2(perezY, cosTheta, gamma, cosGamma2, zenithY);

				// luminance(Y) for clear & overcast sky
				yClear = perezFunctionO2(perezLuminance, cosTheta, gamma, cosGamma2, zenithLuminance);
				yOver = (1.0f + 2.0f * vertex.y) / 3.0f;

				_Y = FastMath.LERP(overcast, yClear, yOver);
				_X = (x_value / y_value) * _Y;
				_Z = ((1.0f - x_value - y_value) / y_value) * _Y;

				colorTemp = new ColorXYZ(_X, _Y, _Z);
				color = colorTemp.convertXYZtoRGB();
				colorTemp = color.convertRGBtoHSV();

				if (isLinearExpControl) { // linear scale
					colorTemp.setValue(colorTemp.getValue() * exposure);
				} else { // exp scale
					colorTemp.setValue(1.0f - FastMath.exp(-exposure * colorTemp.getValue()));
				}
				color = colorTemp.convertHSVtoRGB();

				// gamma control
				color.setGammaCorrection(gammaCorrection);

				// clamp rgb between 0.0 - 1.0
				color.clamp();

				// change the color
				BufferUtils.setInBuffer(color.getRGBA(), colorBuf, j);
			}
		}
		renderRequired = false;
	}

	/**
	 * Returns a LightNode that represents the Sun
	 */
	public LightNode getSun() {
		return sun;
	}

	/**
	 * Set the rootNode to flare
	 */
	public void setRootNode(Node value) {
		if (flare != null) {
			flare.setRootNode(value);
		}
	}

	/**
	 * Set a intensity to Flare
	 */
	public void setIntensity(float value) {
		if (flare != null) {
			flare.setIntensity(value);
		}
	}

	/**
	 * Set a target to LightNode
	 */
	public void setTarget(Spatial node) {
		if (sun != null) {
			sun.setTarget(node);
		}
	}

	public void setSunEnabled(boolean enable) {
		this.sunEnabled = enable;
		sun.getLight().setEnabled(enable);
	}

	public boolean isSunEnabled() {
		return sunEnabled;
	}

	private float calc_solar_declination(float jDay) {
		return (0.4093f * FastMath.sin(FastMath.TWO_PI * (284f + jDay) / 365f));
	}

	private float calc_sunny_time(float lat, float solarDeclin) {
		// Time of hours over horizon
		float sunnyTime;
		sunnyTime = (2.0f * FastMath.acos(-FastMath.tan(lat) * FastMath.tan(solarDeclin)));
		sunnyTime = (sunnyTime * FastMath.RAD_TO_DEG) / 15;
		return sunnyTime;
	}

	/**
	 * Create Lens flare effect
	 */
	private void setupLensFlare() {

		dr = new DirectionalLight();
		dr.setEnabled(true);
		dr.setDiffuse(ColorRGBA.white);
		dr.setAmbient(ColorRGBA.gray);
		dr.setDirection(new Vector3f(0.0f, 0.0f, 0.0f));

		sun = new LightNode("SunNode", DisplaySystem.getDisplaySystem().getRenderer().createLightState());
		sun.setLight(dr);

		Vector3f min2 = new Vector3f(-0.1f, -0.1f, -0.1f);
		Vector3f max2 = new Vector3f(0.1f, 0.1f, 0.1f);
		Box lightBox = new Box("lightbox", min2, max2);
		lightBox.setModelBound(new BoundingBox());
		lightBox.updateModelBound();
		sun.attachChild(lightBox);
		lightBox.setLightCombineMode(LightState.OFF);
		
		String dir = "/home/bigjocker/workspace/KADATH/testdata/texture/";

		// Setup the lensflare textures.
		TextureState[] tex = new TextureState[4];
		tex[0] = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		tex[0].setTexture(TextureManager.loadTexture(dir + "flare1.png", Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR, Image.RGBA8888,
				1.0f, true));
		tex[0].setEnabled(true);

		tex[1] = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		tex[1].setTexture(TextureManager.loadTexture(dir + "flare2.png", Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR));
		tex[1].setEnabled(true);

		tex[2] = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		tex[2].setTexture(TextureManager.loadTexture(dir + "flare3.png", Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR));
		tex[2].setEnabled(true);

		tex[3] = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		tex[3].setTexture(TextureManager.loadTexture(dir + "flare4.png", Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR));
		tex[3].setEnabled(true);

		flare = LensFlareFactory.createBasicLensFlare("flare", tex);

		flare.setIntensity(0.5f);

		// notice that it comes at the end
		sun.attachChild(flare);
		attachChild(sun);
	}

	private float[] getPerez(float[][] distribution, float turbidity) {
		float[] perez = new float[5];
		perez[0] = distribution[0][0] * turbidity + distribution[0][1];
		perez[1] = distribution[1][0] * turbidity + distribution[1][1];
		perez[2] = distribution[2][0] * turbidity + distribution[2][1];
		perez[3] = distribution[3][0] * turbidity + distribution[3][1];
		perez[4] = distribution[4][0] * turbidity + distribution[4][1];
		return perez;
	}

	private float getZenith(float[][] zenithMatrix, float theta, float turbidity) {
		float theta2 = theta * theta;
		float theta3 = theta * theta2;

		return (zenithMatrix[0][0] * theta3 + zenithMatrix[0][1] * theta2 + zenithMatrix[0][2] * theta + zenithMatrix[0][3])
				* turbidity
				* turbidity
				+ (zenithMatrix[1][0] * theta3 + zenithMatrix[1][1] * theta2 + zenithMatrix[1][2] * theta + zenithMatrix[1][3])
				* turbidity
				+ (zenithMatrix[2][0] * theta3 + zenithMatrix[2][1] * theta2 + zenithMatrix[2][2] * theta + zenithMatrix[2][3]);
	}

	private float perezFunctionO1(float[] perezCoeffs, float thetaSun, float zenithValue) {
		float val = (1.0f + perezCoeffs[0] * FastMath.exp(perezCoeffs[1]))
				* (1.0f + perezCoeffs[2] * FastMath.exp(perezCoeffs[3] * thetaSun) + perezCoeffs[4]
						* FastMath.sqr(FastMath.cos(thetaSun)));
		return zenithValue / val;
	}

	private float perezFunctionO2(float[] perezCoeffs, float cosTheta, float gamma, float cosGamma2, float zenithValue) {
		return zenithValue * (1.0f + perezCoeffs[0] * FastMath.exp(perezCoeffs[1] * cosTheta))
				* (1.0f + perezCoeffs[2] * FastMath.exp(perezCoeffs[3] * gamma) + perezCoeffs[4] * cosGamma2);
	}

	/**
	 * clamp the value between min and max values
	 */
	private float clamp(float value, float min, float max) {
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
	}

	class ColorXYZ {
		private float x = 0.0f;
		private float y = 0.0f;
		private float z = 0.0f;
		private float r = 0.0f;
		private float g = 0.0f;
		private float b = 0.0f;
		private float a = 1.0f;
		private float hue = 0.0f;
		private float saturation = 0.0f;
		private float value = 0.0f;

		public ColorXYZ(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setValue(float value) {
			this.value = value;
		}

		public float getValue() {
			return this.value;
		}

		public void clamp() {
			if (r < 0)
				r = 0;
			if (g < 0)
				g = 0;
			if (b < 0)
				b = 0;
			if (r > 1)
				r = 1;
			if (g > 1)
				g = 1;
			if (b > 1)
				b = 1;
		}

		public void setGammaCorrection(float gammaCorrection) {
			r = FastMath.pow(r, gammaCorrection);
			g = FastMath.pow(g, gammaCorrection);
			b = FastMath.pow(b, gammaCorrection);
		}

		/**
		 * Retorna o RGBA color
		 */
		public ColorRGBA getRGBA() {
			return new ColorRGBA(r, g, b, a);
		}

		/**
		 * Converte XYZ to RGB color
		 */
		public ColorXYZ convertXYZtoRGB() {
			this.r = 3.240479f * x - 1.537150f * y - 0.498535f * z;
			this.g = -0.969256f * x + 1.875992f * y + 0.041556f * z;
			this.b = 0.055648f * x - 0.204043f * y + 1.057311f * z;
			return this;
		}

		/**
		 * Converte RGB to HSV
		 */
		public ColorXYZ convertRGBtoHSV() {
			float minColor = Math.min(Math.min(r, g), b);
			float maxColor = Math.max(Math.max(r, g), b);
			float delta = maxColor - minColor;

			this.value = maxColor; // Value
			if (!(FastMath.abs(maxColor) < EPSILON)) {
				this.saturation = delta / maxColor; // Saturation
			} else { // r = g = b = 0
				this.saturation = 0.0f; // Saturation = 0
				this.hue = -1; // Hue = undefined
				return this;
			}

			if (FastMath.abs(r - maxColor) < EPSILON)
				this.hue = (g - b) / delta; // between yellow & magenta
			else if (FastMath.abs(g - maxColor) < EPSILON)
				this.hue = 2.0f + (b - r) / delta; // between cyan & yellow
			else
				this.hue = 4.0f + (r - g) / delta; // between magenta & cyan

			this.hue *= 60.0f; // degrees

			if (this.hue < 0.0f)
				this.hue += 360.0f; // positive
			return this;
		}

		/**
		 * Converte HSV to RGB
		 */
		public ColorXYZ convertHSVtoRGB() {
			if (FastMath.abs(saturation) < EPSILON) { // achromatic (grey)
				this.r = value;
				this.g = value;
				this.b = value;
				this.a = value;
			}

			hue /= 60.0f; // sector 0 to 5
			int sector = (int) FastMath.floor(hue);

			float f = hue - sector; // factorial part of hue
			float p = value * (1.0f - saturation);
			float q = value * (1.0f - saturation * f);
			float t = value * (1.0f - saturation * (1.0f - f));
			switch (sector) {
			case 0:
				this.r = value;
				this.g = t;
				this.b = p;
				break;
			case 1:
				this.r = q;
				this.g = value;
				this.b = p;
				break;
			case 2:
				this.r = p;
				this.g = value;
				this.b = t;
				break;
			case 3:
				this.r = p;
				this.g = q;
				this.b = value;
				break;
			case 4:
				this.r = t;
				this.g = p;
				this.b = value;
				break;
			default: // case 5:
				this.r = value;
				this.g = p;
				this.b = q;
				break;
			}
			return this;
		}
	}
}
