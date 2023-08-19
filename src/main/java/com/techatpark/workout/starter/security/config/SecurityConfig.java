package com.techatpark.workout.starter.security.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techatpark.workout.starter.security.filter.TokenAuthenticationFilter;
import com.techatpark.workout.starter.security.service.LearnerProfileService;
import com.techatpark.workout.starter.security.service.TokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
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
     * @param alearnerProfileService
     * @param appProperties          properties
     * @param aCacheManager          aCacheManager
     * @param objectMapper
     * @param auserDetailsService
     */
    public SecurityConfig(final LearnerProfileService alearnerProfileService,
                          final AppProperties appProperties,
                          final CacheManager aCacheManager,
                          final ObjectMapper objectMapper,
                          final UserDetailsService auserDetailsService) {
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
     *
     * @return authenticationProvider
     */
    @Bean
    public TokenProvider tokenProvider() {
        return tokenProvider;
    }

    /**
     * Hi.
     *
     * @return webSecurityCustomizer
     * @throws Exception exception
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(antMatcher("/api/metrics/**"),
                        antMatcher("/h2-console"),
                        antMatcher("/h2-console/**"),
                        antMatcher("/swagger-ui.html"),
                        antMatcher("/swagger-ui/*"),
                        antMatcher("/v3/api-docs"),
                        antMatcher("/v3/api-docs/*"),
                        antMatcher("/questions/**"),
                        antMatcher("/ta/questions/*"),
                        antMatcher("/chat"),
                        antMatcher("/chat/*"),
                        antMatcher("/chat/*/*"),
                        antMatcher("/chat/*/*/*"),
                        antMatcher("/api/auth/me"),
                        antMatcher("/api/auth/login")
                );
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
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher("/api/auth/login"),
                                antMatcher("/api/auth/signup"),
                                antMatcher("/practices/basic/index.html"),
                                antMatcher("/ta/practices/basic/index.html"),
                                antMatcher("/favicon.ico"),
                                antMatcher("/error"),
                                antMatcher("/h2-console"),
                                antMatcher("/h2-console/**"),
                                antMatcher("/events"),
                                antMatcher("/events/**"),
                                antMatcher("/ta/events"),
                                antMatcher("/ta/events/**"),
                                antMatcher("/questions/biology/botany"),
                                antMatcher("/ta/questions/biology/botany"),
                                antMatcher("/v3/api-docs"),
                                antMatcher("/oauth2/**")
                        )
                        .permitAll()
                        .requestMatchers(toH2Console()).permitAll()
                        .anyRequest()
                        .authenticated())
                .cors(config -> {
                })
                .sessionManagement(config -> config
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(config -> config
                        .authenticationEntryPoint(
                                new RestAuthenticationEntryPoint()))
                .headers(config -> config
                        .frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::disable));

        // Add our custom Token based authentication filter
        http.addFilterBefore(tokenAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

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
