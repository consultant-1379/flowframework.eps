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
package com.ericsson.component.aia.services.eps.io.adapter.hornetq.connection;

/**
 * The Class HornetQConnectionInfo contains info about HornetQ Connection: host, port and windowSize in Mb.
 */
public class HornetQConnectionInfo {

    private final String host;

    private final String port;

    private final int windowSizeMb;

    /**
     * Instantiates a new hornetQ connection info.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param windowSizeMb
     *            the window size mb
     */
    public HornetQConnectionInfo(final String host, final String port, final int windowSizeMb) {
        super();
        this.host = host;
        this.port = port;
        this.windowSizeMb = windowSizeMb;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "HornetQConnectionInfo [host=" + host + ", port=" + port + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((host == null) ? 0 : host.hashCode());
        result = (prime * result) + ((port == null) ? 0 : port.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return equalsHornetQConnectionInfo((HornetQConnectionInfo) obj);
    }

    private boolean equalsHornetQConnectionInfo(final HornetQConnectionInfo other) {
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (port == null) {
            if (other.port != null) {
                return false;
            }
        } else if (!port.equals(other.port)) {
            return false;
        }
        return true;
    }

    /**
     * @return the windowSizeMb
     */
    public int getWindowSizeMb() {
        return windowSizeMb;
    }

}
