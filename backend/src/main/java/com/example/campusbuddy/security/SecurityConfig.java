package com.example.campusbuddy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 关闭CSRF，便于前后端分离开发
                .authorizeHttpRequests(auth -> auth
                        // 放行swagger和静态资源
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/static/**",
                                "/public/**")
                        .permitAll()
                        // 其他接口默认全部放行，后续可改为authenticated()
                        .anyRequest().permitAll());
        return http.build();
    }
}
