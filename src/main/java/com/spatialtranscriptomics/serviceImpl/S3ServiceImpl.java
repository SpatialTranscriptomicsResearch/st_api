/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */

package com.spatialtranscriptomics.serviceImpl;

import com.spatialtranscriptomics.service.S3Service;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class S3ServiceImpl implements S3Service {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(S3ServiceImpl.class);

	@Autowired
	AmazonS3Client s3Client;
        
        private @Value("${s3.imagebucket}")
	String imagesBucket;
	
	private @Value("${s3.imagepath}")
	String imagesPath;
        
        private @Value("${s3.pipelinebucket}")
	String pipelineBucket;
        
        private @Value("${s3.experimentspath}")
	String experimentsPath;
        
        @Override
	public void deleteImageData(List<String> imageNames) {
		ObjectListing objects = s3Client.listObjects(imagesBucket, imagesPath);
		List<S3ObjectSummary> objs = objects.getObjectSummaries();
		if (objs.isEmpty()) {
			return;
		}
		List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<DeleteObjectsRequest.KeyVersion>();
		for (S3ObjectSummary o : objs) {
			for (String imageName : imageNames) {
				if (o.getKey().equals(imageName)) {
					//System.out.println("Adding image: " + imageName);
					DeleteObjectsRequest.KeyVersion kv = new DeleteObjectsRequest.KeyVersion(o.getKey());
					keysToDelete.add(kv);
				}
			}
		}
		if (keysToDelete.isEmpty()) {
			return;
		}
                DeleteObjectsRequest req = new DeleteObjectsRequest(pipelineBucket);
		req.setKeys(keysToDelete);
		s3Client.deleteObjects(req);
	}
        
        @Override
	public void deleteExperimentData(String experimentId) {
		String path = experimentsPath + experimentId;
		ObjectListing objects = s3Client.listObjects(pipelineBucket, path);
		List<S3ObjectSummary> objs = objects.getObjectSummaries();
		if (objs.isEmpty()) {
			return;
		}
		List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<DeleteObjectsRequest.KeyVersion>();
		for (S3ObjectSummary o : objs) {
			DeleteObjectsRequest.KeyVersion kv = new DeleteObjectsRequest.KeyVersion(o.getKey());
			keysToDelete.add(kv);
		}
		if (keysToDelete.isEmpty()) {
			return;
		}
		DeleteObjectsRequest req = new DeleteObjectsRequest(pipelineBucket);
		req.setKeys(keysToDelete);
		s3Client.deleteObjects(req);
	}
        
}