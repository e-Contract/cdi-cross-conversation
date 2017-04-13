/*
 * eID Android project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.cdi.crossconversation;

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
        String androidCodeParameter = httpServletRequest.getParameter("androidCode");
        Bean bean = (Bean) contextual;
        if (null == androidCodeParameter) {
            // webbrowser
            HttpSession httpSession = httpServletRequest.getSession();
            String androidCode = (String) getCrossConversationIdentifier(httpSession);
            if (null == androidCode) {
                androidCode = UUID.randomUUID().toString();
                httpSession.setAttribute(CrossConversationScopedContext.class.getName() + ".webBrowserCode", androidCode);
                LOGGER.debug("new android code: {}", androidCode);
                CROSS_CONVERSATIONS.put(androidCode, new ConcurrentHashMap<String, InstanceEntry>());
            }
            Map<String, InstanceEntry> androidInstances = CROSS_CONVERSATIONS.get(androidCode);
            String beanName = bean.getBeanClass().getName();
            LOGGER.debug("bean name: {}", beanName);
            InstanceEntry instanceEntry = (InstanceEntry) androidInstances.get(beanName);
            if (null == instanceEntry) {
                if (null == creationalContext) {
                    return null;
                }
                Object instance = contextual.create(creationalContext);
                instanceEntry = new InstanceEntry(instance, contextual, creationalContext);
                androidInstances.put(beanName, instanceEntry);
            }
            return (T) instanceEntry.getInstance();
        } else {
            // android
            LOGGER.debug("android code: {}", androidCodeParameter);

            // update in session for testing purposes for the moment
            HttpSession httpSession = httpServletRequest.getSession();
            httpSession.setAttribute(CrossConversationScopedContext.class.getName() + ".androidCode", androidCodeParameter);

            Map<String, InstanceEntry> androidInstances = CROSS_CONVERSATIONS.get(androidCodeParameter);
            if (null == androidInstances) {
                // non-existing android code
                throw new ContextNotActiveException();
            }
            String beanName = bean.getBeanClass().getName();
            LOGGER.debug("bean name: {}", beanName);
            InstanceEntry instanceEntry = (InstanceEntry) androidInstances.get(beanName);
            if (null == instanceEntry) {
                if (null == creationalContext) {
                    return null;
                }
                Object instance = contextual.create(creationalContext);
                instanceEntry = new InstanceEntry(instance, contextual, creationalContext);
                androidInstances.put(beanName, instanceEntry);
            }
            return (T) instanceEntry.getInstance();
        }
    }

    public static String getCrossConversationIdentifier(HttpSession httpSession) {
        String androidCode = (String) httpSession.getAttribute(CrossConversationScopedContext.class.getName() + ".webBrowserCode");
        return androidCode;
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
