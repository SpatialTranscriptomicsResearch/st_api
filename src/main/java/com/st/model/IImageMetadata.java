package com.st.model;

import org.joda.time.DateTime;

/**
 * This interface defines the ImageMetadata model. Applications that use the API
 * must implement the same model.
 */
public interface IImageMetadata {

    public String getImageType();

    public void setImageType(String filename);
    
    public String getFilename();

    public void setFilename(String filename);

    public DateTime getLastModified();

    public DateTime getCreated();

    public void setLastModified(DateTime d);

    public void setCreated(DateTime d);

    public long getSize();

    public void setSize(long size);

}
