package ru.webbi.services;

import java.time.OffsetDateTime;

public interface CalendarService {

    boolean checkWeekend(OffsetDateTime currentTime);
    boolean isWorkingTime(OffsetDateTime currentTime);
}
