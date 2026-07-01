package hr.algebra.nrako.instapound.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            httpResponse.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self'; img-src 'self' data:; font-src 'self'; " +
                            "connect-src 'self'; frame-ancestors 'self'; form-action 'self';");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            chain.doFilter(request, response);
        }
    }
}
