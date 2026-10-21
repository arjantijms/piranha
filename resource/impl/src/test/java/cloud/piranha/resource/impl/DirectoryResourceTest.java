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
package cloud.piranha.resource.impl;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The JUnit tests for DirectoryResource class.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
class DirectoryResourceTest {

    /**
     * Test getResource method.
     */
    @Test
    void testGetResource() {
        DirectoryResource resource = new DirectoryResource();
        assertNull(resource.getResource("/resource"));
    }

    /**
     * Test getResourceAsStream method.
     */
    @Test
    void testGetResourceAsStream() {
        DirectoryResource resource = new DirectoryResource();
        assertNull(resource.getResourceAsStream("/resource"));
    }

    /**
     * Test getResourceAsStream method.
     */
    @Test
    void testGetResourceAsStream2() {
        DirectoryResource resource = new DirectoryResource(new File("."));
        assertNotNull(resource.getResourceAsStream("pom.xml"));
    }

    /**
     * Test getRootDirectory method, of class DirectoryResource.
     */
    @Test
    void testGetRootDirectory() {
        DirectoryResource resource = new DirectoryResource();
        resource.setRootDirectory(new File("src/main/java"));
        assertNotNull(resource.getRootDirectory());
    }

    /**
     * Test that a location with dot segments resolves within the root directory.
     *
     * @param directory the temporary directory.
     * @throws IOException when an I/O error occurs.
     */
    @Test
    void testDotSegmentsWithinRoot(@TempDir Path directory) throws IOException {
        Path root = Files.createDirectories(directory.resolve("root"));
        Files.writeString(Files.createDirectories(root.resolve("css")).resolve("site.css"), "CSS");
        Files.writeString(root.resolve("index.html"), "INDEX");
        DirectoryResource resource = new DirectoryResource(root.toFile());
        assertNotNull(resource.getResource("/css/../index.html"));
        try (InputStream stream = resource.getResourceAsStream("/./css/../index.html")) {
            assertEquals("INDEX", new String(stream.readAllBytes(), UTF_8));
        }
    }

    /**
     * Test that a location that goes outside the root directory is not found.
     *
     * @param directory the temporary directory.
     * @throws IOException when an I/O error occurs.
     */
    @Test
    void testLocationOutsideRoot(@TempDir Path directory) throws IOException {
        Path root = Files.createDirectories(directory.resolve("root"));
        Files.createDirectories(root.resolve("css"));
        Files.writeString(directory.resolve("outside.txt"), "OUTSIDE");
        DirectoryResource resource = new DirectoryResource(root.toFile());
        for (String location : new String[] { "/../outside.txt", "../outside.txt", "/css/../../outside.txt", "/css\\..\\..\\outside.txt" }) {
            assertNull(resource.getResource(location), location);
            assertNull(resource.getResourceAsStream(location), location);
        }
    }

    /**
     * Test that a resource without a root directory finds nothing, not even an absolute file.
     *
     * @param directory the temporary directory.
     * @throws IOException when an I/O error occurs.
     */
    @Test
    void testNoRootDirectory(@TempDir Path directory) throws IOException {
        Path file = Files.writeString(directory.resolve("file.txt"), "FILE");
        DirectoryResource resource = new DirectoryResource();
        assertNull(resource.getResource(file.toString()));
        assertNull(resource.getResourceAsStream(file.toString()));
    }
}
