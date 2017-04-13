/*
 * eID Android project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.cdi.crossconversation;

import java.util.Collection;
import java.util.Map;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AndroidScopedHttpSessionListener implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LOGGER.debug("session created: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession httpSession = se.getSession();
        LOGGER.debug("session destroyed: {}", httpSession.getId());
        // we terminate the web browser android scope
        String androidCode = AndroidScopedContext.getWebBrowserCode(httpSession);
        if (null == androidCode) {
            return;
        }
        Map<String, AndroidScopedContext.InstanceEntry> instanceEntryMap = AndroidScopedContext.androidStore.remove(androidCode);
        if (null == instanceEntryMap) {
            return;
        }
        LOGGER.debug("destroying android scope: {}", androidCode);
        Collection<AndroidScopedContext.InstanceEntry> instanceEntries = instanceEntryMap.values();
        for (AndroidScopedContext.InstanceEntry instanceEntry : instanceEntries) {
            instanceEntry.destroy();
        }
    }
}
