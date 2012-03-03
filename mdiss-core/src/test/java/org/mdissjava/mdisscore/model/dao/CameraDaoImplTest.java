package org.mdissjava.mdisscore.model.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
import org.mdissjava.mdisscore.model.dao.impl.CameraDaoImpl;
import org.mdissjava.mdisscore.model.pojo.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.Datastore;

public class CameraDaoImplTest {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testInsertFind() {
		Datastore db = MorphiaDatastoreFactory.getDatastore("test");
		this.logger.info("[TEST] testInsertField");

		CameraDao cameraDao = new CameraDaoImpl(db);
		// Creation of a new Camera to insert
		Camera camera = new Camera();
		camera.setBrand("Brand1");
		camera.setModel("Canon 35D");

		// Insertion in the Mongo db
		cameraDao.insertCamera(camera);
		// The inserted camera is find in the Mongo db
		List<Camera> cameraList = cameraDao.findCamera(camera);
		// If the returned camera's brand is "Brand1" the insert went good
		assertEquals(cameraList.get(0).getBrand(), "Brand1");
	}

	@Test
	public void testDelete() {
		this.logger.info("[TEST] testDelete");
		Datastore db = MorphiaDatastoreFactory.getDatastore("test");
		
		CameraDao cameraDao = new CameraDaoImpl(db);
		// Creation of a new Camera
		Camera camera = new Camera();
		camera.setBrand("Brand2");
		camera.setModel("Canon 40D");
		// Insertion of the camera in the Mongo db
		cameraDao.insertCamera(camera);
		// Deletion of the same camera in the Mongo db
		cameraDao.deleteCamera(camera);
		List<Camera> cameraList = cameraDao.findCamera(camera);
		// If finding the camera returns an empty list means that it didn't find
		// the camera because it was deleted
		assertTrue(cameraList.isEmpty());

	}

	@Test
	public void testUpdate() {
		this.logger.info("[TEST] testUpdate");
		Datastore db = MorphiaDatastoreFactory.getDatastore("test");
		
		CameraDao cameraDao = new CameraDaoImpl(db);
		Camera camera = new Camera();
		camera.setBrand("Brand3");
		camera.setModel("Canon 50D");
		// A new camera is inserted in the Mongo db
		cameraDao.insertCamera(camera);
		// The camera's model is changed
		camera.setModel("NOKIA");
		// The camera is updated in the Mongo db
		cameraDao.updateCamera(camera);
		List<Camera> cameraList = cameraDao.findCamera(camera);
		assertFalse(cameraList.isEmpty());
		// If the returned camera have the second model name it went correct the
		// update in the Mongo db
		assertEquals(cameraList.get(0).getModel(), "NOKIA");
	}

}
