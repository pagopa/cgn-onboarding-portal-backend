package it.gov.pagopa.cgn.portal.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.TimeZone;

public interface Iso8601TimestampCompatible {

    DateTimeFormatter ISO_8601_UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                                                .withZone(TimeZone.getTimeZone("UTC").toZoneId());


    static String toISO8601UTCTimestamp(LocalDate ld) {
        return Objects.nonNull(ld) ? ISO_8601_UTC_FORMATTER.format(Timestamp.valueOf(ld.atStartOfDay()).toInstant()):"";
    }

    static LocalDate toLocalDate(String timestamp) {
        return Objects.nonNull(timestamp) ? Instant.parse(timestamp).atZone(ZoneId.of("UTC")).toLocalDate():null;
    }

    default String getISO8601UTCTimestamp(LocalDate ld) {
        return toISO8601UTCTimestamp(ld);
    }

    default LocalDate getLocalDate(String timestamp) {
        return toLocalDate(timestamp);
    }
}
