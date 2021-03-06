/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.VersionInfo;

import com.googlecode.sardine.Version;

/**
 * Helper class with static methods.
 *
 * @author mirko
 */
public final class HttpClientUtils {

    /**
     *
     */
    private static final int MAX_TOTAL_CONNECTIONS = 100;

    /**
     * Helper class with static methods.
     */
    private HttpClientUtils() {
        // Helper class with static methods.
    }

    /**
     * Adds handling of compression to the client.
     *
     * @param client
     *            the {@link DefaultHttpClient}.
     */
    public static void enableCompression(DefaultHttpClient client) {
        client.addRequestInterceptor(new RequestAcceptEncoding());
        client.addResponseInterceptor(new ResponseContentEncoding());
    }

    /**
     * Creates default params, set maximal total connections to {@link HttpClientUtils#MAX_TOTAL_CONNECTIONS} and a
     * timeout of 3 seconds.
     * 
     * @return httpParams
     */
    public static HttpParams createDefaultHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        final String specification = Version.getSpecification();
        HttpProtocolParams.setUserAgent(params, "Sardine/" + specification == null ? VersionInfo.UNAVAILABLE : specification);
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        return params;
    }

    /**
     * Creates a new {@link SchemeRegistry}.
     *
     * @param sslSocketFactory
     *            alternative {@link SSLSocketFactory}.
     * @param port
     *            alternate port.
     * @return a new {@link SchemeRegistry}.
     */
    public static SchemeRegistry createDefaultSchemeRegistry(SSLSocketFactory sslSocketFactory, Integer port) {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", port != null ? port : 80, PlainSocketFactory.getSocketFactory()));
        final int sslPort = port != null ? port : 443;
        if (sslSocketFactory != null) {
            schemeRegistry.register(new Scheme("https", sslPort, sslSocketFactory));
        } else {
            schemeRegistry.register(new Scheme("https", sslPort, SSLSocketFactory.getSocketFactory()));
        }
        return schemeRegistry;
    }

    /**
     * Creates a new {@link DefaultHttpClient} with default settings.
     *
     * @param sslSocketFactory
     *            alternative {@link SSLSocketFactory}.
     *
     * @param port
     *            alternative port
     * @return a parameterized {@link DefaultHttpClient}.
     */
    public static DefaultHttpClient createDefaultHttpClient(SSLSocketFactory sslSocketFactory, Integer port) {
        final HttpParams params = createDefaultHttpParams();
        final SchemeRegistry schemeRegistry = createDefaultSchemeRegistry(sslSocketFactory, port);
        final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
        return new DefaultHttpClient(cm, params);
    }

    /**
     * Creates a new {@link DefaultHttpClient} with default settings.
     * 
     * @param connectionManager
     *            to use.
     * @return a parameterized {@link DefaultHttpClient}.
     */
    public static DefaultHttpClient createDefaultHttpClient(ClientConnectionManager connectionManager) {
        return new DefaultHttpClient(connectionManager, createDefaultHttpParams());
    }

    /**
     * Checks that destinationUrl ends with a slash when sourceUrl ends with a slash.
     *
     * @param sourceUrl
     *            source of copy or move
     * @param destinationUrl
     *            destination of copy or move
     * @throws IllegalArgumentException
     *             during mismatch
     */
    static void checkConsistentSlashes(String sourceUrl, String destinationUrl) {
        if (sourceUrl.endsWith("/") && !destinationUrl.endsWith("/")) {
            throw new IllegalArgumentException("destinationUrl must end with a / when sourceUrl ends with / " + destinationUrl);
        }
    }

    /**
     * Creates a new {@link StringEntity} whose Content-Type is set to text/xml.
     *
     * @param xml any string, not checked for validity or wellformedness.
     * @return
     */
    public static StringEntity newXmlStringEntityFromString(final String xml) {
        final StringEntity stringEntity;
        try {
            stringEntity = new StringEntity(xml, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not get encoding utf-8?" + xml, e);
        }
        stringEntity.setContentType("text/xml; charset=utf-8");
        return stringEntity;
    }

}
