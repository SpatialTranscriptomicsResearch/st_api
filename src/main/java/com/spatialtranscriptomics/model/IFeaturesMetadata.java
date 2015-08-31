/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */
package com.spatialtranscriptomics.model;

import org.joda.time.DateTime;

/**
 * This interface defines the FeaturesMetadata model. Applications that use the
 * API must implement the same model.
 */

public interface IFeaturesMetadata {

    public String getDatasetId();
    
    public void setDatasetId(String id);
    
    public String getFilename();

    public void setFilename(String filename);

    public DateTime getLastModified();

    public DateTime getCreated();

    public void setLastModified(DateTime d);

    public void setCreated(DateTime d);

    public long getSize();

    public void setSize(long size);

}
