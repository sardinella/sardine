package com.googlecode.sardine;

import java.net.URI;
import java.util.Date;
import java.util.Map;

import com.googlecode.sardine.util.SardineUtil;

/**
 * Describes a resource on a remote server. This could be a directory or an actual file.
 *
 * @author jonstevens
 */
public class DavResource {

    private final String baseUrl;

    private final String name;

    private final Date creation;

    private final Date modified;

    private final String contentType;

    private final Long contentLength;

    private final boolean currentDirectory;

    private final Map<String, String> customProps;

    private final String url;

    private final String nameDecoded;

    /**
     * Represents a webdav response block.
     *
     * @param name
     *            the name of the resource, with all /'s removed
     */
    public DavResource(String baseUrl, String name, Date creation, Date modified, String contentType,
            Long contentLength, boolean currentDirectory, Map<String, String> customProps) {
        this.baseUrl = baseUrl;
        this.name = name;
        this.creation = creation == null ? null : new Date(creation.getTime());
        this.modified = modified == null ? null : new Date(modified.getTime());
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.currentDirectory = currentDirectory;
        this.customProps = customProps;
        this.nameDecoded = SardineUtil.decode(name);
        final URI baseUri;
        try {
            baseUri = URI.create(baseUrl + "/").normalize();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create URI from baseUrl=" + baseUrl, e);
        }
        try {
            if (isDirectory() && currentDirectory) {
                this.url = baseUri.toASCIIString();
            } else if (isDirectory()) {
                this.url = baseUri.resolve(URI.create(name + "/")).toASCIIString();
            } else {
                this.url = baseUri.resolve(URI.create(name)).toASCIIString();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create URI from baseUrl=" + baseUrl + ", name=" + name, e);
        }
    }

    /** */
    public String getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * A URLEncoded version of the name as returned by the server.
     */
    public String getName() {
        return this.name;
    }

    /**
     * A URLDecoded version of the name.
     */
    public String getNameDecoded() {
        return this.nameDecoded;
    }

    /** */
    public Date getCreation() {
        return new Date(this.creation.getTime());
    }

    /** */
    public Date getModified() {
        return new Date(this.modified.getTime());
    }

    /** */
    public String getContentType() {
        return this.contentType;
    }

    /** */
    public Long getContentLength() {
        return this.contentLength;
    }

    /**
     * Absolute url to the resource.
     */
    public String getAbsoluteUrl() {
        return this.url;
    }

    /**
     * Does this resource have a contentType of httpd/unix-directory?
     */
    public final boolean isDirectory() {
        return (this.contentType != null && this.contentType.equals("httpd/unix-directory"));
    }

    /**
     * Is this the current directory for the path we requested? ie: if we requested: http://foo.com/bar/dir/, is this
     * the DavResource for that directory?
     */
    public boolean isCurrentDirectory() {
        return this.currentDirectory;
    }

    /** */
    public Map<String, String> getCustomProps() {
        return this.customProps;
    }

    /** */
    @Override
    public String toString() {
        return "DavResource [baseUrl=" + this.baseUrl + ", contentLength=" + this.contentLength + ", contentType="
                + this.contentType + ", creation=" + this.creation + ", modified=" + this.modified + ", name="
                + this.name + ", nameDecoded=" + this.getNameDecoded() + ", getAbsoluteUrl()=" + this.getAbsoluteUrl()
                + ", isDirectory()=" + this.isDirectory() + "]";
    }
}
