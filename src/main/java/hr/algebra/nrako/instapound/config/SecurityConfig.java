package hr.algebra.nrako.instapound.config;

import hr.algebra.nrako.instapound.service.implementations.CustomOAuth2UserServiceImpl;
import hr.algebra.nrako.instapound.service.implementations.CustomUserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final CustomOAuth2UserServiceImpl oAuth2UserService;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/photos/browse/**", "/api/photos/search/**",
                                "/api/photos/file/**", "/api/photos/{id}", "/api/user/packages", "/h2-console/**",
                                "/error", "/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/photos/upload/**", "/api/photos/edit/**", "/api/user/package/**")
                            .hasAnyRole("REGISTERED", "ADMIN")
                        .anyRequest().authenticated()
                ).formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .defaultSuccessUrl("/api/user/profile")
                        .permitAll()
                ).logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                ).headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        // Enable OAuth2 login only if client registrations are configured
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    .defaultSuccessUrl("/api/user/profile")
            );
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
