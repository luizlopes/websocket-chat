package org.luizlopes.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class ChatController {

    private static List<String> usersConnected = new CopyOnWriteArrayList<>();

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/message")
    public void message(Message message, Principal principal) throws Exception {
        message.setSender(principal.getName());
        if (message.getRecipient().isEmpty()) {
            template.convertAndSend("/topic/chat", message);
        } else {
            if (message.senderNotIsRecipient()) {
                template.convertAndSendToUser(message.getRecipient(), "/queue/chat", message);
            }
            template.convertAndSendToUser(message.getSender(), "/queue/chat", message);
        }

        addUserIfNeeded(principal);
    }

    @MessageMapping("/newConnection")
    @SendTo("/topic/activeUsers")
    public List<String> message(Principal principal) throws Exception {
        addUserIfNeeded(principal);
        return usersConnected;
    }

    private void addUserIfNeeded(Principal principal) {
        if (!usersConnected.contains(principal.getName())) {
            usersConnected.add(principal.getName());
            template.convertAndSend("/topic/activeUsers", usersConnected);
        }
    }

}
