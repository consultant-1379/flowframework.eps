package com.ericsson.component.aia.services.eps.builtin.components.mesa.manager;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Injectable;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.DeploymentAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;

/**
 * Manages lifecycle of policies.
 *
 * @see ViewListener
 * @see DeploymentAware
 *
 * @see Injectable
 */
public interface Manager extends ViewListener, DeploymentAware, Injectable {
}
