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
package com.example.office.logger.formatter;

import java.util.Date;

import com.example.office.utils.DateTimeUtils;

/**
 * Implements simple formatted to store exception information.
 */
public class SimpleExceptionFormatter implements IFormatter {

    /**
     * Line separator.
     */
    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Separator between logged entities.
     */
    private static String LOGS_SEPARATOR = "--------------------------";

    @Override
    public String format(Exception exception, String message) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(formatDate(new Date()));
        stringBuffer.append(LINE_SEPARATOR);

        if (message != null) {
            stringBuffer.append(formatMessage(message));
            stringBuffer.append(LINE_SEPARATOR);
        }

        if (exception != null) {
            String formattedException = formatException(exception);
            if (formattedException != null) {
                stringBuffer.append(formattedException);
            }
        }

        stringBuffer.append(LOGS_SEPARATOR + LINE_SEPARATOR);
        return stringBuffer.toString();
    }

    /**
     * Formats date to log.
     *
     * @param date Date to log.
     *
     * @return String with formatted date.
     */
    private String formatDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        return DateTimeUtils.formatDate(date);
    }

    /**
     * Formats message.
     *
     * @param message Message to format.
     *
     * @return String with formatted message.
     */
    private String formatMessage(String message) {
        return message;
    }

    /**
     * Formats exception.
     *
     * @param exception Exception to format.
     *
     * @return String with formatted exception.
     */
    private String formatException(Exception exception) {
        if (exception == null) {
            return null;
        }

        StringBuffer stringBuffer = new StringBuffer();

        try {
            stringBuffer.append(exception.getMessage());
            stringBuffer.append(LINE_SEPARATOR);

            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            if (stackTraceElements != null && stackTraceElements.length > 0) {
                int stackTraceLength = stackTraceElements.length;
                for (int i = stackTraceLength - 1; i >= 0; i--) {
                    stringBuffer.append("File name: " + stackTraceElements[i].getFileName() + LINE_SEPARATOR);
                    stringBuffer.append("Method: " + stackTraceElements[i].getMethodName() + LINE_SEPARATOR);
                    stringBuffer.append("Line number: " + stackTraceElements[i].getLineNumber() + LINE_SEPARATOR);
                }
            }
        } catch (final Exception e) {
            // Do not log this exception.
        }

        return stringBuffer.toString();
    }
}
