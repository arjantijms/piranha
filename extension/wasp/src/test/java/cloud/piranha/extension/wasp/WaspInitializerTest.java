/*
 * Copyright (c) 2002-2025 Manorrock.com. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cloud.piranha.extension.wasp;

import cloud.piranha.core.impl.DefaultJspConfigDescriptor;
import cloud.piranha.core.impl.DefaultJspPropertyGroupDescriptor;
import cloud.piranha.core.impl.DefaultWebApplication;
import cloud.piranha.resource.impl.DirectoryResource;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;
import java.io.File;
import java.util.Set;
import static cloud.piranha.extension.wasp.WaspInitializer.INIT_PARAMETER_PROPERTY_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the WaspInitializer.
 * 
 * @author Manfred Riem (mriem@manorrock.com)
 */
class WaspInitializerTest {

    /**
     * Test onStartup method.
     * 
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testOnStartup() throws Exception {
        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.addResource(new DirectoryResource(new File("src/test/webapp/waspinitializer")));
        webApp.addInitializer(new WaspInitializer());
        webApp.initialize();
        assertTrue(webApp.isInitialized());
    }

    /**
     * Test that onStartup maps the JSP servlet to JSP pages and JSP documents.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testOnStartupMapsJspPagesAndDocuments() throws Exception {
        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.addResource(new DirectoryResource(new File("src/test/webapp/waspinitializer")));
        webApp.addInitializer(new WaspInitializer());
        webApp.initialize();
        assertEquals(Set.of("*.jsp", "*.jspx"), Set.copyOf(webApp.getServletRegistration("jsp").getMappings()));
    }

    /**
     * Test that onStartup maps the JSP servlet to the URL patterns of the JSP property groups too.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testOnStartupMapsJspPropertyGroups() throws Exception {
        DefaultJspPropertyGroupDescriptor jspPropertyGroup = new DefaultJspPropertyGroupDescriptor();
        jspPropertyGroup.getUrlPatterns().add("*.xsp");
        jspPropertyGroup.getUrlPatterns().add("*.jsp");
        DefaultJspConfigDescriptor jspConfigDescriptor = new DefaultJspConfigDescriptor();
        jspConfigDescriptor.getJspPropertyGroups().add(jspPropertyGroup);

        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.addResource(new DirectoryResource(new File("src/test/webapp/waspinitializer")));
        webApp.setJspConfigDescriptor(jspConfigDescriptor);
        webApp.addInitializer(new WaspInitializer());
        webApp.initialize();
        assertEquals(Set.of("*.jsp", "*.jspx", "*.xsp"), Set.copyOf(webApp.getServletRegistration("jsp").getMappings()));
    }

    /**
     * Test that onStartup leaves *.jspx to a servlet of the application that's mapped to it, without that keeping the
     * JSP servlet from its other mappings, and that a JSP property group takes precedence over a servlet mapping with
     * the same URL pattern.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testOnStartupWithApplicationServletMappings() throws Exception {
        DefaultJspPropertyGroupDescriptor jspPropertyGroup = new DefaultJspPropertyGroupDescriptor();
        jspPropertyGroup.getUrlPatterns().add("/identical.jsp");
        jspPropertyGroup.getUrlPatterns().add("/group/*");
        DefaultJspConfigDescriptor jspConfigDescriptor = new DefaultJspConfigDescriptor();
        jspConfigDescriptor.getJspPropertyGroups().add(jspPropertyGroup);

        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.addResource(new DirectoryResource(new File("src/test/webapp/waspinitializer")));
        webApp.setJspConfigDescriptor(jspConfigDescriptor);
        webApp.addInitializer((classes, servletContext)
                -> servletContext.addServlet("application", new HttpServlet() {}).addMapping("*.jspx", "/identical.jsp"));
        webApp.addInitializer(new WaspInitializer());
        webApp.initialize();
        assertEquals(Set.of("*.jsp", "/identical.jsp", "/group/*"), Set.copyOf(webApp.getServletRegistration("jsp").getMappings()));
        assertEquals(Set.of("*.jspx"), Set.copyOf(webApp.getServletRegistration("application").getMappings()));
    }

    /**
     * Test that system properties set init parameters of the JSP servlet, taking precedence over those onStartup sets
     * itself.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testOnStartupWithInitParameterSystemProperties() throws Exception {
        System.setProperty(INIT_PARAMETER_PROPERTY_PREFIX + "development", "false");
        System.setProperty(INIT_PARAMETER_PROPERTY_PREFIX + "compilerSourceVM", "17");
        try {
            DefaultWebApplication webApp = new DefaultWebApplication();
            webApp.addResource(new DirectoryResource(new File("src/test/webapp/waspinitializer")));
            webApp.addInitializer(new WaspInitializer());
            webApp.initialize();
            ServletRegistration registration = webApp.getServletRegistration("jsp");
            assertEquals("false", registration.getInitParameter("development"));
            assertEquals("17", registration.getInitParameter("compilerSourceVM"));
            assertEquals("1.8", registration.getInitParameter("compilerTargetVM"));
        } finally {
            System.clearProperty(INIT_PARAMETER_PROPERTY_PREFIX + "development");
            System.clearProperty(INIT_PARAMETER_PROPERTY_PREFIX + "compilerSourceVM");
        }
    }
}
