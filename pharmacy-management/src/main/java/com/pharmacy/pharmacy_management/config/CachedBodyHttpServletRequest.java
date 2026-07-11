package com.pharmacy.pharmacy_management.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * BUG FIX — replaces ContentCachingRequestWrapper in LoginRateLimitFilter.
 *
 * ContentCachingRequestWrapper only supports "let something downstream read
 * the body once, then inspect a cached copy afterward" (e.g. for logging
 * after the fact). It does NOT support "read the body here in the filter,
 * then let the controller read it again" — once the filter drains the
 * stream, it's exhausted, and any later getInputStream() call returns
 * nothing. That silently broke every /api/auth/login request: the filter
 * consumed the body to extract the username, then the controller's
 * @RequestBody got an empty body and authentication failed regardless of
 * the credentials submitted.
 *
 * This wrapper reads the body into memory once, then hands out a brand new
 * ByteArrayInputStream from that buffer every time getInputStream() is
 * called — so the filter and the controller can each read the full body
 * independently, as many times as needed.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        CachedBodyServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // not needed for synchronous servlet processing
        }

        @Override
        public int read() {
            return buffer.read();
        }
    }
}
