package account.utils;

import account.exception.WrongDateException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateParser {
    public static LocalDate parse(String period) {
        if (!isValid(period))
            throw new WrongDateException();
        return LocalDate.parse(period + "-01", DateTimeFormatter.ofPattern("MM-yyyy-dd"));
    }

    public static String parse(String period, String pattern) {
        return parse(period).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String parse(LocalDate period) {
        return period.format(DateTimeFormatter.ofPattern("MM-yyyy"));
    }

    public static boolean isValid(String period) {
        return period.matches("(0[1-9]|1[0-2])-\\d{4}");
    }

}
