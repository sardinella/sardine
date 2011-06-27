/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
import org.apache.http.HttpStatus;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.HttpResponseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.sardine.DavResource;

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
    
    private final static Logger LOG = LoggerFactory.getLogger(WithPasswordTest.class);

    final String server;

    final String userName;

    final String password;

    final String passwordEncoding;

    final SardineHttpClientImpl sardine;

    private String testDirectory;

    private static String FILE_CONTENT = "hällo welt";

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
        assertEquals(1, sardine.list(testDirectory).size());
    }

    @Test
    public void moreTests() throws IOException {
        sardine.put(testDirectory + "foo.txt", FILE_CONTENT.getBytes());
        assertEquals(2, sardine.list(testDirectory).size());
        sardine.copy(testDirectory + "foo.txt", testDirectory + "bar.txt");
        assertEquals(3, sardine.list(testDirectory).size());
        final String renamed = testDirectory + "bar-renamed.txt";
        sardine.move(testDirectory + "bar.txt", renamed);
        final List<DavResource> resources = sardine.list(testDirectory);
        assertEquals(3, resources.size());
        final InputStream stream = sardine.get(renamed);
        try {
            assertEquals(FILE_CONTENT, IOUtils.toString(stream));
        } finally {
            stream.close();
        }
        assertTrue(sardine.exists(renamed));
        final DavResource davResource = resources.get(1);
        final Map<String, String> customProps = davResource.getCustomProps();
        customProps.put("mööp", "müüp");
        sardine.patch(renamed, customProps, null);
        final Map<String, String> newCustomProps = sardine.list(renamed).get(0).getCustomProps();
        assertEquals(newCustomProps.get("mööp"), "müüp");
    }

    @Test
    public void testCopyWithoutAndWithOverWrite() throws IOException {
        sardine.put(testDirectory + "foo.txt", FILE_CONTENT.getBytes());
        assertEquals(2, sardine.list(testDirectory).size());
        sardine.copy(testDirectory + "foo.txt", testDirectory + "bar.txt");
        assertEquals(3, sardine.list(testDirectory).size());
        try {
            sardine.copy(testDirectory + "foo.txt", testDirectory + "bar.txt");
        } catch (HttpResponseException e) {
            LOG.debug("{}", e.toString());
            assertThat("Expected 412 Precondition failed ", e.getStatusCode(), is(HttpStatus.SC_PRECONDITION_FAILED));
        }
        assertEquals(3, sardine.list(testDirectory).size());
        sardine.copyReplacing(testDirectory + "foo.txt", testDirectory + "bar.txt");
        assertEquals(3, sardine.list(testDirectory).size());
    }

    @Test
    public void testMoveWithoutAndWithOverWrite() throws IOException {
        sardine.put(testDirectory + "foo.txt", FILE_CONTENT.getBytes());
        assertEquals(2, sardine.list(testDirectory).size());
        sardine.copy(testDirectory + "foo.txt", testDirectory + "bar.txt");
        assertEquals(3, sardine.list(testDirectory).size());
        try {
            sardine.move(testDirectory + "foo.txt", testDirectory + "bar.txt");
        } catch (HttpResponseException e) {
            LOG.debug("{}", e.toString());
            assertThat("Expected 412 Precondition failed ", e.getStatusCode(), is(HttpStatus.SC_PRECONDITION_FAILED));
        }
        assertEquals(3, sardine.list(testDirectory).size());
        sardine.moveReplacing(testDirectory + "foo.txt", testDirectory + "bar.txt");
        assertEquals(2, sardine.list(testDirectory).size());
    }

    @Test
    public void moreTestsGzipped() throws IOException {
        HttpClientUtils.enableCompression(sardine.getHttpClient());
        moreTests();
    }

}
