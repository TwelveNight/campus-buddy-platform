package com.example.campusbuddy.security;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.service.UserRoleService;
import com.example.campusbuddy.service.UserCacheService;
import com.example.campusbuddy.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                try {
                    Claims claims = jwtUtil.parseToken(jwt);
                    Long userId = claims.get("userId", Long.class);
                    String username = claims.getSubject();

                    // 检查token是否在Redis中
                    String cachedToken = userCacheService.getCachedUserToken(userId);
                    if (cachedToken == null || !cachedToken.equals(jwt)) {
                        // token已失效或被强制登出
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(R.fail(401, "登录状态已失效，请重新登录")));
                        return;
                    }

                    // 检查用户状态
                    User user = userCacheService.getCachedUser(userId);
                    if (user == null || !"ACTIVE".equals(user.getStatus())) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(R.fail(401, "账号被禁用或不可用")));
                        return;
                    }

                    // 获取用户角色列表
                    List<String> roles = userRoleService.getUserRoles(userId);

                    // 将角色转换为SimpleGrantedAuthority对象
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 创建认证对象并设置到上下文
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将用户ID放入request属性，便于后续处理
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    request.setAttribute("roles", roles);

                } catch (ExpiredJwtException e) {
                    logger.error("JWT token已过期", e);
                } catch (UnsupportedJwtException | MalformedJwtException | SignatureException
                        | IllegalArgumentException e) {
                    logger.error("无效的JWT token", e);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("无法设置用户认证", e);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(R.fail(401, "认证失败")));
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
