/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.tasks;

import com.microsoft.assetmanagement.AssetApplication;
import com.microsoft.assetmanagement.datasource.ListItemsDataSource;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

public class SaveCarTask extends AsyncTask<CarListViewItem, Void, Void> {

	private ListItemsDataSource mSource;
	private Activity mActivity;
	private ProgressDialog mDialog;
	private AssetApplication mApplication;
	private Throwable mThrowable;

	public SaveCarTask(Activity activity) {
		mActivity = activity;
		mDialog = new ProgressDialog(mActivity);
		mApplication = (AssetApplication) activity.getApplication();
		mSource = new ListItemsDataSource(mApplication);
	}

	protected void onPreExecute() {
		mDialog.setTitle("Saving car...");
		mDialog.setMessage("Please wait.");
		mDialog.setCancelable(false);
		mDialog.setIndeterminate(true);
		mDialog.show();
	}

	@Override
	protected void onPostExecute(Void result) {
		if (mDialog.isShowing()) {
			mDialog.dismiss();
		}

		if (mThrowable == null) {
			Toast.makeText(mActivity, "Car saved.", Toast.LENGTH_SHORT).show();
		} else {
			mApplication.handleError(mThrowable);
		}
		NavUtils.navigateUpFromSameTask(mActivity);
	}

	@Override
	protected Void doInBackground(CarListViewItem... params) {
		CarListViewItem viewItem = params[0];
		if (viewItem != null) {
			try {
				mSource.saveNewCar(viewItem);
			} catch (Throwable t) {
				mThrowable = t;
			}
		} else {
			mThrowable = new IllegalArgumentException(
					"params argument must contain at least a CarListViewItem");
		}
		return null;
	}
}
