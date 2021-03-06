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
package be.e_contract.cdi.crossconversation.impl;

import be.e_contract.cdi.crossconversation.CrossConversationScoped;
import be.e_contract.cdi.crossconversation.CrossConversationStrategy;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossConversationScopedContext implements Context, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossConversationScopedContext.class);

    // key: cross conversation identifier -> key bean class name -> instance entry -> instance
    static final Map<String, Map<String, InstanceEntry>> CROSS_CONVERSATIONS = new ConcurrentHashMap<>();

    public static class InstanceEntry {

        private final Object instance;
        private final Contextual contextual;
        private final CreationalContext creationalContext;

        public InstanceEntry(Object instance, Contextual contextual, CreationalContext creationalContext) {
            this.instance = instance;
            this.contextual = contextual;
            this.creationalContext = creationalContext;
        }

        public Object getInstance() {
            return this.instance;
        }

        public void destroy() {
            this.contextual.destroy(this.instance, this.creationalContext);
        }
    }

    public CrossConversationScopedContext() {
        LOGGER.debug("constructor");
    }

    @Override
    public Class<? extends Annotation> getScope() {
        LOGGER.debug("getScope");
        return CrossConversationScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        LOGGER.debug("get with creational context");
        HttpServletRequest httpServletRequest = CrossConversationScopedFilter.getHttpServletRequest();
        if (null == httpServletRequest) {
            throw new ContextNotActiveException();
        }
        CrossConversationStrategy crossConversationStrategy = BeanProvider.getContextualReference(CrossConversationStrategy.class);
        String linkedCrossConversationIdentifier = crossConversationStrategy.getLinkedCrossConversationIdentifier(httpServletRequest);
        Bean bean = (Bean) contextual;
        if (null == linkedCrossConversationIdentifier) {
            // webbrowser
            HttpSession httpSession = httpServletRequest.getSession();
            String crossConversationIdentifier = (String) getCrossConversationIdentifier(httpSession);
            if (null == crossConversationIdentifier) {
                crossConversationIdentifier = UUID.randomUUID().toString();
                httpSession.setAttribute(CrossConversationScopedContext.class.getName() + ".crossConversationIdentifier", crossConversationIdentifier);
                LOGGER.debug("new cross conversation: {}", crossConversationIdentifier);
                CROSS_CONVERSATIONS.put(crossConversationIdentifier, new ConcurrentHashMap<String, InstanceEntry>());
            }
            Map<String, InstanceEntry> crossConversationInstances = CROSS_CONVERSATIONS.get(crossConversationIdentifier);
            String beanName = bean.getBeanClass().getName();
            LOGGER.debug("bean name: {}", beanName);
            InstanceEntry instanceEntry = (InstanceEntry) crossConversationInstances.get(beanName);
            if (null == instanceEntry) {
                if (null == creationalContext) {
                    return null;
                }
                Object instance = contextual.create(creationalContext);
                instanceEntry = new InstanceEntry(instance, contextual, creationalContext);
                crossConversationInstances.put(beanName, instanceEntry);
            }
            return (T) instanceEntry.getInstance();
        } else {
            // linked client
            LOGGER.debug("linked cross conversation identifier: {}", linkedCrossConversationIdentifier);

            // update in session for testing purposes for the moment
            HttpSession httpSession = httpServletRequest.getSession();
            httpSession.setAttribute(CrossConversationScopedContext.class.getName() + ".linkedCrossConversationIdentifier", linkedCrossConversationIdentifier);

            Map<String, InstanceEntry> crossConversationInstances = CROSS_CONVERSATIONS.get(linkedCrossConversationIdentifier);
            if (null == crossConversationInstances) {
                // non-existing linked cross conversation identifier
                throw new ContextNotActiveException();
            }
            String beanName = bean.getBeanClass().getName();
            LOGGER.debug("bean name: {}", beanName);
            InstanceEntry instanceEntry = (InstanceEntry) crossConversationInstances.get(beanName);
            if (null == instanceEntry) {
                if (null == creationalContext) {
                    return null;
                }
                Object instance = contextual.create(creationalContext);
                instanceEntry = new InstanceEntry(instance, contextual, creationalContext);
                crossConversationInstances.put(beanName, instanceEntry);
            }
            return (T) instanceEntry.getInstance();
        }
    }

    public static String getCrossConversationIdentifier(HttpSession httpSession) {
        String crossConversdationIdentifier = (String) httpSession.getAttribute(CrossConversationScopedContext.class.getName() + ".crossConversationIdentifier");
        return crossConversdationIdentifier;
    }

    public static String getLinkedCrossConversationIdentifier(HttpSession httpSession) {
        String linkedCrossConversationIdentifier = (String) httpSession.getAttribute(CrossConversationScopedContext.class.getName() + ".linkedCrossConversationIdentifier");
        return linkedCrossConversationIdentifier;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        LOGGER.debug("get");
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        LOGGER.debug("isActive");
        HttpServletRequest httpServletRequest = CrossConversationScopedFilter.getHttpServletRequest();
        return httpServletRequest != null;
    }
}
