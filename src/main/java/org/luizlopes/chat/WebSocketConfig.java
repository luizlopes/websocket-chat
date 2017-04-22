package org.luizlopes.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket-poc")
                .withSockJS().setSessionCookieNeeded(true);
    }

    @Bean
    public PresenceChannelInterceptor presenceChannelInterceptor() {
        return new PresenceChannelInterceptor();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(presenceChannelInterceptor());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(8);
        registration.setInterceptors(presenceChannelInterceptor());
    }


    public class PresenceChannelInterceptor extends ChannelInterceptorAdapter {

        @Override
        public void postSend(org.springframework.messaging.Message<?> message, MessageChannel channel, boolean sent) {

            StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

            // ignore non-STOMP messages like heartbeat messages
            if (sha.getCommand() == null) {
                return;
            }

            String sessionId = sha.getSessionId();

            switch (sha.getCommand()) {
                case CONNECT:
                    log.debug("STOMP Connect [sessionId: " + sessionId + "]");
                    break;
                case CONNECTED:
                    log.debug("STOMP Connected [sessionId: " + sessionId + "]");
                    break;
                case DISCONNECT:
                    log.debug("STOMP Disconnect [sessionId: " + sessionId + "]");
                    break;
                default:
                    break;

            }
        }
    }

}
