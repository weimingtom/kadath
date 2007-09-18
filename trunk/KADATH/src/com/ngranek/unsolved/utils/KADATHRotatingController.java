package com.ngranek.unsolved.utils;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;


/**
 * @author Ing. William Anez (cucho)
 *
 */
public class KADATHRotatingController {
	
	public static void rotateXSpatial(Spatial spatial, float angle) {
		rotateAxisSpatial(spatial, angle, new Vector3f(1, 0, 0).normalizeLocal());
	}
	
	public static void rotateYSpatial(Spatial spatial, float angle) {
		rotateAxisSpatial(spatial, angle, new Vector3f(0, 1, 0).normalizeLocal());
	}
	
	public static void rotateZSpatial(Spatial spatial, float angle) {
		rotateAxisSpatial(spatial, angle, new Vector3f(0, 0, 1).normalizeLocal());
	}

	private static void rotateAxisSpatial(Spatial spatial, float angle, Vector3f axis) {
		Quaternion rot = new Quaternion();
		rot.fromAngleNormalAxis( angle, axis );
		spatial.getLocalRotation().multLocal( rot );
	}
	
}
