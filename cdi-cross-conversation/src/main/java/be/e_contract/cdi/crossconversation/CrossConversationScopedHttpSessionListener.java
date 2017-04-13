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

import java.util.Collection;
import java.util.Map;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class CrossConversationScopedHttpSessionListener implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossConversationScopedHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LOGGER.debug("session created: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession httpSession = se.getSession();
        LOGGER.debug("session destroyed: {}", httpSession.getId());
        // we terminate the web browser cross conversation scope
        String crossConversationIdentifier = CrossConversationScopedContext.getCrossConversationIdentifier(httpSession);
        if (null == crossConversationIdentifier) {
            return;
        }
        Map<String, CrossConversationScopedContext.InstanceEntry> instanceEntryMap
                = CrossConversationScopedContext.CROSS_CONVERSATIONS.remove(crossConversationIdentifier);
        if (null == instanceEntryMap) {
            return;
        }
        LOGGER.debug("destroying cross conversation scope: {}", crossConversationIdentifier);
        Collection<CrossConversationScopedContext.InstanceEntry> instanceEntries = instanceEntryMap.values();
        for (CrossConversationScopedContext.InstanceEntry instanceEntry : instanceEntries) {
            instanceEntry.destroy();
        }
    }
}
