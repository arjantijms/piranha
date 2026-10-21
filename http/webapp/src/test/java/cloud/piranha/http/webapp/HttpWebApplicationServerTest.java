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

import cloud.piranha.core.impl.DefaultWebApplication;
import cloud.piranha.http.impl.DefaultHttpServer;
import cloud.piranha.http.api.HttpServer;
import cloud.piranha.resource.impl.DirectoryResource;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The JUnit tests for the HttpWebApplicationServer class.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
class HttpWebApplicationServerTest {

    /**
     * Test addMapping method.
     */
    @Test
    void testAddMapping() {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.setServletContextName("mycontext");
        server.addWebApplication(webApp);
        server.addMapping("notthere", "notreal");
        assertEquals(webApp, server.getRequestMapper().findMapping("/notreal"));
    }

    /**
     * Test addMapping method.
     */
    @Test
    void testAddMapping2() {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.setServletContextName("mycontext");
        server.addWebApplication(webApp);
        server.addMapping("mycontext", "mycontextmapping");
        assertEquals(webApp, server.getRequestMapper().findMapping("/mycontextmapping"));
    }

    /**
     * Test addMapping method.
     */
    @Test
    void testAddMapping3() {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.setServletContextName("mycontext");
        server.addWebApplication(webApp);
        server.addMapping("mycontext", "myurlpattern");
        server.addMapping("mycontext", "myurlpattern");
        assertEquals(webApp, server.getRequestMapper().findMapping("/myurlpattern"));
    }

    /**
     * Test getRequestMapper method.
     */
    @Test
    void testGetRequestMapper() {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        server.setRequestMapper(new HttpWebApplicationServerRequestMapper());
        assertNotNull(server.getRequestMapper());
    }
    
