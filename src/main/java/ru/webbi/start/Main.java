package ru.webbi.start;

import ru.webbi.services.CalendarService;
import ru.webbi.services.InMemoryCalendarService;
import ru.webbi.services.ParserCalendarService;

import java.time.OffsetDateTime;
import java.util.Scanner;

public class Main {
    private static final CalendarService calendarService1 = new ParserCalendarService();
    private static final CalendarService calendarService2 = new InMemoryCalendarService();

    public static void main(String[] args) {
        //Пример ввода: 2024-05-19T15:26:29.960714+05:00

        Scanner in = new Scanner(System.in);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(in.nextLine());

        System.out.println(calendarService1.checkWeekend(offsetDateTime));
        System.out.println(calendarService1.isWorkingTime(offsetDateTime));

        System.out.println(calendarService2.checkWeekend(offsetDateTime));
        System.out.println(calendarService2.isWorkingTime(offsetDateTime));
    }
}
