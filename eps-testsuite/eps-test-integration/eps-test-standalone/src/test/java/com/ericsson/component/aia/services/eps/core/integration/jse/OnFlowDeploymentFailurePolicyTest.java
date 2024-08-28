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
package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.security.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.junit.*;
import org.junit.runners.Parameterized.Parameters;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.FlowDeploymentFailurePolicy;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;

@Ignore("This test should be run manually only")
public class OnFlowDeploymentFailurePolicyTest {

    private static final String FLOWS_INVALID_INPUT_OUTPUT_FLOW_XML = "/flows/invalid_input_output_flow.xml";
    private static final String FLOWS_OK_MODULE_INPUTSTREAM_FLOW_XML = "/flows/flow_with_single_destroyable_component.xml";

    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    public final String nameSpace;

    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] { { "" }, { "com.standalone.script" } });
    }

    public OnFlowDeploymentFailurePolicyTest(final String nameSpace) {
        this.nameSpace = nameSpace;
    }

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    @Test
    public void test_deploy_invalid_module() throws Exception {
        try {
            epsTestUtil.deployModule(FLOWS_OK_MODULE_INPUTSTREAM_FLOW_XML);
            Assert.assertEquals(1, epsTestUtil.getEpsInstance().getModuleManager().getDeployedModulesCount());
            TestUndeploymentComponent.clear();
            Assert.assertFalse(TestUndeploymentComponent.isDestroyed());
        } catch (final Exception exc) {
            Assert.fail();
        }
        System.setProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY, FlowDeploymentFailurePolicy.CONTINUE.toString());
        Assert.assertFalse(TestPassThroughInvalidInitEventHandler.isDestroyInvoked);
        try {
            epsTestUtil.deployModule(FLOWS_INVALID_INPUT_OUTPUT_FLOW_XML);
            Assert.fail();
        } catch (final Exception exc) {
            Assert.assertTrue(true);
            Assert.assertTrue(TestPassThroughInvalidInitEventHandler.isDestroyInvoked);
        }
        TestPassThroughInvalidInitEventHandler.clear();
        Assert.assertFalse(TestUndeploymentComponent.isDestroyed());
        final SecurityManager defaultSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new NoExitSecurityManager());
        System.setProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY, FlowDeploymentFailurePolicy.STOP_JVM.toString());
        TestUndeploymentComponent.clear();
        Assert.assertFalse(TestUndeploymentComponent.isDestroyed());
        try {
            epsTestUtil.deployModule(FLOWS_INVALID_INPUT_OUTPUT_FLOW_XML);
            Assert.fail();
        } catch (final ExitException ee) {
            Assert.assertTrue(true);
            Assert.assertTrue(TestUndeploymentComponent.isDestroyed());
            Assert.assertTrue(TestPassThroughInvalidInitEventHandler.isDestroyInvoked);
        } catch (final Exception exc) {
            Assert.fail();
        }
        System.setProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY, FlowDeploymentFailurePolicy.CONTINUE.toString());
        System.setSecurityManager(defaultSecurityManager);
    }

    protected static class ExitException extends SecurityException {
        public final int status;
        private static final long serialVersionUID = 1L;

        public ExitException(final int status) {
            super("There is no escape!");
            this.status = status;
        }
    }

    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(final Permission perm) {
            // allow anything.
        }

        @Override
        public void checkPermission(final Permission perm, final Object context) {
            // allow anything.
        }

        @Override
        public void checkExit(final int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

}
