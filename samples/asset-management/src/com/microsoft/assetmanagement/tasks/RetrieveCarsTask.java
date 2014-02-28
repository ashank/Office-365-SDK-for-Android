/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.tasks;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.microsoft.assetmanagement.AssetApplication;
import com.microsoft.assetmanagement.CarListActivity;
import com.microsoft.assetmanagement.adapters.CarItemAdapter;
import com.microsoft.assetmanagement.datasource.ListItemsDataSource;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;

public class RetrieveCarsTask extends AsyncTask<String, Void, ArrayList<CarListViewItem>> {

	private ProgressDialog mDialog;
	private Context mContext;
	private CarListActivity mActivity;
	private ListItemsDataSource mSource;
	private AssetApplication mApplication;
	private Throwable mThrowable;
	private int mStoredRotation;

	public RetrieveCarsTask(CarListActivity activity) {
		mActivity = activity;
		mContext = activity;
		mDialog = new ProgressDialog(mContext);
		mApplication = (AssetApplication) activity.getApplication();
		mSource = new ListItemsDataSource(mApplication);
	}

	protected void onPreExecute() {

		mStoredRotation = mActivity.getRequestedOrientation();
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		mDialog.setTitle("Retrieving cars...");
		mDialog.setMessage("Please wait.");
		mDialog.setCancelable(false);
		mDialog.setIndeterminate(true);
		mDialog.show();
	}

	@Override
	protected void onPostExecute(final ArrayList<CarListViewItem> carItems) {
		if (mDialog.isShowing()) {
			mDialog.dismiss();
			mActivity.setRequestedOrientation(mStoredRotation);
		}
		if (carItems != null) {

			CarItemAdapter adapter = new CarItemAdapter(mActivity, carItems);
			mActivity.setListAdapter(adapter);
			adapter.notifyDataSetChanged();
			Toast.makeText(mContext, "Finished loading cars", Toast.LENGTH_LONG).show();
		} else {
			mApplication.handleError(mThrowable);
		}
	}

	protected ArrayList<CarListViewItem> doInBackground(final String... args) {
		try {
			ArrayList<CarListViewItem> items = mSource.getDefaultListViewItems();
			return items;
		} catch (Exception e) {
			mThrowable = e;
			return null;
		}
	}
}
