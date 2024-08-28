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
package com.ericsson.component.aia.services.eps.pe.esper;

import java.util.Date;

public class Ticker {
    private final String symbol;
    private final Double price;
    private final Date timeStamp;

    public Ticker(String symbol, double price, long timeStamp) {
        this.symbol = symbol;
        this.price = price;
        this.timeStamp = new Date(timeStamp);
    }

    public double getPrice() {
        return price;
    }

    public String getSymbol() {
        return symbol;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Price: " + price.toString() + " time: " + timeStamp.toString();
    }
}