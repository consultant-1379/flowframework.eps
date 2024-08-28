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
package com.ericsson.component.aia.services.eps.pe;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.services.eps.pe.ProcessingEngineContext;
import com.ericsson.component.aia.itpf.common.config.Configuration;

public class ProcessingEngineContextTest {
    private ProcessingEngineContext context;
    private Configuration mockConfiguration;

    @Before
    public void setup() {
        mockConfiguration = mock(Configuration.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_constructor_null() {
        new ProcessingEngineContext(null);
    }

    @Test
    public void test_constructor_notNull() {
        context = new ProcessingEngineContext(mockConfiguration);
        final Configuration configuration = Whitebox.getInternalState(context, "configuration");
        assertNotNull(configuration);
    }

    @Test
    public void test_getConfiguration() {
        context = new ProcessingEngineContext(mockConfiguration);
        assertNotNull(context.getConfiguration());
    }
}
