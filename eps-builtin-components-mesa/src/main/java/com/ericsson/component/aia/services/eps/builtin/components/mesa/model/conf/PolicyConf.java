package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Model;

/**
 * The Interface PolicyConf.
 */
public interface PolicyConf extends Model {

    /**
     * Gets the policy core id.
     *
     * @return the policy core id
     */
    Id getPolicyCoreId();

    /**
     * Gets the policy conf id.
     *
     * @return the policy conf id
     */
    Id getPolicyConfId();

    /**
     * Gets the confs.
     *
     * @return the confs list
     */
    List<Conf> getConfs();
}