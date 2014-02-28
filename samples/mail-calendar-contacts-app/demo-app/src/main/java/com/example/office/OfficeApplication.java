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
package com.example.office;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;

import com.example.office.logger.Logger;

/**
 * Implements the application instance.
 *
 * Gives access to application context and Android managers such as connectivity manager, telephony manager, etc.
 */
public class OfficeApplication extends Application {

    /**
     * Application core package name.
     */
    private static final String PACKAGE_NAME = "com.example.office";

    /**
     * The application instance.
     */
    private static OfficeApplication sApplication;

    /**
     * Connectivity Manager Service.
     */
    private static ConnectivityManager sConnectivityManager;

    /**
     * Telephony Manager service.
     */
    private static TelephonyManager sTelephonyManager;

    /**
     * Notification manager service.
     */
    private static NotificationManager sNotificationManager;

    /**
     * Layout Inflater service.
     */
    private static LayoutInflater sLayoutInflater;

    /**
     * Location manager service.
     */
    private static LocationManager sLocationManager;

    /**
     * Power manager.
     */
    private static PowerManager sPowerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    /**
     * Retrieves context for the application.
     *
     * @return context for the application.
     */
    public static Context getContext() {
        return sApplication.getBaseContext();
    }

    /**
     * Returns an instance of connectivity manager service.
     *
     * @return Valid {@link ConnectivityManager} service object.
     */
    public static ConnectivityManager getConnectivityManager() {
        return sConnectivityManager == null ? sConnectivityManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) : sConnectivityManager;
    }

    /**
     * Returns an instance of telephony manager service.
     *
     * @return Valid {@link TelephonyManager} service object.
     */
    public static TelephonyManager getTelephonyManager() {
        return sTelephonyManager == null ? sTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE)
                : sTelephonyManager;
    }

    /**
     * Retrieves notification manager.
     *
     * @return Notification manager.
     */
    public static NotificationManager getNotificationManager() {
        return sNotificationManager == null ? sNotificationManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) : sNotificationManager;
    }

    /**
     * Returns an instance of layout inflater service.
     *
     * @return Valid {@link LayoutInflater} service object.
     */
    public static LayoutInflater getLayoutInflater() {
        return sLayoutInflater == null ? sLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                : sLayoutInflater;
    }

    /**
     * Retrieves an instance of location manager service.
     *
     * @return An instance of location manager service.
     */
    public static LocationManager getLocationManager() {
        return sLocationManager == null ? sLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE)
                : sLocationManager;
    }

    /**
     * Retrieves an instance of the power manager.
     *
     * @return An instance of the power manager.
     */
    public static PowerManager getPowerManager() {
        return sPowerManager == null ? sPowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE) : sPowerManager;
    }

    /**
     * Retrieves application version from manifest file.
     *
     * @return Application version defined in manifest file.
     */
    public final static String getVersion() {
        try {
            PackageInfo pinfo = sApplication.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return pinfo.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Logger.logApplicationException(e, OfficeApplication.class.getSimpleName() + ".getVersion(): Failed.");
        }

        return null;
    }

    /**
     * Retrieves application version code from manifest file.
     *
     * @return Application version code defined in manifest file, otherwise returns <code>0<code>.
     */
    public final static int getVersionCode() {
        try {
            PackageInfo pinfo = sApplication.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return pinfo.versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Logger.logApplicationException(e, OfficeApplication.class.getSimpleName() + ".getVersionCode(): Failed.");
        }

        return 0;
    }
}
