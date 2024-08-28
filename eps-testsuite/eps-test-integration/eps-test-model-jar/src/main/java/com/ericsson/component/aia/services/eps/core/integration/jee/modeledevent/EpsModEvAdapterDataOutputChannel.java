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
 * Defines a modeled channel. It's used to send modeled events from EPS to the modeled event bus during tests.
 */
@EModel(name = "epsModEvAdapterDataOutputChannel", description = "Channel description text")
@ChannelDefinition(channelType = ChannelType.POINT_TO_POINT, channelURI = "jms:/queue/EPS_Output_Data")
public class EpsModEvAdapterDataOutputChannel {

}
