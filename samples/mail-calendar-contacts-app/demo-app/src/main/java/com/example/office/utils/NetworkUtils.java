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

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.example.office.Configuration;
import com.example.office.Constants;
import com.example.office.OfficeApplication;
import com.example.office.logger.Logger;
import com.example.office.mail.data.NetworkState;

public class NetworkUtils {

    /**
     * Check data result: success.
     */
    public static int CHECK_DATA_RESULT_SUCCESS = 0;

    /**
     * Data connection state - Disconnected.
     */
    public static int NETWORK_UTILS_CONNECTION_STATE_DISCONNECTED = 1;

    /**
     * Data connection state - Connecting.
     */
    public static int NETWORK_UTILS_CONNECTION_STATE_CONNECTING = 2;

    /**
     * Data connection state - Connected.
     */
    public static int NETWORK_UTILS_CONNECTION_STATE_CONNECTED = 3;

    /**
     * Data connection state - Suspended.
     */
    public static int NETWORK_UTILS_CONNECTION_STATE_SUSPENDED = 4;

    /**
     * Data connection state - Unknown.
     */
    public static int NETWORK_UTILS_CONNECTION_STATE_UNKNOWN = 5;

    /**
     * SIM state - Unknown.
     */
    public static int NETWORK_UTILS_SIM_STATE_UNKNOWN = 1;

    /**
     * SIM state - Absent.
     */
    public static int NETWORK_UTILS_SIM_STATE_ABSENT = 2;

    /**
     * SIM state - PIN required.
     */
    public static int NETWORK_UTILS_SIM_STATE_PIN_REQUIRED = 3;

    /**
     * SIM state - PUK required.
     */
    public static int NETWORK_UTILS_SIM_STATE_PUK_REQUIRED = 4;

    /**
     * SIM state - Network Locked.
     */
    public static int NETWORK_UTILS_SIM_STATE_NETWORK_LOCKED = 5;

    /**
     * SIM state - Ready.
     */
    public static int NETWORK_UTILS_SIM_STATE_READY = 6;

    /**
     * Network type - Unknown.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_UNKNOWN = 1;

    /**
     * Network type - GPRS.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_GPRS = 2;

    /**
     * Network type - EDGE.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_EDGE = 3;

    /**
     * Network type - UMTS.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_UMTS = 4;

    /**
     * Network type - CDMA.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_CDMA = 5;

    /**
     * Network type - EVDO0.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_EVDO0 = 6;

    /**
     * Network type - EVDOA.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_EVDOA = 7;

    /**
     * Network type - 1xRTT.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_1XRTT = 8;

    /**
     * Network type - HSDPA.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_HSDPA = 9;

    /**
     * Network type - HSUPA.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_HSUPA = 10;

    /**
     * Network type - HSPA.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_HSPA = 11;

    /**
     * Network type - IDEN.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_IDEN = 12;

    /**
     * Network type - eHRPD.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_EHRPD = 13;

    /**
     * Network type - EvdoB.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_EVDOB = 14;

    /**
     * Network type - HSPA+.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_HSPA_PLUS = 15;

    /**
     * Network type - LTE.
     */
    public static int NETWORK_UTILS_NETWORK_TYPE_LTE = 16;

