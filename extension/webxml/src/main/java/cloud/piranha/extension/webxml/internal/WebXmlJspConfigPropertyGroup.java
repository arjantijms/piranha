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

import java.util.List;

/**
 * A jsp-property-group inside jsp-config of web.xml/web-fragment.xml.
 *
 * <p>
 * Each value is the text of the element of the same name, or null when that element is absent.
 *
 * @param urlPatterns the URL patterns.
 * @param elIgnored the el-ignored value.
 * @param errorOnELNotFound the error-on-el-not-found value.
 * @param pageEncoding the page-encoding value.
 * @param scriptingInvalid the scripting-invalid value.
 * @param isXml the is-xml value.
 * @param includePreludes the include-prelude values.
 * @param includeCodas the include-coda values.
 * @param deferredSyntaxAllowedAsLiteral the deferred-syntax-allowed-as-literal value.
 * @param trimDirectiveWhitespaces the trim-directive-whitespaces value.
 * @param defaultContentType the default-content-type value.
 * @param buffer the buffer value.
 * @param errorOnUndeclaredNamespace the error-on-undeclared-namespace value.
 * @author Arjan Tijms
 */
public record WebXmlJspConfigPropertyGroup(
        List<String> urlPatterns,
        String elIgnored,
        String errorOnELNotFound,
        String pageEncoding,
        String scriptingInvalid,
        String isXml,
        List<String> includePreludes,
        List<String> includeCodas,
        String deferredSyntaxAllowedAsLiteral,
        String trimDirectiveWhitespaces,
        String defaultContentType,
        String buffer,
        String errorOnUndeclaredNamespace) {

}
