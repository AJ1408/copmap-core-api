package in.copmap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Tracking tracking, Alerts alerts) {
    public record Jwt(String secret, long accessTtlMinutes) {}
    public record Tracking(int maxPingsPerMinute, int noSignalMinutes) {}
    public record Alerts(int dedupeTtlMinutes) {}
}
