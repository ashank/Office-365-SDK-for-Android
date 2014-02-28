package com.microsoft.office365.demo;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;

import com.microsoft.adal.AuthenticationContext;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBar().getThemedContext(),
						android.R.layout.simple_list_item_1, android.R.id.text1, new String[] {
								getString(R.string.demo_option1)}), this);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {

		switch (position) {
        case 0:
            Fragment discoveryFragment = new DiscoveryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, discoveryFragment).commit();
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		AuthenticationContext authContext = ((DemoApplication) getApplication())
				.getAuthenticationContext();

		if (authContext != null) {
			authContext.onActivityResult(requestCode, resultCode, data);
		}
	}
}
