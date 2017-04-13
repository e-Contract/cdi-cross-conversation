/*
 * eID Android project.
 *
 * Copyright 2016 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.cdi.crossconversation;

import java.io.Serializable;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidScopedExtension implements Extension, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedExtension.class);

    public void registerContext(@Observes AfterBeanDiscovery event) {
        LOGGER.debug("adding AndroidScoped context");
        event.addContext(new AndroidScopedContext());
    }
}
