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
import org.apache.http.HttpVersion;
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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.googlecode.sardine.httpclient.ExistsResponseHandler;
import com.googlecode.sardine.httpclient.GzipSupportRequestInterceptor;
import com.googlecode.sardine.httpclient.GzipSupportResponseInterceptor;
import com.googlecode.sardine.httpclient.HttpCopy;
import com.googlecode.sardine.httpclient.HttpMkCol;
import com.googlecode.sardine.httpclient.HttpMove;
import com.googlecode.sardine.httpclient.HttpPropFind;
import com.googlecode.sardine.httpclient.HttpPropPatch;
import com.googlecode.sardine.httpclient.MultiStatusResponseHandler;
import com.googlecode.sardine.httpclient.VoidResponseHandler;
import com.googlecode.sardine.model.Creationdate;
import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Getlastmodified;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;

/**
 * Implementation of the Sardine interface. This is where the meat of the Sardine library lives.
 * 
 * @author jonstevens
 */
public class SardineHttpClientImpl implements Sardine {

    /** */
    Factory factory;

    /** */
    DefaultHttpClient client;

    /** was a username/password passed in? */
    boolean authEnabled;

    private boolean supportsCompression;

    /** */
    public SardineHttpClientImpl(Factory factory) throws SardineException {
        this(factory, null, null, null, null);
    }

    /** */
    public SardineHttpClientImpl(Factory factory, String username, String password) throws SardineException {
        this(factory, username, password, null, null);
    }

    /** */
    public SardineHttpClientImpl(Factory factory, String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner) throws SardineException {
        this(factory, username, password, sslSocketFactory, routePlanner, null);
    }

