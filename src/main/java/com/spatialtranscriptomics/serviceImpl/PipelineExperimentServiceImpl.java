/*
 * Copyright (C) 2012 Spatial Transcriptomics AB
 * Read LICENSE for more information about licensing terms
 * Contact: Jose Fernandez Navarro <jose.fernandez.navarro@scilifelab.se>
 */

package com.spatialtranscriptomics.serviceImpl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spatialtranscriptomics.model.MongoUserDetails;
import com.spatialtranscriptomics.model.PipelineExperiment;
import com.spatialtranscriptomics.service.PipelineExperimentService;

/**
 * This class implements the store/retrieve logic to MongoDB for the data model class "PipelineExperiment".
 * The DB connection is handled in a MongoOperations object, which is configured in mvc-dispatcher-servlet.xml
 */

@Service
public class PipelineExperimentServiceImpl implements PipelineExperimentService {

	private static final Logger logger = Logger
			.getLogger(PipelineExperimentServiceImpl.class);

	@Autowired
	MongoUserDetailsServiceImpl customUserDetailsService;

	@Autowired
	MongoOperations mongoTemplateExperimentDB;

	// ROLE_ADMIN: all.
	// ROLE_CM:    own account.
	// ROLE_USER:  none.
	public PipelineExperiment find(String id) {
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin()) {
			return mongoTemplateExperimentDB.findOne(new Query(Criteria.where("id").is(id)), PipelineExperiment.class);
		}
		return mongoTemplateExperimentDB.findOne(new Query(Criteria.where("id").is(id).and("account_id").is(currentUser.getId())), PipelineExperiment.class);
	}

	// ROLE_ADMIN: all.
	// ROLE_CM:    own account.
	// ROLE_USER:  none.
	public PipelineExperiment findByName(String name) {
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin()) {
			return mongoTemplateExperimentDB.findOne(new Query(Criteria.where("name").is(name)), PipelineExperiment.class);
		}
		return mongoTemplateExperimentDB.findOne(new Query(Criteria.where("name").is(name).and("account_id").is(currentUser.getId())), PipelineExperiment.class);
	}

	// ROLE_ADMIN: all.
	// ROLE_CM:    own account.
	// ROLE_USER:  none.
	public List<PipelineExperiment> list() {
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin()) {
			return mongoTemplateExperimentDB.findAll(PipelineExperiment.class);
		}
		return mongoTemplateExperimentDB.find(new Query(Criteria.where("account_id").is(currentUser.getId())), PipelineExperiment.class);

	}

	// ROLE_ADMIN: all.
	// ROLE_CM:    own account.
	// ROLE_USER:  none.
	public PipelineExperiment add(PipelineExperiment experiment) {
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin() || currentUser.getId().equals(experiment.getAccount_id())) {
			logger.info("Adding PipelineExperiment");
			mongoTemplateExperimentDB.insert(experiment);
			return experiment;
		} else {
                    logger.info("Not adding PipelineExperiment - access mismatch.");
                }
		return null;
	}

	// ROLE_ADMIN: all.
	// ROLE_CM:    own account.
	// ROLE_USER:  none.
	public void update(PipelineExperiment experiment) {
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin() || currentUser.getId().equals(experiment.getAccount_id())) {
			logger.info("Updating PipelineExperiment " + experiment.getId());
			mongoTemplateExperimentDB.save(experiment);
		} else {
                    logger.info("Not updating PipelineExperiment - access mismatch.");
                }
	}

	// ROLE_ADMIN: all.
	// ROLE_CM:    own account.
	// ROLE_USER:  none.
	public void delete(String id) {
		PipelineExperiment exp = find(id);
		if (exp == null) { return; }
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin() || currentUser.getId().equals(exp.getId())) {
			logger.info("Deleting PipelineExperiment " + id);
			mongoTemplateExperimentDB.remove(exp);
		} else {
                    logger.info("Not deleting PipelineExperiment - access mismatch.");
                }
	}

	// ROLE_ADMIN: all.
	// ROLE_CM:    own accounts.
	// ROLE_USER:  none.
	public List<PipelineExperiment> findByAccount(String accountId) {	
		MongoUserDetails currentUser = customUserDetailsService.loadCurrentUser();
		if (currentUser.isAdmin() || currentUser.getId().equals(accountId)) {
			return mongoTemplateExperimentDB.find(new Query(Criteria.where("account_id").is(accountId)), PipelineExperiment.class);
		}
		return null;
	}
        
        @Override
        public void clearAccount(String accountId) {
            List<PipelineExperiment> l = list();
            for (PipelineExperiment pe : l) {
                if (pe.getAccount_id() != null && pe.getAccount_id().equals(accountId)) {
                    pe.setAccount_id("");
                    update(pe);
                }
            }
        }

}
