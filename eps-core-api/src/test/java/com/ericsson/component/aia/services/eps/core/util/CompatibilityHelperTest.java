package com.ericsson.component.aia.services.eps.core.util;

import junit.framework.Assert;

import org.junit.Test;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.component.aia.services.eps.adapter.InputAdapter;
import com.ericsson.component.aia.services.eps.adapter.OutputAdapter;
import com.ericsson.component.aia.services.eps.core.util.CompatibilityHelper;


/*
 * import com.ericsson.oss.itpf.common.io.Adapter;
import com.ericsson.oss.services.eps.adapter.InputAdapter;
import com.ericsson.oss.services.eps.adapter.OutputAdapter;
 * 
 * 
 * */

public class CompatibilityHelperTest {

    private static final String INPUT_URL = "epsApiInput:";
    private static final String OUTPUT_URL = "epsApiOutput:";

    @Test
    public void ff2EpsInputAdapterTest() {

        final InputAdapter inputAdapter = new EpsTestInputAdapter();
        final com.ericsson.component.aia.itpf.common.io.InputAdapter ffInputAdapter = (com.ericsson.component.aia.itpf.common.io.InputAdapter) CompatibilityHelper
                .newInstance(inputAdapter);

        Assert.assertTrue(ffInputAdapter.understandsURI(INPUT_URL));
        Assert.assertFalse(((EpsTestInputAdapter) inputAdapter).isInitCalled());
        Assert.assertFalse(((EpsTestInputAdapter) inputAdapter).isOnEventCalled());
        Assert.assertFalse(((EpsTestInputAdapter) inputAdapter).isDestroyCalled());

        ffInputAdapter.init(null);
        ffInputAdapter.onEvent(null);
        ffInputAdapter.destroy();

        Assert.assertTrue(((EpsTestInputAdapter) inputAdapter).isInitCalled());
        Assert.assertTrue(((EpsTestInputAdapter) inputAdapter).isOnEventCalled());
        Assert.assertTrue(((EpsTestInputAdapter) inputAdapter).isDestroyCalled());
    }

    @Test
    public void ff2EpsOnputAdapterTest() {

        final OutputAdapter outputAdapter = new EpsTestOutputAdapter();
        final com.ericsson.component.aia.itpf.common.io.OutputAdapter ffOutputAdapter = (com.ericsson.component.aia.itpf.common.io.OutputAdapter) CompatibilityHelper
                .newInstance(outputAdapter);

        Assert.assertTrue(ffOutputAdapter.understandsURI(OUTPUT_URL));
        Assert.assertFalse(((EpsTestOutputAdapter) outputAdapter).isInitCalled());
        Assert.assertFalse(((EpsTestOutputAdapter) outputAdapter).isOnEventCalled());
        Assert.assertFalse(((EpsTestOutputAdapter) outputAdapter).isDestroyCalled());

        ffOutputAdapter.init(null);
        ffOutputAdapter.onEvent(null);
        ffOutputAdapter.destroy();

        Assert.assertTrue(((EpsTestOutputAdapter) outputAdapter).isInitCalled());
        Assert.assertTrue(((EpsTestOutputAdapter) outputAdapter).isOnEventCalled());
        Assert.assertTrue(((EpsTestOutputAdapter) outputAdapter).isDestroyCalled());
    }

}
