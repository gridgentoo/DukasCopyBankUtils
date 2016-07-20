package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class DateTimeUtil {

    public static final String defaultDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final DateTimeFormatter defaultformatter =
            DateTimeFormatter.ofPattern(defaultDateFormat);
    public static final ZoneId localZoneId = ZoneId.systemDefault();
    public static final ZoneId dukascopyZoneId = ZoneId.ofOffset("UTC", ZoneOffset.UTC);

    private DateTimeUtil() {
    }

    public static final Instant instantFromMillis(final long millis) {
        return Instant.ofEpochMilli(millis);
    }

    public static final LocalDateTime dateTimeFromMillis(final long millis) {
        final Instant instant = instantFromMillis(millis);
        return LocalDateTime.ofInstant(instant, localZoneId);
    }

    public static final long localMillisFromDateTime(final LocalDateTime localDateTime) {
        return checkNotNull(localDateTime)
                .atZone(localZoneId)
                .toInstant()
                .toEpochMilli();
    }

    public static final long localMillisNow() {
        return localMillisFromDateTime(LocalDateTime.now());
    }

    public static final long millisFromNano(final long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    public static final String format(final LocalDateTime localDateTime) {
        return checkNotNull(localDateTime).format(defaultformatter);
    }

    public static final String millisToString(final long millis) {
        return dateTimeFromMillis(millis).format(defaultformatter);
    }
}
