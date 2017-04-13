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
public class CrossConversationScopedFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossConversationScopedFilter.class);

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
