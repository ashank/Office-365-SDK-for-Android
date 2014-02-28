/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

public class AssetPreferences {

	private SharedPreferences mPreferences;

	public static final String SHAREPOINT_SITE_LISTS = "prefSharepointLists";

	public AssetPreferences(Context context, SharedPreferences preferences) {
		mPreferences = preferences;
	}

	public String getLibraryName() {
		String library = mPreferences.getString("prefLibraryName", null);
		return library;
	}

	public int getListDisplaySize() {
		return Integer.parseInt(mPreferences.getString("prefListSize", "20"));
	}

	public String getAuthenticationMethod() {
		return mPreferences.getString("listPref", null);
	}

	public String getSharepointServer() {
		return mPreferences.getString("prefSharepointUrl", null);
	}

	public String getSiteRelativeUrl() {
		return mPreferences.getString("prefSiteRelativeUrl", null);
	}

	public String getNTLMUser() {
		return mPreferences.getString("prefNTLMUser", null);
	}

	public String getNTLMPassword() {
		return mPreferences.getString("prefNTLMPassword", null);
	}

	public String getAuthorityUrl() {
		return mPreferences.getString("prefOauthAuthorityUrl", null);
	}

	public String getClientId() {
		return mPreferences.getString("prefOauthClientId", null);
	}

	public String getResourceUrl() {
		return mPreferences.getString("prefOauthResourceUrl", null);
	}

	public String getUserHint() {
		return mPreferences.getString("prefUserHint", null);
	}

	public void setDefaultSharepointList(String listName) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString("prefDefaultList", listName);
		editor.commit();
	}

	public void storeSharepointListUrl(String listName) {
		ArrayList<String> listNames = getStringArrayPref(SHAREPOINT_SITE_LISTS);
		listNames.add(listName);
		setStringArrayPref(SHAREPOINT_SITE_LISTS, listNames);
		setDefaultSharepointList(listName);
	}

	public ArrayList<String> getSharepointListNames() {
		return getStringArrayPref(SHAREPOINT_SITE_LISTS);
	}

	public void clear() {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.clear();
		editor.commit();
	}

	private void setStringArrayPref(String key, ArrayList<String> values) {
		SharedPreferences.Editor editor = mPreferences.edit();
		JSONArray a = new JSONArray();
		for (int i = 0; i < values.size(); i++) {
			a.put(values.get(i));
		}
		if (!values.isEmpty()) {
			editor.putString(key, a.toString());
		} else {
			editor.putString(key, null);
		}
		editor.commit();
	}

	private ArrayList<String> getStringArrayPref(String key) {
		String json = mPreferences.getString(key, null);
		ArrayList<String> urls = new ArrayList<String>();
		if (json != null) {
			try {
				JSONArray a = new JSONArray(json);
				for (int i = 0; i < a.length(); i++) {
					String url = a.optString(i);
					urls.add(url);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}
}
