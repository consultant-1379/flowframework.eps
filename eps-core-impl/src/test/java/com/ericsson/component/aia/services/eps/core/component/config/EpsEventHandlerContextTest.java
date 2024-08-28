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
package com.ericsson.component.aia.services.eps.core.component.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventHandlerContext;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class EpsEventHandlerContextTest {

    private static final int EVENT_INPUT_HANDLERS_SIZE = 3;

    private EpsModule mockedModule;
    private Configuration mockedConfiguration;
    private EventInputHandler[] eventInputHandlers;
    private EpsEventHandlerContext epsComponentContext;

    // Mock EventInputHandlers based on the EVENT_INPUT_HANDLERS_SIZE
    private EventInputHandler[] mockEventInputHandlers() {
        final EventInputHandler[] handlers = new EventInputHandler[EVENT_INPUT_HANDLERS_SIZE];
        for (int i = 0; i < EVENT_INPUT_HANDLERS_SIZE; i++) {
            handlers[i] = mock(EventInputHandler.class);
        }
        return handlers;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        mockedConfiguration = mock(Configuration.class);
        eventInputHandlers = this.mockEventInputHandlers();
        mockedModule = mock(EpsModule.class);
        epsComponentContext = new EpsEventHandlerContext(mockedConfiguration, mockedModule, "componentId1");
    }

    @Test
    public void epsComponentContext_NullConfiguration_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Configuration must not be null");
        new EpsEventHandlerContext(null, mockedModule, "componentId2");
    }

    @Test
    public void epsComponentContext_NullModule_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new EpsEventHandlerContext(mockedConfiguration, null, "componentId3");
    }

    @Test
    public void addEventSubscriber_NullSubscriber_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        epsComponentContext.addEventSubscriber(null, "abc");
    }

    @Test
    public void addEventSubscriber_NullIdentifier_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        epsComponentContext.addEventSubscriber(eventInputHandlers[0], null);
    }

    @Test
    public void getEventHandlerConfiguration_NewEpsComponentContext_ReturnConfiguration() throws Exception {
        final Configuration configuration = epsComponentContext.getEventHandlerConfiguration();
        assertSame(mockedConfiguration, configuration);
    }

    @Test
    public void getEventSubscribers_SubscribersExist_ReturnAllSubscribers() throws Exception {
        int idx = 0;
        for (final EventInputHandler eih : eventInputHandlers) {
            final String identifier = "identifier_" + idx;
            idx++;
            epsComponentContext.addEventSubscriber(eih, identifier);
        }
        final Collection<EventSubscriber> subscribers = epsComponentContext.getEventSubscribers();
        Assert.assertEquals(eventInputHandlers.length, subscribers.size());
        final Set<String> identifiers = new HashSet<>();
        for (final EventSubscriber sub : subscribers) {
            identifiers.add(sub.getIdentifier());
        }
        for (int j = 0; j < eventInputHandlers.length; j++) {
            Assert.assertTrue(identifiers.contains("identifier_" + j));
        }
    }
}
