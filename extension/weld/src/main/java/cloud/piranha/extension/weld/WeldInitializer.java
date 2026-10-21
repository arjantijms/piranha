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
package cloud.piranha.extension.weld;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.weld.environment.servlet.EnhancedListener;

import cloud.piranha.core.api.WebApplication;

/**
 * The Weld Integration ServletContainerInitializer.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class WeldInitializer implements ServletContainerInitializer {

    /**
     * The Servlet context attribute with the initializers the ServletContainerInitializer extension doesn't add
     * (ServletContainerInitializerExtension.IGNORE_INITIALIZERS_PROPERTY).
     */
    private static final String IGNORE_INITIALIZERS_ATTRIBUTE =
        "cloud.piranha.extension.scinitializer.ServletContainerInitializerExtension.ignoreInitializers";

    /**
     * Constructor.
     */
    public WeldInitializer() {
    }

    /**
     * On startup.
     *
     * @param classes the annotated classes.
     * @param servletContext the servlet context.
     * @throws ServletException when a serious error occurs.
     */
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("WELD_CONTEXT_ID_KEY", servletContext.toString());

        if (Boolean.valueOf(System.getProperty("piranha.emptyBeansXmlModeAll", "false"))) {
            servletContext.setInitParameter("org.jboss.weld.environment.servlet.emptyBeansXmlModeAll", "true");
        }

        servletContext.setInitParameter("org.jboss.weld.environment.servlet.archive.isolation", "true");

        WebApplication webApplication = (WebApplication) servletContext;
        WeldInitListener weldInitListener = webApplication.createListener(WeldInitListener.class);

        try {
            webApplication.getManager().setObjectInstanceManager(new WeldObjectInstanceManager());
            servletContext.addListener(weldInitListener);

            // Start Weld now, with Weld's own initializer, so that the initializers after this one (e.g. those of
            // Soteria, Transact and EclipseLink) can use CDI. Its discovery is suppressed, see
            // ignoreDiscoveredWeldInitializer.
            new EnhancedListener().onStartup(classes, servletContext);
        } catch (IllegalStateException e) {
            // CDI not available
        }
    }

    /**
     * Keeps the ServletContainerInitializer extension from adding Weld's own initializer, which this initializer
     * runs itself. Weld's initializer doesn't check whether Weld was already started, so running it twice would
     * start Weld twice. Called by the extensions that add this initializer, before the ServletContainerInitializer
     * extension is configured.
     *
     * @param webApplication the web application.
     */
    public static void ignoreDiscoveredWeldInitializer(WebApplication webApplication) {
        Set<String> ignored = new LinkedHashSet<>();
        Object existing = webApplication.getAttribute(IGNORE_INITIALIZERS_ATTRIBUTE);
        if (existing instanceof Collection<?> initializers) {
            initializers.forEach(initializer -> ignored.add(initializer.toString()));
        }
        ignored.add(EnhancedListener.class.getName());
        webApplication.setAttribute(IGNORE_INITIALIZERS_ATTRIBUTE, ignored);
    }
}
