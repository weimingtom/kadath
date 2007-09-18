package com.ngranek.unsolved.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import com.jme.animation.AnimationController;
import com.jme.animation.Bone;
import com.jme.animation.BoneAnimation;
import com.jme.animation.SkinNode;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.XMLparser.Converters.ObjToJme;
import com.jmex.model.collada.ColladaImporter;
import com.ngranek.unsolved.client.config.KADATHConfig;

/**
 * @author Ing. William Anez (cucho)
 *
 */
public class KADATHModelLoader {


	public static Spatial loadObjModel(String modelName, String modelDir, String textureDir) {

		ObjToJme converter = new ObjToJme();
		String baseDir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir");

		try {
			URL objFile = new URL("file://" + baseDir + modelDir + modelName + ".obj");
			URL mtlFile = new URL("file://" + baseDir + modelDir + modelName + ".mtl");
			URL texDir = new URL("file://" + baseDir + textureDir);

			converter.setProperty("mtllib", mtlFile);
			converter.setProperty("texdir", texDir);

			ByteArrayOutputStream BO = new ByteArrayOutputStream();
			converter.convert(objFile.openStream(), BO);

			Spatial model = (Spatial) BinaryImporter.getInstance().load(
					new ByteArrayInputStream(BO.toByteArray()));

			model.setName(modelName);
			model.setModelBound(new BoundingBox());
			model.updateModelBound();

			return model;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Spatial loadJmeModel(String modelName, String modelDir) {

		try {

			String baseDir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir");
			URL jmeFile = new URL("file://" + baseDir + modelDir + modelName + ".jme");
			FileInputStream in = new FileInputStream(baseDir + modelDir + modelName + ".jme");
			ByteArrayOutputStream BO = new ByteArrayOutputStream();

			int c;

			while ((c = in.read()) != -1) {
				BO.write(c);
			}

			if (in != null) {
				in.close();
			}

			Spatial model = (Spatial) BinaryImporter.getInstance().load(
					new ByteArrayInputStream(BO.toByteArray()));

			model.setName(modelName);

			model.setModelBound(new BoundingSphere());
			model.updateModelBound();

			return model;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Object[] loadColladaAnimation(String modelName, String modelDir, String animationName,
			AnimationController ac) {

		try {

			String baseDir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir");
			URL texDir = new URL("file://" + baseDir + modelDir);
			InputStream mobboss = new FileInputStream(new File(baseDir + modelDir + "/" + modelName + ".dae"));
			InputStream animation = null;

			if (animationName != null) {
				animation = new FileInputStream(new File(baseDir + modelDir + "/" + animationName + ".dae"));
			}

			if (mobboss == null) {
				System.out.println("Unable to find file");
				System.exit(0);
			}

			ColladaImporter.load(mobboss, texDir, "model");

			SkinNode sn = ColladaImporter.getSkinNode(ColladaImporter.getSkinNodeNames().get(0));
			Bone skel = ColladaImporter.getSkeleton(ColladaImporter.getSkeletonNames().get(0));

			ColladaImporter.cleanUp();
			ColladaImporter.load(animation, texDir, "anim");

			ArrayList<String> animations = ColladaImporter.getControllerNames();

			if (animations != null) {

				System.out.println("Number of animations: " + animations.size());

				for (int i = 0; i < animations.size(); i++) {
					System.out.println(animations.get(i));
				}

				BoneAnimation anim1 = ColladaImporter.getAnimationController(animations.get(0));

				ac = new AnimationController();
				ac.addAnimation(anim1);
				ac.setRepeatType(Controller.RT_WRAP);
				ac.setActive(true);
				ac.setActiveAnimation(anim1);

				skel.addController(ac);
			}

			ColladaImporter.cleanUp();

			return new Object[] { sn, skel};

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Spatial loadColladaModel(String modelName, String modelDir) {

		try {
			String baseDir = KADATHConfig.getProperty("com.ngranek.unsolved.base.dir");
			//url to the location of the model's textures
			URL texDir = new URL("file://" + baseDir + modelDir);

			//this stream points to the model itself.
			InputStream mobboss = new FileInputStream(new File(baseDir + modelDir + "/" + modelName + ".dae"));

			if (mobboss == null) {
				System.out.println("Unable to find file, did you include jme-test.jar in classpath?");
				System.exit(0);
			}

			ColladaImporter.load(mobboss, texDir, "model");
			Spatial spatial = (Spatial) ColladaImporter.getModel();

			spatial.setName(modelName);

			ColladaImporter.cleanUp();

			//all done clean up.
			ColladaImporter.cleanUp();

			return spatial;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}
