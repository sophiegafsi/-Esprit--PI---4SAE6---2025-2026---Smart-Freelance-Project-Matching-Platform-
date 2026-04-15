package freelink.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable()) // Disable CSRF for easier testing
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/h2-console/**"),
                                                                new AntPathRequestMatcher("/"),
                                                                new AntPathRequestMatcher("/login**"),
                                                                new AntPathRequestMatcher("/error"),
                                                                new AntPathRequestMatcher("/api/users/register"),
                                                                new AntPathRequestMatcher("/api/users/forgot-password"),
                                                                new AntPathRequestMatcher(
                                                                                "/api/users/forgot-password/**"),
                                                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                                                new AntPathRequestMatcher("/swagger-ui/**"),
                                                                new AntPathRequestMatcher("/api/users/set-role"),
                                                                new AntPathRequestMatcher(
                                                                                "/api/users/cleanup-duplicates"),
                                                                new AntPathRequestMatcher(
                                                                                "/api/users/become-freelancer"),
                                                                new AntPathRequestMatcher("/api/users/{id}", "GET"),
                                                                new AntPathRequestMatcher("/api/users/keycloak/**", "GET"),
                                                                new AntPathRequestMatcher("/api/users/*/notifications/**"),
                                                                new AntPathRequestMatcher("/api/users/notifications/by-keycloak/**"),
                                                                new AntPathRequestMatcher("/api/users/notifications/**"))
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userAuthoritiesMapper(userAuthoritiesMapper()))
                                                .defaultSuccessUrl("http://localhost:4200", true))
                                .logout(logout -> logout
                                                .logoutSuccessUrl("http://localhost:4200/login")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID"));

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
                return converter;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        public static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
                @Override
                public Collection<GrantedAuthority> convert(Jwt jwt) {
                        Set<GrantedAuthority> authorities = new HashSet<>();

                        // 1. Realm Roles
                        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                        if (realmAccess != null && realmAccess.containsKey("roles")) {
                                Collection<String> roles = (Collection<String>) realmAccess.get("roles");
                                authorities.addAll(roles.stream()
                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                .collect(Collectors.toList()));
                        }

                        // 2. Client Roles (resource_access)
                        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                        if (resourceAccess != null) {
                                resourceAccess.forEach((client, access) -> {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> clientAccess = (Map<String, Object>) access;
                                        if (clientAccess.containsKey("roles")) {
                                                Collection<String> roles = (Collection<String>) clientAccess.get("roles");
                                                authorities.addAll(roles.stream()
                                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                                .collect(Collectors.toList()));
                                        }
                                });
                        }

                        return authorities;
                }
        }

        @Bean
        public GrantedAuthoritiesMapper userAuthoritiesMapper() {
                return (authorities) -> {
                        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

                        authorities.forEach(authority -> {
                                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                                        Map<String, Object> attributes = oidcUserAuthority.getAttributes();
                                        if (attributes.containsKey("realm_access")) {
                                                @SuppressWarnings("unchecked")
                                                Map<String, Object> realmAccess = (Map<String, Object>) attributes
                                                                .get("realm_access");
                                                if (realmAccess != null && realmAccess.containsKey("roles")) {
                                                        Collection<String> roles = (Collection<String>) realmAccess
                                                                        .get("roles");
                                                        roles.forEach(role -> mappedAuthorities.add(
                                                                        new SimpleGrantedAuthority("ROLE_" + role)));
                                                }
                                        }
                                }
                                mappedAuthorities.add(authority);
                        });
                        return mappedAuthorities;
                };
        }

}
