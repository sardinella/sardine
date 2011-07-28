/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.model.Creationdate;
import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Getlastmodified;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Resourcetype;
import com.googlecode.sardine.model.Response;

/**
 * Helper class to convert a {@link Multistatus} Response to a {@link DavResource}.
 * 
 * @author mirko
 */
public class ResponseToDavResource {

    private final Response resp;

    private final Prop prop;

    /**
     * The default date if nothing is provided for {@link Creationdate} or {@link Getlastmodified}.
     */
    static final String DEFAULT_DATE = "1970-01-01'T'00:00:00.000'Z'";

    /**
     * The default content-type if {@link Getcontenttype} is not set in the {@link Multistatus} response.
     */
    static final String DEFAULT_CONTENT_TYPE = "application/octetstream";

    /**
     * The default content-lenght if {@link Getcontentlength} is not set in the {@link Multistatus} response.
     */
    static final String DEFAULT_CONTENT_LENGTH = "0";

    /**
     * content-type for {@link Collection}.
     */
    public static final String HTTPD_UNIX_DIRECTORY_CONTENT_TYPE = "httpd/unix-directory";

    private final String baseUrl;

    private final String hostPart;

    /**
     * Is this a directory.
     */
    private final boolean isDirectory;

    /**
     * @param resp
     *            to convert
     * @param hostPart
     *            including schema.
     * @param baseUrl
     *            actually the path component.
     */
    public ResponseToDavResource(final Response resp, final String baseUrl, final String hostPart) {
        this.resp = resp;
        this.baseUrl = baseUrl;
        this.hostPart = hostPart;
        prop = resp.getPropstat().get(0).getProp();
        final Resourcetype resourcetype = prop.getResourcetype();
        if (resourcetype != null) {
            isDirectory = resourcetype.getCollection() != null;
        } else {
            isDirectory = false;
        }
    }

    /**
     * Converts the given {@link Response} to a {@link DavResource}.
     * 
     * @return new {@link DavResource} from {@link Response}.
     */
    public DavResource toDavResource() {

        final String finalBaseUrl;
        final boolean currentDirectory;
        final String href = resp.getHref().get(0);

        // figure out the name of the file and set
        // the baseUrl if it isn't already set (like when
        // we are looking for just one file)
        final String name;
        if (baseUrl != null) {
            final String finalHref;
            finalBaseUrl = baseUrl;
            // Some (broken) servers don't return a href with a trailing /
            if ((href.length() == baseUrl.length() - 1) && baseUrl.endsWith("/")) {
                finalHref = href + "/";
            } else {
                finalHref = href;
            }

            if (finalHref.startsWith(hostPart)) {
                name = calculateNameFromComponentsHavingHostPart(finalHref);
            } else {
                name = finalHref.substring(baseUrl.length());
            }
            currentDirectory = "".equals(name) || (name.length() == 0);
        } else {
            // figure out the name of the file
            final int last = href.lastIndexOf("/") + 1;
            name = href.substring(last);
            // this is the part after the host, but without the file
            finalBaseUrl = href.substring(0, last);
            currentDirectory = false;
        }

        final Map<String, String> customProps = SardineUtil.extractCustomProps(prop.getAny());
        final String creationdate = retrieveCreationDate();
        final String modifieddate = retrieveModifiedDate(creationdate);
        final String contentType = retrieveContentType();
        final String contentLength = retrieveContentLength();
        final String finalName = removeTrailingSlashFromDirectoryName(name);

        return new DavResource(hostPart + finalBaseUrl, finalName, SardineUtil.parseDate(creationdate),
                SardineUtil.parseDate(modifieddate), contentType, Long.valueOf(contentLength), currentDirectory,
                customProps);
    }

    /**
     * Calculares the name of the resource.
     * @param finalHref
     * @return name of the resource.
     */
    private String calculateNameFromComponentsHavingHostPart(String finalHref) {
        return finalHref.substring(hostPart.length() + baseUrl.length());
    }

