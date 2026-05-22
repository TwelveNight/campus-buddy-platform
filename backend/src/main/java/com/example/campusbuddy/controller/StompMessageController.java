package com.example.campusbuddy.controller;

import com.example.campusbuddy.dto.PrivateMessageDTO;
import com.example.campusbuddy.service.PrivateMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class StompMessageController {
    private final PrivateMessageService privateMessageService;

    public StompMessageController(PrivateMessageService privateMessageService) {
        this.privateMessageService = privateMessageService;
    }

    @MessageMapping("/messages")
    public void sendPrivateMessage(PrivateMessageDTO dto, Principal principal) {
        if (principal == null) {
            return;
        }

        try {
            Long senderId = Long.parseLong(principal.getName());
            privateMessageService.sendMessage(senderId, dto);
        } catch (NumberFormatException ignored) {
            // Invalid STOMP principal. The handshake handler only accepts numeric IDs.
        }
    }
}
