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
package cloud.piranha.extension.micro;

import cloud.piranha.core.api.WebApplicationExtension;
import cloud.piranha.core.api.WebApplicationExtensionContext;
import cloud.piranha.extension.security.jakarta.JakartaSecurityExtension;
import cloud.piranha.extension.wasp.WaspExtension;
import cloud.piranha.extension.wasp.WaspJspManagerExtension;
import cloud.piranha.extension.webxml.WebXmlExtension;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the MicroExtension class.
 */
class MicroExtensionTest {

    /**
     * Test that the WaSP JSP manager is added before web.xml is processed, as
     * web.xml registers a servlet declared with a jsp-file through it.
     */
    @Test
    void testJspManagerBeforeWebXml() {
        List<Class<? extends WebApplicationExtension>> extensions = getExtensions();
        int jspManager = extensions.indexOf(WaspJspManagerExtension.class);
        int webXml = extensions.indexOf(WebXmlExtension.class);
        assertTrue(jspManager >= 0, "WaspJspManagerExtension is missing");
        assertTrue(jspManager < webXml, "WaspJspManagerExtension comes after WebXmlExtension");
    }

    /**
     * Test that WaSP is added before Jakarta Security, whose initializer starts Weld: Weld adds its EL resolver, which
     * resolves bean names in the EL of pages, only when WaSP's JspFactory is there already.
     */
    @Test
    void testWaspBeforeWeld() {
        List<Class<? extends WebApplicationExtension>> extensions = getExtensions();
        int wasp = extensions.indexOf(WaspExtension.class);
        int security = extensions.indexOf(JakartaSecurityExtension.class);
        assertTrue(wasp >= 0, "WaspExtension is missing");
        assertTrue(wasp < security, "WaspExtension comes after JakartaSecurityExtension");
    }

    private static List<Class<? extends WebApplicationExtension>> getExtensions() {
        List<Class<? extends WebApplicationExtension>> extensions = new ArrayList<>();
        new MicroExtension().extend(new WebApplicationExtensionContext() {
            @Override
            public void add(Class<? extends WebApplicationExtension> extension) {
                extensions.add(extension);
            }

            @Override
            public void add(WebApplicationExtension extension) {
                extensions.add(extension.getClass());
            }

            @Override
            public void remove(Class<? extends WebApplicationExtension> extension) {
                extensions.remove(extension);
            }
        });
        return extensions;
    }
}