    /**
     * Retrieves network info data. *
     *
     * @return Current network info data.
     */
    public static NetworkState getNetworkState(Context context) {
        NetworkState callInfoData = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
            String mcc = "0";
            String mnc = "0";
            String networkOperator = telephonyManager.getNetworkOperator();
            if (networkOperator != null && networkOperator.length() > 0) {
                mcc = telephonyManager.getNetworkOperator().substring(0, 3);
                mnc = telephonyManager.getNetworkOperator().substring(3);
            }

            int[] neighborCellId;
            int[] neighborCellRssi;

            List<NeighboringCellInfo> neighboringList = telephonyManager.getNeighboringCellInfo();
            if (neighboringList != null && !neighboringList.isEmpty()) {
                neighborCellId = new int[neighboringList.size()];
                neighborCellRssi = new int[neighboringList.size()];
                for (int i = 0; i < neighboringList.size(); i++) {
                    neighborCellRssi[i] = neighboringList.get(i).getRssi();
                    neighborCellId[i] = neighboringList.get(i).getCid();
                }
            } else {
                neighborCellId = new int[] { -1 };
                neighborCellRssi = new int[] { -1 };
            }

            callInfoData = new NetworkState(System.currentTimeMillis(), (location != null) ? location.getCid() : 0, /* rssi, dbm, asu, */0, 0, 0,
                    mcc, mnc, (location != null) ? location.getLac() : 0, NetworkUtils.isInRoaming(context),
                    NetworkUtils.getDataConnectionState(context), NetworkUtils.getSimState(context), NetworkUtils.getNetworkType(context),
                    NetworkUtils.isWiFiEnabled(context), neighborCellId, neighborCellRssi);
        } catch (final Exception e) {
            Logger.logApplicationException(e, NetworkUtils.class.getSimpleName() + ".getNetworkInfoData(): Failed.");
            e.printStackTrace();
        }
        return callInfoData;
    }

    /**
     * Checks if SIM card is installed on a device.
     *
     * @return <code>True</code> if device has SIM card, <code>false</code> otherwise.
     */
    public static boolean isSimInstalled(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int state = manager.getSimState();

        if (state != TelephonyManager.SIM_STATE_READY) {
            return false;
        } else {
            return true;
        }
    }

    public static int getDataConnectionState(Context context) {
        if (!isSimInstalled(context)) {
            return NETWORK_UTILS_CONNECTION_STATE_DISCONNECTED;
        }

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        for (NetworkInfo info : manager.getAllNetworkInfo()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (info.getState() == State.CONNECTED) {
                    return NETWORK_UTILS_CONNECTION_STATE_CONNECTED;
                } else if (info.getState() == State.DISCONNECTED) {
                    return NETWORK_UTILS_CONNECTION_STATE_DISCONNECTED;
                } else if (info.getState() == State.CONNECTING) {
                    return NETWORK_UTILS_CONNECTION_STATE_CONNECTING;
                } else if (info.getState() == State.DISCONNECTING) {
                    return NETWORK_UTILS_CONNECTION_STATE_DISCONNECTED;
                } else if (info.getState() == State.SUSPENDED) {
                    return NETWORK_UTILS_CONNECTION_STATE_SUSPENDED;
                } else if (info.getState() == State.UNKNOWN) {
                    return NETWORK_UTILS_CONNECTION_STATE_UNKNOWN;
                }
            }
        }

        return NETWORK_UTILS_CONNECTION_STATE_DISCONNECTED;
    }

    /**
     * Retrieves SIM state name.
     *
     * @return SIM state name.
     */
    public static int getSimState(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telephonyManager.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return NETWORK_UTILS_SIM_STATE_UNKNOWN;
            case TelephonyManager.SIM_STATE_ABSENT:
                return NETWORK_UTILS_SIM_STATE_ABSENT;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return NETWORK_UTILS_SIM_STATE_PIN_REQUIRED;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return NETWORK_UTILS_SIM_STATE_PUK_REQUIRED;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return NETWORK_UTILS_SIM_STATE_NETWORK_LOCKED;
            case TelephonyManager.SIM_STATE_READY:
                return NETWORK_UTILS_SIM_STATE_READY;
        }
        return NETWORK_UTILS_SIM_STATE_UNKNOWN;
    }

    /**
     * Retrieves network type in human readable format.
     *
     * @return Network type in human readable format.
     */
    public static int getNetworkType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();

        switch (networkType) {
            case 7:
                return NETWORK_UTILS_NETWORK_TYPE_1XRTT;
            case 4:
                return NETWORK_UTILS_NETWORK_TYPE_CDMA;
            case 2:
                return NETWORK_UTILS_NETWORK_TYPE_EDGE;
            case 14:
                return NETWORK_UTILS_NETWORK_TYPE_EHRPD;
            case 5:
                return NETWORK_UTILS_NETWORK_TYPE_EVDO0;
            case 6:
                return NETWORK_UTILS_NETWORK_TYPE_EVDOA;
            case 12:
                return NETWORK_UTILS_NETWORK_TYPE_EVDOB;
            case 1:
                return NETWORK_UTILS_NETWORK_TYPE_GPRS;
            case 8:
                return NETWORK_UTILS_NETWORK_TYPE_HSDPA;
            case 10:
                return NETWORK_UTILS_NETWORK_TYPE_HSPA;
            case 15:
                return NETWORK_UTILS_NETWORK_TYPE_HSPA_PLUS;
            case 9:
                return NETWORK_UTILS_NETWORK_TYPE_HSUPA;
            case 11:
                return NETWORK_UTILS_NETWORK_TYPE_IDEN;
            case 13:
                return NETWORK_UTILS_NETWORK_TYPE_LTE;
            case 3:
                return NETWORK_UTILS_NETWORK_TYPE_UMTS;
            case 0:
                return NETWORK_UTILS_NETWORK_TYPE_UMTS;
        }

        return NETWORK_UTILS_NETWORK_TYPE_UNKNOWN;
    }

    /**
     * Checks if device is in roaming.
     *
     * @return <code>True</code> if device is in roaming network, <code>false</code> otherwise.
     */
    public static boolean isInRoaming(Context context) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return manager.isNetworkRoaming();
        } catch (final Exception e) {
            Logger.logApplicationException(e, NetworkUtils.class.getSimpleName() + ".isInRoaming(): Failed.");
        }
        return false;
    }

    /**
     * Retrieves SIM ICCID number.
     *
     * @return SIM ICCID number.
     */
    public static String getCurrentSimCardNumber() {
        try {
            if (Configuration.EMULATE_SIM_PRESENT) {
                return Constants.MOCK_SIM;
            } else {
                return OfficeApplication.getTelephonyManager().getSimSerialNumber();
            }
        } catch (final Exception e) {
            Logger.logApplicationException(e, NetworkUtils.class.getSimpleName() + ".getCurrentSimCardNumber(): Failed.");
        }
        return null;
    }

    /**
     * Checks wi-fi connection.
     *
     * @return <code>True</code> if Wi-Fi connection is open, <code>false</code> otherwise.
     */
    public static boolean isWiFiEnabled(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((info != null) && (info.isAvailable()) && (info.isConnected())) {
            return true;
        }

        return false;
    }
}
