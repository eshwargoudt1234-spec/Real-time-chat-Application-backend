package com.chatapp.config;

import com.chatapp.security.CustomUserDetailsService;
import com.chatapp.security.JwtAuthFilter;
import com.chatapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/** REST security: stateless JWT, BCrypt passwords, CORS. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Frontend URLs allowed to call the API (see app.cors.allowed-origins) */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil,
                                                   CustomUserDetailsService userDetailsService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // we use JWT, not cookies
            .cors(Customizer.withDefaults())                    // uses corsConfigurationSource() below
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))   // SockJS may use an iframe transport
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()    // register + login are public
                .requestMatchers("/ws/**").permitAll()          // WebSocket handshake; JWT is checked on STOMP CONNECT
                .requestMatchers("/", "/api/health", "/error").permitAll()
                .anyRequest().authenticated())                  // everything else needs a valid token
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> writeJson(res, 401, "Unauthorized",
                        "Please log in to continue"))
                .accessDeniedHandler((req, res, e) -> writeJson(res, 403, "Forbidden",
                        "You do not have permission to access this resource")))
            .addFilterBefore(new JwtAuthFilter(jwtUtil, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** CORS: lets the separate frontend (another port/domain) call the REST API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);   // /ws has its own origin check in WebSocketConfig
        return source;
    }

    private void writeJson(jakarta.servlet.http.HttpServletResponse res, int status, String error, String message)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }
}
