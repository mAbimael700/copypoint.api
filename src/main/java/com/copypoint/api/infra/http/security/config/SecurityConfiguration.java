package com.copypoint.api.infra.http.security.config;

import com.copypoint.api.infra.http.authentication.filter.AuthenticationFilter;
import com.copypoint.api.infra.http.authentication.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    @Autowired
    private AuthenticationFilter authenticationFilter;

    /* @Autowired
    private AuthorizationFilter authorizationFilter;
    */

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity

                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Usar la configuración CORS
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Le indiamos a Spring el tipo de sesion
                .authorizeHttpRequests((auth) ->
                        auth
                                .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/sign-up").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/payment-methods").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/webhook/whatsapp/*").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/webhook/whatsapp/*").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/webhook/mercadopago").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/webhook/mercadopago").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Para CORS preflight
                                .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //.addFilterAfter(authorizationFilter, AuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
