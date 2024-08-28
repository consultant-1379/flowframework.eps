package com.ericsson.component.aia.services.eps.core.parsing;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.parsing.EpsModeledFlowParser;
import com.ericsson.component.aia.services.eps.core.parsing.EpsModuleParser;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.FlowDefinition;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.direct.DirectModelAccess;
import com.ericsson.oss.itpf.modeling.schema.util.DtdModelHandlingUtil;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.itpf.sdk.resources.Resources;

@RunWith(MockitoJUnitRunner.class)
public class EpsModeledModServiceParserTest {

    public class TestEpsModeledFlowParser extends EpsModeledFlowParser {

        private ModelService mockModelService;

        @Override
        protected ModelService getModelService() {
            return mockModelService;
        }

    }

    @InjectMocks
    final private EpsModuleParser<ModelInfo> parser = new TestEpsModeledFlowParser();

    @Mock
    private ModelService mockModelService;

    @Mock
    private DirectModelAccess mockDma;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        when(mockModelService.getDirectAccess()).thenReturn(mockDma);

    }

    private FlowDefinition mockModelService(final String fname) {
        final Unmarshaller unmarshaller = DtdModelHandlingUtil.getUnmarshaller(SchemaConstants.FBP_FLOW);
        final InputStream inputStream = Resources.getClasspathResource(fname).getInputStream();
        Assert.assertNotNull(inputStream);
        try {
            final Object root = unmarshaller.unmarshal(new StreamSource(inputStream));
            return (FlowDefinition) root;
        } catch (final JAXBException jaxbexc) {
            Assert.fail("Invalid module - unable to parse it! Details: " + jaxbexc.getMessage());
        }
        return null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null() {
        parser.parseModule(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_malformed_urn() {
        final ModelInfo urn = ModelInfo.fromUrn(SchemaConstants.FBP_FLOW + "/eps" + "/flow_01" + "/1.2.3");
        when(mockDma.getAsJavaTree(urn, FlowDefinition.class)).thenReturn(mockModelService("/flow_01.xml"));
        final EpsModule module = parser.parseModule(urn);
        Assert.assertNotNull(module);
        Assert.assertEquals("com.ericsson.test", module.getNamespace());
        Assert.assertEquals("test_name", module.getName());
        Assert.assertEquals("1.1.0", module.getVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_model_not_found() {
        final ModelInfo urn = ModelInfo.fromUrn(SchemaConstants.FBP_FLOW + ".eps." + "not_xml." + "*");
        when(mockDma.getAsJavaTree(any(ModelInfo.class), eq(FlowDefinition.class))).thenReturn(null);
        parser.parseModule(urn);
    }

    @Test
    public void test_flow_01() {
        final ModelInfo urn = ModelInfo.fromUrn("/" + SchemaConstants.FBP_FLOW + "/eps" + "/flow_01" + "/*");
        when(mockDma.getAsJavaTree(any(ModelInfo.class), eq(FlowDefinition.class))).thenReturn(mockModelService("/flow_01.xml"));
        final EpsModule module = parser.parseModule(urn);
        Assert.assertNotNull(module);
        Assert.assertEquals("com.ericsson.test", module.getNamespace());
        Assert.assertEquals("test_name", module.getName());
        Assert.assertEquals("1.1.0", module.getVersion());
    }

}
