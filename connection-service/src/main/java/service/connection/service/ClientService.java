package service.connection.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import service.connection.DTO.Move;
import service.connection.DTO.OneVsOne;

@Service
public class ClientService {

    private final RedisTemplate<String, String> redisTemplate;

    public ClientService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addPlayerToQueue(String username) {
        if (username != null) {
            this.redisTemplate.opsForValue().set("RANDOM-MATCH-READY-" + username, username, 15, TimeUnit.MINUTES);
        }
    }

    public boolean isOnline(String username) {
        String status = this.redisTemplate.opsForValue().get(username);
        
        if (status == null)
            return false;
        else
            return status.equals("online") ? true : false;
    }

    public OneVsOne generatePlayersOneVsOne() {

        if (this.redisTemplate.keys("RANDOM-MATCH-READY-" + "*").size() >= 2) {
            String username_1 = this.redisTemplate.opsForValue()
                    .getAndDelete(this.redisTemplate.keys("RANDOM-MATCH-READY-" + "*").iterator().next());
            String username_2 = this.redisTemplate.opsForValue()
                    .getAndDelete(this.redisTemplate.keys("RANDOM-MATCH-READY-" + "*").iterator().next());

            if (username_1 != null && username_2 != null) {
                return new OneVsOne(username_1, username_2);
            }
        }
        return null;
    }

    public String sendToGameService(OneVsOne players) {

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8090")
                .build();

        return restClient.post()
                .uri("/game")
                .contentType(MediaType.APPLICATION_JSON)
                .body(players)
                .retrieve()
                .body(String.class);
    }

    public String sendToGameService(Move move) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8090")
                .build();

        return restClient.post()
                .uri("/move")
                .contentType(MediaType.APPLICATION_JSON)
                .body(move.payload)
                .retrieve()
                .body(String.class);

    }

}
