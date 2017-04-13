/*
 * eID Android project.
 *
 * Copyright 2016 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package test.integ.be.e_contract.cdi.crossconversation;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import be.e_contract.cdi.crossconversation.CrossConversationScoped;

@CrossConversationScoped
public class AndroidScopedObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedObject.class);

    private final String value;
    
    public AndroidScopedObject() {
        LOGGER.debug("constructor");
        this.value = UUID.randomUUID().toString();
    }
    
    public void method() {
        LOGGER.debug("method invoked: {}", this);
    }
    
    public String getValue() {
        return this.value;
    }
}
