package gorbachev.id;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SimpleDateTest {

    @Test
    public void test1() {
        String date = "13.12.2024";
        System.out.println(LocalDate.parse(date));
    }
}
