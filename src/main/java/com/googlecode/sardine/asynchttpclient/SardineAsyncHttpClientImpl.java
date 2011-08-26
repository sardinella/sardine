package com.googlecode.sardine.asynchttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.util.ResponseToDavResource;
import com.googlecode.sardine.util.SardineUtil;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

/**
 * Implementation of the Sardine interface for {@link AsyncHttpClient}. This is where the meat of the Sardine library
 * lives.
 * 
 * @author mfriedenhagen
 */
public class SardineAsyncHttpClientImpl implements Sardine {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(SardineAsyncHttpClientImpl.class);

    private final AsyncHttpClient client;

    /**
     * 
     */
    public SardineAsyncHttpClientImpl(final AsyncHttpClient client) {
        this.client = client;
    }

    /** {@inheritDoc} */
    public List<DavResource> list(String url) throws IOException {
        final URI uri = URI.create(url);
        final String method = "PROPFIND";
        final RequestBuilder builder = new RequestBuilder(method)
            .setHeader("Depth", "1") //
            .setHeader("Content-Type", "text/xml; charset=utf-8") //
            .setBody(SardineUtil.getDefaultPropfindXML());
        final Response response = getResponse(builder, method, url);
        final Multistatus multistatus = SardineUtil.getMultistatus(SardineUtil.createUnmarshaller(),
                response.getResponseBodyAsStream(), url);
        return ResponseToDavResource.fromMultiStatus(uri, multistatus);
    }

    /** {@inheritDoc} */
    public List<DavResource> patch(String url, Map<String, String> addProps, List<String> removeProps)
            throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("PATCH");
    }

    /** {@inheritDoc} */
    public InputStream get(String url) throws IOException {
        final Response response = getResponse(new RequestBuilder(), "GET", url);
        return response.getResponseBodyAsStream();
    }

    /** {@inheritDoc} */
    public InputStream getInputStream(String url) throws IOException {
        return get(url);
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("PUT");
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("PUT");
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data, String contentType) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("PUT");
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("PUT");
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("PUT");
    }

    /** {@inheritDoc} */
    public void delete(String url) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("DELETE");
    }

    /** {@inheritDoc} */
    public void createDirectory(String url) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("MKCOL");
    }

    /** {@inheritDoc} */
    public void move(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("MOVE");
    }

    /** {@inheritDoc} */
    public void moveReplacing(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("MOVE");
    }

    /** {@inheritDoc} */
    public void copy(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("COPY");
    }

    /** {@inheritDoc} */
    public void copyReplacing(String sourceUrl, String destinationUrl) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("COPY");
    }

    /** {@inheritDoc} */
    public boolean exists(String url) throws IOException {
        final Response response = getResponse(new RequestBuilder(), "HEAD", url);
        return response.getStatusCode() == 200;
    }

    /**
     * Returns the response for the {@link RequestBuilder}.
     * 
     * @param builder
     *            to execute
     * @param method
     *            which is executed
     * @param url
     *            of the request
     * @return response for the request
     * @throws IOException
     *             when something goes wrong.
     * @param method
     * @param url
     * @return
     * @throws IOException
     */
    Response getResponse(final RequestBuilder builder, final String method, String url) throws IOException {
        final URI uri = URI.create(url);
        final Request request = builder
                .setMethod(method)
                .setUrl(uri.toASCIIString())
                .build();
        LOG.trace("{} {}", method, url);
        final ListenableFuture<Response> executeRequest = client.executeRequest(request);
        final Response response;
        try {
            response = executeRequest.get();
        } catch (InterruptedException e) {
            throw new IOException(createErrorMessage(method, url), e);
        } catch (ExecutionException e) {
            throw new IOException(createErrorMessage(method, url), e);
        }
        LOG.trace("status: {} {}", response.getStatusCode(), response.getStatusText());
        return response;
    }

    /**
     * Creates the error message including method and url.
     * 
     * @param method
     *            of the request
     * @param url
     *            of the request
     * @return error message.
     */
    String createErrorMessage(final String method, String url) {
        return "Could not " + method + " " + url;
    }

}
