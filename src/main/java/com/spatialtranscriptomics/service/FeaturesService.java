/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */


package com.spatialtranscriptomics.service;

import com.spatialtranscriptomics.model.FeaturesMetadata;
import com.spatialtranscriptomics.model.MongoUserDetails;
import java.io.InputStream;
import java.util.List;

/**
 * Interface for the features service.
 */
public interface FeaturesService {
 
    /**
     * Returns true if a dataset (and thus features) is granted.
     * @param datasetId the dataset ID.
     * @param user the user.
     * @return true if granted.
     */
    public boolean datasetIsGranted(String datasetId, MongoUserDetails user);
    
    /**
     * Returns all features files' metadata.
     * @return metadata list.
     */
    public List<FeaturesMetadata> listMetadata();
    
    /**
     * Returns a features file metadata.
     * @param id the dataset ID.
     * @return the metadata.
     */
    public FeaturesMetadata getMetadata(String id);
    
    /**
     * Adds or updates a features file.
     * @param id the dataset ID.
     * @param gzipfile the file, gzipped in BASE64-encoding.
     * @return true if file was updated; false if added.
     */
    public boolean addUpdate(String id, byte[] gzipfile);
    
    /**
     * Finds a features file.
     * @param id the dataset ID.
     * @return the features file gzipped, as an input stream.
     */
    public InputStream find(String id);
    
    /**
     * Deletes a features file.
     * @param id the dataset ID.
     */
    public void delete(String id);
}