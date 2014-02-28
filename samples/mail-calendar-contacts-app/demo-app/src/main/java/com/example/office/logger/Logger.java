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
package com.example.office.logger;

import com.example.office.Configuration;
import com.example.office.logger.formatter.IFormatter;
import com.example.office.logger.formatter.SimpleExceptionFormatter;
import com.example.office.logger.writer.FileWriter;
import com.example.office.logger.writer.IWriter;
import com.example.office.logger.writer.WriterFactory;

/**
 * Helper class to log messages.
 */
public final class Logger {

    /**
     * The writer.
     */
    private static IWriter sWriter;

    /**
     * The formatter.
     */
    private static IFormatter sFormatter;

    /**
     * Static constructor.
     */
    static {
        sWriter = WriterFactory.getWriter();
        sFormatter = new SimpleExceptionFormatter();

        if (!Configuration.LOG_ENABLED) {
        	Logger.deleteLogs();
        }
    }

    /**
     * Initializes logger. Parameters won't be assigned if one of the parameters
     * have null value.
     *
     * @param initWriter Writer.
     * @param initFormatter Formatter.
     */
    public static void init(IWriter initWriter, IFormatter initFormatter) {
        if (initWriter == null || initFormatter == null) {
            return;
        }

        sWriter = initWriter;
        sFormatter = initFormatter;
    }

    /**
     * Retrieves all logged entities united to the string.
     *
     * @return Logged content.
     */
    public static synchronized String getContent() {
        return Configuration.LOG_ENABLED ? sWriter.getContent() : "";
    }

    /**
     * Logs exception with specified message.
     *
     * @param exception The exception.
     * @param friendlyMessage The friendly message.
     */
    public static void logApplicationException(Exception exception, String friendlyMessage) {
        if (Configuration.LOG_ENABLED) logEntry(exception, friendlyMessage);
    }

    /**
     * Logs message.
     *
     * @param friendlyMessage The friendly message.
     */
    public static void logMessage(String friendlyMessage) {
        if (Configuration.LOG_ENABLED) logEntry(null, friendlyMessage);
    }

    /**
     * Logs trace message.
     *
     * @param friendlyMessage The friendly message.
     */
    public static void logTraceMessage(String friendlyMessage) {
        if(!Configuration.TRACE_ENABLED || !Configuration.LOG_ENABLED) {
            return;
        }

        logEntry(null, friendlyMessage);
    }

    /**
     * Logs entry.
     *
     * @param exception The exception.
     * @param friendlyMessage The friendly message.
     */
    private static synchronized void logEntry(Exception exception, String friendlyMessage) {
        if (Configuration.LOG_ENABLED) sWriter.write(sFormatter.format(exception, friendlyMessage));
    }

    /**
     * Deletes logfile.
     */
    public static void deleteLogs() {
    	try {
        	if (!Configuration.LOG_ENABLED) {
        	    FileWriter.deleteLogFile();
            }
        } catch (final Exception e) {
        	e.printStackTrace();
        }
    }
}
