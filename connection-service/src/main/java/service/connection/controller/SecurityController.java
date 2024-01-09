package service.connection.controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200", exposedHeaders = "Access-Control-Allow-Origin")
@RestController
public class SecurityController {

    private final RedisTemplate<String, String> redisTemplate;

    public SecurityController(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("ws/auth/temporal-token")
    public String getTemporalToken(@AuthenticationPrincipal Jwt jwt) {

        UUID token = UUID.randomUUID();
        String username = jwt.getClaimAsString("preferred_username");

        if (username != null) {
            this.redisTemplate.opsForValue().set("ws-" + username, token.toString(), 10, TimeUnit.SECONDS);
            return token.toString();
        }
        
        return "ERROR";
    }

}
