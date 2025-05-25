package com.example.campusbuddy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Autowired
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Autowired
        private JwtAccessDeniedHandler jwtAccessDeniedHandler;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 放行静态资源和WebSocket
                                                .requestMatchers("/static/**", "/websocket-test.html", "/websocket-test-new.html", "/favicon.ico", "/ws/**").permitAll()
                                                // 放行API登录注册等公开接口
                                                .requestMatchers("/api/auth/**", "/api/user/register", "/api/user/login").permitAll()
                                                // 其余接口需要认证
                                                .anyRequest().authenticated()
                                                )
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:8080")); // 允许的前端源
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // 允许的方法
                configuration.setAllowedHeaders(Arrays.asList("*")); // 允许所有请求头
                configuration.setAllowCredentials(true); // 允许发送凭证
                configuration.setAllowCredentials(true); // 允许携带凭证
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // 对所有路径应用CORS配置
                return source;
        }
}
