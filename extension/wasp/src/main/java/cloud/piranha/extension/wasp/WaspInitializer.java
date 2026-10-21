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

import cloud.piranha.core.api.WebApplication;
import jakarta.servlet.Registration;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.jsp.JspFactory;
import java.io.File;
import static java.io.File.pathSeparator;
import java.lang.System.Logger;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import org.glassfish.wasp.runtime.JspFactoryImpl;
import org.glassfish.wasp.runtime.TldScanner;

/**
 * The WaSP initializer.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class WaspInitializer implements ServletContainerInitializer {

    /**
     * The prefix of the system properties that set init parameters of the JSP servlet, before the ones this
     * initializer sets itself, e.g. cloud.piranha.extension.wasp.WaspInitializer.initParameter.development=false.
     */
    public static final String INIT_PARAMETER_PROPERTY_PREFIX = "cloud.piranha.extension.wasp.WaspInitializer.initParameter.";

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = System.getLogger(WaspInitializer.class.getName());

    /**
     * Constructor.
     */
    public WaspInitializer() {
    }

    /**
     * Initialize WaSP.
     *
     * @param classes the classes.
     * @param servletContext the Servlet context.
     * @throws ServletException when a Servlet error occurs.
     */
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        WebApplication application = (WebApplication) servletContext;

        LOGGER.log(DEBUG, "Initializing WaSP");

        if (JspFactory.getDefaultFactory() == null) {
            JspFactory.setDefaultFactory(new JspFactoryImpl());
        }
        
        if (application.getServletRegistration("jsp") == null) {
            ServletRegistration.Dynamic registration = application.addServlet("jsp", "org.glassfish.wasp.servlet.JspServlet");

            // JSP pages and JSP documents, unless the application maps these to a servlet of its own. One at a time,
            // as addMapping adds none of its patterns if one of them is mapped already.
            registration.addMapping("*.jsp");
            registration.addMapping("*.jspx");

            // The resources the JSP property groups match are JSP pages too. A property group takes precedence over a
            // servlet mapping with the same URL pattern.
            for (String urlPattern : getJspPropertyGroupUrlPatterns(application)) {
                application.removeServletMapping(urlPattern);
                registration.addMapping(urlPattern);
            }

            // First, as setInitParameter doesn't change an init parameter that's set already
            setInitParametersFromSystemProperties(registration);
            registration.setInitParameter("classpath", getClassPath(application));
            registration.setInitParameter("compilerSourceVM", "1.8");
            registration.setInitParameter("compilerTargetVM", "1.8");
        }
        
        application.setAttribute("org.glassfish.wasp.useMultiJarScanAlgo", true);

        LOGGER.log(DEBUG, "Initialized WaSP");

        TldScanner scanner = new TldScanner();
        scanner.onStartup(classes, servletContext);
    }

    /**
     * Set the init parameters that the system properties with {@link #INIT_PARAMETER_PROPERTY_PREFIX} give: each
     * one's name is that of the system property without the prefix.
     *
     * @param registration the registration of a JSP servlet.
     */
    static void setInitParametersFromSystemProperties(Registration registration) {
        Properties systemProperties = System.getProperties();
        for (String propertyName : systemProperties.stringPropertyNames()) {
            if (propertyName.startsWith(INIT_PARAMETER_PROPERTY_PREFIX)) {
                registration.setInitParameter(
                        propertyName.substring(INIT_PARAMETER_PROPERTY_PREFIX.length()),
                        systemProperties.getProperty(propertyName));
            }
        }
    }

    /**
     * Get the URL patterns of the JSP property groups.
     *
     * @param servletContext the servlet context.
     * @return the URL patterns.
     */
    private Set<String> getJspPropertyGroupUrlPatterns(ServletContext servletContext) {
        Set<String> urlPatterns = new LinkedHashSet<>();
        JspConfigDescriptor jspConfigDescriptor = servletContext.getJspConfigDescriptor();
        if (jspConfigDescriptor != null) {
            for (JspPropertyGroupDescriptor jspPropertyGroup : jspConfigDescriptor.getJspPropertyGroups()) {
                urlPatterns.addAll(jspPropertyGroup.getUrlPatterns());
            }
        }

        return urlPatterns;
    }

    /**
     * Gets the full classpath used for JSP
     *
     * @param application the application
     * @return the full classpath used for JSP
     */
    private String getClassPath(WebApplication application) {
        String classpath =
                System.getProperty("jdk.module.path", System.getProperty("java.class.path")) +
                getClassesDirectory(application) +
                getJarFiles(application);

        LOGGER.log(TRACE, () -> "WaSP classpath is: " + classpath);

        return classpath;
    }

    /**
     * Get the WEB-INF/classes directory.
     *
     * @param servletContext the servlet context.
     * @return the classes directory.
     */
    private String getClassesDirectory(ServletContext servletContext) {
        String classesDirectory = servletContext.getRealPath("/WEB-INF/classes");
        if (classesDirectory == null) {
            return "";
        }

        return pathSeparator + classesDirectory;
    }

    /**
     * Get the WEB-INF/lib JAR files.
     *
     * @param servletContext the servlet context.
     * @return the location of the JAR files.
     */
    private String getJarFiles(ServletContext servletContext) {
        StringBuilder jarFiles = new StringBuilder();
        String realPath = servletContext.getRealPath("/WEB-INF/lib");
        if (realPath != null) {
            File directory = new File(realPath);
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getAbsolutePath().endsWith(".jar")) {
                            jarFiles.append(pathSeparator);
                            jarFiles.append(file.getAbsolutePath());
                        }
                    }
                }
            }
        }

        return jarFiles.toString();
    }

    static {
        // This is a hack until Jasper handles JPMS
        if (WaspInitializer.class.getModule().isNamed()) {
            String oldExtDirs = System.getProperty("java.ext.dirs", "");
            System.setProperty("java.ext.dirs", System.getProperty("jdk.module.path") + pathSeparator + oldExtDirs);
        }
    }
}