    /**
     * Retrieves modifieddate from props. modifieddate is sometimes not set if that's the case, use creation date
     * instead.
     * 
     * @param creationdate
     * @return
     */
    String retrieveModifiedDate(final String creationdate) {
        final String modifieddate;
        final Getlastmodified glm = prop.getGetlastmodified();
        if ((glm != null) && (glm.getContent().size() == 1)) {
            modifieddate = glm.getContent().get(0);
        } else {
            modifieddate = creationdate;
        }
        return modifieddate;
    }

    /**
     * Retrieves creationdate from props. If it is not available return {@link ResponseToDavResource#DEFAULT_DATE}.
     * 
     * @return creation date
     */
    String retrieveCreationDate() {
        final String creationdate;
        final Creationdate gcd = prop.getCreationdate();
        if ((gcd != null) && (gcd.getContent().size() == 1)) {
            creationdate = gcd.getContent().get(0);
        } else {
            creationdate = ResponseToDavResource.DEFAULT_DATE;
        }
        return creationdate;
    }

    /**
     * Retrieves the content-type from prop or set it to {@link ResponseToDavResource#DEFAULT_CONTENT_TYPE}. If
     * isDirectory always set the content-type to {@link ResponseToDavResource#HTTPD_UNIX_DIRECTORY_CONTENT_TYPE}.
     * 
     * @param prop
     *            from {@link Multistatus}
     * 
     * @return the content type.
     */
    String retrieveContentType() {
        final String contentType;
        // Make sure that directories have the correct content type.
        if (isDirectory) {
            // Need to correct the contentType to identify as a directory.
            contentType = ResponseToDavResource.HTTPD_UNIX_DIRECTORY_CONTENT_TYPE;
        } else {
            final Getcontenttype gtt = prop.getGetcontenttype();
            if ((gtt != null) && (gtt.getContent().size() == 1)) {
                contentType = gtt.getContent().get(0);
            } else {
                contentType = ResponseToDavResource.DEFAULT_CONTENT_TYPE;
            }
        }
        return contentType;
    }

    /**
     * Retrieves content-length from props. If it is not available return
     * {@link ResponseToDavResource#DEFAULT_CONTENT_LENGTH}.
     * 
     * @return contentlength
     */
    String retrieveContentLength() {
        final String contentLength;
        final Getcontentlength gcl = prop.getGetcontentlength();
        if ((gcl != null) && (gcl.getContent().size() == 1)) {
            contentLength = gcl.getContent().get(0);
        } else {
            contentLength = DEFAULT_CONTENT_LENGTH;
        }
        return contentLength;
    }

    /**
     * Removes trailing slash from name if this is a directory.
     * 
     * @param name
     *            of the resource
     * @return final name
     */
    String removeTrailingSlashFromDirectoryName(final String name) {
        final String finalName;
        // Remove the final / from the name for directories
        if (name.endsWith("/") && isDirectory) {
            finalName = name.substring(0, name.length() - 1);
        } else {
            finalName = name;
        }
        return finalName;
    }

    /**
     * Returns a List of {@link DavResource}s from the given {@link Multistatus}. The name of the resource is calculated
     * from the last path element of the Href.
     *
     * @param uri
     *            of the initial request.
     * @param multistatus
     *            from the request.
     * @return a List of {@link DavResource}s
     */
    public static List<DavResource> fromMultiStatus(final URI uri, Multistatus multistatus) {

        final List<Response> responses = multistatus.getResponse();
        final List<DavResource> resources = new ArrayList<DavResource>(responses.size());

        // Get the part of the url from the start to the first slash
        // ie: http://server.com
        final String hostPart;
        try {
            hostPart = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null)
                    .toASCIIString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not get hostpart from " + uri, e);
        }

        final String baseUrl;
        if (uri.getPath().endsWith("/")) {
            baseUrl = uri.getPath();
        } else {
            baseUrl = null;
        }

        for (final Response resp : responses) {
            final ResponseToDavResource toDavResource = new ResponseToDavResource(resp, baseUrl, hostPart);
            resources.add(toDavResource.toDavResource());
        }
        return resources;
    }

}
