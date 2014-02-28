/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.microsoft.office365.Action;
import com.microsoft.office365.Credentials;

// TODO: Auto-generated Javadoc
/**
 * The Class MainActivity.
 */
public class MainActivity extends Activity {

	/** The m application. */
	private AssetApplication mApplication;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mApplication = (AssetApplication) getApplication();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences: {
			startActivity(new Intent(this, AppSettingsActivity.class));
			return true;
		}
		case R.id.menu_show_cars: {
			boolean hasConfig = mApplication.hasConfigurationSettings()
					&& mApplication.hasDefaultList();
			if (hasConfig) {
				try {
					mApplication.authenticate(this).done(new Action<Credentials>() {
						@Override
						public void run(Credentials obj) throws Exception {
							startActivity(new Intent(MainActivity.this,
									CarListActivity.class));
						}
					});
				} catch (Throwable t) {
					Log.e("Asset", t.getMessage());
				}
			} else {
				checkPreferences();
			}
			return true;
		}
		default:
			return true;
		}
	}

	/**
	 * Check preferences.
	 */
	private void checkPreferences() {
		Intent i = null;
		boolean hasConfig = mApplication.hasConfigurationSettings();
		if (!hasConfig) {
			i = new Intent(MainActivity.this, AppSettingsActivity.class);
			startActivity(i);
		}
	}
}
