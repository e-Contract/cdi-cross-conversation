/*
 * eID Android project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
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

@WebServlet("/browser")
public class AndroidScopedBrowserTestServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedBrowserTestServlet.class);

    @Inject
    private AndroidScopedObject androidScopedObject;

    @Inject
    private CrossConversationManager androidManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("doGet");
        this.androidScopedObject.method();
        response.getWriter().print(this.androidManager.getCode());
    }
}