    /**
     * Test process method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testProcess() throws Exception {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        HttpServer httpServer = new DefaultHttpServer(8180, server, false);
        DefaultWebApplication application = new DefaultWebApplication();
        application.setContextPath("/context");
        application.addServlet("snoop", new TestSnoopServlet());
        application.addServletMapping("snoop", "/snoop/*");
        server.addWebApplication(application);
        server.initialize();
        server.start();
        httpServer.start();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:8180/context/snoop/index.html")).build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            assertEquals(200, response.statusCode());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        httpServer.stop();
        server.stop();
    }

    @Test
    void testSessionUrlRewriting() throws Exception {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        HttpServer httpServer = new DefaultHttpServer(8181, server, false);
        DefaultWebApplication application = new DefaultWebApplication();
        application.setContextPath("/context");
        application.addServlet("snoop", new TestSnoopServlet());
        application.addServletMapping("snoop", "/snoop/*");
        server.addWebApplication(application);
        server.initialize();
        server.start();
        httpServer.start();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:8181/context/snoop/index.html;jsessionid=customsessionid")).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("customsessionid"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        httpServer.stop();
        server.stop();
    }

    @Test
    void testSessionUrlRewriting2() throws Exception {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        HttpServer httpServer = new DefaultHttpServer(8182, server, false);
        DefaultWebApplication application = new DefaultWebApplication();
        application.setContextPath("/context");
        application.addServlet("snoop", new TestSnoopServlet());
        application.addServletMapping("snoop", "/snoop/*");
        server.addWebApplication(application);
        server.initialize();
        server.start();
        httpServer.start();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:8182/context/snoop/index.html;jsessionid=sessionIdURL"))
                .headers("Cookie", "JSESSIONID=sessionIdCookie")
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("sessionIdCookie"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        httpServer.stop();
        server.stop();
    }

    /**
     * Test that empty path segments (other than the last) are removed before the request is mapped, as in the URI path
     * canonicalization of the Servlet specification (3.5.2).
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testEmptySegmentsAreRemoved() throws Exception {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        HttpServer httpServer = new DefaultHttpServer(8183, server, false);
        DefaultWebApplication application = new DefaultWebApplication();
        application.setContextPath("/context");
        application.addServlet("snoop", new TestSnoopServlet());
        application.addServletMapping("snoop", "/snoop/*");
        server.addWebApplication(application);
        server.initialize();
        server.start();
        httpServer.start();
        try {
            assertEquals("/context", get("http://localhost:8183/context//snoop//index.html?getContextPath"));
            assertEquals("/snoop", get("http://localhost:8183/context//snoop//index.html?getServletPath"));
            assertEquals("/index.html", get("http://localhost:8183/context//snoop//index.html?getPathInfo"));
            assertEquals("/index/", get("http://localhost:8183/context/snoop//index//?getPathInfo"));
        } finally {
            httpServer.stop();
            server.stop();
        }
    }

    /**
     * Test that dot segments are removed before the request is mapped, so they can't be used to get to WEB-INF,
     * META-INF or files outside the web application, and that a path that goes above the root is rejected.
     *
     * @param directory the temporary directory.
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testDotSegmentsAreNormalized(@TempDir Path directory) throws Exception {
        Path root = Files.createDirectories(directory.resolve("webapp"));
        Files.writeString(root.resolve("index.html"), "INDEX");
        Files.writeString(Files.createDirectories(root.resolve("css")).resolve("site.css"), "CSS");
        Files.writeString(Files.createDirectories(root.resolve("WEB-INF")).resolve("secret.txt"), "SECRET");
        Files.writeString(Files.createDirectories(root.resolve("META-INF")).resolve("secret.txt"), "META");
        Files.writeString(directory.resolve("outside.txt"), "OUTSIDE");

        HttpWebApplicationServer server = new HttpWebApplicationServer();
        HttpServer httpServer = new DefaultHttpServer(8184, server, false);
        DefaultWebApplication application = new DefaultWebApplication();
        application.setContextPath("");
        application.addResource(new DirectoryResource(root.toFile()));
        server.addWebApplication(application);
        server.initialize();
        server.start();
        httpServer.start();
        try {
            assertEquals("200 INDEX", rawGet(8184, "/css/../index.html"));
            assertEquals("200 INDEX", rawGet(8184, "/./index.html"));
            assertEquals(404, status(rawGet(8184, "/./WEB-INF/secret.txt")));
            assertEquals(404, status(rawGet(8184, "/css/../WEB-INF/secret.txt")));
            assertEquals(404, status(rawGet(8184, "/./META-INF/secret.txt")));
            assertEquals(400, status(rawGet(8184, "/../outside.txt")));
            assertEquals(400, status(rawGet(8184, "/css/../../outside.txt")));
            assertEquals(400, status(rawGet(8184, "/css\\..\\WEB-INF\\secret.txt")));
        } finally {
            httpServer.stop();
            server.stop();
        }
    }

    /**
     * Test that a request that maps to no web application gets a 404 response.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testNoWebApplication() throws Exception {
        HttpWebApplicationServer server = new HttpWebApplicationServer();
        HttpServer httpServer = new DefaultHttpServer(8185, server, false);
        DefaultWebApplication application = new DefaultWebApplication();
        application.setContextPath("/context");
        server.addWebApplication(application);
        server.initialize();
        server.start();
        httpServer.start();
        try {
            assertEquals(404, status(rawGet(8185, "/other/index.html")));
        } finally {
            httpServer.stop();
            server.stop();
        }
    }

    private static String get(String uri) throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(new URI(uri)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return response.body().strip();
    }

    /**
     * Sends a GET request with the path exactly as given, which an HTTP client might normalize otherwise.
     *
     * @param port the port.
     * @param path the path.
     * @return the status code, and the body when the status is 200, separated by a space.
     * @throws IOException when an I/O error occurs.
     */
    private static String rawGet(int port, String path) throws IOException {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(10000);
            socket.getOutputStream().write(("GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n").getBytes(US_ASCII));
            socket.getOutputStream().flush();
            String response = new String(socket.getInputStream().readAllBytes(), UTF_8);
            if (response.isEmpty()) {
                return "no response";
            }
            String[] headAndBody = response.split("\r?\n\r?\n", 2);
            String statusCode = headAndBody[0].lines().findFirst().orElseThrow().split(" ")[1];
            return statusCode.equals("200") && headAndBody.length > 1 ? statusCode + " " + headAndBody[1].strip() : statusCode;
        }
    }

    private static int status(String response) {
        return response.equals("no response") ? -1 : Integer.parseInt(response.split(" ")[0]);
    }
}
