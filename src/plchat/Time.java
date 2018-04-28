package plchat;

import java.util.*;

class Time
{

    private static final Calendar calendar;
    
    static
    {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"));
    }
    
    static Calendar getCalendar()
    {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar;
    }
    
}
