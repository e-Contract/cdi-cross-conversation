/*
 * CDI Cross Conversation project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package be.e_contract.cdi.crossconversation;

import java.io.Serializable;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossConversationScopedExtension implements Extension, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossConversationScopedExtension.class);

    public void registerContext(@Observes AfterBeanDiscovery event) {
        LOGGER.debug("adding CrossConversationScoped context");
        event.addContext(new CrossConversationScopedContext());
    }
}
