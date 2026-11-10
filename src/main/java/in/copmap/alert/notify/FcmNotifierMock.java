package in.copmap.alert.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FcmNotifierMock implements Notifier {

    @Override
    public void send(NotificationMessage msg) {
        log.info("[FCM MOCK] to={} title={} body={}", msg.userId(), msg.title(), msg.body());
    }
}
