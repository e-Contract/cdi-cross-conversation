/*
 * eID Android project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.cdi.crossconversation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@ApplicationScoped
public class CrossConversationManager {

    /**
     * Retrieves the cross conversation scope identifier at the web browser
     * side.
     *
     * @return
     */
    public String getCrossConversationIdentifier() {
        HttpServletRequest httpServletRequest = CrossConversationScopedFilter.getHttpServletRequest();
        if (null == httpServletRequest) {
            throw new ContextNotActiveException();
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (null == httpSession) {
            throw new ContextNotActiveException();
        }
        String androidCode = CrossConversationScopedContext.getCrossConversationIdentifier(httpSession);
        return androidCode;
    }

    /**
     * Retrieves the Android scope code at the android side. This is mainly for
     * testing purposes.
     *
     * @return
     */
    public String getAndroidCode() {
        HttpServletRequest httpServletRequest = CrossConversationScopedFilter.getHttpServletRequest();
        if (null == httpServletRequest) {
            throw new ContextNotActiveException();
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (null == httpSession) {
            throw new ContextNotActiveException();
        }
        String androidCode = (String) httpSession.getAttribute(CrossConversationScopedContext.class.getName() + ".androidCode");
        return androidCode;
    }
}
