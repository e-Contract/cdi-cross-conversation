/*
 * eID Android project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package test.integ.be.e_contract.cdi.crossconversation;

import be.e_contract.cdi.crossconversation.AndroidManager;
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
public class AndroidScopedAndroidCodeServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedAndroidCodeServlet.class);

    @Inject
    private AndroidScopedObject androidScopedObject;

    @Inject
    private AndroidManager androidManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("doGet");
        this.androidScopedObject.method();
        response.getWriter().print(this.androidManager.getAndroidCode());
    }
}
