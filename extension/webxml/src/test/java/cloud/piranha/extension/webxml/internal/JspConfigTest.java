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
package cloud.piranha.extension.webxml.internal;

import cloud.piranha.core.impl.DefaultWebApplication;
import cloud.piranha.resource.impl.DirectoryResource;
import cloud.piranha.extension.wasp.WaspJspManagerInitializer;
import cloud.piranha.extension.webxml.WebXmlInitializer;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests testing web.xml &lt;jsp-config&gt;.
 * 
 * @author Manfred Riem (mriem@manorrock.com)
 */
class JspConfigTest {

    /**
     * Test getJspConfig method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void tetGetJspConfig() throws Exception {
        DefaultWebApplication webApplication = new DefaultWebApplication();
        webApplication.addResource(new DirectoryResource(new File("src/test/webxml/jspConfig")));
        webApplication.addInitializer(new WaspJspManagerInitializer());
        webApplication.addInitializer(new WebXmlInitializer());
        webApplication.initialize();
        assertNotNull(webApplication.getJspConfigDescriptor());
        JspConfigDescriptor descriptor = webApplication.getJspConfigDescriptor();
        assertFalse(descriptor.getTaglibs().isEmpty());
    }

    /**
     * Test that all taglibs and all jsp-property-groups are made available, with the values of their elements.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testGetJspPropertyGroups() throws Exception {
        DefaultWebApplication webApplication = new DefaultWebApplication();
        webApplication.addResource(new DirectoryResource(new File("src/test/webxml/jspPropertyGroup")));
        webApplication.addInitializer(new WaspJspManagerInitializer());
        webApplication.addInitializer(new WebXmlInitializer());
        webApplication.initialize();
        JspConfigDescriptor descriptor = webApplication.getJspConfigDescriptor();
        assertNotNull(descriptor);

        List<TaglibDescriptor> taglibs = List.copyOf(descriptor.getTaglibs());
        assertEquals(2, taglibs.size());
        assertEquals("http://tag.lib/one", taglibs.get(0).getTaglibURI());
        assertEquals("/WEB-INF/one.tld", taglibs.get(0).getTaglibLocation());
        assertEquals("http://tag.lib/two", taglibs.get(1).getTaglibURI());
        assertEquals("/WEB-INF/two.tld", taglibs.get(1).getTaglibLocation());

        List<JspPropertyGroupDescriptor> propertyGroups = List.copyOf(descriptor.getJspPropertyGroups());
        assertEquals(2, propertyGroups.size());

        JspPropertyGroupDescriptor allElements = propertyGroups.get(0);
        assertEquals(List.of("*.xsp", "/xml/*"), List.copyOf(allElements.getUrlPatterns()));
        assertEquals("true", allElements.getElIgnored());
        assertEquals("true", allElements.getErrorOnELNotFound());
        assertEquals("UTF-8", allElements.getPageEncoding());
        assertEquals("true", allElements.getScriptingInvalid());
        assertEquals("true", allElements.getIsXml());
        assertEquals(List.of("/WEB-INF/prelude1.jspf", "/WEB-INF/prelude2.jspf"), List.copyOf(allElements.getIncludePreludes()));
        assertEquals(List.of("/WEB-INF/coda.jspf"), List.copyOf(allElements.getIncludeCodas()));
        assertEquals("true", allElements.getDeferredSyntaxAllowedAsLiteral());
        assertEquals("true", allElements.getTrimDirectiveWhitespaces());
        assertEquals("text/xml", allElements.getDefaultContentType());
        assertEquals("16kb", allElements.getBuffer());
        assertEquals("true", allElements.getErrorOnUndeclaredNamespace());

        JspPropertyGroupDescriptor minimal = propertyGroups.get(1);
        assertEquals(List.of("/minimal/*"), List.copyOf(minimal.getUrlPatterns()));
        assertNull(minimal.getElIgnored());
        assertNull(minimal.getErrorOnELNotFound());
        assertNull(minimal.getPageEncoding());
        assertNull(minimal.getScriptingInvalid());
        assertNull(minimal.getIsXml());
        assertTrue(minimal.getIncludePreludes().isEmpty());
        assertTrue(minimal.getIncludeCodas().isEmpty());
        assertNull(minimal.getDeferredSyntaxAllowedAsLiteral());
        assertNull(minimal.getTrimDirectiveWhitespaces());
        assertNull(minimal.getDefaultContentType());
        assertNull(minimal.getBuffer());
        assertNull(minimal.getErrorOnUndeclaredNamespace());
    }
}
