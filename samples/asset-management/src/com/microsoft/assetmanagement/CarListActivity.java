/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.microsoft.assetmanagement.adapters.CarItemAdapter;
import com.microsoft.assetmanagement.tasks.RetrieveCarsTask;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;

// TODO: Auto-generated Javadoc
/**
 * The Class CarListActivity.
 */
public class CarListActivity extends FragmentActivity {

	/** The Constant ITEM. */
	private static final String ITEM = "item";
	
	/** The Constant ITEM_EXTRA. */
	private static final String ITEM_EXTRA = "data";

	/** The m list view. */
	private ListView mListView;
	
	/** The m application. */
	private AssetApplication mApplication;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_sharepoint_lists);

		new RetrieveCarsTask(CarListActivity.this).execute();

		mApplication = (AssetApplication) getApplication();
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View arg1, int position, long arg3) {
				openSelectedCar(position);
			}
		});
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home: {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		case R.id.menu_new_car: {
			startActivity(new Intent(CarListActivity.this, DisplayCarActivity.class));
			return true;
		}
		case R.id.menu_refresh: {
			new RetrieveCarsTask(CarListActivity.this).execute();
			return true;
		}
		default:
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		NavUtils.navigateUpFromSameTask(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.car_list_menu, menu);
		return true;
	}

	/**
	 * Sets the list adapter.
	 *
	 * @param adapter the new list adapter
	 */
	public void setListAdapter(CarItemAdapter adapter) {
		mListView.setAdapter(adapter);
	}

	/**
	 * Open selected car.
	 *
	 * @param position the position
	 */
	public void openSelectedCar(int position) {
	    CarListViewItem carItem = (CarListViewItem) mListView.getItemAtPosition(position);
		Intent i = new Intent(this, DisplayCarActivity.class);
		JSONObject payload = new JSONObject();
		try {
			JSONObject itemJson = new JSONObject(carItem.getListItem().toString());
			payload.put(ITEM, itemJson);
			i.putExtra(ITEM_EXTRA, payload.toString());
			startActivity(i);
		} catch (Throwable t) {
			mApplication.handleError(t);
		}
	}
}
