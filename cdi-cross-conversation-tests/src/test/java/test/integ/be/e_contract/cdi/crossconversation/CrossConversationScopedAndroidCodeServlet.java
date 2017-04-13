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
package test.integ.be.e_contract.cdi.crossconversation;

import be.e_contract.cdi.crossconversation.CrossConversationManager;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/android")
public class CrossConversationScopedAndroidCodeServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossConversationScopedAndroidCodeServlet.class);

    @Inject
    private CrossConversationScopedObject androidScopedObject;

    @Inject
    private CrossConversationManager androidManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("doGet");
        this.androidScopedObject.method();
        response.getWriter().print(this.androidManager.getAndroidCode());
    }
}
