package in.copmap.alert.notify;

public record NotificationMessage(
        Long userId,
        String title,
        String body
) {}
