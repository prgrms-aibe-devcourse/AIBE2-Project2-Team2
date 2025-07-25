package org.example.backend.chat.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketLoggingInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String sessionId = accessor.getSessionId();
        String command = accessor.getCommand() != null ? accessor.getCommand().name() : "UNKNOWN";
        String destination = accessor.getDestination();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";

        log.info(
                "\n================= [WEBSOCKET MESSAGE] =================\n" +
                        "📡 Command    : {}\n" +
                        "📡 SessionId  : {}\n" +
                        "📡 User       : {}\n" +
                        "📡 Destination: {}\n" +
                        "📡 Payload    : {}\n" +
                        "========================================================",
                command, sessionId, user, destination, message.getPayload()
        );

        return message;
    }
}