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
package cloud.piranha.http.webapp;

import cloud.piranha.http.api.HttpServerProcessor;
import cloud.piranha.http.api.HttpServerProcessorEndState;
import static cloud.piranha.http.api.HttpServerProcessorEndState.ASYNCED;
import static cloud.piranha.http.api.HttpServerProcessorEndState.COMPLETED;
import cloud.piranha.http.api.HttpServerRequest;
import cloud.piranha.http.api.HttpServerResponse;
import cloud.piranha.core.api.WebApplication;
import cloud.piranha.core.api.WebApplicationRequest;
import cloud.piranha.core.api.WebApplicationResponse;
import cloud.piranha.core.api.WebApplicationServer;
import cloud.piranha.core.api.WebApplicationServerRequestMapper;
import static cloud.piranha.http.api.HttpServerProcessorEndState.UPGRADED;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.lang.System.Logger;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default WebApplicationServer.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class HttpWebApplicationServer implements HttpServerProcessor, WebApplicationServer {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = System.getLogger(HttpWebApplicationServer.class.getName());

    /**
     * Stores the running boolean.
     */
    protected boolean running = false;

    /**
     * Stores the request mapper.
     */
    protected WebApplicationServerRequestMapper requestMapper;

    /**
     * Stores the web applications.
     */
    protected final Map<String, WebApplication> webApplications;

    /**
     * Constructor.
     */
    public HttpWebApplicationServer() {
        this.requestMapper = new HttpWebApplicationServerRequestMapper();
        this.webApplications = new ConcurrentHashMap<>();
    }

    /**
     * Add a context path mapping.
     *
     * @param servletContextName the servlet context name.
     * @param contextPath the context path.
     */
    public void addMapping(String servletContextName, String contextPath) {
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Adding context path: {0} for: {1}", contextPath, servletContextName);
        }
        for (WebApplication webApp : webApplications.values()) {
            if (webApp.getServletContextName().equals(servletContextName)) {
                requestMapper.addMapping(webApp, contextPath);
                break;
            }
        }
    }

    @Override
    public void addWebApplication(WebApplication webApplication) {
        if (LOGGER.isLoggable(DEBUG)) {
            LOGGER.log(DEBUG, () -> "Adding web application with context path: " + webApplication.getContextPath());
        }
        webApplications.put(webApplication.getContextPath(), webApplication);
        requestMapper.addMapping(webApplication, webApplication.getContextPath());
    }

    @Override
    public WebApplicationServerRequestMapper getRequestMapper() {
        return requestMapper;
    }

    @Override
    public void initialize() {
        if (LOGGER.isLoggable(DEBUG)) {
            LOGGER.log(DEBUG, "Starting initialization of {0} web application(s)", webApplications.size());
        }
        for (WebApplication webApp : webApplications.values()) {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(webApp.getClassLoader());
                if (LOGGER.isLoggable(TRACE)) {
                    LOGGER.log(TRACE, "Initializing web application at: {0}", webApp.getContextPath());
                }
                webApp.initialize();
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        if (LOGGER.isLoggable(DEBUG)) {
            LOGGER.log(DEBUG, "Finished initialization of {0} web application(s)", webApplications.size());
        }
    }

    @Override
    public HttpServerProcessorEndState process(HttpServerRequest request, HttpServerResponse response) {
        HttpServerProcessorEndState state = COMPLETED;
        try {
            HttpWebApplicationRequest serverRequest = new HttpWebApplicationRequest(request);
            HttpWebApplicationResponse serverResponse = new HttpWebApplicationResponse(response);
            service(serverRequest, serverResponse);
            if (serverRequest.isAsyncStarted()) {
                state = ASYNCED;
            }
            if (serverRequest.isUpgraded()) {
                state = UPGRADED;
            }
        } catch (Throwable t) {
            LOGGER.log(ERROR, "An error occurred while processing the request", t);
        }
        
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Processor end state: {0}", state.name());
        }
        
        return state;
    }

    /**
     * Service method.
     *
     * @param request the request.
     * @param response the response.
     * @throws IOException when an I/O error occurs.
     * @throws ServletException when a servlet error occurs.
     */
    @Override
    public void service(WebApplicationRequest request, WebApplicationResponse response) throws IOException, ServletException {
        String requestUri = request.getRequestURI();

        if (requestUri == null) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, "Request URI is invalid");
            }
            sendError(response, 500);
            return;
        }

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Request URI: {0}", requestUri);
        }

        // The request URI stays as sent. The path the request is mapped with has no empty, "." or ".." segments, so
        // they can't be used to get around the protection of WEB-INF and META-INF, or out of the web application
        // (Servlet 3.5.2).
        String path = normalizePath(requestUri);
        if (path == null) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, "Request URI goes above the root or has a backslash: {0}", requestUri);
            }
            sendError(response, 400);
            return;
        }

        WebApplication webApplication = requestMapper.findMapping(path);
        if (webApplication == null) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, "No web application found for request URI: {0}", requestUri);
            }
            sendError(response, 404);
            return;
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(webApplication.getClassLoader());
            String contextPath = webApplication.getContextPath();
            request.setContextPath(contextPath);
            request.setServletPath(path.substring(contextPath.length()));
            request.setWebApplication(webApplication);
            response.setWebApplication(webApplication);

            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, "Context Path: {0}", contextPath);
                LOGGER.log(TRACE, "Servlet Path: {0}", request.getServletPath());
            }
            
            webApplication.service(request, response);

            // Make sure the request is fully read wrt parameters (if any still)
            request.getParameterMap();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    /**
     * Normalizes the path of a request: empty segments other than the last one and "." segments are removed, and a
     * ".." segment removes the segment before it. So {@code /a//b/./c/../} becomes {@code /a/b/}. A path that doesn't
     * start with a slash, like {@code *}, stays as it is.
     *
     * @param path the path.
     * @return the normalized path, or null when the path goes above the root or has a backslash, which some file
     * systems take as a separator.
     */
    static String normalizePath(String path) {
        if (path.indexOf('\\') != -1) {
            return null;
        }
        if (!path.startsWith("/")) {
            return path;
        }

        String[] segments = path.split("/", -1);
        List<String> normalized = new ArrayList<>();
        for (int i = 1; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.equals("..")) {
                if (normalized.isEmpty()) {
                    return null;
                }
                normalized.remove(normalized.size() - 1);
            } else if (!segment.equals(".") && (!segment.isEmpty() || i == segments.length - 1)) {
                // An empty last segment is the trailing slash
                normalized.add(segment);
            }
        }
        return "/" + String.join("/", normalized);
    }

    /**
     * Sends an error for a request that no web application handles, which is why the response is flushed here.
     *
     * @param response the response.
     * @param status the status code.
     * @throws IOException when an I/O error occurs.
     */
    private static void sendError(WebApplicationResponse response, int status) throws IOException {
        response.sendError(status);
        response.flushBuffer();
    }

    @Override
    public void setRequestMapper(WebApplicationServerRequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    @Override
    public void start() {
        if (!running) {
            LOGGER.log(DEBUG, "Starting HTTP web application server");
            webApplications.values().forEach(webApp -> {
                ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(webApp.getClassLoader());
                    webApp.start();
                } finally {
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                }
            });
            LOGGER.log(DEBUG, "Started HTTP web application server");
            running = true;
        }
    }

    @Override
    public void stop() {
        if (running) {
            LOGGER.log(DEBUG, "Stopping HTTP web application server");
            webApplications.values().forEach(webApp -> {
                ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(webApp.getClassLoader());
                    webApp.stop();
                } finally {
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                }
            });
            LOGGER.log(DEBUG, "Stopped HTTP web application server");
            running = false;
        }
    }
}
