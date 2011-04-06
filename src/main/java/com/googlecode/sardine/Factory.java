package com.googlecode.sardine;

import java.io.IOException;

import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.googlecode.sardine.httpclient.SardineHttpClientImpl;


/**
 * The factory class is responsible for instantiating the JAXB stuff as well as the instance to SardineImpl.
 * 
 * @author jonstevens
 */
public class Factory {
    /** */
    private final static Factory INSTANCE = new Factory();

    /** */
    protected static Factory instance() {
        return INSTANCE;
    }

    /** */
    public Sardine begin() throws IOException {
        return this.begin(null, null, null, null, null);
    }

    /** */
    public Sardine begin(SSLSocketFactory sslSocketFactory) throws IOException {
        return this.begin(null, null, sslSocketFactory);
    }

    /** */
    public Sardine begin(String username, String password) throws IOException {
        return this.begin(username, password, null, null, null);
    }

    /** */
    public Sardine begin(String username, String password, Integer port) throws IOException {
        return this.begin(username, password, null, null, port);
    }

    /** */
    public Sardine begin(String username, String password, HttpRoutePlanner routePlanner) throws IOException {
        return this.begin(username, password, null, routePlanner);
    }

    /** */
    public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory) throws IOException {
        return this.begin(username, password, sslSocketFactory, null, null);
    }

    /** */
    public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner) throws IOException {
        return this.begin(username, password, sslSocketFactory, routePlanner, null);
    }

    /** */
    public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory,
            HttpRoutePlanner routePlanner, Integer port) throws IOException {
        return new SardineHttpClientImpl(username, password, sslSocketFactory, routePlanner, port);
    }
}
