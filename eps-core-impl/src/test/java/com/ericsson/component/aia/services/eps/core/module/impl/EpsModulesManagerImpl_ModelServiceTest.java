package com.ericsson.component.aia.services.eps.core.module.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.Counter;
import com.ericsson.component.aia.services.eps.core.module.impl.EpsModulesManagerImpl;
import com.ericsson.component.aia.services.eps.core.parsing.EpsModeledFlowParser;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.FlowDefinition;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.direct.DirectModelAccess;
import com.ericsson.oss.itpf.modeling.schema.util.DtdModelHandlingUtil;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.itpf.sdk.resources.Resources;

@RunWith(MockitoJUnitRunner.class)
public class EpsModulesManagerImpl_ModelServiceTest {

    public static class TestEpsModeledFlowParser extends EpsModeledFlowParser {

        private ModelService mockModelService;

        @Override
        protected ModelService getModelService() {
            return mockModelService;
        }

    }

    public class EpsModulesManagerImplStub extends EpsModulesManagerImpl {

        public EpsModulesManagerImplStub() {
            super(mockEpsStatisticsRegister);
        }

        @Override
        protected EpsModeledFlowParser getModeledFlowParser() {
            return EpsModulesManagerImpl_ModelServiceTest.stubParser;
        }
    }

    @InjectMocks
    private static EpsModeledFlowParser stubParser = new TestEpsModeledFlowParser();

    @Mock
    private ModelService mockModelService;

    @Mock
    private static EpsStatisticsRegister mockEpsStatisticsRegister;


    private final Counter mockCounter = mock(Counter.class);

    @Mock
    private DirectModelAccess mockDma;

    private EpsModulesManagerImpl managerStub;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        when(mockModelService.getDirectAccess()).thenReturn(mockDma);
        when(mockEpsStatisticsRegister.isStatisticsOn()).thenReturn(true);
        when(mockEpsStatisticsRegister.createCounter(anyString())).thenReturn(mockCounter);
        managerStub = new EpsModulesManagerImplStub();
    }

    private FlowDefinition mockModelService(final String fname) {
        final Unmarshaller unmarshaller = DtdModelHandlingUtil.getUnmarshaller(SchemaConstants.FBP_FLOW);
        final InputStream inputStream = Resources.getClasspathResource(fname).getInputStream();
        Assert.assertNotNull("mockModelService - NULL InputStream", inputStream);
        try {
            final Object root = unmarshaller.unmarshal(new StreamSource(inputStream));
            Assert.assertNotNull("mockModelService - NULL FlowDefinition", root);
            //Assert.fail("mockModelService - test output : " + root.toString());
            return (FlowDefinition) root;
        } catch (final JAXBException jaxbexc) {
            Assert.fail("mockModelService - unable to parse flow! Details: " + jaxbexc.getMessage());
        } catch (final Exception exc) {
            Assert.fail("mockModelService - unexpected exception parsing flow! Details: " + exc.getMessage());
        }
        return null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_modelservice_deploy_null_module() {
        managerStub.deployModuleFromModel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_modelservice_deploy_invalid_module() {
        final String urn = "/" + SchemaConstants.FBP_FLOW + "/eps" + "/not_xml" + "/1.2.3";
        when(mockDma.getAsJavaTree(ModelInfo.fromUrn(urn), FlowDefinition.class)).thenThrow(
                new IllegalArgumentException("Invalid module - unable to parse it! Details: Mocked"));
        Assert.assertNotNull(urn);
        managerStub.deployModuleFromModel(urn);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_modelservice_deploy_invalid_module2() {
        final String urn = "//com.ericsson.test" + "/test_name" + "/1.1.0";
        when(mockDma.getAsJavaTree(ModelInfo.fromUrn(urn), FlowDefinition.class)).thenThrow(
                new IllegalArgumentException("Invalid module - unable to parse it! Details: Mocked"));
        Assert.assertNotNull(urn);
        managerStub.deployModuleFromModel(urn);
    }

    @Test
    public void test_modelservice_deploy_undeploy_valid_module() {
        final String urn = "/" + SchemaConstants.FBP_FLOW + "/com.ericsson.test" + "/test_name" + "/1.1.0";
        when(mockDma.getAsJavaTree(ModelInfo.fromUrn(urn), FlowDefinition.class)).thenReturn(mockModelService("/flow_01.xml"));
        Assert.assertNotNull(urn);
        Assert.assertEquals(0, managerStub.getDeployedModulesCount());
        final String moduleIdentifier = managerStub.deployModuleFromModel(urn);
        Assert.assertNotNull(moduleIdentifier);
        Assert.assertEquals(1, managerStub.getDeployedModulesCount());
        Assert.assertEquals(1, managerStub.sendControlEventToAllModules(new ControlEvent(10)));
        Assert.assertTrue(managerStub.sendControlEventToModuleById(moduleIdentifier, new ControlEvent(22)));
        final boolean undeployed = managerStub.undeployModule(moduleIdentifier);
        Assert.assertTrue(undeployed);
        Assert.assertEquals(0, managerStub.getDeployedModulesCount());
        Assert.assertEquals(0, managerStub.sendControlEventToAllModules(new ControlEvent(10)));
        Assert.assertFalse(managerStub.sendControlEventToModuleById(moduleIdentifier, new ControlEvent(22)));
    }

}
