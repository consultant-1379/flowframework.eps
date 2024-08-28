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

/**
 * @since 1.1.108
 */
@XmlRootElement
public class ShortArrayWrapper implements Serializable {
	
	private static final long serialVersionUID = 1887674480092073354L;
	
	private Short[] shortArray;
	
	/**
	 * Empty constructor, the Short Array is not initialized.
	 */
	public ShortArrayWrapper() {
	}
	
	/**
	 * Initializes the ShortArrayWrapper with specified shortArray.
	 * 
	 * @param shortArray
	 *            The array of shorts to initialize the new ShortArrayWrapper.
	 * 
	 */
	public ShortArrayWrapper(final Short[] shortArray) {
		this.shortArray = shortArray;
	}
	
	public Short[] getShortArray() {
		return shortArray;
	}
	
	public void setShortArray(final Short[] shortArray) {
		this.shortArray = shortArray;
	}
}
