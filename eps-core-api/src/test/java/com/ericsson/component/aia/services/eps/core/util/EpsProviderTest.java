package com.ericsson.component.aia.services.eps.core.util;

import org.junit.Assert;
import org.junit.Test;

import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

public class EpsProviderTest {

    private final EpsProvider provider = EpsProvider.getInstance();

    @Test
    public void test_no_input() {
        final InputAdapter inputAdapter = provider.loadInputAdapter("does not exist", "id");
        Assert.assertNull(inputAdapter);
        final OutputAdapter outputAdapter = provider.loadOutputAdapter("does not exist", "id");
        Assert.assertNull(outputAdapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_instance_id() {
        provider.loadInputAdapter("does not exist", null);
        provider.loadOutputAdapter("does not exist", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_uri() {
        provider.loadInputAdapter(null, "ab");
        provider.loadOutputAdapter(null, "ab");
    }

    @Test
    public void test_not_same() {
        final InputAdapter ia1 = provider.loadInputAdapter("test:/abc1", "inst1");
        final InputAdapter ia2 = provider.loadInputAdapter("test:/abc2", "inst2");
        Assert.assertNotNull(ia1);
        Assert.assertNotNull(ia2);
        Assert.assertFalse(ia1 == ia2);

        final OutputAdapter oa1 = provider.loadOutputAdapter("test-out:/abc1", "inst1");
        final OutputAdapter oa2 = provider.loadOutputAdapter("test-out:/abc2", "inst2");
        Assert.assertNotNull(oa1);
        Assert.assertNotNull(oa2);
        Assert.assertFalse(oa1 == oa2);
    }
    
    @Test
    public void test_not_same_legacy() {
        final InputAdapter ia1 = provider.loadInputAdapter("epsApiInput:/abc1", "epsnst1");
        final InputAdapter ia2 = provider.loadInputAdapter("epsApiInput:/abc2", "epsinst2");
        Assert.assertNotNull(ia1);
        Assert.assertNotNull(ia2);
        Assert.assertFalse(ia1 == ia2);

        final OutputAdapter oa1 = provider.loadOutputAdapter("epsApiOutput:/abc1", "epsinst1");
        final OutputAdapter oa2 = provider.loadOutputAdapter("epsApiOutput:/abc2", "epsinst2");
        Assert.assertNotNull(oa1);
        Assert.assertNotNull(oa2);
        Assert.assertFalse(oa1 == oa2);
    }

    @Test
    public void test_same() {
        final InputAdapter ia1 = provider.loadInputAdapter("test:/abc1", "inst1");
        final InputAdapter ia2 = provider.loadInputAdapter("test:/abc2", "inst1");
        Assert.assertNotNull(ia1);
        Assert.assertNotNull(ia2);
        Assert.assertTrue(ia1 == ia2);

        final OutputAdapter oa1 = provider.loadOutputAdapter("test-out:/abc1", "inst1");
        final OutputAdapter oa2 = provider.loadOutputAdapter("test-out:/abc2", "inst1");
        Assert.assertNotNull(oa1);
        Assert.assertNotNull(oa2);
        Assert.assertTrue(oa1 == oa2);
    }
    
    @Test
    public void test_same_legacy() {
        final InputAdapter ia1 = provider.loadInputAdapter("epsApiInput:/abc1", "epsinst1");
        final InputAdapter ia2 = provider.loadInputAdapter("epsApiInput:/abc2", "epsinst1");
        Assert.assertNotNull(ia1);
        Assert.assertNotNull(ia2);
        Assert.assertTrue(ia1 == ia2);

        final OutputAdapter oa1 = provider.loadOutputAdapter("epsApiOutput:/abc1", "epsinst1");
        final OutputAdapter oa2 = provider.loadOutputAdapter("epsApiOutput:/abc2", "epsinst1");
        Assert.assertNotNull(oa1);
        Assert.assertNotNull(oa2);
        Assert.assertTrue(oa1 == oa2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_input_adapter_same_name_different_class() {
        provider.loadInputAdapter("test:", "inst1");
        provider.loadInputAdapter("hzTest:", "inst1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_output_adapter_same_name_different_class() {
        provider.loadOutputAdapter("test-out:", "inst1");
        provider.loadOutputAdapter("hzTest:", "inst1");
    }

}
