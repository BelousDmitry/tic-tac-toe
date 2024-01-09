package service.connection.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;



//Interceptor for websocker connection

@Component
public class CustomInterceptor implements ChannelInterceptor  {


    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private String username;
    private String session_id;

    public CustomInterceptor(SimpMessagingTemplate messagingTemplate, RedisTemplate<String, String> redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        //On CONNECT saves into redis 
        //websocket session id as a key and username as value
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            this.username = accessor.getFirstNativeHeader("preferred_username");
            this.session_id = (String) accessor.getHeader("simpSessionId");
            this.redisTemplate.opsForValue().set(session_id, username);
            this.redisTemplate.opsForValue().set(username, "online");
        }

        //On DISCONNECT delete from redis 
        //On DISCONNECT we can get only id, but using id we can get username from Redis
        //Using username we can notify other player who had subscribed
        else if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            this.session_id = (String) accessor.getHeader("simpSessionId");
            this.username = (String) this.redisTemplate.opsForValue().getAndDelete(session_id);
            this.redisTemplate.opsForValue().getAndDelete(username);
            this.redisTemplate.opsForValue().getAndDelete("RANDOM-MATCH-READY-" + username);
            this.messagingTemplate.convertAndSend("/topic/game/connection/failed/" + this.username, "DISCONNECT");

        }

        //On UNSUBSCRIBE delete from redis 
        //On UNSUBSCRIBE (means that player left game web page) we take him off the queue
        //if it was done during the game, we let other player know about it
        else if (accessor != null && StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            this.session_id = (String) accessor.getHeader("simpSessionId");
            this.username = (String) this.redisTemplate.opsForValue().get(session_id);
            this.redisTemplate.opsForValue().getAndDelete("RANDOM-MATCH-READY-" + username);         
            this.messagingTemplate.convertAndSend("/topic/game/connection/failed/" + this.username, "UNSUBSCRIBE");

        }

        return message;
    }
    
}
