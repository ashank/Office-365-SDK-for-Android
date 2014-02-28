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

// TODO: Auto-generated Javadoc
/**
 * The Class RetrieveCarsTask.
 */
public class RetrieveCarsTask extends AsyncTask<String, Void, ArrayList<CarListViewItem>> {

	/** The m dialog. */
	private ProgressDialog mDialog;
	
	/** The m context. */
	private Context mContext;
	
	/** The m activity. */
	private CarListActivity mActivity;
	
	/** The m source. */
	private ListItemsDataSource mSource;
	
	/** The m application. */
	private AssetApplication mApplication;
	
	/** The m throwable. */
	private Throwable mThrowable;
	
	/** The m stored rotation. */
	private int mStoredRotation;

	/**
	 * Instantiates a new retrieve cars task.
	 *
	 * @param activity the activity
	 */
	public RetrieveCarsTask(CarListActivity activity) {
		mActivity = activity;
		mContext = activity;
		mDialog = new ProgressDialog(mContext);
		mApplication = (AssetApplication) activity.getApplication();
		mSource = new ListItemsDataSource(mApplication);
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {

		mStoredRotation = mActivity.getRequestedOrientation();
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		mDialog.setTitle("Retrieving cars...");
		mDialog.setMessage("Please wait.");
		mDialog.setCancelable(false);
		mDialog.setIndeterminate(true);
		mDialog.show();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
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
