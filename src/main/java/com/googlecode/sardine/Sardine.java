package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * The main interface for Sardine operations.
 *
 * @author jonstevens
 */
public interface Sardine {

    /**
     * Gets a directory listing.
     */
    @WebdavOnly(method="PROPFIND")
    public List<DavResource> getResources(String url) throws IOException;

    /**
     * Adds or removes custom properties for a url.
     */
    @WebdavOnly(method="PROPPATCH")
    public void setCustomProps(String url, Map<String, String> addProps, List<String> removeProps)
            throws IOException;

    /**
     * Gets an input stream for url. It is the callers responsibility to close the stream after usage.
     */
    public InputStream get(String url) throws IOException;

    /**
     * Gets an input stream for url. It is the callers responsibility to close the stream after usage.
     * Use {@link Sardine#get(String)} instead.
     */
    @Deprecated
    public InputStream getInputStream(String url) throws IOException;

    /**
     * Sends data given as a byte array to a server.
     */
    public void put(String url, byte[] data) throws IOException;

    /**
     * Sends data given as an {@link InputStream} to a server.
     */
    public void put(String url, InputStream dataStream) throws IOException;

    /**
     * Sends data given as a byte array to a server with a specific content type header.
     */
    public void put(String url, byte[] data, String contentType) throws IOException;

    /**
     * Sends data given as an {@link InputStream} to a server with a specific content type header.
     */
    public void put(String url, InputStream dataStream, String contentType) throws IOException;

    /**
     * Sends data given as an {@link InputStream} to a server with a specific content type header.
     */
    public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException;

    /**
     * Deletes a resource at the specified url.
     */
    public void delete(String url) throws IOException;

    /**
     * Creates a directory at the specified url
     */
    @WebdavOnly(method="MKCOL")
    public void createDirectory(String url) throws IOException;

    /**
     * Moves from source to destination. Does not assume overwrite.
     */
    @WebdavOnly(method="MOVE")
    public void move(String sourceUrl, String destinationUrl) throws IOException;

    /**
     * Moves from source to destination. Assumes overwrite.
     */
    @WebdavOnly(method="MOVE")
    public void moveReplacing(String sourceUrl, String destinationUrl) throws IOException;

    /**
     * Copies from source to destination. Does not assume overwrite.
     */
    @WebdavOnly(method="COPY")
    public void copy(String sourceUrl, String destinationUrl) throws IOException;

    /**
     * Copies from source to destination. Assumes overwrite.
     */
    @WebdavOnly(method="COPY")
    public void copyReplacing(String sourceUrl, String destinationUrl) throws IOException;

    /**
     * Performs a HEAD request to see if a resource exists or not. Anything outside of the 200-299 response code range
     * returns false.
     */
    public boolean exists(String url) throws IOException;

}
