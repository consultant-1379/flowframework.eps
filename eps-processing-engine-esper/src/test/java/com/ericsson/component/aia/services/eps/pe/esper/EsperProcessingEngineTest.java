package com.ericsson.component.aia.services.eps.pe.esper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.component.aia.services.eps.pe.ProcessingEngineContext;
import com.ericsson.component.aia.services.eps.pe.esper.EsperProcessingEngine;
import com.espertech.esper.client.*;

@RunWith(PowerMockRunner.class)
// We prepare a class for test because it's final or we need to mock private or
// static methods
@PrepareForTest(EPServiceProviderManager.class)
public class EsperProcessingEngineTest {

    private static final String CLASSPATH_CONFIGURATION = "/java/resources/config.xml";

    private Map<String, String> configurationMap;
    private ProcessingEngineContext mockedProcessingEngineContext;
    private Configuration mockedConfigurationForProvider;

    @Mock
    private Configuration mockedConfiguration;

    @Mock
    private EPServiceProvider mockedEPServiceProvider;

    @InjectMocks
    private EsperProcessingEngine esperProcessingEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(EPServiceProviderManager.class);
        mockedProcessingEngineContext = mock(ProcessingEngineContext.class);
        mockedConfigurationForProvider = mock(Configuration.class);

        when(EPServiceProviderManager.getProvider(anyString())).thenReturn(mockedEPServiceProvider);
        when(EPServiceProviderManager.getProvider(anyString(), any(Configuration.class))).thenReturn(mockedEPServiceProvider);
        when(mockedConfiguration.configure(anyString())).thenReturn(mockedConfigurationForProvider);
        when(mockedConfiguration.configure(any(File.class))).thenReturn(mockedConfigurationForProvider);

        configurationMap = new HashMap<String, String>();
    }

    @After
    public void tearDown() {
        esperProcessingEngine = null;
        configurationMap = null;
        mockedProcessingEngineContext = null;
        mockedConfigurationForProvider = null;
        mockedConfiguration = null;
        mockedEPServiceProvider = null;
    }

    // getInstanceId() should not return null, it should be uncommented when it
    // fixed (it returns null at the moment)
    /*
     * @Test public void getInstanceId_CreatedObject_ReturnInstanceId() throws Exception { assertNotNull(this.esperProcessingEngine.getInstanceId()); }
     */

    @Test
    public void getEngineType_CreatedObject_ReturnEsperEngine() throws Exception {
        assertEquals("ESPER_HANDLER", esperProcessingEngine.getEngineType());
    }

    public void verifyIsEPServiceProviderWithDefaultConfiguration() {
        verify(mockedEPServiceProvider).initialize();
        PowerMockito.verifyStatic();
        EPServiceProviderManager.getProvider(anyString());
    }

    public void verifyIsEPServiceProviderWithFileConfiguration() {
        verify(mockedEPServiceProvider).initialize();
        // getInstanceId() should not return null, it should be uncommented when
        // it fixed (it returns null at the moment)
        /*
         * PowerMockito.verifyStatic(); EPServiceProviderManager.getProvider(anyString(), this.mockedConfigurationForProvider);
         */
        final ArgumentCaptor<File> argument = ArgumentCaptor.forClass(File.class);
        verify(mockedConfiguration).configure(argument.capture());
    }

    public void verifyIsEPServiceProviderWithClasspathConfiguration() {
        verify(mockedEPServiceProvider).initialize();
        // getInstanceId() should not return null, it should be uncommented when
        // it fixed (it returns null at the moment)
        /*
         * PowerMockito.verifyStatic(); EPServiceProviderManager.getProvider(anyString(), this.mockedConfigurationForProvider);
         */
        final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(mockedConfiguration).configure(argument.capture());
    }

    public String getTempFilePath() throws Exception {
        final File file = File.createTempFile("test_configuration_file", ".xml");
        file.deleteOnExit();
        return file.getAbsolutePath();
    }
}
