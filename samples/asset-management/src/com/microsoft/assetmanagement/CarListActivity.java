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

public class CarListActivity extends FragmentActivity {

	private static final String ITEM = "item";
	private static final String ITEM_EXTRA = "data";

	private ListView mListView;
	private AssetApplication mApplication;

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

	@Override
	public void onBackPressed() {
		NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.car_list_menu, menu);
		return true;
	}

	public void setListAdapter(CarItemAdapter adapter) {
		mListView.setAdapter(adapter);
	}

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
