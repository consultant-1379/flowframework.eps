/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT © Ericsson AB 2013-2015 - All Rights Reserved
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
 * @author esarlag
 *
 */
@EModel(namespace = "MyTestNameSpace", name = "MyTestInputEventSubclass", version = "1.0.0", description = "MyTestInputEventSubclass")
@EventTypeDefinition(channelUrn = "//global/epsModEvAdapterDataInputChannel")
public class TestInputModeledEventSubclass extends TestInputModeledEvent {

    @NotNull
    @EventAttribute
    @EModelAttribute(mandatory = true, description = "Integer subclass field")
    private int subClassField;

    public int getSubClassField() {
        return subClassField;
    }

    public void setSubClassField(final int subClassField) {
        this.subClassField = subClassField;
    }

}
