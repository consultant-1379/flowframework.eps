/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.coordination;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;

/**
 * The Class CoordinationUtil retrieves the epsName reading several hostname files depending by the platform.
 */
public class CoordinationUtil {

    private static final String DELIMITER = "@";
    private static final String UNKNOWN = "UNKNOWN Process Id";
    private static final String HOSTNAME = "hostname";
    private static final String HOSTNAME_FILE = "/etc/hostname";
    private static final String ALTERNATIVE_HOSTNAME_FILE = "/etc/sysconfig/network";
    private static final String HOSTNAME_CAPITAL = HOSTNAME.toUpperCase();
    private static final String EQUAL_SIGN = "=";
    private static final String HOSTNAME_CAPITAL_PLUS_EQUAL = HOSTNAME_CAPITAL + EQUAL_SIGN;

    /**
     * Gets the qualified eps name.
     *
     * @param epsName
     *            the eps name
     * @return the qualified eps name
     */
    public static String getQualifiedEpsName(final String epsName) {
        String hostName = System.getenv(HOSTNAME_CAPITAL);
        if (hostName != null) {
            return getQualifiedName(epsName, hostName);
        }
        hostName = readHostnameFromRedHat();
        if (!isEmpty(hostName)) {
            return getQualifiedName(epsName, hostName);
        }
        hostName = readHostnameFromUbuntu();
        if (!isEmpty(hostName)) {
            return getQualifiedName(epsName, hostName);
        }
        return getQualifiedName(epsName, readHostNameFromProcess());
    }

    /**
     * Checks if is empty.
     *
     * @param hostName
     *            the host name
     * @return true, if is empty
     */
    private static boolean isEmpty(final String hostName) {
        return "".equalsIgnoreCase(hostName);
    }

    /**
     * Gets the qualified name.
     *
     * @param epsName
     *            the eps name
     * @param hostName
     *            the host name
     * @return the qualified name
     */
    private static String getQualifiedName(final String epsName, final String hostName) {
        return hostName + DELIMITER + getProcessId() + DELIMITER + epsName;
    }

    /**
     * Read hostname from red hat.
     *
     * @return the string
     */
    private static String readHostnameFromRedHat() {
        final String emptyStr = "";
        final Path file = Paths.get(ALTERNATIVE_HOSTNAME_FILE);
        if (!Files.exists(file)) {
            return emptyStr;
        }
        try {
            final List<String> lines = Files.readAllLines(file, Charset.defaultCharset());
            if (lines.size() < 1) {
                return emptyStr;
            }
            for (final String line : lines) {
                if (isHostName(line)) {
                    return line.split(EQUAL_SIGN)[1].trim();
                }
            }
        } catch (final IOException e) {
            return emptyStr;
        }
        return emptyStr;
    }

    /**
     * Checks if is host name.
     *
     * @param hostName
     *            the host name
     * @return true, if is host name
     */
    private static boolean isHostName(final String hostName) {
        if ((hostName == null) || !hostName.startsWith(HOSTNAME_CAPITAL_PLUS_EQUAL)) {
            return false;
        }
        return true;
    }

    /**
     * Reads first line of {@code /etc/hostname} which contains host name.
     *
     * @return host name from {@code /etc/hostname}
     */
    private static String readHostnameFromUbuntu() {
        final Path file = Paths.get(HOSTNAME_FILE);
        if (!Files.exists(file)) {
            return "";
        }
        try {
            final List<String> lines = Files.readAllLines(file, Charset.defaultCharset());
            return !lines.isEmpty() && (lines.get(0) != null) ? lines.get(0).trim() : null;
        } catch (final IOException e) {
            return "";
        }
    }

    /**
     * Runs process to get a hostname.
     *
     * @return the string
     */
    private static String readHostNameFromProcess() {
        String hostName = "";
        try {
            final Process process = Runtime.getRuntime().exec(HOSTNAME);
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            final byte[] contents = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(contents)) != -1) {
                hostName = new String(contents, 0, bytesRead);
            }
        } catch (final Exception exception) {
            return "";
        }
        return hostName.trim();
    }

    /**
     * Gets process id from managed bean for the runtime system of the Java virtual machine.
     *
     * @return the process id
     */
    private static String getProcessId() {
        final String fullProcessName = ManagementFactory.getRuntimeMXBean().getName();
        if ((fullProcessName != null) && fullProcessName.contains(DELIMITER)) {
            return fullProcessName.split(DELIMITER)[0];
        }
        return UNKNOWN;
    }

}
