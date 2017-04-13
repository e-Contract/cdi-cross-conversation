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

import be.e_contract.cdi.crossconversation.impl.CrossConversationScopedContext;
import be.e_contract.cdi.crossconversation.impl.CrossConversationScopedFilter;
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
     * Retrieves the linked cross conversation identifier. This is mainly for
     * testing purposes.
     *
     * @return
     */
    public String getLinkedCrossConversationIdentifier() {
        HttpServletRequest httpServletRequest = CrossConversationScopedFilter.getHttpServletRequest();
        if (null == httpServletRequest) {
            throw new ContextNotActiveException();
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (null == httpSession) {
            throw new ContextNotActiveException();
        }
        String linkedCrossConversationIdentifier = CrossConversationScopedContext.getLinkedCrossConversationIdentifier(httpSession);
        return linkedCrossConversationIdentifier;
    }
}
