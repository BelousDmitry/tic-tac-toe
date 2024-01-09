package service.connection.security;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

//security layer for websocket connection
//using custom temporal token (see SecurityController)
// see the article for more understanding
// https://nuvalence.io/insights/websocket-token-based-authentication/
@Component
@Order(HIGHEST_PRECEDENCE + 1)
public class WebSocketFilter extends OncePerRequestFilter {

    private final RequestMatcher uriMatcher = new AntPathRequestMatcher("/ws/game");
    private final RedisTemplate<String, String> redisTemplate;

    public WebSocketFilter(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {

        String temporalToken = request.getParameter("temporal_token");
        String username = request.getParameter("preferred_username");



        if (temporalToken.equals(this.redisTemplate.opsForValue().getAndDelete("ws-" + username))) {
            filterChain.doFilter(request, response);
        }else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        RequestMatcher matcher = new NegatedRequestMatcher(uriMatcher);
        return matcher.matches(request);
    }

}
