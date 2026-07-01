package hr.algebra.nrako.instapound.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IpUtilsTest {

    private IpUtils ipUtils;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        ipUtils = new IpUtils();
    }

    @Test
    void getClientIp_shouldReturnFirstIpFromXForwardedForHeader() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.0.21");
        String result = ipUtils.getClientIp(request);
        assertEquals("192.168.0.21", result);
    }

    @Test
    void getClientIp_shouldReturnRemoteAddressWhenXForwardedForIsNull() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        String result = ipUtils.getClientIp(request);
        assertEquals("127.0.0.1", result);
    }

    @Test
    void getClientIp_shouldReturnRemoteAddressWhenXForwardedForIsEmpty() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        String result = ipUtils.getClientIp(request);
        assertEquals("127.0.0.1", result);
    }
}
