/**
 * Copyright 2013 Mirko Friedenhagen
 */

package com.googlecode.sardine;

import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Mirko Friedenhagen
 */
public class SardineFactoryTest {

    final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin() throws Exception {
        Sardine result = SardineFactory.begin();
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_SSLSocketFactory() throws Exception {
        Sardine result = SardineFactory.begin(sslSocketFactory);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_String_String() throws Exception {
        String username = "";
        String password = "";
        Sardine result = SardineFactory.begin(username, password);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_3args_1() throws Exception {
        String username = "";
        String password = "";
        Integer port = 80;
        Sardine result = SardineFactory.begin(username, password, port);
        assertNotNull(result);

    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_3args_2() throws Exception {
        String username = "";
        String password = "";
        Sardine result = SardineFactory.begin(username, password, sslSocketFactory);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_HttpRoutePlanner() throws Exception {
        HttpRoutePlanner routePlanner = null;
        Sardine result = SardineFactory.begin(routePlanner);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_HttpRoutePlanner_SSLSocketFactory() throws Exception {
        HttpRoutePlanner routePlanner = null;
        Sardine result = SardineFactory.begin(routePlanner, sslSocketFactory);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_3args_3() throws Exception {
        String username = "";
        String password = "";
        HttpRoutePlanner routePlanner = null;
        Sardine result = SardineFactory.begin(username, password, routePlanner);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_4args() throws Exception {
        String username = "";
        String password = "";
        HttpRoutePlanner routePlanner = null;
        Sardine result = SardineFactory.begin(username, password, sslSocketFactory, routePlanner);
        assertNotNull(result);
    }

    /**
     * Test of begin method, of class SardineFactory.
     */
    @Test
    public void testBegin_5args() throws Exception {
        String username = "";
        String password = "";
        HttpRoutePlanner routePlanner = null;
        Integer port = 80;
        Sardine result = SardineFactory.begin(username, password, sslSocketFactory, routePlanner, port);
        assertNotNull(result);
    }

}
