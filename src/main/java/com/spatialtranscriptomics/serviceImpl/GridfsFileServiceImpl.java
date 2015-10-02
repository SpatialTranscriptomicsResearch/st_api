package com.spatialtranscriptomics.serviceImpl;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.spatialtranscriptomics.exceptions.CustomBadRequestException;
import com.spatialtranscriptomics.file.File;
import com.spatialtranscriptomics.file.GridfsDBFile;
import com.spatialtranscriptomics.file.GridfsFile;
import com.spatialtranscriptomics.model.LastModifiedDate;
import com.spatialtranscriptomics.service.FileService;
import com.spatialtranscriptomics.util.StringOperations;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * The GridFSService is used to perform CRUD operations on files in GridFS.
 */
@Service
public class GridfsFileServiceImpl implements FileService {

    // This should be injected with the specific version that you want to use.
    // In other words here is where you decide which database should be used
    // by this GridFSServiceImpl instance.
    @Autowired
    private GridFsTemplate mongoGridFsTemplate;

    // Have two classes of methods.
    // One class that takes byte arrays and one class that takes InputStreams.

    /**
     * Stores a file in GridFS. Can be used to create or update a file in GridFS.
     * @param inputStream The input stream that should be read and stored in GridFS.
     * @param filename The filename that the file should be stored under.
     * @return
     */
    @Override
    public File storeFile(InputStream inputStream, String filename, String contentType) {
        validateContentType(contentType);

        GridFSFile gridFSFile =  mongoGridFsTemplate.store(inputStream, filename, contentType);

        if(gridFSFile == null) {
            return null;
        }

        return new GridfsFile(gridFSFile);
    }

    /**
     * Validates the content type.
     * The only rule right now is that the content type can not be blank.
     * @param contentType
     */
    private void validateContentType(String contentType) {
        if(StringOperations.isBlank(contentType)) {
            String message = "Content-Type is required. Can not store a file in the file store without specifying the Content-Type.";
            throw new CustomBadRequestException(message);
        }
    }

    /**
     * Creates a query matching a GridFS file with the given filename.
     * @param filename
     * @return
     */
    private Query getFilenameQuery(String filename) {
        return new Query(GridFsCriteria.whereFilename().is(filename));
    }

    /**
     * Gets a file from GridFS for the given filename.
     * @param filename
     * @return
     */
    @Override
    public File getFile(String filename) {

        GridFSDBFile gridFSDBFile = mongoGridFsTemplate.findOne(getFilenameQuery(filename));

        if(gridFSDBFile == null) {
            return null;
        }

        return new GridfsDBFile(gridFSDBFile);
    }

    /**
     * Delets a file from GridFS.
     * @param filename The filename of the file that should be deleted.
     */
    @Override
    public void deleteFile(String filename) {
        mongoGridFsTemplate.delete(getFilenameQuery(filename));
    }

    @Override
    public LastModifiedDate getLastModified(String filename) {
        File file = getFile(filename);
        DateTime uploadDate = new DateTime(file.getUploadDate());

        return new LastModifiedDate(uploadDate);
    }

    /**
     * Sets the mongoGridFsTemplate used to perform operations against GridFS. Here is where you can control
     * in which database the contents are going to be stored.
     * @param mongoGridFsTemplate
     */
    public void setMongoGridFsTemplate(GridFsTemplate mongoGridFsTemplate) {
        this.mongoGridFsTemplate = mongoGridFsTemplate;
    }
}
