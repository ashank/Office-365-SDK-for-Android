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
package com.example.office.logger.writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Environment;

/**
 * Implements logger to store log in the file. Stores logs on SD card.
 */
public final class FileWriter implements IWriter {

    /**
     * File name to store log information.
     */
    private final static String LOG_FILE_NAME = "Office365MailDemoLog.txt";

    /**
     * Instance of the class.
     */
    private static FileWriter sInstance;

    /**
     * Private constructor to prevent creating an instance of the class.
     */
    private FileWriter() {
    }

    /**
     * Retrieves an instance of the class.
     *
     * @return Instance of the class.
     */
    public static FileWriter getInstance() {
        return sInstance == null ? sInstance = new FileWriter() : sInstance;
    }

    @Override
    public synchronized void write(String stringToWrite) {
        File logFile = getLogFile();
        if (logFile == null || stringToWrite == null) {
            return;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            fileOutputStream.write(stringToWrite.getBytes());
            fileOutputStream.close();
        } catch (final Exception e) {
        }
    }

    @Override
    public synchronized String getContent() {
        StringBuffer output = new StringBuffer();
        BufferedReader bufferedReader = null;
        try {
            File logFile = getLogFile();
            if (logFile == null) {
                return output.toString();
            }

            FileInputStream fileInputStream = new FileInputStream(logFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            String separator = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line);
                output.append(separator);
            }
        } catch (final Exception e) {
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }

        return output.toString();
    }

    /**
     * Retrieves log file or null if something goes wrong.
     *
     * @return Log file.
     */
    private static File getLogFile() {
        File file = null;
        try {
            String externalStorageState = Environment.getExternalStorageState();
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED) && externalStorageDirectory != null) {
                file = new File(externalStorageDirectory, LOG_FILE_NAME);
            }
        } catch (final Exception e) {
        }

        return file;
    }

    /**
     * Deletes log file.
     */
    public static void deleteLogFile() {
        try {
            File logFile = getLogFile();
            if (logFile != null && logFile.exists()) logFile.delete();
        } catch (final Exception e) {
        }
    }
}