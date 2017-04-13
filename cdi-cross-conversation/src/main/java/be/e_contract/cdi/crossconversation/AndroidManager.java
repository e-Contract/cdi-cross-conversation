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
public class AndroidManager {

    /**
     * Retrieves the Android scope code at the web browser side.
     *
     * @return
     */
    public String getCode() {
        HttpServletRequest httpServletRequest = AndroidScopedFilter.getHttpServletRequest();
        if (null == httpServletRequest) {
            throw new ContextNotActiveException();
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (null == httpSession) {
            throw new ContextNotActiveException();
        }
        String androidCode = AndroidScopedContext.getWebBrowserCode(httpSession);
        return androidCode;
    }

    /**
     * Retrieves the Android scope code at the android side. This is mainly for
     * testing purposes.
     *
     * @return
     */
    public String getAndroidCode() {
        HttpServletRequest httpServletRequest = AndroidScopedFilter.getHttpServletRequest();
        if (null == httpServletRequest) {
            throw new ContextNotActiveException();
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (null == httpSession) {
            throw new ContextNotActiveException();
        }
        String androidCode = (String) httpSession.getAttribute(AndroidScopedContext.class.getName() + ".androidCode");
        return androidCode;
    }
}
