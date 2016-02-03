package net.rrm.ehour.domain;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum CalendarExceptionType {
    NORMAL_DAY(Integer.valueOf(0)), // Not an exception at all
    NON_WORKING_DAY(Integer.valueOf(1)),
    WORKING_DAY(Integer.valueOf(2));

    private static final ConcurrentMap<Integer, CalendarExceptionType> ExceptionTypeIntegerMapping = new ConcurrentHashMap<>();
    private Integer value;

    static
    {
        ExceptionTypeIntegerMapping.put(Integer.valueOf(0), NON_WORKING_DAY);
        ExceptionTypeIntegerMapping.put(Integer.valueOf(1), NON_WORKING_DAY);
        ExceptionTypeIntegerMapping.put(Integer.valueOf(2), WORKING_DAY);
    }

    /**
     * Get Calendar Exception Type by Integer Value
     * @param value
     * @return CalendarExceptionType if Integer Value is valid. Always return NORMAL_DAY if value is not found
     *
     */
    public static final CalendarExceptionType getCalendarExceptionTypeByValue(Integer value) {
        CalendarExceptionType result = ExceptionTypeIntegerMapping.get(value);

        return (result == null) ? NORMAL_DAY : result;
    }

    CalendarExceptionType(Integer value)
    {
        this.value = value;
    }

    public Integer getValue()
    {
        return value;
    }

}
