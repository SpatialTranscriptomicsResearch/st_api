package com.spatialtranscriptomics.model;

import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This class maps the PipelineExperiment data model object into a MongoDB
 * Document We use the @Document annotation of Spring Data for the mapping. We
 * also do data validation using Hibernate validator constraints.
 * 
 * A pipeline experiment represents the computational part of an experiment
 * performed on Amazon EMR, in which a raw dataset is mapped, annotated, etc.
 * to obtain a set of features as a feature file.
 */
@Document(collection = "pipelineexperiment")
public class PipelineExperiment implements IPipelineExperiment {

    @Id
    String id;

    @Indexed(unique = true)
    @NotBlank(message = "Name must not be blank.")
    String name;

    //@NotBlank(message = "Account must not be blank.")
    @Indexed(unique = false)
    String account_id;

    @NotBlank(message = "EMR job ID must not be blank.")
    String emr_jobflow_id;

    String emr_state;

    DateTime emr_creation_date_time;

    DateTime emr_end_date_time;

    String emr_last_state_change_reason;

    @CreatedDate
    private DateTime created_at;

    @LastModifiedDate
    private DateTime last_modified;

    /**
     * Default constructor is needed by Jackson, in
     * case other constructors are added.
     */
    public PipelineExperiment() {}
    
    // id is set automatically by MongoDB
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getEmr_jobflow_id() {
        return this.emr_jobflow_id;
    }

    @Override
    public void setEmr_jobflow_id(String emrJobflowId) {
        this.emr_jobflow_id = emrJobflowId;

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;

    }

    @Override
    public String getAccount_id() {
        return account_id;
    }

    @Override
    public void setAccount_id(String id) {
        this.account_id = id;
    }

    @Override
    public String getEmr_state() {
        return this.emr_state;
    }

    @Override
    public void setEmr_state(String state) {
        this.emr_state = state;
    }

    @Override
    public DateTime getEmr_creation_date_time() {
        return this.emr_creation_date_time;
    }

    @Override
    public void setEmr_creation_date_time(DateTime creationDateTime) {
        this.emr_creation_date_time = creationDateTime;
    }

    @Override
    public DateTime getEmr_end_date_time() {
        return this.emr_end_date_time;
    }

    @Override
    public void setEmr_end_date_time(DateTime endDateTime) {
        this.emr_end_date_time = endDateTime;
    }

    @Override
    public String getEmr_last_state_change_reason() {
        return emr_last_state_change_reason;
    }

    @Override
    public void setEmr_last_state_change_reason(String lastStateChangeReason) {
        this.emr_last_state_change_reason = lastStateChangeReason;
    }

    @Override
    public DateTime getCreated_at() {
        return created_at;
    }

    @Override
    public void setCreated_at(DateTime created) {
        this.created_at = created;
    }

    @Override
    public DateTime getLast_modified() {
        return last_modified;
    }
}
