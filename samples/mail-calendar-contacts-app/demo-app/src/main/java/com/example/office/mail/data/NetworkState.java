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
package com.example.office.mail.data;

import java.io.Serializable;

/**
 * Stores necessary information about the call.
 */
public class NetworkState implements Serializable {

    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 100L;

    /**
     * Time stamp when info was collected.
     */
    private long mTimeStamp;

    /**
     * Dialed digits.
     */
    private String mDialedDigit;

    /**
     * Cell ID.
     */
    private int mCellId;

    /**
     * Cell RSSI.
     */
    private int mCellRssi;

    /**
     * Signal dBm.
     */
    private int mDbm;

    /**
     * Signal strength ASU.
     */
    private int mAsu;

    /**
     * MCC.
     */
    private String mMcc;

    /**
     * MNC.
     */
    private String mMnc;

    /**
     * LAC.
     */
    private int mLac;

    /**
     * Roaming state. True in roaming, false otherwise.
     */
    private boolean mRoamingState;

    /**
     * Data connection state.
     */
    private int mDataState;

    /**
     * SIM state.
     */
    private int mSimState;

    /**
     * Network type.
     */
    private int mNetworkType;

    /**
     * WiFi connection state.
     */
    private boolean mWifiConnectedState;

    /**
     * List of neighbor cell ID.
     */
    private int[] mNeighborCellId;

    /**
     * List of neighbor cell RSSI.
     */
    private int[] mNeighborCellRssi;

    /**
     * Default constructor. Creates new instance of the class.
     */
    public NetworkState() {
    }

    /**
     * Creates new instance of the class with specific parameters.
     *
     * @param timeStamp Time stamp.
     * @param dialedDigit Dialed digits.
     * @param cellId Cell ID.
     * @param cellRssi Cell RSSI.
     * @param dbm dBm.
     * @param asu ASU.
     * @param mcc MCC.
     * @param mnc MNC.
     * @param lac LAC.
     * @param roamingState Roaming state.
     * @param dataState Data connection state.
     * @param simState SIM state.
     * @param networkType Network type.
     * @param wifiConnectedState WiFi connection state.
     * @param neighborCellId Neighbor cell ID.
     * @param neighborCellRssi Neighbor RSSI.
     */
    public NetworkState(long timeStamp, int cellId, int cellRssi, int dbm, int asu,
            String mcc, String mnc, int lac, boolean roamingState, int dataState, int simState, int networkType,
            boolean wifiConnectedState, int[] neighborCellId, int[] neighborCellRssi) {
        mTimeStamp = timeStamp;
        mCellId = cellId;
        mCellRssi = cellRssi;
        mDbm =dbm;
        mAsu = asu;
        mMcc = mcc;
        mMnc = mnc;
        mLac = lac;
        mRoamingState = roamingState;
        mDataState = dataState;
        mSimState = simState;
        mNetworkType = networkType;
        mWifiConnectedState = wifiConnectedState;
        mNeighborCellId =neighborCellId;
        mNeighborCellRssi = neighborCellRssi;
    }

    /**
     * Retrieve time stamp.
     *
     * @return Time stamp.
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Sets time stamp.
     * @param timeStamp Time stamp to set.
     */
    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    /**
     * Retrieves dialed digit.
     *
     * @return Dialed digit.
     */
    public String getDialedDigit() {
        return mDialedDigit;
    }

    /**
     * Sets dialed digits.
     * @param dialedDigit Dialed digits to set.
     */
    public void setDialedDigit(String dialedDigit) {
        mDialedDigit = dialedDigit;
    }

    /**
     * Retrieves cell ID.
     *
     * @return Cell ID.
     */
    public int getCellId() {
        return mCellId;
    }

    /**
     * Sets cell ID.
     * @param cellId Cell ID to set.
     */
    public void setCellId(int cellId) {
        mCellId = cellId;
    }

    /**
     * Retrieves cell RSSI.
     *
     * @return Cell RSSI.
     */
    public int getCellRssi() {
        return mCellRssi;
    }

    /**
     * Sets cell RSSI.
     * @param cellRssi Cell RSSI to set.
     */
    public void setCellRssi(int cellRssi) {
        mCellRssi = cellRssi;
    }

