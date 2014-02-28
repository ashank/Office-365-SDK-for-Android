/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.example.office.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.example.office.logger.Logger;

/**
 * Helper class to work with dates for example formats date to string.
 */
public class DateTimeUtils {

    /**
     * Default time date format.
     */
    private static final String DEFAULT_TIME_DATE_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ssZ";

    /**
     * Private constructor to prevent creating an instance of the class.
     */
    private DateTimeUtils() {}

    /**
     * Formats date to the string with default time date format.
     * 
     * @param time Date to format.
     * 
     * @return Result string.
     */
    public static String formatDate(Date time) {
        SimpleDateFormat dateformat = new SimpleDateFormat(DEFAULT_TIME_DATE_FORMAT);
        return dateformat.format(time);
    }

    public static Date parseDate(String ts) {
        SimpleDateFormat dateformat = new SimpleDateFormat(DEFAULT_TIME_DATE_FORMAT);
        try {
            return dateformat.parse(ts);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Retrieves UTC time as string.
     * 
     * @param date Date to parse.
     * 
     * @return UTC time as string for given time.
     */
    public static String getUtcTimeStringFromDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH':'mm':'ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        return dateFormat.format(date);

    }

    /**
     * Formats date to the string with specific time date format.
     * 
     * @param date Date to format.
     * @param format Format of result string.
     * 
     * @return Formatted date string.
     */
    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Saves date in originalDate, but applies time from appliedDate.
     * 
     * @param originalDate Date where time will be changed.
     * @param appliedDate Date which will be used to get time and copy it.
     * 
     * @return Date with date from original date and new time.
     */
    @SuppressWarnings("deprecation")
    public static Date copyTime(Date originalDate, Date appliedDate) {
        Date date = new Date(originalDate.getTime());
        date.setHours(appliedDate.getHours());
        date.setMinutes(appliedDate.getMinutes());
        date.setSeconds(appliedDate.getSeconds());
        return date;
    }

    /**
     * Returns {@link SimpleDateFormat} instance that formats date in default application format (time only for recent dates, day and month
     * for messages earlier than year and month/day/year for older)
     * 
     * @param date Date will be formatted
     * @return Formatter which will be format dates in default application format or null if something went wrong
     */
    public static SimpleDateFormat getDefaultFormatter(Date date) {
        try {
            Date now = new Date();
            if (now.before(date)) {
                // Message from future!
                // Specify locale explicitly, otherwise formatter will use system locale.
                // Can format month as "nov." instead of "Nov"
                return new SimpleDateFormat("MM/dd/yyyy h:mm:ss a", Locale.US);
            }

            // ignore leap years and leap seconds
            long ms = now.getTime() - date.getTime();
            long seconds = ms / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long years = days / 365;
            days %= 365;
            hours %= 24;
            minutes %= 60;
            seconds %= 60;
            ms %= 1000;

            SimpleDateFormat formatter;
            if (years >= 1) {
                formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            } else if (days >= 1) {
                formatter = new SimpleDateFormat("MMM, d", Locale.US);
            } else {
                // TODO should we use 24 hour format?
                formatter = new SimpleDateFormat("h:mm:ss a", Locale.US);
            }

            formatter.setTimeZone(TimeZone.getDefault());
            return formatter;
        } catch (Exception e) {
            Logger.logApplicationException(e, DateTimeUtils.class.getSimpleName() + ".getFormatter(): Error.");
        }
        return null;
    }
}