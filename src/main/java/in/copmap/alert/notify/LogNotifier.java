package in.copmap.alert.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Slf4j
public class LogNotifier implements Notifier {

    @Override
    public void send(NotificationMessage msg) {
        log.info("[NOTIFY] to={} title={} body={}", msg.userId(), msg.title(), msg.body());
    }
}
