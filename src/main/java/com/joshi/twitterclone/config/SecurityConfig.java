package com.joshi.twitterclone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/ws/**"),
                    new AntPathRequestMatcher("/ws-feed/**")
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Static assets & Public pages
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/privacy",
                    "/terms",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/uploads/**",
                    "/favicon.svg",
                    "/favicon.ico",
                    "/ws/**",
                    "/ws-feed/**"
                ).permitAll()

                // Admin Moderation
                .requestMatchers(
                    "/marketplace/admin",
                    "/marketplace/admin/**"
                ).hasRole("ADMIN")

                // Authenticated Platform Features (Matching both exact and wildcards)
                .requestMatchers(
                    "/home",
                    "/timeline",
                    "/timeline/**",
                    "/messages",
                    "/messages/**",
                    "/marketplace",
                    "/marketplace/**",
                    "/resume",
                    "/resume/**",
                    "/u",
                    "/u/**",
                    "/explore",
                    "/explore/**"
                ).authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }
}