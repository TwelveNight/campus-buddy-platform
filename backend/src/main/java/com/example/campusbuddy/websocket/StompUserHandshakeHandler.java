package com.example.campusbuddy.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;

public class StompUserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String userId = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("userId");

        if (userId != null && userId.matches("\\d+")) {
            attributes.put("userId", userId);
            return new StompUserPrincipal(userId);
        }

        return super.determineUser(request, wsHandler, attributes);
    }
}
