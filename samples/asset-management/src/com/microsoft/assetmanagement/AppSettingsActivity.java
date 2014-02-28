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

// TODO: Auto-generated Javadoc
/**
 * The Class AppSettingsActivity.
 */
public class AppSettingsActivity extends PreferenceActivity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onAttachFragment(android.app.Fragment)
	 */
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppSettingsFragment fragment = new AppSettingsFragment();

		getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
		PreferenceManager.setDefaultValues(this, R.xml.auth_settings, false);

	}

	/**
	 * The Class AppSettingsFragment.
	 */
	public static class AppSettingsFragment extends PreferenceFragment {

		/** The m application. */
		private AssetApplication mApplication;

		/* (non-Javadoc)
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
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
