package com.st.util;

import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Misc operations for formatting dates.
 */
public class DateOperations {
    
    /** For reformatting date time to HTTP format. */
    public static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC().withLocale(Locale.US);

    /**
     * Safe return HTTP string. Should return 1 Jan 2012 if date is null.
     * @param date the date.
     * @return the HTTP (RFC1123) date string.
     */
    public static String getHTTPDateSafely(DateTime date) {
        if (date == null) {
            date = new DateTime(2012, 1, 1, 0, 0);
        }
        return RFC1123_DATE_TIME_FORMATTER.print(date);
    }
    
    /**
     * Parses a HTTP (RFC1123) date string.
     * @param str the date string.
     * @return the date.
     */
    public static DateTime parseHTTPDate(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        return RFC1123_DATE_TIME_FORMATTER.parseDateTime(str);
    }
    
    /**
     * Compares the last modified dates between a web resource and a request
     * @param resTime the time of the web resource
     * @param reqTime the time of the request (from the client)
     * @return true if the web resource time is after the request time which
     * means that the requested object has been modified
     */
    public static boolean checkIfModified(DateTime resTime, DateTime reqTime) {
        if (resTime == null) {
            resTime = new DateTime(2012, 1, 1 ,0 ,0);
        }
  
        // NOTE: Only precision within day.
        resTime = new DateTime(resTime.getYear(),
                resTime.getMonthOfYear(), 
                resTime.getDayOfMonth(), 
                resTime.getHourOfDay(), 
                resTime.getMinuteOfHour(), 
                resTime.getSecondOfMinute());
        
        return resTime.isAfter(reqTime);
    }

}
