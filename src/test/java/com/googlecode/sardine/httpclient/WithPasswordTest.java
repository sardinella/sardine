/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.params.AuthParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.TUtils;

/**
 * A little integrative tests, environment is set in ${user.home}/sardine-it-test.properties, an xml properties file.
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
 * &lt;properties>
 *   &lt;entry key="webdavServer">https://webdav.example.com/&lt;/entry>
 *   &lt;entry key="webdavUser">USERNAME&lt;/entry>
 *   &lt;entry key="webdavPassword">PASSWORD&lt;/entry>
 *   &lt;entry key="webdavPasswordEncoding">iso-8859-1&lt;/entry>
 * &lt;/properties>
 * </pre>
 * 
 * @author mirko
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
        assumeTrue(sardineProperties.exists());
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
        TUtils.setHttpClientLogging();
        sardine = new SardineHttpClientImpl(userName, password);
        AuthParams.setCredentialCharset(sardine.getHttpClient().getParams(), passwordEncoding);
        testDirectory = server + "sardine-test/";
    }

    @Before
    public void createTestDirectory() throws IOException {
        sardine.createDirectory(testDirectory);
    }

    @After
    public void deleteTestDirectory() throws IOException {
        sardine.delete(testDirectory);
    }

    @Test
    public void listDirectory() throws IOException {
        assertEquals(1, sardine.getResources(testDirectory).size());
    }

    @Test
    public void moreTests() throws IOException {
        final String content = "hällo welt";
        sardine.put(testDirectory + "foo.txt", content.getBytes());
        assertEquals(2, sardine.getResources(testDirectory).size());
        sardine.copy(testDirectory + "foo.txt", testDirectory + "bar.txt");
        assertEquals(3, sardine.getResources(testDirectory).size());
        final String renamed = testDirectory + "bar-renamed.txt";
        sardine.move(testDirectory + "bar.txt", renamed);
        final List<DavResource> resources = sardine.getResources(testDirectory);
        assertEquals(3, resources.size());
        final InputStream stream = sardine.getInputStream(renamed);
        try {
            assertEquals(content, IOUtils.toString(stream));
        } finally {
            stream.close();
        }
        assertTrue(sardine.exists(renamed));
        final DavResource davResource = resources.get(1);
        final Map<String, String> customProps = davResource.getCustomProps();
        customProps.put("mööp", "müüp");
        sardine.setCustomProps(renamed, customProps, null);
        final Map<String, String> newCustomProps = sardine.getResources(renamed).get(0).getCustomProps();
        assertEquals(newCustomProps.get("mööp"), "müüp");
    }

    @Test
    public void moreTestsGzipped() throws IOException {
        HttpClientUtils.enableCompression(sardine.getHttpClient());
        moreTests();
    }

}
