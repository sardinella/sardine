/**
 * Copyright 2011 Mirko Friedenhagen 
 */

package com.googlecode.sardine.asynchttpclient;

import com.googlecode.sardine.AbstractSardineImplTest;
import com.googlecode.sardine.Sardine;

/**
 * @author mirko
 *
 */
public class SardineAsyncHttpClientImplTest extends AbstractSardineImplTest {

    /** {@inheritDoc} */
    @Override
    protected Sardine createSardine() {
        return new SardineAsyncHttpClientImpl();
    }

}
