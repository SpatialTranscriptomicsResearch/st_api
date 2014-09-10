/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */

package com.spatialtranscriptomics.controller;

import com.spatialtranscriptomics.exceptions.BadRequestResponse;
import com.spatialtranscriptomics.exceptions.CustomBadRequestException;
import com.spatialtranscriptomics.exceptions.CustomNotFoundException;
import com.spatialtranscriptomics.exceptions.NotFoundResponse;
import com.spatialtranscriptomics.model.ImageAlignment;
import com.spatialtranscriptomics.serviceImpl.DatasetServiceImpl;
import com.spatialtranscriptomics.serviceImpl.ImageAlignmentServiceImpl;
import com.spatialtranscriptomics.serviceImpl.S3ServiceImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * This class is Spring MVC controller class for the API endpoint "rest/imagealignment". It implements the methods available at this endpoint.
 */

@Repository
@Controller
@RequestMapping("/rest/imagealignment")
public class ImageAlignmentController {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ImageAlignmentController.class);

	@Autowired
	ImageAlignmentServiceImpl imagealignmentService;
        
        @Autowired
	DatasetServiceImpl datasetService;
        
        @Autowired
	S3ServiceImpl s3Service;

	// list / list for chip
	@Secured({"ROLE_CM","ROLE_ADMIN"})
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody
	List<ImageAlignment> list(@RequestParam(value = "chip", required = false) String chipId) {
		List<ImageAlignment> imagealignments = null;
		if (chipId != null) {
			imagealignments = imagealignmentService.findByChip(chipId);
		} else {
			imagealignments = imagealignmentService.list();
		}
		if (imagealignments == null) {
			throw new CustomNotFoundException("No imagealignments found or you dont have permissions to access them.");
		}
		return imagealignments;
	}

	// get
	@Secured({"ROLE_USER", "ROLE_CM","ROLE_ADMIN"})
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public @ResponseBody
	ImageAlignment get(@PathVariable String id) {
		ImageAlignment imagealignment = imagealignmentService.find(id);
		if (imagealignment == null) {
			throw new CustomNotFoundException("An imagealignment with this ID does not exist or you dont have permissions to access it.");
		}
		return imagealignment;
	}
        
        // get last modified
	@Secured({"ROLE_USER", "ROLE_CM","ROLE_ADMIN"})
	@RequestMapping(value = "/lastmodified/{id}", method = RequestMethod.GET)
	public @ResponseBody
	DateTime getLastModified(@PathVariable String id) {
		ImageAlignment imagealignment = imagealignmentService.find(id);
		if (imagealignment == null) {
			throw new CustomNotFoundException("An imagealignment with this ID does not exist or you dont have permissions to access it.");
		}
		return imagealignment.getLast_modified();
	}

	// add
	@Secured({"ROLE_CM","ROLE_ADMIN"})
	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	ImageAlignment add(@RequestBody @Valid ImageAlignment imagealignment, BindingResult result) {
		// ImageAlignment validation
		if (result.hasErrors()) {
			// TODO send error messages here
			throw new CustomBadRequestException("ImageAlignment is invalid. Missing required fields?");
		}
		if (imagealignment.getId() != null) {
			throw new CustomBadRequestException("The imagealignment you want to add must not have an ID. The ID will be autogenerated.");
		}
		if (imagealignmentService.findByName(imagealignment.getName()) != null) {
			throw new CustomBadRequestException("An imagealignment with this name already exists. ImageAlignment names are unique.");
		}
		return imagealignmentService.add(imagealignment);
	}

	// update
	@Secured({"ROLE_CM","ROLE_ADMIN"})
	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	public @ResponseBody
	void update(@PathVariable String id, @RequestBody @Valid ImageAlignment imagealignment,
			BindingResult result) {
		// ImageAlignment validation
		if (result.hasErrors()) {
			// TODO send error messages here
			throw new CustomBadRequestException("ImageAlignment is invalid. Missing required fields?");
		}
		if (!id.equals(imagealignment.getId())) {
			throw new CustomBadRequestException("ImageAlignment ID in request URL does not match ID in content body.");
		} else if (imagealignmentService.find(id) == null) {
			throw new CustomBadRequestException("An ImageAlignment with this ID does not exist or you don't have permissions to access it.");
		} else {
			imagealignmentService.update(imagealignment);
		}
	}

	// delete
	@Secured({"ROLE_CM","ROLE_ADMIN"})
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public @ResponseBody
	void delete(@PathVariable String id,
                @RequestParam(value="cascade", required = false, defaultValue = "true") boolean cascade) {
            if (!imagealignmentService.deleteIsOK(id)) {
                throw new CustomBadRequestException("You do not have permission to delete this image alignment.");
            }
            ImageAlignment imal = imagealignmentService.find(id);
            imagealignmentService.delete(id);
            if (cascade && imal != null) {
                imagealignmentService.delete(id);
                datasetService.setUnabledForImageAlignment(id);
                HashSet<String> todel = new HashSet<String>(1024);
                todel.add(imal.getFigure_blue());
                todel.add(imal.getFigure_red());
                List<ImageAlignment> imals = imagealignmentService.list();
                for (ImageAlignment ia : imals) {
                        if (!ia.getId().equals(id)) {
                                todel.remove(ia.getFigure_blue());
                                todel.remove(ia.getFigure_red());
                        }
                }
                s3Service.deleteImageData(new ArrayList<String>(todel));
            }
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
	BadRequestResponse handleNotFoundException(CustomBadRequestException ex) {
		return new BadRequestResponse(ex.getMessage());
	}

}
