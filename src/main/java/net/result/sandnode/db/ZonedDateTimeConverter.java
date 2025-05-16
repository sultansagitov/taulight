package net.result.sandnode.db;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, String> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");

    @Override
    public String convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            ZonedDateTime utcZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
            return utcZonedDateTime.format(formatter);
        }
        return null;
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dbData, formatter);
            return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
        }
        return null;
    }
}
