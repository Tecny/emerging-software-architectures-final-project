package com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.configuration;

import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


import java.util.List;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final BearerTokenService tokenService;
    private final BCryptHashingService hashingService;
    private final AuthenticationEntryPoint unauthorizedRequestHandler;

    @Bean
    public BearerAuthorizationRequestFilter authorizationRequestFilter() {
        return new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(hashingService);
        return authenticationProvider;
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return hashingService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(configurer -> configurer.configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000", "http://localhost:4200", "http://localhost:8180"));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            cors.setAllowedHeaders(List.of("*"));
            cors.setAllowCredentials(true);
            return cors;
        }));

        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        http.csrf(csrfConfigurer -> csrfConfigurer
                        .csrfTokenRepository(csrfTokenRepository)
                        .ignoringRequestMatchers(request ->
                                request.getRequestURI().startsWith("/api/v1/authentication/") ||
                                        (request.getRequestURI().equals("/api/v1/reservations/use-qr-token") && request.getMethod().equalsIgnoreCase("POST")) ||
                                        (request.getRequestURI().equals("/api/v1/users/sign-up") && request.getMethod().equalsIgnoreCase("POST")) ||
                                        (request.getRequestURI().equals("/api/v1/recover-password/forgot-password") && request.getMethod().equalsIgnoreCase("POST")) ||
                                        (request.getRequestURI().equals("/api/v1/recover-password/reset-password") && request.getMethod().equalsIgnoreCase("POST")) ||
                                        request.getRequestURI().startsWith("/api/v1/users/") ||
                                        request.getRequestURI().startsWith("/api/v1/subscriptions/") ||
                                        request.getRequestURI().startsWith("/api/v1/deposit/") ||
                                        request.getRequestURI().startsWith("/api/v1/deposit/create-deposit") ||
                                        request.getRequestURI().startsWith("/api/v1/users/name/") ||
                                        request.getRequestURI().startsWith("/api/v1/users/email/") ||
                                        request.getRequestURI().startsWith("/api/v1/users/password/") ||
                                        request.getRequestURI().startsWith("/api/v1/payments/") ||
                                        request.getRequestURI().startsWith("/api/v1/rooms/") ||
                                        request.getRequestURI().startsWith("/api/v1/chat/") ||
                                        request.getRequestURI().startsWith("/api/v1/player-lists") ||
                                        request.getRequestURI().startsWith("/api/v1/sport-spaces/") ||
                                        request.getRequestURI().startsWith("/api/v1/sport-spaces") ||
                                        request.getRequestURI().startsWith("/api/v1/reservations") ||
                                        request.getRequestURI().startsWith("/api/v1/bank-transfer")
                        )
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedRequestHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(
                                "/api/v1/authentication/**",
                                "/api/v1/subscriptions/payment/**",
                                "/api/v1/deposit/payment-deposits/**",
                                "/ws/chat",
                                "/api/v1/recover-password/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/sign-up").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/authentication/is-authenticated").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservations/use-qr-token").permitAll()
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public WebSecurityConfiguration(@Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService, BearerTokenService tokenService, BCryptHashingService hashingService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
    }
}