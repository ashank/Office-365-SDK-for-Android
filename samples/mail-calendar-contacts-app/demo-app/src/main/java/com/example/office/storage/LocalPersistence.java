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
package com.example.office.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;

import com.example.office.logger.Logger;

/**
 * Serializes/deserializes an object to/from a private local file.
 */
public class LocalPersistence {

    /**
     * Serializes an object to a private local file.
     *
     * @param context Application context.
     * @param object Object to serialize.
     * @param filename Filename to save to object to.
     *
     * @throws IOException
     */
    public static void writeObjectToFile(Context context, Object object, String filename) throws IOException {

        ObjectOutputStream objectOut = null;
        try {
            FileOutputStream fileOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            fileOut.getFD().sync();

        } catch (IOException ex) {
            Logger.logApplicationException(ex, LocalPersistence.class.getSimpleName() + ".writeObjectToFile(): Error.");
        } finally {
            if (objectOut != null) {
                objectOut.close();
            }
        }
    }

    /**
     * Deserializes an object from a private local file.
     *
     * @param context Application context.
     * @param filename Filename to load to object from.
     *
     * @return Deserialized object.
     *
     * @throws IOException
     */
    public static Object readObjectFromFile(Context context, String filename) throws IOException {
        ObjectInputStream objectIn = null;
        Object object = null;

        try {
            FileInputStream fileIn = context.getApplicationContext().openFileInput(filename);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();
        } catch (Exception ex) {
        	Logger.logApplicationException(ex, LocalPersistence.class.getSimpleName() + ".readObjectFromFile(): Error.");
        } finally {
            if (objectIn != null) {
                objectIn.close();
            }
        }

        return object;
    }
}
