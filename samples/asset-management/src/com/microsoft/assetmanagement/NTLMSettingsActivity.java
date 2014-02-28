/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.microsoft.assetmanagement.R;

public class NTLMSettingsActivity extends PreferenceActivity  {

	@Override
	public void onCreate(Bundle savedInstance) {

		super.onCreate(savedInstance);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment(), null).commit();
		PreferenceManager.setDefaultValues(this, R.xml.auth_settings, false);
	}

	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.ntlm_settings);
		}
	}
}
