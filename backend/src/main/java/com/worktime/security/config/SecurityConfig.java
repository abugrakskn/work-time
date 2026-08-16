package com.worktime.security.config;

import com.worktime.security.handler.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            SecurityErrorResponseWriter securityErrorResponseWriter
    ) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api/auth/login"
                ).permitAll()


                .requestMatchers("/api/users/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,"/api/projects/**")
                .hasAnyRole("ADMIN","EMPLOYEE")

                .requestMatchers("/api/projects/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,"/api/tasks/**")
                .hasAnyRole("ADMIN","EMPLOYEE")

                .requestMatchers(HttpMethod.PATCH, "/api/tasks/*/status")
                .hasAnyRole("ADMIN","EMPLOYEE")

                .requestMatchers("/api/tasks/**")
                .hasRole("ADMIN")


                .anyRequest().authenticated()
        )
        .cors(cors -> cors.configurationSource(corsConfigurationSource())
        )
        .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
        )
        .securityContext(context -> context
                .securityContextRepository(securityContextRepository)
        )
        .exceptionHandling(exception -> exception
                .authenticationEntryPoint(
                        (request, response, authException)
                        -> securityErrorResponseWriter.write(
                                response,
                                HttpStatus.UNAUTHORIZED,
                                "Authentication is required to access this resource"
                        )
                )
                .accessDeniedHandler(
                        (request, response, accessDeniedException)
                                -> securityErrorResponseWriter.write(
                                        response,
                                HttpStatus.FORBIDDEN,
                                "You do not have permission to access this resource"
                        )
                )
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ){
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(userDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
