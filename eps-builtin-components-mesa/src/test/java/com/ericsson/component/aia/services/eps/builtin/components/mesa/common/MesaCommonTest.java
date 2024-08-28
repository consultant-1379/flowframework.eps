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
package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Name;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Type;

public class MesaCommonTest {

    @Test(expected = IllegalArgumentException.class)
    public void typeTest() {
        Type.parse("nothing");
    }

    @Test
    public void nameTest() {
        final Name nameTest = new Name("testName", "somewhere.com", "1.0");
        final Name nameTest2 = new Name("testName2", "somewhere.com", "1.0");
        final Name nameTest3 = new Name(null, "somewhere.com", "1.0");
        final Name nameTest4 = new Name("testName3", null, "1.0");
        final Name nameTest5 = new Name("testName4", "somewhere.com", null);
        nameTest.hashCode();
        assertFalse(nameTest2.equals(nameTest));
        assertNotNull(nameTest2);
        assertFalse(nameTest2.equals(new Integer(2)));
        assertFalse(nameTest3.equals(nameTest));
        assertFalse(nameTest4.equals(nameTest));
        assertFalse(nameTest5.equals(nameTest));
        assertTrue(nameTest.equals(nameTest));
    }

}
