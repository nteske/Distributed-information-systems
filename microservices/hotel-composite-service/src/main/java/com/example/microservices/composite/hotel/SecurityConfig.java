package com.example.microservices.composite.hotel;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.*;

@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
			.authorizeExchange()
				.pathMatchers("/actuator/**").permitAll()
				.pathMatchers(POST, "/hotel-composite/**").hasAuthority("SCOPE_hotel:write")
				.pathMatchers(DELETE, "/hotel-composite/**").hasAuthority("SCOPE_hotel:write")
				.pathMatchers(GET, "/hotel-composite/**").hasAuthority("SCOPE_hotel:read")
				.anyExchange().authenticated()
				.and()
			.oauth2ResourceServer()
				.jwt();
		return http.build();
	}
}