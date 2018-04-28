package plchat;

import java.util.*;

import static java.util.Calendar.*;

class Time
{

    public static final Calendar calendar;
    
    static
    {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"));
    }
    
    static Object[] timeData()
    {
        return new Object[] {
            calendar.get(DAY_OF_MONTH),
            calendar.get(MONTH) + 1,
            calendar.get(YEAR),
            calendar.get(HOUR_OF_DAY),
            calendar.get(MINUTE),
            calendar.get(SECOND),
        };
    }

}
