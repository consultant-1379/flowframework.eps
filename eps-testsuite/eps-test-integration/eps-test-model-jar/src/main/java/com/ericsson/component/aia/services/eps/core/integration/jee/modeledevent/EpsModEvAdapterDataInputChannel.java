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
import com.ericsson.oss.itpf.modeling.annotation.channel.ChannelDefinition;
import com.ericsson.oss.itpf.modeling.annotation.channel.ChannelType;

/**
 * Defines a modeled channel. It's used to receive modeled events from the modeled event bus into EPS during tests.
 */
@EModel(name = "epsModEvAdapterDataInputChannel", description = "Channel description text")
@ChannelDefinition(channelType = ChannelType.POINT_TO_POINT, channelURI = "jms:/queue/EPS_Input_Data")
public class EpsModEvAdapterDataInputChannel {

}
