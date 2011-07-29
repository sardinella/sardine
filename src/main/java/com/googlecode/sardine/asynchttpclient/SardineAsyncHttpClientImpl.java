package com.googlecode.sardine.asynchttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

/**
 * Implementation of the Sardine interface for {@link AsyncHttpClient}. This is where the meat of the Sardine library lives.
 *
 * @author mfriedenhagen
 */
public class SardineAsyncHttpClientImpl implements Sardine {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(SardineAsyncHttpClientImpl.class);
    private AsyncHttpClient client;

    /**
     * 
     */
    public SardineAsyncHttpClientImpl() {
        client = new AsyncHttpClient();
    }
    
    /** {@inheritDoc} */
    public List<DavResource> list(String url) throws IOException {
        // TODO Auto-generated method stub
        LOG.trace("PROPFIND {}", url);
        return null;
    }

    /** {@inheritDoc} */
    public List<DavResource> patch(String url, Map<String, String> addProps, List<String> removeProps)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public InputStream get(String url) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public InputStream getInputStream(String url) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data, String contentType) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void delete(String url) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void createDirectory(String url) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void move(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void moveReplacing(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void copy(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void copyReplacing(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public boolean exists(String url) throws IOException {
        LOG.info("HEAD {}", url);
        final RequestBuilder builder = new RequestBuilder("HEAD");
        final Request request = builder.setUrl(url).build();
        final ListenableFuture<Response> executeRequest = client.executeRequest(request);
        final Response response;
        try {
            response = executeRequest.get();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            throw new IOException("Message:", e);
        } catch (ExecutionException e) {
            throw new IOException("Message:", e);
        }
        LOG.info("status: {} {}", response.getStatusCode(), response.getStatusText());
        return false;
    }

}
