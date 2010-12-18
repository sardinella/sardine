package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
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

import com.googlecode.sardine.httpclient.ExistsResponseHandler;
import com.googlecode.sardine.httpclient.HttpClientUtils;
import com.googlecode.sardine.httpclient.HttpCopy;
import com.googlecode.sardine.httpclient.HttpMkCol;
import com.googlecode.sardine.httpclient.HttpMove;
import com.googlecode.sardine.httpclient.HttpPropFind;
import com.googlecode.sardine.httpclient.HttpPropPatch;
import com.googlecode.sardine.httpclient.MultiStatusResponseHandler;
import com.googlecode.sardine.httpclient.VoidResponseHandler;
import com.googlecode.sardine.httpclient.WrappedInputStream;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.ResponseToDavResource;
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;

/**
 * Implementation of the Sardine interface. This is where the meat of the Sardine library lives.
 * 
 * @author jonstevens
 */
public class SardineHttpClientImpl implements Sardine {

    /** */
    final DefaultHttpClient client;

    /** was a username/password passed in? */
    final boolean authEnabled;

    /** */
    public SardineHttpClientImpl() throws SardineException {
        this(null, null, null, null, null);
    }

    /** */
    public SardineHttpClientImpl(String username, String password) throws SardineException {
        this(username, password, null, null);
    }

    /** */
    public SardineHttpClientImpl(String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner) throws SardineException {
        this(username, password, sslSocketFactory, routePlanner, null);
    }

    public SardineHttpClientImpl(final DefaultHttpClient httpClient) {
        this.client = httpClient;
        this.authEnabled = false;
    }

    /** */
    public SardineHttpClientImpl(final DefaultHttpClient httpClient, String username, String password)
            throws SardineException {
        this.client = httpClient;
        this.client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
        this.authEnabled = true;
    }

    /**
     * Main constructor.
     */
    public SardineHttpClientImpl(String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner, Integer port) throws SardineException {
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
    public List<DavResource> getResources(final String url) throws SardineException {
        final URI uri = URI.create(url);
        HttpPropFind propFind = new HttpPropFind(uri.toASCIIString());
        propFind.setEntity(SardineUtil.getResourcesEntity());
        final Unmarshaller unmarshaller = SardineUtil.createUnmarshaller();
        final MultiStatusResponseHandler responseHandler = new MultiStatusResponseHandler(url, unmarshaller);
        final Multistatus multistatus = wrapResponseHandlerExceptions(propFind, responseHandler);
        return fromMultiStatus(uri, multistatus);
    }

    /**
     * Wraps all checked exceptions coming from the responseHandler to {@link SardineException}.
     * 
     * @param <T>
     *            return type
     * @param request
     *            to execute
     * @param responseHandler
     *            for the type
     * @return parsed response
     * @throws SardineException
     */
    <T> T wrapResponseHandlerExceptions(final HttpRequestBase request, final ResponseHandler<T> responseHandler)
            throws SardineException {
        try {
            setAuthenticationOnMethod(request);
            return client.execute(request, responseHandler);
        } catch (ClientProtocolException e) {
            throw new SardineException(e);
        } catch (SardineException e) {
            throw e;
        } catch (IOException e) {
            throw new SardineException(e);
        } catch (AuthenticationException e) {
            throw new SardineException(e);
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
            throw new RuntimeException("Could not get hostpart from " + uri, e);
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
    public void setCustomProps(String url, Map<String, String> setProps, List<String> removeProps)
            throws SardineException {
        final HttpPropPatch propPatch = new HttpPropPatch(url);
        propPatch.setEntity(SardineUtil.getResourcePatchEntity(setProps, removeProps));
        wrapResponseHandlerExceptions(propPatch, new VoidResponseHandler(url,
                "Failed to set custom properties on resources."));
    }

    /** {@inheritDoc} */
    public InputStream getInputStream(String url) throws SardineException {
        final HttpGet get = new HttpGet(url);
        final HttpResponse response = this.executeWrapper(get);
        final StatusLine statusLine = response.getStatusLine();
        if (!SardineUtil.isGoodResponse(statusLine.getStatusCode())) {
            get.abort();
            throw new SardineException(url, statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }

        try {
            return new WrappedInputStream(url, response);
        } catch (IOException ex) {
            get.abort();
            throw new SardineException(ex);
        }
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data) throws SardineException {
        put(url, data, null);
    }

    /** {@inheritDoc} */
    public void put(String url, byte[] data, String contentType) throws SardineException {
        HttpPut put = new HttpPut(url);
        ByteArrayEntity entity = new ByteArrayEntity(data);
        put(url, put, entity, null);
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream) throws SardineException {
        put(url, dataStream, null);
    }

    /** {@inheritDoc} */
    public void put(String url, InputStream dataStream, String contentType) throws SardineException {
        HttpPut put = new HttpPut(url);
        // A length of -1 means "go until end of stream"
        InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
        put(url, put, entity, contentType);
    }

    /**
     * Private helper for doing the work of a put
     */
    private void put(final String url, HttpPut put, AbstractHttpEntity entity, String contentType)
            throws SardineException {
        put.setEntity(entity);
        if (contentType != null) {
            put.setHeader("Content-Type", contentType);
        }
        wrapResponseHandlerExceptions(put, new VoidResponseHandler(url, "PUT failed"));
    }

    /** {@inheritDoc} */
    public void delete(String url) throws SardineException {
        final HttpDelete delete = new HttpDelete(url);
        wrapResponseHandlerExceptions(delete, new VoidResponseHandler(url, "DELETE failed"));
    }

    /** {@inheritDoc} */
    public void move(String sourceUrl, String destinationUrl) throws SardineException {
        HttpMove move = new HttpMove(sourceUrl, destinationUrl);
        wrapResponseHandlerExceptions(move, new VoidResponseHandler(sourceUrl, "MOVE sourceUrl: " + sourceUrl
                + " to destinationUrl: " + destinationUrl + " failed"));
    }

    /** {@inheritDoc} */
    public void copy(String sourceUrl, String destinationUrl) throws SardineException {
        HttpCopy copy = new HttpCopy(sourceUrl, destinationUrl);
        wrapResponseHandlerExceptions(copy, new VoidResponseHandler(sourceUrl, "COPY sourceUrl: " + sourceUrl
                + " to destinationUrl: " + destinationUrl + " failed"));
    }

    /** {@inheritDoc} */
    public void createDirectory(String url) throws SardineException {
        HttpMkCol mkcol = new HttpMkCol(url);
        wrapResponseHandlerExceptions(mkcol, new VoidResponseHandler(url, "MKCOL failed"));
    }

    /** {@inheritDoc} */
    public boolean exists(final String url) throws SardineException {
        final HttpHead head = new HttpHead(url);
        return wrapResponseHandlerExceptions(head, new ExistsResponseHandler(url));
    }

    /**
     * Small wrapper around HttpClient.execute() in order to wrap the IOException into a SardineException.
     */
    private HttpResponse executeWrapper(HttpRequestBase base) throws SardineException {
        try {
            setAuthenticationOnMethod(base);
            return this.client.execute(base);
        } catch (IOException ex) {
            base.abort();
            throw new SardineException(ex);
        } catch (AuthenticationException e) {
            base.abort();
            throw new SardineException(e);
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
