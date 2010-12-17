/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import java.util.Map;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.model.Collection;
import com.googlecode.sardine.model.Creationdate;
import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Getlastmodified;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Response;

/**
 * Helper class to convert {@link Multistatus} to a {@link DavResource}.
 * 
 * @author mirko
 */
public class ResponseToDavResource {

    private final Response resp;

    private final Prop prop;

    /**
     * 
     */
    public static final String DEFAULT_DATE = "1970-01-01'T'00:00:00.000'Z'";

    /**
     * The default content-type if nothing is in the {@link Multistatus} response.
     */
    public static final String DEFAULT_CONTENT_TYPE = "application/octetstream";

    /**
     * content-type for {@link Collection}.
     */
    public static final String HTTPD_UNIX_DIRECTORY_CONTENT_TYPE = "httpd/unix-directory";

    private final String baseUrl;

    private final String hostPart;

    /**
     * @param resp
     * @param hostPart
     * @param baseUrl
     */
    public ResponseToDavResource(final Response resp, final String baseUrl, final String hostPart) {
        this.resp = resp;
        this.baseUrl = baseUrl;
        this.hostPart = hostPart;
        prop = resp.getPropstat().get(0).getProp();
    }

    /**
     * Retrieves modifieddate from props. modifieddate is sometimes not set if that's the case, use creationdate
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
     * @param prop
     * @return
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
     * Retrieves the contenttype from prop or set it to {@link ResponseToDavResource#DEFAULT_CONTENT_TYPE}. If
     * isDirectory always set the contenttype to {@link ResponseToDavResource#HTTPD_UNIX_DIRECTORY_CONTENT_TYPE}.
     * 
     * @param isDirectory
     *            true if this is a directory.
     * @param prop
     *            from {@link Multistatus}
     * @return the content type.
     */
    String retrieveContentType(boolean isDirectory) {
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

    public DavResource toDavResource() {

        final String finalBaseUrl;
        boolean currentDirectory = false;
        boolean isDirectory = false;

        String href = resp.getHref().get(0);

        // figure out the name of the file and set
        // the baseUrl if it isn't already set (like when
        // we are looking for just one file)
        final String name;
        final String finalName;
        if (baseUrl != null) {
            finalBaseUrl = baseUrl;
            // Some (broken) servers don't return a href with a trailing /
            if ((href.length() == baseUrl.length() - 1) && baseUrl.endsWith("/")) {
                href += "/";
            }

            if (href.startsWith(hostPart)) {
                name = href.substring(hostPart.length() + baseUrl.length());
            } else {
                name = href.substring(baseUrl.length());
            }
            if ("".equals(name) || (name.length() == 0)) {
                // This is the directory itself.
                isDirectory = true;
                currentDirectory = true;
            }
        } else {
            // figure out the name of the file
            int last = href.lastIndexOf("/") + 1;
            name = href.substring(last);

            // this is the part after the host, but without the file
            finalBaseUrl = href.substring(0, last);
        }

        // Remove the final / from the name for directories
        if (name.endsWith("/")) {
            finalName = name.substring(0, name.length() - 1);
            isDirectory = true;
        } else {
            finalName = name;
        }

        Prop prop = resp.getPropstat().get(0).getProp();

        // SVN returns a content-type of text/html, but collection is set.
        if (prop.getResourcetype().getCollection() != null) {
            isDirectory = true;
        }

        final Map<String, String> customProps = SardineUtil.extractCustomProps(prop.getAny());

        final String creationdate = retrieveCreationDate();
        final String modifieddate = retrieveModifiedDate(creationdate);
        final String contentType = retrieveContentType(isDirectory);
        String contentLength = "0";
        Getcontentlength gcl = prop.getGetcontentlength();
        if ((gcl != null) && (gcl.getContent().size() == 1))
            contentLength = gcl.getContent().get(0);

        return new DavResource(hostPart + finalBaseUrl, finalName, SardineUtil.parseDate(creationdate),
                SardineUtil.parseDate(modifieddate), contentType, Long.valueOf(contentLength), currentDirectory,
                customProps);
    }

}
