/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.params.AuthParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.sardine.SardineHttpClientImpl;
import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 * A little integrative tests, environment is set in $user.home/sardine.properties, an xml file. 
 */
public class WithPasswordTest {

    final String server;

    final String userName;

    final String password;

    final String passwordEncoding;

    final SardineHttpClientImpl sardine;

    private String testDirectory;

    /**
     * @throws IOException
     * 
     */
    public WithPasswordTest() throws IOException {
        final Properties properties = new Properties();
        final File sardineProperties = new File(System.getProperty("user.home"), "sardine-it-test.properties");
        final FileInputStream in = new FileInputStream(sardineProperties);
        try {
            properties.loadFromXML(in);
        } finally {
            in.close();
        }
        server = properties.getProperty("webdavServer");
        userName = properties.getProperty("webdavUser");
        password = properties.getProperty("webdavPassword");
        passwordEncoding = properties.getProperty("webdavPasswordEncoding");
        assumeThat(server, notNullValue());
        assumeThat(userName, notNullValue());
        assumeThat(password, notNullValue());
        sardine = new SardineHttpClientImpl(userName, password);
        AuthParams.setCredentialCharset(sardine.getHttpClient().getParams(), passwordEncoding);
        testDirectory = server + "sardine-test/";        
    }

    @Before
    public void createTestDirectory() throws SardineException {
        sardine.createDirectory(testDirectory);
    }

    @After
    public void deleteTestDirectory() throws SardineException {
        sardine.delete(testDirectory);
    }

    @Test
    public void listDirectory() throws SardineException {
        assertEquals(1, sardine.getResources(testDirectory).size());
    }

    @Test
    public void moreTests() throws IOException {
        final String content = "hällo welt";
        sardine.put(testDirectory + "foo.txt", content.getBytes());
        assertEquals(2, sardine.getResources(testDirectory).size());
        sardine.copy(testDirectory + "foo.txt",  testDirectory + "bar.txt");
        assertEquals(3, sardine.getResources(testDirectory).size());
        final String renamed = testDirectory + "bar-renamed.txt";
        sardine.move(testDirectory + "bar.txt", renamed);
        assertEquals(3, sardine.getResources(testDirectory).size());        
        final InputStream stream = sardine.getInputStream(renamed);
        try {
            assertEquals(content, IOUtils.toString(stream));
        } finally {
            stream.close();
        }
        assertTrue(sardine.exists(renamed));
    }
    
    @Test
    public void moreTestsGzipped() throws IOException {
        HttpClientUtils.enableCompression(sardine.getHttpClient());
        moreTests();
    }

}
