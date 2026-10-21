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
package cloud.piranha.core.impl;

import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The default JspPropertyGroupDescriptor.
 *
 * <p>
 * Each value is the one of the element of the same name in the jsp-property-group, or null when that element is
 * absent.
 *
 * @author Arjan Tijms
 */
public class DefaultJspPropertyGroupDescriptor implements JspPropertyGroupDescriptor {

    /**
     * Stores the URL patterns.
     */
    private List<String> urlPatterns = new ArrayList<>();

    /**
     * Stores the el-ignored value.
     */
    private String elIgnored;

    /**
     * Stores the error-on-el-not-found value.
     */
    private String errorOnELNotFound;

    /**
     * Stores the page-encoding value.
     */
    private String pageEncoding;

    /**
     * Stores the scripting-invalid value.
     */
    private String scriptingInvalid;

    /**
     * Stores the is-xml value.
     */
    private String isXml;

    /**
     * Stores the include-prelude values.
     */
    private List<String> includePreludes = new ArrayList<>();

    /**
     * Stores the include-coda values.
     */
    private List<String> includeCodas = new ArrayList<>();

    /**
     * Stores the deferred-syntax-allowed-as-literal value.
     */
    private String deferredSyntaxAllowedAsLiteral;

    /**
     * Stores the trim-directive-whitespaces value.
     */
    private String trimDirectiveWhitespaces;

    /**
     * Stores the default-content-type value.
     */
    private String defaultContentType;

    /**
     * Stores the buffer value.
     */
    private String buffer;

    /**
     * Stores the error-on-undeclared-namespace value.
     */
    private String errorOnUndeclaredNamespace;

    /**
     * Constructor.
     */
    public DefaultJspPropertyGroupDescriptor() {
    }

    @Override
    public Collection<String> getUrlPatterns() {
        return urlPatterns;
    }

    @Override
    public String getElIgnored() {
        return elIgnored;
    }

    @Override
    public String getErrorOnELNotFound() {
        return errorOnELNotFound;
    }

    @Override
    public String getPageEncoding() {
        return pageEncoding;
    }

    @Override
    public String getScriptingInvalid() {
        return scriptingInvalid;
    }

    @Override
    public String getIsXml() {
        return isXml;
    }

    @Override
    public Collection<String> getIncludePreludes() {
        return includePreludes;
    }

    @Override
    public Collection<String> getIncludeCodas() {
        return includeCodas;
    }

    @Override
    public String getDeferredSyntaxAllowedAsLiteral() {
        return deferredSyntaxAllowedAsLiteral;
    }

    @Override
    public String getTrimDirectiveWhitespaces() {
        return trimDirectiveWhitespaces;
    }

    @Override
    public String getDefaultContentType() {
        return defaultContentType;
    }

    @Override
    public String getBuffer() {
        return buffer;
    }

    @Override
    public String getErrorOnUndeclaredNamespace() {
        return errorOnUndeclaredNamespace;
    }

    /**
     * Set the el-ignored value.
     *
     * @param elIgnored the el-ignored value.
     */
    public void setElIgnored(String elIgnored) {
        this.elIgnored = elIgnored;
    }

    /**
     * Set the error-on-el-not-found value.
     *
     * @param errorOnELNotFound the error-on-el-not-found value.
     */
    public void setErrorOnELNotFound(String errorOnELNotFound) {
        this.errorOnELNotFound = errorOnELNotFound;
    }

    /**
     * Set the page-encoding value.
     *
     * @param pageEncoding the page-encoding value.
     */
    public void setPageEncoding(String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }

    /**
     * Set the scripting-invalid value.
     *
     * @param scriptingInvalid the scripting-invalid value.
     */
    public void setScriptingInvalid(String scriptingInvalid) {
        this.scriptingInvalid = scriptingInvalid;
    }

    /**
     * Set the is-xml value.
     *
     * @param isXml the is-xml value.
     */
    public void setIsXml(String isXml) {
        this.isXml = isXml;
    }

    /**
     * Set the deferred-syntax-allowed-as-literal value.
     *
     * @param deferredSyntaxAllowedAsLiteral the deferred-syntax-allowed-as-literal value.
     */
    public void setDeferredSyntaxAllowedAsLiteral(String deferredSyntaxAllowedAsLiteral) {
        this.deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
    }

    /**
     * Set the trim-directive-whitespaces value.
     *
     * @param trimDirectiveWhitespaces the trim-directive-whitespaces value.
     */
    public void setTrimDirectiveWhitespaces(String trimDirectiveWhitespaces) {
        this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
    }

    /**
     * Set the default-content-type value.
     *
     * @param defaultContentType the default-content-type value.
     */
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    /**
     * Set the buffer value.
     *
     * @param buffer the buffer value.
     */
    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    /**
     * Set the error-on-undeclared-namespace value.
     *
     * @param errorOnUndeclaredNamespace the error-on-undeclared-namespace value.
     */
    public void setErrorOnUndeclaredNamespace(String errorOnUndeclaredNamespace) {
        this.errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
    }
}
