package com.googlecode.sardine.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.ResponseToDavResource;
import com.googlecode.sardine.util.SardineUtil;

/**
 * Implementation of the Sardine interface for {@link DefaultHttpClient}. This is where the meat of the Sardine library lives.
 *
 * @author jonstevens
 */
public class SardineHttpClientImpl implements Sardine {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(SardineHttpClientImpl.class);

    /** our httpclient. */
    private final DefaultHttpClient client;

    /** was a username/password passed in? */
    private final boolean authEnabled;

    /** */
    public SardineHttpClientImpl() throws IOException {
        this(null, null, null, null, null);
    }

    /** */
    public SardineHttpClientImpl(String username, String password) throws IOException {
        this(username, password, null, null);
    }

    /** */
    private SardineHttpClientImpl(String username, String password, SSLSocketFactory sslSocketFactory,
                                  HttpRoutePlanner routePlanner) throws IOException {
        this(username, password, sslSocketFactory, routePlanner, null);
    }

    public SardineHttpClientImpl(final DefaultHttpClient httpClient) {
        this.client = httpClient;
        this.authEnabled = false;
    }

    /** */
    public SardineHttpClientImpl(final DefaultHttpClient httpClient, String username, String password)
            throws IOException {
        putUsernameInMDC(username);
        this.client = httpClient;
        this.client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
        this.authEnabled = true;
    }

    /**
     * Puts the username in the {@link MDC} for logging. If no username is given default to 'ANONYMOUS'.
     * 
     * @param username
     */
    private void putUsernameInMDC(String username) {
        MDC.put("userName", username == null ? "ANONYMOUS" : username);
    }

    /**
     * Main constructor.
     */
    public SardineHttpClientImpl(String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner, Integer port) throws IOException {
        putUsernameInMDC(username);
        this.client = HttpClientUtils.createDefaultHttpClient(sslSocketFactory, port);

        // for proxy configurations
        if (routePlanner != null)
            this.client.setRoutePlanner(routePlanner);

        if ((username != null) && (password != null)) {
            this.client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
            this.authEnabled = true;
        } else {
            authEnabled = false;
        }
    }

    /** {@inheritDoc} */
    public List<DavResource> list(final String url) throws IOException {
        final URI uri = URI.create(url);
        final HttpPropFind propFind = new HttpPropFind(uri.toASCIIString());
        propFind.setEntity(HttpClientUtils.newXmlStringEntityFromString(SardineUtil.getDefaultPropfindXML()));
        final Unmarshaller unmarshaller = SardineUtil.createUnmarshaller();
        final MultiStatusResponseHandler responseHandler = new MultiStatusResponseHandler(url, unmarshaller);
        final Multistatus multistatus = wrapResponseHandlerExceptions(propFind, responseHandler);
        return fromMultiStatus(uri, multistatus);
    }

    /**
     * Wraps all checked exceptions coming from the responseHandler to {@link IOException}.
     *
     * @param <T>
     *            return type
     * @param request
     *            to execute
     * @param responseHandler
     *            for the type
     * @return parsed response
     * @throws IOException when something goes wrong. request will be aborted in this case.
     */
    <T> T wrapResponseHandlerExceptions(final HttpRequestBase request, final ResponseHandler<T> responseHandler)
            throws IOException {
        try {
            setAuthenticationOnMethod(request);
            return client.execute(request, responseHandler);
        } catch (ClientProtocolException e) {
            request.abort();
            throw e;
        } catch (IOException e) {
            request.abort();
            throw e;
        } catch (AuthenticationException e) {
            request.abort();
            throw new IOException(e);
        }
    }

    /**
     * Returns a List of {@link DavResource}s from the given {@link Multistatus}. The name of the resource is calculated
     * from the last path element of the Href.
     *
     * @param uri
     *            of the initial request.
     * @param multistatus
     *            from the request.
     * @return a List of {@link DavResource}s
     */
    List<DavResource> fromMultiStatus(final URI uri, Multistatus multistatus) {

        final List<Response> responses = multistatus.getResponse();
        final List<DavResource> resources = new ArrayList<DavResource>(responses.size());

        // Get the part of the url from the start to the first slash
        // ie: http://server.com
        final String hostPart;
        try {
            hostPart = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null)
                    .toASCIIString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not get hostpart from " + uri, e);
        }

