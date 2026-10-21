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
package cloud.piranha.resource.shrinkwrap;

import static java.util.Collections.list;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import cloud.piranha.resource.impl.DefaultResourceManager;

/**
 * The JUnit tests for the ShrinkWrapResource class.
 */
class ShrinkWrapResourceTest {

    /**
     * A jar in WEB-INF/lib as Piranha Micro imports it: one without a manifest, as ShrinkWrap creates it.
     */
    private static ShrinkWrapResource createJarResource() {
        JavaArchive jar = create(JavaArchive.class, "taglib.jar")
                .addAsResource(new StringAsset("<taglib/>"), "META-INF/taglib.tld");

        return new ShrinkWrapResource(
                create(ZipImporter.class, "taglib.jar")
                        .importFrom(jar.as(ZipExporter.class).exportAsInputStream())
                        .as(JavaArchive.class));
    }

    /**
     * Test getResource method for a directory named with a trailing slash, as
     * ClassLoader.getResources("META-INF/") names it.
     */
    @Test
    void testGetResourceDirectoryWithTrailingSlash() {
        ShrinkWrapResource resource = createJarResource();

        URL url = resource.getResource("META-INF/");
        assertNotNull(url);
        assertEquals("shrinkwrap://taglib.jar/META-INF/", url.toString());
        assertNotNull(resource.getResource("META-INF"));
    }

    /**
     * Test getResource method for a file named with a trailing slash, which
     * names a directory that isn't there.
     */
    @Test
    void testGetResourceFileWithTrailingSlash() {
        ShrinkWrapResource resource = createJarResource();

        assertNotNull(resource.getResource("META-INF/taglib.tld"));
        assertNull(resource.getResource("META-INF/taglib.tld/"));
    }

    /**
     * Test getResourceAsStream method for the root of the archive, which gives
     * the archive as a zip, as the URL of a jar gives the jar.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testGetResourceAsStreamRootIsZip() throws Exception {
        ShrinkWrapResource resource = createJarResource();

        List<String> entryNames = new ArrayList<>();
        try (InputStream stream = resource.getResourceAsStream("/");
                ZipInputStream zip = new ZipInputStream(stream)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                entryNames.add(entry.getName());
            }
        }

        assertTrue(entryNames.contains("META-INF/taglib.tld"), "META-INF/taglib.tld not in " + entryNames);
    }

    /**
     * Test getResourceAsStream method for the root of the archive, read as a
     * zip input stream, as a class path scanner reads it: it lists the entries
     * of the archive.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testGetResourceAsStreamRootListsEntries() throws Exception {
        ShrinkWrapResource resource = createJarResource();

        List<String> entryNames = new ArrayList<>();
        try (ZipInputStream zip = (ZipInputStream) resource.getResourceAsStream("/")) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                entryNames.add(entry.getName());
            }
        }

        assertTrue(entryNames.contains("/META-INF/taglib.tld"), "/META-INF/taglib.tld not in " + entryNames);
    }

    /**
     * Test getResource method for the root of the archive, whose URL opens as
     * the archive as a zip, as getResourceAsStream gives it.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testGetResourceRootOpensAsZip() throws Exception {
        ShrinkWrapResource resource = createJarResource();

        List<String> entryNames = new ArrayList<>();
        try (InputStream stream = resource.getResource("/").openStream();
                ZipInputStream zip = new ZipInputStream(stream)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                entryNames.add(entry.getName());
            }
        }

        assertTrue(entryNames.contains("META-INF/taglib.tld"), "META-INF/taglib.tld not in " + entryNames);
    }

    /**
     * Test that the class loader finds the META-INF directory of a jar without
     * a manifest, as a class path scanner looks for it.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testClassLoaderGetResourcesDirectory() throws Exception {
        DefaultResourceManager manager = new DefaultResourceManager();
        manager.addResource(createJarResource());

        IsolatingResourceManagerClassLoader classLoader = new IsolatingResourceManagerClassLoader("test");
        classLoader.setResourceManager(manager);

        List<URL> urls = list(classLoader.getResources("META-INF/"));
        assertTrue(urls.stream().anyMatch(url -> url.toString().equals("shrinkwrap://taglib.jar/META-INF/")),
                "META-INF/ of taglib.jar not found in " + urls);
    }
}
