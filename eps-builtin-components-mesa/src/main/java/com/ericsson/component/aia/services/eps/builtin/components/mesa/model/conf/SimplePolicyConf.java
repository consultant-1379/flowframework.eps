package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * Java representation of conf policy XML descriptor.
 */
public final class SimplePolicyConf implements PolicyConf {

    private final Id policyCoreId;
    private final Id policyConfId;
    private final List<Conf> confs;

    /**
     * Instantiates a new simple policy conf.
     *
     * @param policyCoreId
     *            the policy core id
     * @param policyConfId
     *            the policy conf id
     */
    public SimplePolicyConf(final Id policyCoreId, final Id policyConfId) {
        super();
        this.policyCoreId = policyCoreId;
        this.policyConfId = policyConfId;
        confs = new ArrayList<Conf>();
    }

    /**
     * Append.
     *
     * @param conf
     *            the conf
     */
    public void append(final Conf conf) {
        confs.add(conf);
    }

    @Override
    public Id getPolicyCoreId() {
        return policyCoreId;
    }

    @Override
    public Id getPolicyConfId() {
        return policyConfId;
    }

    @Override
    public List<Conf> getConfs() {
        return confs;
    }
}