        final String baseUrl;
        if (uri.getPath().endsWith("/")) {
            baseUrl = uri.getPath();
        } else {
            baseUrl = null;
        }

        for (final Response resp : responses) {
            final ResponseToDavResource toDavResource = new ResponseToDavResource(resp, baseUrl, hostPart);
            resources.add(toDavResource.toDavResource());
        }
        return resources;
    }

    /** {@inheritDoc} */
    public void patch(String url, Map<String, String> setProps, List<String> removeProps)
            throws IOException {
        LOG.trace("PROPPATCH '{}', setProps={}, removeProps={}", new Object[] { url, setProps, removeProps });
        final HttpPropPatch propPatch = new HttpPropPatch(url);
        final String resourcePatchXml = SardineUtil.getResourcePatchXml(setProps, removeProps);
        propPatch.setEntity(HttpClientUtils.newXmlStringEntityFromString(resourcePatchXml));
        wrapResponseHandlerExceptions(propPatch, new VoidResponseHandler(url,
                "Failed to set custom properties on resources."));
    }

    /** {@inheritDoc} */
    public InputStream get(String url) throws IOException {
        LOG.trace("GET '{}'", url);
        // This method can not use a ResponseHandler,
        // as InputStream is consumed otherwise.
        final HttpGet get = new HttpGet(url);
        final HttpResponse response = this.executeWrapper(get);
        final VoidResponseHandler handler = new VoidResponseHandler(url, "GET failed");
        try {
            handler.handleResponse(response);
            // Will consume the entity when the stream is closed
            return new ConsumingInputStream(response);
        } catch (IOException ex) {
            get.abort();
            throw new IOException("Error while accessing " + url, ex);
        }
    }

    /** {@inheritDoc} */
    @Deprecated
    public InputStream getInputStream(String url) throws IOException {
        return get(url);
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data) throws IOException {
        LOG.trace("PUT '{}', data.length={}", url, data.length);
        put(url, data, null);
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data, String contentType) throws IOException {
        final HttpPut put = new HttpPut(url);
        final ByteArrayEntity entity = new ByteArrayEntity(data);
        put(url, put, entity, null, true);
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream) throws IOException {
        put(url, dataStream, null);
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType) throws IOException {
        final HttpPut put = new HttpPut(url);
        // A length of -1 means "go until end of stream"
        final InputStreamEntity entity = new InputStreamEntity(dataStream, -1L);
        put(url, put, entity, contentType, true);
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException {
        final HttpPut put = new HttpPut(url);
        // A length of -1 means "go until end of stream"
        final InputStreamEntity entity = new InputStreamEntity(dataStream, -1L);
        put(url, put, entity, contentType, expectContinue);
    }
    /**
     * Private helper for doing the work of a put
     */
    void put(final String url, HttpPut put, AbstractHttpEntity entity, String contentType, boolean expectContinue)
            throws IOException {
        LOG.trace("PUT '{}', entity={}, contentType='{}', expectContinue='{}'", new Object[]{url, entity, contentType, expectContinue});
        put.setEntity(entity);
        if (contentType != null) {
            put.setHeader("Content-Type", contentType);
        }
        if (expectContinue) {
            put.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        }
        try {
            wrapResponseHandlerExceptions(put, new VoidResponseHandler(url, "PUT failed"));
        } catch (HttpResponseException e) {
            if (isStatusExpectationFailedAndEntityRepeatable(e, entity)) {
                put.removeHeaders(HTTP.EXPECT_DIRECTIVE);
                wrapResponseHandlerExceptions(put, new VoidResponseHandler(url, "PUT failed even with removed " + HTTP.EXPECT_DIRECTIVE));
            } else {
                throw e;
            }
        }
    }

    /**
     * Helper method for non integrative testing of error condition of
     * {@link SardineHttpClientImpl#put(String, HttpPut, AbstractHttpEntity, String, boolean)}.
     * 
     * @param e
     *            received Exception
     * @param entity
     *            to put
     * @return true when {@link HttpStatus#SC_EXPECTATION_FAILED} and
     */
    boolean isStatusExpectationFailedAndEntityRepeatable(HttpResponseException e, AbstractHttpEntity entity) {
        return e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED && entity.isRepeatable();
    }

    /** {@inheritDoc} */
    public void delete(String url) throws IOException {
        LOG.trace("DELETE '{}'", url);
        final HttpDelete delete = new HttpDelete(url);
        wrapResponseHandlerExceptions(delete, new VoidResponseHandler(url, "DELETE failed"));
    }

    /** {@inheritDoc} */
    public void move(String sourceUrl, String destinationUrl) throws IOException {
        move(sourceUrl, destinationUrl, false);
    }


    /** {@inheritDoc} */
    public void moveReplacing(String sourceUrl, String destinationUrl) throws IOException {
        move(sourceUrl, destinationUrl, true);
    }

    /**
     * @param sourceUrl
     * @param destinationUrl
     * @param overwrite
     * @throws IOException
     */
    private void move(String sourceUrl, String destinationUrl, final boolean overwrite) throws IOException {
        LOG.trace("MOVE '{}' to '{}', overwrite={}", new Object[] { sourceUrl, destinationUrl, overwrite });
        final HttpMove move = new HttpMove(sourceUrl, destinationUrl, overwrite);
        wrapResponseHandlerExceptions(move, new VoidResponseHandler(sourceUrl, "MOVE '" + sourceUrl
                + "' to '" + destinationUrl + "' failed"));
    }
    
    /** {@inheritDoc} */
    public void copy(String sourceUrl, String destinationUrl) throws IOException {
        copy(sourceUrl, destinationUrl, false);
    }

    /** {@inheritDoc} */
    public void copyReplacing(String sourceUrl, String destinationUrl) throws IOException {
        copy(sourceUrl, destinationUrl, true);
    }

    /**
     * @param sourceUrl
     * @param destinationUrl
     * @param overwrite
     * @throws IOException
     */
    private void copy(String sourceUrl, String destinationUrl, final boolean overwrite) throws IOException {
        LOG.trace("COPY '{}' to '{}', overwrite={}", new Object[] { sourceUrl, destinationUrl, overwrite });
        final HttpCopy copy = new HttpCopy(sourceUrl, destinationUrl, overwrite);
        wrapResponseHandlerExceptions(copy, new VoidResponseHandler(sourceUrl, "COPY '" + sourceUrl
                + " to '" + destinationUrl + "' failed"));
    }

    /** {@inheritDoc} */
    public void createDirectory(String url) throws IOException {
        LOG.trace("MKCOL '{}'", url);
        HttpMkCol mkcol = new HttpMkCol(url);
        wrapResponseHandlerExceptions(mkcol, new VoidResponseHandler(url, "MKCOL failed"));
    }

    /** {@inheritDoc} */
    public boolean exists(final String url) throws IOException {
        LOG.trace("HEAD '{}'", url);
        final HttpHead head = new HttpHead(url);
        return wrapResponseHandlerExceptions(head, new ExistsResponseHandler(url));
    }

    /**
     * Small wrapper around HttpClient.execute() in order to wrap the IOException into a IOException
     * and setting authentication.
     */
    private HttpResponse executeWrapper(HttpRequestBase base) throws IOException {
        try {
            setAuthenticationOnMethod(base);
            return this.client.execute(base);
        } catch (IOException ex) {
            base.abort();
            throw new IOException(ex);
        } catch (AuthenticationException e) {
            base.abort();
            throw new IOException(e);
        }
    }

    /**
     * @param base
     * @throws AuthenticationException
     */
    void setAuthenticationOnMethod(HttpRequestBase base) throws AuthenticationException {
        if (this.authEnabled) {
            final Credentials creds = this.client.getCredentialsProvider().getCredentials(AuthScope.ANY);
            AuthParams.setCredentialCharset(base.getParams(), AuthParams.getCredentialCharset(client.getParams()));
            base.setHeader(new BasicScheme().authenticate(creds, base));
        }
    }

    /** {@inheritDoc} */
    public DefaultHttpClient getHttpClient() {
        return client;
    }

}
