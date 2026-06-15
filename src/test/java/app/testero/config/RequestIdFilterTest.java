package app.testero.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    @DisplayName("generates request ID and sets it in response header and MDC")
    void generatesRequestId() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var mdcCapture = new AtomicReference<String>();

        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req,
                                 jakarta.servlet.ServletResponse res) {
                mdcCapture.set(MDC.get(RequestIdFilter.MDC_KEY));
            }
        };

        filter.doFilterInternal(request, response, chain);

        String headerValue = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(headerValue).isNotBlank();
        assertThat(mdcCapture.get()).isEqualTo(headerValue);
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("reuses incoming X-Request-Id header")
    void reusesIncomingRequestId() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "custom-id-123");
        var response = new MockHttpServletResponse();
        var mdcCapture = new AtomicReference<String>();

        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req,
                                 jakarta.servlet.ServletResponse res) {
                mdcCapture.set(MDC.get(RequestIdFilter.MDC_KEY));
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("custom-id-123");
        assertThat(mdcCapture.get()).isEqualTo("custom-id-123");
    }

    @Test
    @DisplayName("clears MDC even when filter chain throws")
    void clearsMdcOnException() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req,
                                 jakarta.servlet.ServletResponse res) throws ServletException {
                throw new ServletException("boom");
            }
        };

        try {
            filter.doFilterInternal(request, response, chain);
        } catch (Exception ignored) {
            // expected
        }

        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}
