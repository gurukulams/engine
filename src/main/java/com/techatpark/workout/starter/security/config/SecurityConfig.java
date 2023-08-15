package com.techatpark.workout.starter.security.config;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techatpark.workout.starter.security.filter.TokenAuthenticationFilter;
import com.techatpark.workout.starter.security.service.LearnerProfileService;
import com.techatpark.workout.starter.security.service.LearnerService;
import com.techatpark.workout.starter.security.service.TokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * The type Security config.
 */
@Configuration
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

        /**
         * Learner Service.
         */
        private final LearnerService learnerService;

        /**
         * Learner Details Service.
         */
        private final LearnerProfileService learnerProfileService;

        /**
         * inject the customUserDetailsService object dependency.
         */
        private final UserDetailsService userDetailsService;



        /**
         * TokenAuthenticationFilter.
         */
        private final TokenAuthenticationFilter tokenAuthenticationFilter;

        /**
         * TokenProvider.
         */
        private final TokenProvider tokenProvider;
        /**
         * Creates Security Config.
         *
         * @param alearnerService
         * @param alearnerProfileService
         * @param appProperties         properties
         * @param aCacheManager         aCacheManager
         * @param objectMapper
         * @param auserDetailsService
         */
        public SecurityConfig(final LearnerService alearnerService,
                      final LearnerProfileService alearnerProfileService,
                      final AppProperties appProperties,
                      final CacheManager aCacheManager,
                      final ObjectMapper objectMapper,
                      final UserDetailsService auserDetailsService) {
                this.learnerService = alearnerService;
                this.learnerProfileService = alearnerProfileService;

                userDetailsService = auserDetailsService;
                tokenProvider = new TokenProvider(appProperties,
                        aCacheManager, objectMapper, userDetailsService,
                        learnerProfileService);



                tokenAuthenticationFilter = new TokenAuthenticationFilter(
                        tokenProvider);
        }

        /**
         * Aithe Provide.
         * @return authenticationProvider
         */
        @Bean
        public TokenProvider tokenProvider() {
                return tokenProvider;
        }

        /**
         * Hi.
         * @return webSecurityCustomizer
         * @throws Exception exception
         */
        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring()
                        .requestMatchers("/api/metrics/**",
                                "/h2-console", "/h2-console/**",
                                "/swagger-ui.html", "/swagger-ui/*",
                                "/v3/api-docs", "/v3/api-docs/*",
                                "/questions/**", "/ta/questions/*",
                                "/chat",
                                "/chat/*",
                                "/chat/*/*",
                                "/chat/*/*/*",
                                "/api/auth/me",
                                "/api/auth/login");
        }

        /**
         * method configure is overrided here.
         *
         * @param http http
         * @return filterChain
         * @throws Exception exception
         */
        @Bean
        public SecurityFilterChain filterChain(
                final HttpSecurity http) throws Exception {
                http
                        .authorizeHttpRequests()
                        .requestMatchers("/api/auth/login",
                                "/api/auth/signup",
                                "/practices/basic/index.html",
                                "/ta/practices/basic/index.html",
                                "/favicon.ico",
                                "/error",
                                "/h2-console",
                                "/h2-console/**",
                                "/events",
                                "/events/**",
                                "/ta/events",
                                "/ta/events/**",
                                "/questions/biology/botany",
                                "/ta/questions/biology/botany",
                                "/v3/api-docs",
                                "/oauth2/*")
                        .permitAll()
                        .requestMatchers(toH2Console()).permitAll()
                        .anyRequest()
                        .authenticated()
                        .and()
                        .cors()
                        .and()
                        .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                        .csrf()
                        .disable()
                        .formLogin()
                        .disable()
                        .httpBasic()
                        .disable()
                        .exceptionHandling()
                        .authenticationEntryPoint(
                                new RestAuthenticationEntryPoint());

                // Add our custom Token based authentication filter
                http.addFilterBefore(tokenAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

                http.headers().frameOptions().disable();
                return http.build();
        }

        /**
         * In Production we can controld the filters from application.yml.
         *
         * @return corsFilter cors filter
         */
        @Bean
        public CorsFilter corsFilter() {
                final CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.addAllowedHeader("*");
                config.setAllowedMethods(
                        List.of("GET",
                                "POST", "PUT", "PATCH",
                                "DELETE", "OPTIONS"));

                final UrlBasedCorsConfigurationSource source =
                        new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                return new CorsFilter(source);
        }
}
