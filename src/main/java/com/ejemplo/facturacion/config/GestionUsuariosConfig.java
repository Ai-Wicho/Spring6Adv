package com.ejemplo.facturacion.config;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // Importante
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Importante

import com.ejemplo.facturacion.security.JwtFilter;

@Configuration
public class GestionUsuariosConfig {

    @Bean
    public PasswordEncoder passwordEncoder() throws NoSuchAlgorithmException {
        SecureRandom s = SecureRandom.getInstanceStrong();
        return new BCryptPasswordEncoder(4, s);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter)
            throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.httpBasic(Customizer.withDefaults());

        // Configuración de rutas (Rúbrica Puntos 11-14)
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/autenticar").permitAll() // Público
                .requestMatchers("/actuator/**").permitAll() // Público (Health)
                .anyRequest().authenticated() // Todo lo demás requiere Token
        );

        // La aplicación no debe guardar estado (Stateless) porque usamos Tokens
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Insertamos nuestro filtro JWT antes del filtro de autenticación estándar
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}