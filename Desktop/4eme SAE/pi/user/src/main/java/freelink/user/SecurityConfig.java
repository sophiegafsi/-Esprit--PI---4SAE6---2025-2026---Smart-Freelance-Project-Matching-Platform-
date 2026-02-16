package freelink.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(org.springframework.security.config.Customizer.withDefaults())
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
                                                                                "/api/users/become-freelancer"))
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(org.springframework.security.config.Customizer.withDefaults()))
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
        public GrantedAuthoritiesMapper userAuthoritiesMapper() {
                return (authorities) -> {
                        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

                        authorities.forEach(authority -> {
                                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                                        Map<String, Object> attributes = oidcUserAuthority.getAttributes();
                                        if (attributes.containsKey("realm_access")) {
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

        @Bean
        public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
                org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
                configuration.setAllowedOrigins(java.util.Arrays.asList("http://localhost:4200"));
                configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
