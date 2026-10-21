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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Input stream for reading from the root of an archive.
 *
 * <p>
 * Read as a {@link java.util.zip.ZipInputStream}, it lists the entries of the
 * archive, as {@link ShrinkWrapDirectoryInputStream} does for a directory; a
 * class path scanner reads it that way. Read as bytes, it's the archive as a
 * zip, as the URL of a jar gives the jar; the JDK reads it that way to open
 * {@code jar:shrinkwrap://x.jar!/}. The archive is only exported when its bytes
 * are read.
 * </p>
 */
public class ShrinkWrapArchiveInputStream extends ShrinkWrapDirectoryInputStream {

    /**
     * Stores the archive.
     */
    private final Archive<?> archive;

    /**
     * Stores the archive as a zip, once exported.
     */
    private InputStream zip;

    /**
     * Constructor.
     *
     * @param archive the archive.
     * @param nodes the nodes of the archive.
     */
    public ShrinkWrapArchiveInputStream(Archive<?> archive, Collection<Node> nodes) {
        super(nodes);
        this.archive = archive;
    }

    private InputStream zip() {
        if (zip == null) {
            zip = archive.as(ZipExporter.class).exportAsInputStream();
        }
        return zip;
    }

    @Override
    public int read() throws IOException {
        return zip().read();
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        return zip().read(bytes, offset, length);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return zip().readAllBytes();
    }

    @Override
    public byte[] readNBytes(int length) throws IOException {
        return zip().readNBytes(length);
    }

    @Override
    public int readNBytes(byte[] bytes, int offset, int length) throws IOException {
        return zip().readNBytes(bytes, offset, length);
    }

    @Override
    public long skip(long n) throws IOException {
        return zip().skip(n);
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return zip().transferTo(out);
    }

    @Override
    public void close() throws IOException {
        try {
            if (zip != null) {
                zip.close();
            }
        } finally {
            super.close();
        }
    }
}
