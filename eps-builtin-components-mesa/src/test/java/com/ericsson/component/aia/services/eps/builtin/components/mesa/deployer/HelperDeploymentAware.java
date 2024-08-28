package com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer;

import java.util.concurrent.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaDeploymentException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Name;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.DeploymentAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Model;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.PolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Policy;

public final class HelperDeploymentAware implements DeploymentAware {

	private final DeploymentAware underlying;

	private final BlockingQueue<Model> deployQueue = new LinkedBlockingQueue<Model>();

	private final BlockingQueue<String> undeployQueue = new LinkedBlockingQueue<String>();

	public HelperDeploymentAware(final DeploymentAware underlying) {
		super();
		this.underlying = underlying;
	}

	@Override
	public void deployCore(final Policy policy) throws MesaDeploymentException {
		underlying.deployCore(policy);
		deployQueue.add(policy);
	}

	@Override
	public void deployConf(final PolicyConf policyConf) {
		underlying.deployConf(policyConf);
		deployQueue.add(policyConf);
	}

	@Override
	public void undeployCore(final Policy policy)
			throws MesaDeploymentException {
		underlying.undeployCore(policy);
		undeployQueue.add(policy.getName().getName());
	}

	@Override
	public void undeployConf(final Id confId, final Name name) {
		underlying.undeployConf(confId, name);
		undeployQueue.add(String.valueOf(((SimpleId) confId).getId()));
	}

	public void waitForCoreDeployment(final String coreName) throws Exception {
		final Model model = deployQueue.poll(30, TimeUnit.SECONDS);
		if (model == null) {
			throw new IllegalStateException("Core deployment " + coreName
					+ " not found, queue was empty");
		}
		final Policy policy = (Policy) model;
		if (!policy.getName().getName().equalsIgnoreCase(coreName)) {
			throw new IllegalStateException("Core deployment " + coreName
					+ " was expected, but found " + policy.getName().getName());
		}
	}

	public void waitForConfDeployment(final String confName) throws Exception {
		final Model model = deployQueue.poll(30, TimeUnit.SECONDS);
		if (model == null) {
			throw new IllegalStateException("Conf deployment " + confName
					+ " not found, queue was empty");
		}
		final PolicyConf conf = (PolicyConf) model;
		final String policyConfId = String.valueOf(((SimpleId) conf.getPolicyConfId())
				.getId());
		if (!policyConfId.equalsIgnoreCase(confName)) {
			throw new IllegalStateException("Conf deployment " + confName
					+ " was expected, but found " + policyConfId);
		}
	}

	public void waitForConfDeployments(final String... confNames)
			throws Exception {
		final Model model = deployQueue.poll(30, TimeUnit.SECONDS);
		if (model == null) {
			throw new IllegalStateException("Conf deployment(s) " + confNames
					+ " not found, queue was empty");
		}
		final PolicyConf conf = (PolicyConf) model;
		final String policyConfId = String.valueOf(((SimpleId) conf.getPolicyConfId())
				.getId());

		boolean foundInQueue = false;

		for (final String confName : confNames) {
			if (policyConfId.equalsIgnoreCase(confName)) {
				foundInQueue = true;
				break;
			}
		}

		if (!foundInQueue) {
			throw new IllegalStateException("Conf deployment " + confNames
					+ " was expected, but found " + policyConfId);
		}

	}

	public void waitForConfUnDeployment(final String confName) throws Exception {
		final String name = undeployQueue.poll(30, TimeUnit.SECONDS);
		if (name == null) {
			throw new IllegalStateException("Conf undeployment " + confName
					+ " not found, queue was empty");
		}
		if (!name.equalsIgnoreCase(confName)) {
			throw new IllegalStateException("Conf undeployment " + confName
					+ " was expected, but found " + name);
		}
	}

	public void waitForCoreUnDeployment(final String coreName) throws Exception {
		final String name = undeployQueue.poll(30, TimeUnit.SECONDS);
		if (name == null) {
			throw new IllegalStateException("Core undeployment " + coreName
					+ " not found, queue was empty");
		}
		if (!name.equalsIgnoreCase(coreName)) {
			throw new IllegalStateException("Core undeployment " + coreName
					+ " was expected, but found " + name);
		}
	}
}
