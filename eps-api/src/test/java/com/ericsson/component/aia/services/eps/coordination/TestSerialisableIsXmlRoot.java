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
package com.ericsson.component.aia.services.eps.coordination;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestSerialisableIsXmlRoot implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stringVal = "";
    private Integer intVal = 0;
    private Boolean boolVal = false;

    public TestSerialisableIsXmlRoot() {
        super();
    }

    public TestSerialisableIsXmlRoot(final String stringVal, final Integer intVal, final Boolean boolVal) {
        super();
        this.stringVal = stringVal;
        this.intVal = intVal;
        this.boolVal = boolVal;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(final String stringVal) {
        this.stringVal = stringVal;
    }

    public Integer getIntVal() {
        return intVal;
    }

    public void setIntVal(final Integer intVal) {
        this.intVal = intVal;
    }

    public Boolean getBoolVal() {
        return boolVal;
    }

    public void setBoolVal(final Boolean boolVal) {
        this.boolVal = boolVal;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TestSerialisableIsXmlRoot)) {
            return false;
        }

        final TestSerialisableIsXmlRoot convertObj = (TestSerialisableIsXmlRoot) obj;
        if (this.intVal.intValue() != convertObj.intVal.intValue()) {
            return false;
        }
        if (!this.stringVal.trim().equals(convertObj.stringVal.trim())) {
            return false;
        }
        if (this.boolVal.booleanValue() != convertObj.boolVal.booleanValue()) {
            return false;
        }
        return true;
    }
}
