package org.weviewapp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.weviewapp.security.JwtAuthenticationEntryPoint;
import org.weviewapp.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    private UserDetailsService uds;
    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private JwtAuthenticationFilter authenticationFilter;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().and()
                .authorizeHttpRequests((authorize) ->
                                        authorize
                                                .requestMatchers("/api/principal/**").permitAll()
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/product/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/reward/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/report/admin/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET,"/api/product/**").permitAll()
                                                .requestMatchers(HttpMethod.GET,"/api/review/**").permitAll()
                                                .requestMatchers(HttpMethod.GET,"/api/user/**").permitAll()
                                                .requestMatchers("/api/user/**").hasRole("USER")
                                                .requestMatchers("/api/review/**").hasRole("USER")
                                                .requestMatchers("/api/voting/**").hasRole("USER")
                                                .requestMatchers("/api/product/**").hasRole("USER")
                                        .anyRequest().authenticated()
                ).exceptionHandling( exception -> exception
                    .authenticationEntryPoint(authenticationEntryPoint)
                ).sessionManagement( session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}