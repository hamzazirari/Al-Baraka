package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtil() {
    }

    public static String formater(LocalDateTime date) {
        return date.format(FORMAT);
    }

    public static LocalDateTime maintenant() {
        return LocalDateTime.now();
    }
}