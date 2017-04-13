/*
 * eID Android project.
 *
 * Copyright 2016-2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package test.integ.be.e_contract.cdi.crossconversation;

import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import be.e_contract.cdi.crossconversation.CrossConversationScoped;

@RunWith(Arquillian.class)
@RunAsClient
public class AndroidScopedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidScopedTest.class);

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addClasses(AndroidScopedBrowserTestServlet.class, AndroidScopedObject.class, AndroidScopedValueServlet.class,
                        AndroidScopedInvalidateSessionServlet.class, AndroidScopedAndroidCodeServlet.class)
                .addPackages(true, CrossConversationScoped.class.getPackage())
                .addAsWebInfResource(
                        AndroidScopedTest.class
                                .getResource("/META-INF/beans.xml"),
                        "beans.xml")
                .addAsManifestResource(AndroidScopedTest.class.getResource("/META-INF/services/javax.enterprise.inject.spi.Extension"),
                        "services/javax.enterprise.inject.spi.Extension");
        return war;
    }

    private String doGet(HttpClient httpClient, HttpContext httpContext, String location) throws IOException {
        return doGet(httpClient, httpContext, location, HttpServletResponse.SC_OK);
    }

    private String doGet(HttpClient httpClient, HttpContext httpContext, String location, int expectedStatusCode) throws IOException {
        HttpGet httpGet = new HttpGet(location);
        HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        assertEquals(expectedStatusCode, statusCode);
        String result = EntityUtils.toString(httpResponse.getEntity());
        return result;
    }

    @Test
    public void testAndroidScoped() throws Exception {
        String browserLocation = this.baseURL + "browser";
        String valueLocation = this.baseURL + "value";
        LOGGER.debug("location: {}", browserLocation);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();

        HttpContext httpContext = new BasicHttpContext();
        String androidCode = doGet(httpClient, httpContext, browserLocation);
        LOGGER.debug("result: {}", androidCode);
        String value = doGet(httpClient, httpContext, valueLocation);

        String androidCode2 = doGet(httpClient, httpContext, browserLocation);
        LOGGER.debug("result 2: {}", androidCode2);
        String value2 = doGet(httpClient, httpContext, valueLocation);

        assertEquals(androidCode, androidCode2);
        assertEquals(value, value2);
    }

    @Test
    public void testAndroidScopedNewSessionIsNewAndroidScope() throws Exception {
        String browserLocation = this.baseURL + "browser";
        String valueLocation = this.baseURL + "value";
        LOGGER.debug("location: {}", browserLocation);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();

        HttpContext httpContext = new BasicHttpContext();
        String androidCode = doGet(httpClient, httpContext, browserLocation);
        LOGGER.debug("result: {}", androidCode);
        String value = doGet(httpClient, httpContext, valueLocation);

        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(HttpClientContext.COOKIE_STORE);
        cookieStore.clear();

        String androidCode2 = doGet(httpClient, httpContext, browserLocation);
        LOGGER.debug("result 2: {}", androidCode2);
        String value2 = doGet(httpClient, httpContext, valueLocation);

        assertNotEquals(androidCode, androidCode2);
        assertNotEquals(value, value2);
    }

    @Test
    public void testAndroidScopedBasicUsage() throws Exception {
        String browserLocation = this.baseURL + "browser";
        String valueLocation = this.baseURL + "value";
        LOGGER.debug("location: {}", browserLocation);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();

        HttpContext httpContext = new BasicHttpContext();
        String androidCode = doGet(httpClient, httpContext, browserLocation);
        LOGGER.debug("result: {}", androidCode);
        String value = doGet(httpClient, httpContext, valueLocation);

        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(HttpClientContext.COOKIE_STORE);
        cookieStore.clear();

        String androidLocation = this.baseURL + "android?androidCode=" + androidCode;
        valueLocation += "?androidCode=" + androidCode;

        HttpClient androidHttpClient = httpClientBuilder.build();
        HttpContext androidHttpContext = new BasicHttpContext();

        String androidCode2 = doGet(androidHttpClient, androidHttpContext, androidLocation);
        LOGGER.debug("result 2: {}", androidCode2);
        String value2 = doGet(androidHttpClient, androidHttpContext, valueLocation);

        assertEquals(androidCode, androidCode2);
        assertEquals(value, value2);
    }

    @Test
    public void testNonExistingAndroidCodeFails() throws Exception {
        String valueLocation = this.baseURL + "value?androidCode=" + "foobar";
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        HttpContext httpContext = new BasicHttpContext();
        doGet(httpClient, httpContext, valueLocation, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testAndroidSessionInvalidationKeepsResultForWebBrowser() throws Exception {
        String webBrowserTestLocation = this.baseURL + "browser";
        String webBrowserValueLocation = this.baseURL + "value";
        LOGGER.debug("location: {}", webBrowserTestLocation);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient webBrowserHttpClient = httpClientBuilder.build();
        HttpContext webBrowserHttpContext = new BasicHttpContext();

        String webBrowserCode = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserTestLocation);
        LOGGER.debug("result: {}", webBrowserCode);
        String webBrowserValue = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserValueLocation);

        String androidLocation = this.baseURL + "android?androidCode=" + webBrowserCode;
        String androidValueLocation = webBrowserValueLocation + "?androidCode=" + webBrowserCode;
        String androidInvalidateLocation = this.baseURL + "invalidate?androidCode=" + webBrowserCode;

        HttpClient androidHttpClient = httpClientBuilder.build();
        HttpContext androidHttpContext = new BasicHttpContext();

        String androidCode = doGet(androidHttpClient, androidHttpContext, androidLocation);
        LOGGER.debug("result 2: {}", androidCode);
        String androidValue = doGet(androidHttpClient, androidHttpContext, androidValueLocation);

        assertEquals(webBrowserCode, androidCode);
        assertEquals(webBrowserValue, androidValue);

        doGet(androidHttpClient, androidHttpContext, androidInvalidateLocation);

        String webBrowserCode2 = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserTestLocation);
        String webBrowserValue2 = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserValueLocation);

        assertEquals(webBrowserCode, webBrowserCode2);
        assertEquals(webBrowserValue, webBrowserValue2);
    }

    @Test
    public void testWebBrowserSessionInvalidationCreatesNewAndroidScope() throws Exception {
        String webBrowserTestLocation = this.baseURL + "browser";
        String webBrowserValueLocation = this.baseURL + "value";
        String webBrowserInvalidateLocation = this.baseURL + "invalidate";

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient webBrowserHttpClient = httpClientBuilder.build();
        HttpContext webBrowserHttpContext = new BasicHttpContext();

        String webBrowserCode = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserTestLocation);
        String webBrowserValue = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserValueLocation);

        doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserInvalidateLocation);

        String webBrowserCode2 = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserTestLocation);
        String webBrowserValue2 = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserValueLocation);

        assertNotEquals(webBrowserCode, webBrowserCode2);
        assertNotEquals(webBrowserValue, webBrowserValue2);
    }

    @Test
    public void testAndroidSessionInvalidationInvalidatesAndroidSide() throws Exception {
        String webBrowserTestLocation = this.baseURL + "browser";
        String webBrowserInvalidateLocation = this.baseURL + "invalidate";

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient webBrowserHttpClient = httpClientBuilder.build();
        HttpContext webBrowserHttpContext = new BasicHttpContext();

        String webBrowserCode = doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserTestLocation);

        doGet(webBrowserHttpClient, webBrowserHttpContext, webBrowserInvalidateLocation);

        String androidTestLocation = this.baseURL + "android?androidCode=" + webBrowserCode;

        HttpClient androidHttpClient = httpClientBuilder.build();
        HttpContext androidHttpContext = new BasicHttpContext();

        doGet(androidHttpClient, androidHttpContext, androidTestLocation, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testAndroidSessionSeparation() throws Exception {
        String webBrowserTestLocation = this.baseURL + "browser";
        String webBrowserValueLocation = this.baseURL + "value";
        LOGGER.debug("location: {}", webBrowserTestLocation);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        HttpClient webBrowserHttpClient1 = httpClientBuilder.build();

        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        HttpClient webBrowserHttpClient2 = httpClientBuilder.build();

        HttpContext webBrowserHttpContext1 = new BasicHttpContext();
        String webBrowserCode1 = doGet(webBrowserHttpClient1, webBrowserHttpContext1, webBrowserTestLocation);
        String webBrowserValue1 = doGet(webBrowserHttpClient1, webBrowserHttpContext1, webBrowserValueLocation);

        HttpContext webBrowserHttpContext2 = new BasicHttpContext();
        String webBrowserCode2 = doGet(webBrowserHttpClient2, webBrowserHttpContext2, webBrowserTestLocation);
        String webBrowserValue2 = doGet(webBrowserHttpClient2, webBrowserHttpContext2, webBrowserValueLocation);

        assertNotEquals(webBrowserCode1, webBrowserCode2);
        assertNotEquals(webBrowserValue1, webBrowserValue2);

        String androidLocation1 = this.baseURL + "android?androidCode=" + webBrowserCode1;
        String androidValueLocation1 = webBrowserValueLocation + "?androidCode=" + webBrowserCode1;

        HttpClient androidHttpClient = httpClientBuilder.build();

        HttpContext androidHttpContext1 = new BasicHttpContext();
        String androidCode1 = doGet(androidHttpClient, androidHttpContext1, androidLocation1);
        String androidValue1 = doGet(androidHttpClient, androidHttpContext1, androidValueLocation1);

        assertEquals(webBrowserCode1, androidCode1);
        assertEquals(webBrowserValue1, androidValue1);

        String androidLocation2 = this.baseURL + "android?androidCode=" + webBrowserCode2;
        String androidValueLocation2 = webBrowserValueLocation + "?androidCode=" + webBrowserCode2;

        HttpContext androidHttpContext2 = new BasicHttpContext();
        String androidCode2 = doGet(androidHttpClient, androidHttpContext2, androidLocation2);
        String androidValue2 = doGet(androidHttpClient, androidHttpContext2, androidValueLocation2);

        assertEquals(webBrowserCode2, androidCode2);
        assertEquals(webBrowserValue2, androidValue2);
    }
}
