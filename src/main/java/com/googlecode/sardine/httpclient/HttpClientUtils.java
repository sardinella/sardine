/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.googlecode.sardine.Version;

/**
 * Helper class with static methods.
 * 
 * @author mirko
 */
public final class HttpClientUtils {

    /**
     * Helper class with static methods 
     */
    private HttpClientUtils() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param client
     */
    public static void disableCompression(final DefaultHttpClient client) {
        client.removeRequestInterceptorByClass(GzipSupportRequestInterceptor.class);
        client.removeResponseInterceptorByClass(GzipSupportResponseInterceptor.class);
    }

    public static void enableCompression(DefaultHttpClient client) {
        client.addRequestInterceptor(new GzipSupportRequestInterceptor());
        client.addResponseInterceptor(new GzipSupportResponseInterceptor());        
    }

    /**
     * @return
     */
    public static HttpParams createDefaultHttpParams() {
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "Sardine/" + Version.getSpecification());
        return params;
    }

    /**
     * @param sslSocketFactory
     * @param port
     * @return
     */
    public static SchemeRegistry createDefaultSchemeRegistry(SSLSocketFactory sslSocketFactory, Integer port) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port != null ? port : 80));
        if (sslSocketFactory != null)
            schemeRegistry.register(new Scheme("https", sslSocketFactory, port != null ? port : 443));
        else
            schemeRegistry
                    .register(new Scheme("https", SSLSocketFactory.getSocketFactory(), port != null ? port : 443));
        return schemeRegistry;
    }

    /**
     * @param sslSocketFactory
     * @param port
     * @return
     */
    public static DefaultHttpClient createDefaultHttpClient(SSLSocketFactory sslSocketFactory, Integer port) {
        HttpParams params = createDefaultHttpParams();
        SchemeRegistry schemeRegistry = createDefaultSchemeRegistry(sslSocketFactory, port);
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        final DefaultHttpClient defaultHttpClient = new DefaultHttpClient(cm, params);
        return defaultHttpClient;
    }

}
