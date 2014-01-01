/**
 * Copyright 2011 Mirko Friedenhagen 
 */

package com.googlecode.sardine.asynchttpclient;

import org.junit.After;

import com.googlecode.sardine.AbstractSardineImplTest;
import com.googlecode.sardine.Sardine;
import com.ning.http.client.AsyncHttpClient;

/**
 * @author mfriedenhagen
 *
 */
public class SardineAsyncHttpClientImplIT extends AbstractSardineImplTest {

    private AsyncHttpClient client;

    @After
    public void closeClient() {
        client.close();
    }

    /** {@inheritDoc} */
    @Override
    protected Sardine createSardine() {
        client = new AsyncHttpClient();
        return new SardineAsyncHttpClientImpl(client);
    }

}
