/**
 * Copyright 2011 Mirko Friedenhagen
 */

package com.googlecode.sardine;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import com.googlecode.sardine.httpclient.HttpClientUtils;
import com.googlecode.sardine.httpclient.SardineHttpClientImpl;

/**
 * Builder for a {@link HttpClient} based {@link Sardine}.
 *
 * @author mirko
 */
public class SardineBuilder {

    private SSLSocketFactory sslSocketFactory;

    private HttpRoutePlanner routePlanner;

    private final DefaultHttpClient httpClient;

    private int port;

    /**
     * Creates a {@link Sardine} without credentials.
     */
    public SardineBuilder() {
        httpClient = new DefaultHttpClient(getConnectionManager(), HttpClientUtils.createDefaultHttpParams());
    }

    /**
     * Creates a {@link Sardine} with credentials. These are used for every access preemptively.
     */
    public SardineBuilder(final String userName, final String password) {
        this();
        final AuthScope authscope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM,
                AuthScope.ANY_SCHEME);
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        httpClient.getCredentialsProvider().setCredentials(authscope, credentials);
    }

    public SardineBuilder with(final SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public SardineBuilder with(final HttpRoutePlanner routePlanner) {
        httpClient.setRoutePlanner(routePlanner);
        return this;
    }

    public SardineBuilder with(final int port) {
        this.port = port;
        return this;
    }

    public SardineBuilder withCompression() {
        HttpClientUtils.enableCompression(httpClient);
        return this;
    }

    public SardineBuilder withPreemptiveAuthentication() {
        HttpClientUtils.enableCompression(httpClient);
        return this;
    }

    public Sardine build() {
        return new SardineHttpClientImpl(httpClient);

    }

    /**
     * @return
     */
    protected ClientConnectionManager getConnectionManager() {
        return new SingleClientConnManager(HttpClientUtils.createDefaultHttpParams(),
                HttpClientUtils.createDefaultSchemeRegistry(sslSocketFactory, port));
    }

}
