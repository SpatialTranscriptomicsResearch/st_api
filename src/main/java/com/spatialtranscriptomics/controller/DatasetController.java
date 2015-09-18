/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */

package com.spatialtranscriptomics.controller;

import com.spatialtranscriptomics.component.StaticContextAccessor;
import com.spatialtranscriptomics.exceptions.BadRequestResponse;
import com.spatialtranscriptomics.exceptions.CustomBadRequestException;
import com.spatialtranscriptomics.exceptions.CustomInternalServerErrorException;
import com.spatialtranscriptomics.exceptions.CustomInternalServerErrorResponse;
import com.spatialtranscriptomics.exceptions.CustomNotFoundException;
import com.spatialtranscriptomics.exceptions.CustomNotModifiedException;
import com.spatialtranscriptomics.exceptions.NotFoundResponse;
import com.spatialtranscriptomics.exceptions.NotModifiedResponse;
import com.spatialtranscriptomics.model.Dataset;
import com.spatialtranscriptomics.model.LastModifiedDate;
import com.spatialtranscriptomics.serviceImpl.DatasetInfoServiceImpl;
import com.spatialtranscriptomics.serviceImpl.DatasetServiceImpl;
import com.spatialtranscriptomics.serviceImpl.FeaturesServiceImpl;
import com.spatialtranscriptomics.serviceImpl.SelectionServiceImpl;
import com.spatialtranscriptomics.util.DateOperations;
import static com.spatialtranscriptomics.util.DateOperations.checkIfModified;
import static com.spatialtranscriptomics.util.HTTPOperations.getHTTPHeaderWithCache;
import java.util.Iterator;
import java.util.List;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This class is Spring MVC controller class for the API endpoint "rest/dataset". 
 * It implements the methods available at this endpoint.
 */

