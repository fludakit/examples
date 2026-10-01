package com.example;

import io.github.fludakit.sqlinit.version.VersionStrategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom version strategy using date-based versions in format YYYYMMDD.
 * Example: V20240101, V20241231
 */
public class DateVersionStrategy implements VersionStrategy {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public String parse(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        try {
            LocalDate date = LocalDate.parse(version, FORMAT);
            return date.format(FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date version (expected YYYYMMDD): " + version, e);
        }
    }

    @Override
    public int compare(String v1, String v2) {
        return v1.compareTo(v2);
    }
}
