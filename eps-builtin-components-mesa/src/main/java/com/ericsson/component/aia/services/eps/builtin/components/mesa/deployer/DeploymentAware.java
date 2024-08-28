package com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaDeploymentException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Name;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.PolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Policy;

/**
 * Throw exception if anything goes wrong.
 */
public interface DeploymentAware {

    /**
     * Deploying core of conf policies.
     *
     * @param policy
     *            the policy
     * @throws MesaDeploymentException
     *             the mesa deployment exception
     */
    void deployCore(Policy policy) throws MesaDeploymentException;

    /**
     * Undeploying core will automatically cause undeploy of all conf policies.
     *
     * @param policy
     *            the policy
     * @throws MesaDeploymentException
     *             the mesa deployment exception
     */
    void undeployCore(Policy policy) throws MesaDeploymentException;

    /**
     * Deploy conf.
     *
     * @param policyConf
     *            the policy conf
     */
    void deployConf(PolicyConf policyConf);

    /**
     * Name is the name of core policy.
     *
     * @param ident
     *            the id
     * @param name
     *            the name
     */
    void undeployConf(Id ident, Name name);
}
