package com.ikeu.components.autoconfigure.xss;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XssRequestWrapperTest {

    @Test
    void escapeMode_shouldHtmlEncodeScriptTags() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.addParameter("name", "<script>alert('xss')</script>");

        XssRequestWrapper wrapper = new XssRequestWrapper(mock, XssProperties.XssMode.ESCAPE);
        String result = wrapper.getParameter("name");

        assertTrue(result.contains("&lt;script&gt;"), "Should encode <script>");
        assertFalse(result.contains("<script>"), "Should not contain raw tags");
    }

    @Test
    void stripMode_shouldRemoveHtmlTags() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.addParameter("name", "<script>alert('xss')</script>");

        XssRequestWrapper wrapper = new XssRequestWrapper(mock, XssProperties.XssMode.STRIP);
        String result = wrapper.getParameter("name");

        assertEquals("alert('xss')", result);
    }

    @Test
    void safeValue_shouldPassUnchanged() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.addParameter("name", "John Doe");

        XssRequestWrapper wrapper = new XssRequestWrapper(mock, XssProperties.XssMode.ESCAPE);
        assertEquals("John Doe", wrapper.getParameter("name"));
    }

    @Test
    void nullParameter_shouldReturnNull() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        XssRequestWrapper wrapper = new XssRequestWrapper(mock, XssProperties.XssMode.ESCAPE);
        assertNull(wrapper.getParameter("nonexistent"));
    }

    @Test
    void getParameterMap_shouldReturnSanitizedValues() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.addParameter("name", "John");
        mock.addParameter("desc", "<b>bold</b>");

        XssRequestWrapper wrapper = new XssRequestWrapper(mock, XssProperties.XssMode.ESCAPE);
        Map<String, String[]> result = wrapper.getParameterMap();

        assertEquals("John", result.get("name")[0]);
        assertTrue(result.get("desc")[0].contains("&lt;"));
    }

    @Test
    void header_shouldBeSanitized() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.addHeader("X-Custom", "<script>evil</script>");

        XssRequestWrapper wrapper = new XssRequestWrapper(mock, XssProperties.XssMode.ESCAPE);
        String result = wrapper.getHeader("X-Custom");

        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("&lt;"));
    }
}
