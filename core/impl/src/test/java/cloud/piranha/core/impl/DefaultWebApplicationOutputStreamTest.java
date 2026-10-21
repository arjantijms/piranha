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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import static jakarta.servlet.http.HttpServletResponse.SC_SWITCHING_PROTOCOLS;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the DefaultWebApplicationResponse class.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
class DefaultWebApplicationOutputStreamTest {

    /**
     * Test isReady method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testIsReady() throws Exception {
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponse();
        ServletOutputStream outputStream = response.getOutputStream();
        assertTrue(outputStream.isReady());
    }

    /**
     * Test setWriteListener method.
     */
    @Test
    void testWriteListener() throws Exception {
        DefaultWebApplication webApplication = new DefaultWebApplication();
        DefaultWebApplicationRequest request = new DefaultWebApplicationRequest();
        request.setWebApplication(webApplication);
        request.setUpgraded(true);
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponse();
        response.setStatus(SC_SWITCHING_PROTOCOLS);
        webApplication.linkRequestAndResponse(request, response);
        response.setWebApplication(webApplication);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.setWriteListener(new WriteListener() {
            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onWritePossible() throws IOException {
            }
        });
        assertNotNull(response.getWebApplicationOutputStream().getWriteListener());
    }

    /**
     * Test that what's written after the buffer was first flushed is buffered still: it reaches the underlying output
     * stream in parts of the buffer size, not byte by byte, and all of it by flushBuffer.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testWriteAfterFirstFlushIsBuffered() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        byte[] content = new byte[3 * response.getBufferSize() + 100];
        Arrays.fill(content, (byte) 'x');

        response.getOutputStream().write(content);
        assertTrue(response.isCommitted());
        response.flushBuffer();

        assertArrayEquals(content, underlying.bytes.toByteArray());
        assertTrue(underlying.writes <= 4, "writes to the underlying output stream: " + underlying.writes);
    }

    /**
     * Test that the response is committed when its buffer is full, after as many bytes as the buffer holds.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testCommittedWhenBufferIsFull() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        ServletOutputStream outputStream = response.getOutputStream();
        for (int i = 1; i < response.getBufferSize(); i++) {
            outputStream.write('x');
        }
        assertFalse(response.isCommitted());
        outputStream.write('x');
        assertTrue(response.isCommitted());
    }

    /**
     * Test that what was written before resetBuffer isn't written out, not even as zeros.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testResetBuffer() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write("before".getBytes());
        response.resetBuffer();
        outputStream.write("after".getBytes());
        response.flushBuffer();
        assertEquals("after", underlying.bytes.toString());
    }

    /**
     * Test that a write to the output stream reaches the underlying output stream through flushBuffer after the
     * response was committed.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testFlushBufferAfterCommit() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write("committed".getBytes());
        response.flushBuffer();
        outputStream.write(" and more".getBytes());
        response.flushBuffer();
        assertEquals("committed and more", underlying.bytes.toString());
    }

    /**
     * Test that a write through the writer reaches the underlying output stream through flushBuffer after the
     * response was committed.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testFlushBufferAfterCommitWithWriter() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        response.getWriter().write("committed");
        response.flushBuffer();
        response.getWriter().write(" and more");
        response.flushBuffer();
        assertEquals("committed and more", underlying.bytes.toString());
    }

    /**
     * Test that what's written on an upgraded connection reaches the underlying output stream without a flush.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testUpgradedConnectionWritesThrough() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        response.setStatus(SC_SWITCHING_PROTOCOLS);
        response.flushBuffer();
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write("frame".getBytes());
        outputStream.write('!');
        assertEquals("frame!", underlying.bytes.toString());
    }

    /**
     * Test that a Server-Sent Event written after flushBuffer is all that reaches the underlying output stream.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testServerSentEventAfterFlushBuffer() throws Exception {
        CountingOutputStream underlying = new CountingOutputStream();
        DefaultWebApplicationResponse response = new DefaultWebApplicationResponseBuilder()
                .bodyOnly(true)
                .underlyingOutputStream(underlying)
                .build();
        response.setContentType("text/event-stream");
        response.flushBuffer();
        response.getOutputStream().write("data: event\n\n".getBytes());
        assertEquals("data: event\n\n", underlying.bytes.toString());
    }

    /**
     * An output stream that keeps what's written to it, and counts the writes.
     */
    static class CountingOutputStream extends OutputStream {

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        int writes;

        @Override
        public void write(int integer) {
            writes++;
            bytes.write(integer);
        }

        @Override
        public void write(byte[] content, int offset, int length) {
            writes++;
            bytes.write(content, offset, length);
        }
    }
}
