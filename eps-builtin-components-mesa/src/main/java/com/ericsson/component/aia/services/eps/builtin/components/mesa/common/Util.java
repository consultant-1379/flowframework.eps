package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class
 */
public abstract class Util {

    private static final Logger log = LoggerFactory.getLogger("com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Util");

    private Util() {
    }

    /**
     * Close.
     *
     * @param closeable
     *            the closeable
     */
    public static void close(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                log.trace("A closeable has failed to close {}", e);
            }
        }
    }

    /**
     * Assert true.
     *
     * @param expression
     *            the expression
     */
    public static void assertTrue(final boolean expression) {
        if (!expression) {
            throw new MesaException("Assert failed");
        }
    }

    /**
     * Read text file fully.
     *
     * @param fileName
     *            the file name
     * @return the string
     */
    public static String readTextFileFully(final String fileName) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(fileName));
        } catch (final FileNotFoundException e) {
            throw new MesaException("Failed to find the file: " + fileName + " " + e);
        }
        return readTextFromStream(fis);

    }

    /**
     * Read text fully from classpath.
     *
     * @param resourceName
     *            the resource name
     * @return the string
     */
    public static String readTextFullyFromClasspath(final String resourceName) {
        return readTextFullyFromClasspathInternal(resourceName);
    }

    private static String readTextFullyFromClasspathInternal(final String resourceName) {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        return readTextFromStream(inputStream);
    }

    private static String readTextFromStream(final InputStream inputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            final StringBuilder text = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            return text.toString();
        } catch (final IOException e) {
            throw new MesaException("Failed to read in text from a stream" + e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    throw new MesaException("Failed to close a stream" + e);
                }
            }
        }
    }
}
