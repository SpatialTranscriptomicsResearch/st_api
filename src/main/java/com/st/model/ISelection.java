package com.st.model;

import java.util.List;
import org.joda.time.DateTime;

/**
 * This interface defines the Selection model. Applications that use the API
 * must implement the same model.
 */
public interface ISelection {

    public String getId();

    public void setId(String id);

    public boolean getEnabled();

    public void setEnabled(boolean enabled);

    public List<String[]> getGene_hits();

    public void setGene_hits(List<String[]> selection_hits);

    public String getDataset_id();

    public void setDataset_id(String dataset_id);

    public String getAccount_id();

    public void setAccount_id(String account_id);

    public String getName();

    public void setName(String name);

    public String getType();

    public void setType(String type);

    public String getStatus();

    public void setStatus(String status);

    public String getComment();

    public void setComment(String comment);

    public DateTime getCreated_at();

    public void setCreated_at(DateTime created);

    public DateTime getLast_modified();
    
    public void setTissue_snapshot(String tissue_snapshot);
    
    public String getTissue_snapshot();
}