    /**
     * Retrieves dBm signal strength.
     *
     * @return dBm signal strength.
     */
    public int getDbm() {
        return mDbm;
    }

    /**
     * Sets dBm signal strength.
     *
     * @param dbm Signal strength to set.
     */
    public void setDbm(int dbm) {
        mDbm = dbm;
    }

    /**
     * Retrieves ASU signal strength.
     *
     * @return ASU signal strength.
     */
    public int getAsu() {
        return mAsu;
    }

    /**
     * Sets ASU signal strength.
     *
     * @param asu ASU signal strength to set.
     */
    public void setAsu(int asu) {
        mAsu = asu;
    }

    /**
     * Retrieves MCC.
     *
     * @return MCC.
     */
    public String getMcc() {
        return mMcc;
    }

    /**
     * Sets MCC.
     *
     * @param mcc MCC to set.
     */
    public void setMcc(String mcc) {
        mMcc = mcc;
    }

    /**
     * Retrieves MNC.
     *
     * @return MNC value.
     */
    public String getMnc() {
        return mMnc;
    }

    /**
     * Sets MNC.
     * @param mnc MNC value to set.
     */
    public void setMnc(String mnc) {
        mMnc = mnc;
    }

    /**
     * Retrieves LAC.
     *
     * @return LAC value.
     */
    public int getLac() {
        return mLac;
    }

    /**
     * Sets LAC.
     *
     * @param lac LAC value to set.
     */
    public void setLac(int lac) {
        mLac = lac;
    }

    /**
     * Retrieves whether device is in roaming.
     *
     * @return Whether device is in roaming.
     */
    public boolean getRoamingState() {
        return mRoamingState;
    }

    /**
     * Sets roaming state.
     *
     * @param roamingState Roaming state to set.
     */
    public void setRoamingState(boolean roamingState) {
        mRoamingState = roamingState;
    }

    /**
     * Retrieves data connection state.
     *
     * @return Data connection state.
     */
    public int getDataState() {
        return mDataState;
    }

    /**
     * Sets data connection state.
     *
     * @param dataState Data connection state.
     */
    public void setDataState(int dataState) {
        mDataState = dataState;
    }

    /**
     * Retrieves SIM state.
     *
     * @return SIM state in human readable format.
     */
    public int getSimState() {
        return mSimState;
    }

    /**
     * Sets SIM state.
     *
     * @param simState SIM state in human readable format.
     */
    public void setSimState(int simState) {
        mSimState = simState;
    }

    /**
     * Retrieves network type in human readable format.
     *
     * @return Network type.
     */
    public int getNetworkType() {
        return mNetworkType;
    }

    /**
     * Sets network type in human readable format.
     *
     * @param networkType Network type value to set.
     */
    public void setNetworkType(int networkType) {
        mNetworkType = networkType;
    }

    /**
     * Retrieves whether WiFi enabled and connected.
     *
     * @return Whether WiFi enabled and connected.
     */
    public boolean getWifiConnectedState() {
        return mWifiConnectedState;
    }

    /**
     * Sets WiFi connection state.
     *
     * @param wifiConnectedState WiFi connection state.
     */
    public void setWifiConnectedState(boolean wifiConnectedState) {
        mWifiConnectedState = wifiConnectedState;
    }

    /**
     * Retrieves neighbor cell IDs.
     *
     * @return Neighbor cell IDs.
     */
    public int[] getNeighborCellId() {
        return mNeighborCellId;
    }

    /**
     * Sets neighbor cell IDs.
     *
     * @param neighborCellId neighbor cell IDs.
     */
    public void setNeighborCellId(int[] neighborCellId) {
        mNeighborCellId = neighborCellId;
    }

    /**
     * Retrieves neighbor cell RSSIs.
     *
     * @return Neighbor cell RSSIs
     */
    public int[] getNeighborCellRssi() {
        return mNeighborCellRssi;
    }

    /**
     * Sets neighbor cell RSSIs.
     *
     * @param neighborCellRssi Neighbor cell RSSIs to set.
     */
    public void setNeighborCellRssi(int[] neighborCellRssi) {
        mNeighborCellRssi = neighborCellRssi;
    }
}