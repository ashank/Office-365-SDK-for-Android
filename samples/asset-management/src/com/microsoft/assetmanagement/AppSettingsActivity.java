/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class AppSettingsActivity extends PreferenceActivity {

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppSettingsFragment fragment = new AppSettingsFragment();

		getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
		PreferenceManager.setDefaultValues(this, R.xml.auth_settings, false);

	}

	public static class AppSettingsFragment extends PreferenceFragment {

		private AssetApplication mApplication;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.auth_settings);

			mApplication = (AssetApplication) this.getActivity().getApplication();

			Preference myPref = (Preference) findPreference("prefEraseSettings");
			
			myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {

					mApplication.clearPreferences();
					Toast.makeText(mApplication, "Cookies cleared!", Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		}
	}

}
