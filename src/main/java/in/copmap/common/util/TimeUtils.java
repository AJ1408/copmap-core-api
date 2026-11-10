package in.copmap.common.util;

import java.time.Instant;

public final class TimeUtils {

    private TimeUtils() {}

    public static Instant now() {
        return Instant.now();
    }

    public static long toEpochMilli(Instant instant) {
        return instant != null ? instant.toEpochMilli() : 0L;
    }

    public static Instant fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }
}
