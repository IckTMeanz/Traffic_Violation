package vn.icktmeanz.trafficViolation.configuration;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import vn.icktmeanz.trafficViolation.service.implement.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                )
                .authorizeHttpRequests(auth -> auth
                        // public resources
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/user/api/createUserAcc",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // admin
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // authority
                        .requestMatchers("/authority/**")
                        .hasRole("AUTHORITY")

                        // user
                        .requestMatchers("/user/**")
                        .hasRole("USER")

                        // all authenticated
                        .anyRequest()
                        .authenticated()
                )
                //form login
                .formLogin(form -> form

                        // GET /login
                        .loginPage("/login")

                        // POST /toAuthenticate
                        .loginProcessingUrl("/toAuthenticate")

                        // input name="username"
                        .usernameParameter("username")

                        // input name="password"
                        .passwordParameter("password")

                        // login success
                        .successHandler(authenticationSuccessHandler())

                        // login fail
                        .failureHandler(authenticationFailureHandler())

                        .permitAll()
                )
                //logout
                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl("/login?logout")

                        .invalidateHttpSession(true)

                        .deleteCookies("JSESSIONID")

                        .permitAll()
                )
                .userDetailsService(customUserDetailsService)
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean isAuthority = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHORITY"));

            boolean isUser = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

            if (isAdmin) {
                response.sendRedirect("/admin");
            } else if (isAuthority) {
                response.sendRedirect("/authority/upload");
            } else if (isUser) {
                response.sendRedirect("/user/upload");
            } else {
                response.sendRedirect("/login?error");
        }
    };
}

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {

            String message;

            if (exception instanceof DisabledException) {
                message = "Tài khoản đã bị vô hiệu hóa";
            } else {
                message = "Sai tên đăng nhập hoặc mật khẩu";
            }

            response.sendRedirect(
                    "/login?error=" +
                            URLEncoder.encode(message, StandardCharsets.UTF_8)
            );
        };
    }
}