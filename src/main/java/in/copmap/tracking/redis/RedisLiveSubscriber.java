package in.copmap.tracking.redis;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLiveSubscriber implements MessageListener {

    private final SimpMessagingTemplate stomp;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String patrolId = channel.substring(channel.lastIndexOf(':') + 1);
        stomp.convertAndSend("/topic/patrol/" + patrolId, new String(message.getBody(), StandardCharsets.UTF_8));
    }
}
