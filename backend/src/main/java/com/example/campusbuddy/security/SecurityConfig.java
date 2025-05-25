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
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 添加CORS配置
                                .csrf(AbstractHttpConfigurer::disable) // 关闭CSRF，便于前后端分离开发
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 放行登录注册相关接口
                                                .requestMatchers("/api/user/login", "/api/user/register").permitAll()
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
                                                // 允许所有GET请求访问互助任务
                                                .requestMatchers(HttpMethod.GET, "/api/helpinfo/**").permitAll()
                                                // 允许PATCH请求访问 /api/helpinfo/{id}/view 用于更新浏览量
                                                .requestMatchers(HttpMethod.PATCH, "/api/helpinfo/*/view").permitAll()
                                                // 管理员接口限制
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                // 版主接口限制
                                                .requestMatchers("/api/moderator/**").hasAnyRole("ADMIN", "MODERATOR")
                                                // 其他接口需要认证
                                                .anyRequest().authenticated())
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler)) // 添加访问拒绝处理器
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
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 允许的前端源
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // 允许的方法
                configuration.setAllowedHeaders(Arrays.asList("*")); // 允许所有请求头
                configuration.setAllowCredentials(true); // 允许携带凭证
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // 对所有路径应用CORS配置
                return source;
        }
}
