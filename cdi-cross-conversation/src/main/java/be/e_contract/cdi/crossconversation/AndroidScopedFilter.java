/*
 * eID Android project.
 *
 * Copyright 2016 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.cdi.crossconversation;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter("/*")
public class AndroidScopedFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedFilter.class);

    private static final ThreadLocal<HttpServletRequest> HTTP_SERVLET_REQUESTS = new ThreadLocal<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.debug("init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.debug("doFilter");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HTTP_SERVLET_REQUESTS.set(httpServletRequest);
        try {
            chain.doFilter(request, response);
        } finally {
            HTTP_SERVLET_REQUESTS.remove();
        }
    }

    @Override
    public void destroy() {
        LOGGER.debug("destroy");
    }

    public static HttpServletRequest getHttpServletRequest() {
        return HTTP_SERVLET_REQUESTS.get();
    }
}