    /**
     * Main constructor.
     */
    public SardineHttpClientImpl(Factory factory, String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner, Integer port) throws SardineException {
        this.factory = factory;

        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "Sardine/" + Version.getSpecification());

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port != null ? port : 80));
        if (sslSocketFactory != null)
            schemeRegistry.register(new Scheme("https", sslSocketFactory, port != null ? port : 443));
        else
            schemeRegistry
                    .register(new Scheme("https", SSLSocketFactory.getSocketFactory(), port != null ? port : 443));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        this.client = new DefaultHttpClient(cm, params);

        // for proxy configurations
        if (routePlanner != null)
            this.client.setRoutePlanner(routePlanner);

        if ((username != null) && (password != null)) {
            this.client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));

            this.authEnabled = true;
        }
    }

    /** {@inheritDoc} */
    public void enableCompression() {
        if (!this.supportsCompression) {
            this.client.addRequestInterceptor(new GzipSupportRequestInterceptor());
            this.client.addResponseInterceptor(new GzipSupportResponseInterceptor());
            this.supportsCompression = true;
        }
    }

    /** {@inheritDoc} */
    public void disableCompression() {
        if (this.supportsCompression) {
            this.client.removeRequestInterceptorByClass(GzipSupportRequestInterceptor.class);
            this.client.removeResponseInterceptorByClass(GzipSupportResponseInterceptor.class);
            this.supportsCompression = false;
        }
    }

    /** */
    public boolean isCompressionEnabled() {
        return this.supportsCompression;
    }

    /** {@inheritDoc} */
    public List<DavResource> getResources(final String url) throws SardineException {
        final URI uri = URI.create(url);
        HttpPropFind propFind = new HttpPropFind(uri.toASCIIString());
        propFind.setEntity(SardineUtil.getResourcesEntity());
        final Unmarshaller unmarshaller = factory.getUnmarshaller();
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
        } catch (IOException e) {
            throw new SardineException(e);
        } catch (AuthenticationException e) {
            throw new SardineException(e);
        }
    }

    /**
     * @param uri
     * @param multistatus
     * @return
     */
    List<DavResource> fromMultiStatus(final URI uri, Multistatus multistatus) {
        List<Response> responses = multistatus.getResponse();

        List<DavResource> resources = new ArrayList<DavResource>(responses.size());

        String baseUrl = null;
        if (uri.getPath().endsWith("/"))
            baseUrl = uri.getPath();

        // Get the part of the url from the start to the first slash
        // ie: http://server.com
        String hostPart;
        try {
            hostPart = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null)
                    .toASCIIString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Message:", e);
        }
        for (Response resp : responses) {
            boolean currentDirectory = false;
            boolean isDirectory = false;

            String href = resp.getHref().get(0);

            // figure out the name of the file and set
            // the baseUrl if it isn't already set (like when
            // we are looking for just one file)
            String name = null;
            if (baseUrl != null) {
                // Some (broken) servers don't return a href with a trailing /
                if ((href.length() == baseUrl.length() - 1) && baseUrl.endsWith("/")) {
                    href += "/";
                }

                if (href.startsWith(hostPart)) {
                    name = href.substring(hostPart.length() + baseUrl.length());
                } else
                    name = href.substring(baseUrl.length());

                if ("".equals(name) || (name.length() == 0)) {
                    // This is the directory itself.
                    isDirectory = true;
                    currentDirectory = true;
                }
            } else {
                // figure out the name of the file
                int last = href.lastIndexOf("/") + 1;
                name = href.substring(last);

                // this is the part after the host, but without the file
                baseUrl = href.substring(0, last);
            }

            // Remove the final / from the name for directories
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
                isDirectory = true;
            }

            Prop prop = resp.getPropstat().get(0).getProp();

            // SVN returns a content-type of text/html, but collection is set.
            if (prop.getResourcetype().getCollection() != null) {
                isDirectory = true;
            }

            Map<String, String> customProps = SardineUtil.extractCustomProps(prop.getAny());

            String creationdate = null;
            Creationdate gcd = prop.getCreationdate();
            if ((gcd != null) && (gcd.getContent().size() == 1))
                creationdate = gcd.getContent().get(0);

            // modifieddate is sometimes not set
            // if that's the case, use creationdate
            String modifieddate = null;
            Getlastmodified glm = prop.getGetlastmodified();
            if ((glm != null) && (glm.getContent().size() == 1))
                modifieddate = glm.getContent().get(0);
            else
                modifieddate = creationdate;

            String contentType = null;
            Getcontenttype gtt = prop.getGetcontenttype();
            if ((gtt != null) && (gtt.getContent().size() == 1))
                contentType = gtt.getContent().get(0);

            // Make sure that directories have the correct content type.
            if (isDirectory) {
                // Need to correct the contentType to identify as a directory.
                contentType = "httpd/unix-directory";
            }

            String contentLength = "0";
            Getcontentlength gcl = prop.getGetcontentlength();
            if ((gcl != null) && (gcl.getContent().size() == 1))
                contentLength = gcl.getContent().get(0);

            DavResource dr = new DavResource(hostPart + baseUrl, name, SardineUtil.parseDate(creationdate),
                    SardineUtil.parseDate(modifieddate), contentType, Long.valueOf(contentLength), currentDirectory,
                    customProps);

            resources.add(dr);
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
        HttpGet get = new HttpGet(url);

        HttpResponse response = this.executeWrapper(get);

        StatusLine statusLine = response.getStatusLine();
        if (!SardineUtil.isGoodResponse(statusLine.getStatusCode())) {
            get.abort();
            throw new SardineException(url, statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }

        try {
            return response.getEntity().getContent();
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

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.sardine.Sardine#put(java.lang.String, java.io.InputStream, java.lang.String)
     */
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
            Credentials creds = this.client.getCredentialsProvider().getCredentials(AuthScope.ANY);
            AuthParams.setCredentialCharset(base.getParams(), AuthParams.getCredentialCharset(client.getParams()));
            base.setHeader(new BasicScheme().authenticate(creds, base));
        }
    }

    /** {@inheritDoc} */
    // @Override
    public HttpClient getHttpClient() {
        return client;
    }
}
