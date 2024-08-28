/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.util;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * Provides utility methods for resource management.
 */
public abstract class ResourceManagerUtil {

    public static final String FILE_PREFIX = "file:";

    private static final Logger log = LoggerFactory
            .getLogger(ResourceManagerUtil.class);

    private static final String CLASS_SUFFIX = ".class";
    private static final String CLASS_PREFIX = "class:";
    private static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Takes a URI as string and removes the schema name and colon.
     * Schemas expected: {@value #CLASS_PREFIX}, {@value #CLASSPATH_PREFIX},
     * {@value #FILE_PREFIX}
     *
     * @param uri
     *            the uri to normalize, must not be null or empty
     * @return the normalized uri
     * @throws IllegalArgumentException
     *             if the uri is null, empty or has an unrecognised prefix
     */
    public static String normalizeURI(final String uri) {
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalArgumentException("URI must not be null or empty");
        }
        log.debug("Normalizing uri {}", uri);
        if (isClassResourceURI(uri)) {
            String normalized = uri.replace(CLASS_PREFIX, "");
            if (normalized.endsWith(CLASS_SUFFIX)) {
                log.warn(
                        "Class resources MUST NOT end with [{}]. Fixing this for you now!",
                        CLASS_SUFFIX);
                normalized = normalized.replace(CLASS_SUFFIX, "");
            }
            log.debug("Normalized uri is [{}]", normalized);
            return normalized.trim();
        }
        if (isClasspathResourceURI(uri)) {
            final String normalized = uri.replace(CLASSPATH_PREFIX, "");
            log.debug("Normalized uri is [{}]", normalized);
            return normalized.trim();
        }
        if (isFileResourceURI(uri)) {
            final String normalized = uri.replace(FILE_PREFIX, "");
            log.debug("Normalized uri is [{}]", normalized);
            return normalized.trim();
        }
        throw new IllegalArgumentException("Unsupported URI [" + uri
                + "] for normalization!");
    }

    /**
     * @param uri
     *            the URI to test
     * @return true if the URI starts with {@value #CLASS_PREFIX}, otherwise
     *         false
     */
    public static boolean isClassResourceURI(final String uri) {
        return (uri != null) && uri.startsWith(CLASS_PREFIX);
    }

    /**
     * @param uri
     *            the URI to test
     * @return true if the URI starts with {@value #CLASSPATH_PREFIX}, otherwise
     *         false
     */
    public static boolean isClasspathResourceURI(final String uri) {
        return (uri != null) && uri.startsWith(CLASSPATH_PREFIX);
    }

    /**
     * @param uri
     *            the URI to test
     * @return true if the URI starts with {@value #FILE_PREFIX}, otherwise
     *         false
     */
    public static boolean isFileResourceURI(final String uri) {
        return (uri != null) && uri.startsWith(FILE_PREFIX);
    }

    /**
     * Loads resource from provided URI as {@link InputStream}.
     *
     * @param uri
     *            the uri of resource to be loaded. Must not be null or empty
     *            string. Must be valid URI.
     * @return {@link InputStream} if resource was successfully loaded or null
     *         otherwise
     */
    public static InputStream loadResourceAsStreamFromURI(final String uri) {
        final Resource res = loadResourceFromURI(uri);
        log.debug("Loaded resource {} from uri [{}]", res, uri);
        if (res != null) {
            return res.getInputStream();
        }
        log.warn("Was not able to load resource [{}]", uri);
        return null;
    }

    private static Resource loadResourceFromURI(final String uri) {
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unable to load resource from null or empty URI");
        }
        log.debug("Trying to load resource from [{}]", uri);
        if (isClasspathResourceURI(uri)) {
            final String normalized = normalizeURI(uri);
            log.debug("Normalized classpath URI is [{}]", normalized);
            return Resources.getClasspathResource(normalized);
        }
        if (isClassResourceURI(uri)) {
            final String normalized = normalizeURI(uri);
            log.debug("Normalized class URI is [{}]", normalized);
            final String validClasspathResource = normalized.replace(".", "/");
            throw new IllegalArgumentException("Unable to load class ["
                    + validClasspathResource + "] as text");
        }
        if (isFileResourceURI(uri)) {
            final String normalized = normalizeURI(uri);
            log.debug("Loading file resource from uri [{}]", normalized);
            return Resources.getFileSystemResource(normalized);
        }
        throw new IllegalArgumentException("Unknown uri type [" + uri
                + "]. Unable to load it!");
    }

    /**
     *
     * @param uri
     *            a String which identifies the resource
     * @return the resource as a String
     */
    public static String loadResourceAsTextFromURI(final String uri) {
        String txt = null;
        final Resource res = loadResourceFromURI(uri);
        if (res != null) {
            txt = res.getAsText();
        }
        if (txt == null) {
            log.warn(
                    "Was not able to load resource {} as text. Returning null value!",
                    uri);
        }
        return txt;
    }

}