@Repository
@Controller
@RequestMapping("/rest/dataset")
public class DatasetController {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DatasetController.class);

    @Autowired
    DatasetServiceImpl datasetService;

    @Autowired
    FeaturesServiceImpl featuresService;

    @Autowired
    SelectionServiceImpl selectionService;

    @Autowired
    DatasetInfoServiceImpl datasetInfoService;
    
    /**
     * GET|HEAD /dataset/
     * GET|HEAD /dataset/?account={accountId}
     * 
     * Lists enabled datasets.
     * @param accountId the account ID.
     * @param onlyEnabled when true filters out disabled datasets
     * @return the list.
     */
    @Secured({"ROLE_CM", "ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody List<Dataset> list(
            @RequestParam(value = "account", required = false) String accountId,
            @RequestParam(value = "onlyEnabled", required = false, defaultValue = "false") boolean onlyEnabled) {
        
        List<Dataset> datasets;
        if (accountId != null) {
            datasets = datasetService.findByAccount(accountId);
        } else {
            datasets = datasetService.list();
        }
        
        if (datasets == null) {
            logger.info("Returning empty list of datasets");
            throw new CustomNotFoundException("No datasets found or you don't have "
                    + "permissions to access");
        }
        
        if (onlyEnabled) {
            Iterator<Dataset> it = datasets.iterator();
            while (it.hasNext()) {
                Dataset dataset = it.next(); // must be called before you can call i.remove()
                if (!dataset.getEnabled()) {
                    it.remove();
                }
            }
        }
        
        logger.info("Returning list of datasets");
        return datasets;
    }

    /**
     * GET|HEAD /dataset/{id}
     * 
     * Finds an enabled dataset.
     * @param id the dataset ID.
     * @param onlyEnabled if true only a dataset that is enabled can be returned
     * @param ifModifiedSince request timestamp.
     * @return the dataset.
     */
    @Secured({"ROLE_CM", "ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody HttpEntity<Dataset> get(
            @PathVariable String id,
            @RequestParam(value = "onlyEnabled", required = false, defaultValue = "false") boolean onlyEnabled,
            @RequestHeader(value="If-Modified-Since", defaultValue="") String ifModifiedSince) {
        
        Dataset dataset = datasetService.find(id);
        if (dataset == null || (onlyEnabled && !dataset.getEnabled())) {
            logger.info("Failed to return enabled dataset " + id);
            throw new CustomNotFoundException("A dataset with ID " + id + " doesn't exist, "
                    + "is disabled, or you don't have permissions to access");
        }
        
        // Check if already newest.
        DateTime reqTime = DateOperations.parseHTTPDate(ifModifiedSince);
        if (reqTime != null && !checkIfModified(dataset.getLast_modified(), reqTime)) {
            logger.info("Not returning enabled dataset " + id + " since not modified");
            throw new CustomNotModifiedException("This dataset has not been modified");
        }
        
        HttpEntity<Dataset> entity = new HttpEntity<Dataset>(dataset, 
                getHTTPHeaderWithCache(dataset.getLast_modified()));
        logger.info("Returning enabled dataset " + id);
        return entity;
    }
   
    /**
     * GET|HEAD /dataset/lastmodified/{id}
     *
     * Finds a dataset's last modified timestamp.
     * @param id the dataset ID.
     * @return the timestamp.
     */
    @Secured({"ROLE_CM", "ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(value = "/lastmodified/{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody LastModifiedDate getLastModified(@PathVariable String id) {
        
        Dataset dataset = datasetService.find(id);
        if (dataset == null) {
            logger.info("Failed to return last modified time of dataset " + id);
            throw new CustomNotFoundException("A dataset with ID " + id + " doesn't exist, "
                    + "or you don't have permissions to access");
        }
        
        logger.info("Returning last modified time of dataset " + id);
        return new LastModifiedDate(dataset.getLast_modified());
    }

    /**
     * POST /dataset/
     * 
     * Adds a dataset.
     * @param dataset the dataset.
     * @param result binding object from the view.
     * @return the dataset with ID assigned.
     */
    @Secured({"ROLE_CM", "ROLE_ADMIN"})
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Dataset add(
            @RequestBody @Valid Dataset dataset, 
            BindingResult result) {
        
        // Data model validation
        if (result.hasErrors()) {
            logger.info("Failed to add dataset. Missing fields?");
            throw new CustomBadRequestException("Dataset is invalid. Missing required fields?");
        }
        
        if (dataset.getId() != null) {
            logger.info("Failed to add dataset. ID set by user");
            throw new CustomBadRequestException("The dataset you want to add must "
                    + "not have an ID. The ID will be autogenerated");
        } else if (datasetService.findByNameInternal(dataset.getName()) != null) {
            logger.info("Failed to add dataset. Duplicate name");
            throw new CustomBadRequestException("A dataset with this name exists already. "
                    + "Dataset names are unique");
        }
        
        Dataset d = datasetService.add(dataset);
        // HACK: Id needs to be autogenerated at persisting before updating DatasetInfos.
        d.updateGranted_accounts();
        logger.info("Successfully added dataset " + d.getId());
        return d;
    }

    /**
     * PUT /dataset/{id}
     * 
     * Updates a dataset.
     * @param id the dataset ID.
     * @param dataset the dataset.
     * @param result binding object from the view.
     */
    @Secured({"ROLE_CM", "ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public @ResponseBody void update(
            @PathVariable String id, 
            @RequestBody @Valid Dataset dataset, 
            BindingResult result) {
        
        // Data model validation
        if (result.hasErrors()) {
            logger.info("Failed to update dataset. Missing fields?");
            throw new CustomBadRequestException("Dataset is invalid. Missing required fields?");
        }
        
        if (!id.equals(dataset.getId())) {
            logger.info("Failed to update dataset. ID mismatch");
            throw new CustomBadRequestException("ID in request URL does not match ID in content body");
        } else if (datasetService.find(id) == null) {
            logger.info("Failed to update dataset. Missing or failed permissions.");
            throw new CustomBadRequestException("A dataset with ID " + id + " doesn't exist or "
                    + "you don't have permissions to access");
        } else if (datasetService.findByNameInternal(dataset.getName()) != null) {
            if (!datasetService.findByNameInternal(dataset.getName()).getId().equals(id)) {
                logger.info("Failed to update dataset. Duplicated name");
                throw new CustomBadRequestException("Another dataset with this name exists already. "
                        + "Dataset names are unique");
            }
        }
        
        logger.info("Successfully updated dataset " + dataset.getId());
        datasetService.update(dataset);
    }

    /**
     * DELETE /dataset/{id}
     * 
     * Deletes a dataset.
     * @param id the dataset ID.
     */
    @Secured({"ROLE_CM", "ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public @ResponseBody void delete(@PathVariable String id) {
        
        if (!datasetService.deleteIsOkForCurrUser(id)) {
            logger.info("Failed to delete dataset " + id + " Missing permissions");
            throw new CustomBadRequestException("You are not allowed to delete this dataset");
        }
        
        datasetInfoService.deleteForDataset(id);
        datasetService.delete(id);
        featuresService.delete(id);
        selectionService.deleteForDataset(id);
        logger.info("Successfully deleted dataset " + id);
    }
    
    /**
     * Static access to dataset service.
     * @return the bean.
     */
    public static DatasetServiceImpl getStaticDatasetService() {
        return StaticContextAccessor.getBean(DatasetController.class).getDatasetService();
    }

    /**
     * Access to dataset service.
     * @return the bean.
     */
    public DatasetServiceImpl getDatasetService() {
        return this.datasetService;
    }

    @ExceptionHandler(CustomNotModifiedException.class)
    @ResponseStatus(value = HttpStatus.NOT_MODIFIED)
    public @ResponseBody
    NotModifiedResponse handleNotModifiedException(CustomNotModifiedException ex) {
        return new NotModifiedResponse(ex.getMessage());
    }
    
    @ExceptionHandler(CustomNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody
    NotFoundResponse handleNotFoundException(CustomNotFoundException ex) {
        return new NotFoundResponse(ex.getMessage());
    }

    @ExceptionHandler(CustomBadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    BadRequestResponse handleBadRequestException(CustomBadRequestException ex) {
        logger.warn(ex);
        return new BadRequestResponse(ex.getMessage());
    }

    @ExceptionHandler(CustomInternalServerErrorException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    CustomInternalServerErrorResponse handleInternalServerException(CustomInternalServerErrorException ex) {
        logger.error(ex);
        return new CustomInternalServerErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    CustomInternalServerErrorResponse handleRuntimeException(RuntimeException ex) {
        logger.error("Unknown error in dataset controller: " + ex.getMessage());
        return new CustomInternalServerErrorResponse(ex.getMessage());
    }
}
