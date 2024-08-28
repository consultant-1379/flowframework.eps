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
package com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent;

import com.ericsson.oss.itpf.modeling.annotation.EModel;
import com.ericsson.oss.itpf.modeling.annotation.EModelAttribute;
import com.ericsson.oss.itpf.modeling.annotation.constraints.NotNull;
import com.ericsson.oss.itpf.modeling.annotation.eventtype.EventAttribute;
import com.ericsson.oss.itpf.modeling.annotation.eventtype.EventTypeDefinition;

/**
 * Defines a modeled event. It's used to send modeled events from EPS to the modeled event bus during tests.
 */
@EModel(namespace = "MyTestNameSpace", name = "MyTestOutputEvent", version = "1.0.0", description = "MyTestOutputEvent")
@EventTypeDefinition(channelUrn = "//global/epsModEvAdapterDataOutputChannel")
public class TestOutputModeledEvent {
    /**
     *
     */
    @NotNull
    @EventAttribute
    @EModelAttribute(mandatory = true, description = "A string of some text")
    public String myTestValue;

    /**
     * @return the myTestValue
     */
    public String getMyTestValue() {
        return myTestValue;
    }

    /**
     * @param myTestValue
     *            the myTestValue to set
     */
    public void setMyTestValue(final String myTestValue) {
        this.myTestValue = myTestValue;
    }

}