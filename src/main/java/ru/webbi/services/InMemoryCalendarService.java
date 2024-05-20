package ru.webbi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.util.List;

public class InMemoryCalendarService implements CalendarService {
    private static final String DATES_WEEKEND_PATH = "src/main/resources/datesWeekend/%d.json";
    private static final String DATES_PRE_HOLIDAY_PATH = "src/main/resources/datesPre-holiday/%d.json";

    private static final int START_WORK_DAY_TIME_HOURS = 9;
    private static final int END_WORK_DAY_TIME_HOURS = 18;
    private static final int START_WORK_DAY_TIME_MINUTES = 0;
    private static final int END_WORK_DAY_TIME_MINUTES = 0;

    private static final String ZONE_OFFSET_MOSCOW = "+03:00";
    private static final String ERROR_READING = "Error reading file";

    @Override
    public boolean checkWeekend(OffsetDateTime currentTime) {
        try {
            int year = currentTime.getYear();
            Month month = currentTime.getMonth();
            int day = currentTime.getDayOfMonth();
            JSONObject jsonObject = readJsonObjectFromFile(String.format(DATES_WEEKEND_PATH, year));

            List<Integer> holidaysMonth = getMonthNumbers(jsonObject, month);

            return holidaysMonth.contains(day);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(ERROR_READING, e);
        }
    }

    @Override
    public boolean isWorkingTime(OffsetDateTime currentTime) {
        try {
            Month month = currentTime.getMonth();

            JSONObject jsonObject = readJsonObjectFromFile(String.format(DATES_PRE_HOLIDAY_PATH, currentTime.getYear()));

            List<Integer> preHolidaysMonth = getMonthNumbers(jsonObject, month);

            boolean isPreHoliday = preHolidaysMonth != null
                    && preHolidaysMonth.contains(currentTime.getDayOfMonth() + 1);

            return isWorkingDay(currentTime, isPreHoliday);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(ERROR_READING, e);
        }
    }

    private JSONObject readJsonObjectFromFile(String filePath) throws IOException, ParseException {
        File file = new File(filePath);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        return (JSONObject) obj;
    }

    private List<Integer> getMonthNumbers(JSONObject jsonObject, Month month) {
        Object monthArray = jsonObject.get(month.toString());
        if (monthArray == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(monthArray, new TypeReference<>() {
        });
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
