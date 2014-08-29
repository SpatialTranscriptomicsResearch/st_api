/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */

package com.spatialtranscriptomics.service;

import java.awt.image.BufferedImage;
import java.util.List;

import com.spatialtranscriptomics.model.ImageMetadata;

/**
 * Interface for the image service.
 */
public interface ImageService {

	public List<ImageMetadata> list();
	
	public ImageMetadata getImageMetadata(String filename);

	public BufferedImage getBufferedImage(String filename);

        public byte[] getCompressedImage(String filename);
        
	public void add(String filename, BufferedImage img);
        
        public void addCompressed(String filename, byte[] img);

	public void delete(String filename);
}
