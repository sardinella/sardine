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
    public List<DavResource> getResources(String url) throws IOException;

    /**
     * Add or remove custom properties for a url.
     */
    public void setCustomProps(String url, Map<String, String> addProps, List<String> removeProps)
            throws IOException;

    /**
     * Get an input stream for url. It is the callers responsibility to close the stream after usage.
     */
    public InputStream get(String url) throws IOException;

    /**
     * Get an input stream for url. It is the callers responsibility to close the stream after usage.
     * Use {@link Sardine#get(String)} instead.
     */
    @Deprecated
    public InputStream getInputStream(String url) throws IOException;

    /**
     * Uses webdav put to send data to a server
     */
    public void put(String url, byte[] data) throws IOException;

    /**
     * Uses webdav put to send data to a server
     */
    public void put(String url, InputStream dataStream) throws IOException;

    /**
     * Uses webdav put to send data to a server with a specific content type header
     */
    public void put(String url, byte[] data, String contentType) throws IOException;

    /**
     * Uses webdav put to send data to a server with a specific content type header
     */
    public void put(String url, InputStream dataStream, String contentType) throws IOException;

    /**
     * Uses webdav put to send data to a server with a specific content type header
     */
    public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException;

    /**
     * Delete a resource at the specified url
     */
    public void delete(String url) throws IOException;

    /**
     * Uses webdav to create a directory at the specified url
     */
    public void createDirectory(String url) throws IOException;

    /**
     * Move a url to from source to destination. Assumes overwrite.
     */
    public void move(String sourceUrl, String destinationUrl) throws IOException;

    /**
     * Copy a url from source to destination. Assumes overwrite.
     */
    public void copy(String sourceUrl, String destinationUrl) throws IOException;

    /**
     * Performs a HEAD request to see if a resource exists or not. Anything outside of the 200-299 response code range
     * returns false.
     */
    public boolean exists(String url) throws IOException;

}
