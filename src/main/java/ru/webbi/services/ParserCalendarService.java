package ru.webbi.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ParserCalendarService implements CalendarService {
    private static final int START_WORK_DAY_TIME_HOURS = 9;
    private static final int END_WORK_DAY_TIME_HOURS = 18;
    private static final int START_WORK_DAY_TIME_MINUTES = 0;
    private static final int END_WORK_DAY_TIME_MINUTES = 0;

    private static final String ZONE_OFFSET_MOSCOW = "+03:00";

    private static final String STANDARD_URL = "https://www.consultant.ru/law/ref/calendar/proizvodstvennye/";

    @Override
    public boolean checkWeekend(OffsetDateTime currentTime) {

        int year = currentTime.getYear();

        Month month = currentTime.getMonth();
        int day = currentTime.getDayOfMonth();

        String monthName = month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"));

        try {
            Document page = Jsoup.connect(STANDARD_URL + year + "/").get();
            Elements element = page
                    .select("body > div.container > div.row > div.col-pt-9 > div " +
                            "> div.row > div.col-md-3 > table.cal");
            for (Element e : element) {
                String monthElement = e.children().first().children().first().children().first().text().toLowerCase();
                if (!monthElement.equals(monthName)) {
                    continue;
                }

                List<String> weekendDays = List.of(e.select("tbody > tr > td.weekend")
                        .text()
                        .split(" "));
                if (weekendDays.contains(String.valueOf(day))) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isWorkingTime(OffsetDateTime currentTime) {

        int year = currentTime.getYear();

        Month month = currentTime.getMonth();
        int day = currentTime.getDayOfMonth();

        String monthName = month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
        try {
            Document page = Jsoup.connect(STANDARD_URL + year + "/").get();
            Elements elements = page
                    .select("body > div.container > div.row > div.col-pt-9 > blockquote > p");
            boolean isPreHoliday = false;
            for (Element e : elements) {
                var arrData = Arrays.toString(e.text().split(" "));
                if (arrData.contains(monthName) && arrData.contains(String.valueOf(day + 1))) {
                    isPreHoliday = true;
                }
            }

            return isWorkingDay(currentTime, isPreHoliday);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isWorkingDay(OffsetDateTime dateTime, boolean isPreHoliday) {
        LocalDate date = dateTime.toLocalDate();
        ZoneOffset zoneOffset = ZoneOffset.of(ZONE_OFFSET_MOSCOW);

        OffsetDateTime startWorkDay = OffsetDateTime.of(date,
                LocalTime.of(START_WORK_DAY_TIME_HOURS, START_WORK_DAY_TIME_MINUTES), zoneOffset);
        OffsetDateTime endWorkDay = OffsetDateTime.of(date,
                LocalTime.of(END_WORK_DAY_TIME_HOURS, END_WORK_DAY_TIME_MINUTES), zoneOffset);
        if (isPreHoliday) {
            endWorkDay = endWorkDay.minusHours(1);
        }
        return dateTime.isBefore(startWorkDay) || dateTime.isAfter(endWorkDay) || checkWeekend(dateTime);
    }
}
