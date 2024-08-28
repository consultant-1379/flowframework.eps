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
package com.ericsson.component.aia.services.eps.core.coordination.observer;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.component.aia.services.eps.coordination.*;
import com.ericsson.component.aia.services.eps.core.coordination.observer.EpsConfigurationChangeObserver;
import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.NodeType;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EpsConfigurationChangeObserver.class,XMLDeSerializer.class,CoordinationStatus.class})
public class EpsConfigurationChangeObserverTest {
    private static final String INPUT_IP = "inputIP";
    private static final String INPUT_PORT = "inputPort";
    private static final String USER_ID = "userId";
    private static final String FILTER_ID = "FilterId";
    private static final String GROUP_ID = "GroupId";
    private static final String STREAM_LOAD_MONITOR = "StreamLoadMonitor";
    private static final String MONITOR_PERIOD = "MonitorPeriod";
    private static final String COMPONENT_ID = "1234";
    private static final String FLOW_ID = "12345";
    private static final String PATH = "/path";
    private final Map<String, Object> data = new HashMap<>();
    @Mock
    Controllable mockControllable;
    
    final ControllableImpl controllableImpl = new ControllableImpl();
    ControlEvent controlEvent = null;
    
    @Mock 
    private Application mockApplication;
    
    @Mock
    Serializable serializable;

    final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
    
    private final class ControllableImpl implements Controllable{

        private ControlEvent controlEvent;
        @Override
        public void react(final ControlEvent controlEvent) {
            this.controlEvent = controlEvent; 
        }
        public ControlEvent getControlEvent(){
            return controlEvent;
        }
        public void setControlEvent(final ControlEvent controlEvent){
            this.controlEvent = controlEvent;
        }
    }
    @Before
    public void setUpData() {
        MockitoAnnotations.initMocks(this);
        data.put(INPUT_IP, "1.2.3.4");
        data.put(INPUT_PORT, "12345");
        data.put(GROUP_ID, "123");
        data.put(FILTER_ID,"1");
        data.put(USER_ID, "1");
        data.put(STREAM_LOAD_MONITOR, "false");
        data.put(MONITOR_PERIOD, "10000");
        epsAdaptiveConfiguration.setConfiguration(data);
    }
    @Test
    public void testOnCreate(){
        controllableImpl.setControlEvent(null);
        final String xmlData = "<?xml>";
        MockitoAnnotations.initMocks(this);
        mockStatic(XMLDeSerializer.class);
        Mockito.when(XMLDeSerializer.unmarshal(xmlData, EpsAdaptiveConfiguration.class)).thenReturn(epsAdaptiveConfiguration);

        final EpsConfigurationChangeObserver epsConfigurationChangeObserver = new EpsConfigurationChangeObserver(FLOW_ID,COMPONENT_ID,controllableImpl,mockApplication);
        try{
            epsConfigurationChangeObserver.onCreate(NodeType.CONFIGURE, PATH, xmlData);
            fail("Should have thrown a null pointer exception");
        }catch(NullPointerException nullPointerException){
        }
        try {
            spy(CoordinationStatus.class);
            PowerMockito.doNothing().when(CoordinationStatus.class,"setActive",mockApplication,COMPONENT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            epsConfigurationChangeObserver.onCreate(NodeType.CONFIGURE, PATH, xmlData);
        }catch(NullPointerException nullPointerException){
            fail("Should not have thrown a null pointer exception");
        }
        assertEquals("Configuration data was not sent correctly",data, controllableImpl.getControlEvent().getData());
    }
    @Test
    public void testOnUpdate(){
        controllableImpl.setControlEvent(null);
        final String xmlData = "<?xml>";
        MockitoAnnotations.initMocks(this);
        mockStatic(XMLDeSerializer.class);
        Mockito.when(XMLDeSerializer.unmarshal(xmlData, EpsAdaptiveConfiguration.class)).thenReturn(epsAdaptiveConfiguration);

        final EpsConfigurationChangeObserver handlerConfigurationChangeObserver = new EpsConfigurationChangeObserver(FLOW_ID,COMPONENT_ID,controllableImpl,mockApplication);
        try{
            handlerConfigurationChangeObserver.onUpdate(NodeType.CONFIGURE, PATH, xmlData);
            fail("Should have thrown a null pointer exception");
        }catch(NullPointerException nullPointerException){
        }
        try {
            spy(CoordinationStatus.class);
            PowerMockito.doNothing().when(CoordinationStatus.class,"setActive",mockApplication,COMPONENT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            handlerConfigurationChangeObserver.onUpdate(NodeType.CONFIGURE, PATH, xmlData);
        }catch(NullPointerException nullPointerException){
            fail("Should not have thrown a null pointer exception");
        }
        assertEquals("Configuration data was not sent correctly",data, controllableImpl.getControlEvent().getData());
    }
    @Test
    public void testOnCreateWithNonString(){
        MockitoAnnotations.initMocks(this);
        final EpsConfigurationChangeObserver epsConfigurationChangeObserver = new EpsConfigurationChangeObserver(FLOW_ID,COMPONENT_ID,mockControllable,mockApplication);
        try{
            epsConfigurationChangeObserver.onCreate(NodeType.CONFIGURE, PATH, serializable);
        }catch(NullPointerException nullPointerException){
            fail("Should not have thrown a null pointer exception");
        }
        Mockito.verifyZeroInteractions(mockControllable);
    }
    @Test
    public void testOnRemove(){
        MockitoAnnotations.initMocks(this);
        final EpsConfigurationChangeObserver epsConfigurationChangeObserver = new EpsConfigurationChangeObserver(FLOW_ID,COMPONENT_ID,mockControllable,mockApplication);
        epsConfigurationChangeObserver.onRemove(NodeType.CONFIGURE, PATH);
        Mockito.verifyZeroInteractions(mockControllable);
    }
}
