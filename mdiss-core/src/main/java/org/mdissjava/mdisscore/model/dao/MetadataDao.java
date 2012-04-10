package org.mdissjava.mdisscore.model.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.mdissjava.mdisscore.model.pojo.Metadata;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;

public interface MetadataDao {

	public Metadata obtenerMetadata(FileInputStream foto, String format) throws MetadataException, ImageProcessingException, IOException;
	public String getExtension(File f);

}
