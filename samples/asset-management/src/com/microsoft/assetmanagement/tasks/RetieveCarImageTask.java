package com.microsoft.assetmanagement.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.widget.EditText;

import com.microsoft.assetmanagement.AssetApplication;
import com.microsoft.assetmanagement.DisplayCarActivity;
import com.microsoft.assetmanagement.R;
import com.microsoft.assetmanagement.adapters.DisplayCarAdapter;
import com.microsoft.assetmanagement.files.SharepointListsClientWithFiles;
import com.microsoft.assetmanagement.files.SharepointListsClientWithFiles.DocumentLibraryItem;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.lists.SPListItem;

// TODO: Auto-generated Javadoc
/**
 * The Class RetieveCarImageTask.
 */
public class RetieveCarImageTask extends AsyncTask<String, Void, DocumentLibraryItem> {

	/** The m dialog. */
	private ProgressDialog mDialog;
	
	/** The m context. */
	private Context mContext;
	
	/** The m activity. */
	private DisplayCarActivity mActivity;
	
	/** The m application. */
	private AssetApplication mApplication;
	
	/** The m throwable. */
	private Throwable mThrowable;
	
	/** The m stored rotation. */
	private int mStoredRotation;
	
	/** The m list item. */
	private SPListItem mListItem;
	
	/** The m document library item. */
	private DocumentLibraryItem mDocumentLibraryItem;
	
	/**
	 * Instantiates a new retieve car image task.
	 *
	 * @param activity the activity
	 * @param listItem the list item
	 */
	public RetieveCarImageTask(DisplayCarActivity activity, SPListItem listItem) {
		mActivity = activity;
		mContext = activity;
		mDialog = new ProgressDialog(mContext);
		mApplication = (AssetApplication) activity.getApplication();
		mListItem = listItem;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {

		mStoredRotation = mActivity.getRequestedOrientation();
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		mDialog.setTitle("Retrieving image...");
		mDialog.setMessage("Please wait.");
		mDialog.setCancelable(false);
		mDialog.setIndeterminate(true);
		mDialog.show();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(final DocumentLibraryItem image) {
		if (mDialog.isShowing()) {
			mDialog.dismiss();
			mActivity.setRequestedOrientation(mStoredRotation);
		}
		if (image == null) {
			mApplication.handleError(mThrowable);
		}
		else{
			CarListViewItem carViewItem = new CarListViewItem(mListItem, mDocumentLibraryItem.getContent());
			mActivity.setCarViewItem(carViewItem);
			EditText carTitle = (EditText) mActivity.findViewById(R.id.textCarTitle);;
			EditText carDescription = (EditText) mActivity.findViewById(R.id.textCarDescription);
			carTitle.setText(carViewItem.getData("Title"));
			carDescription.setText(carViewItem.getData("Description"));
			DisplayCarAdapter listAdapter = new DisplayCarAdapter(mActivity, carViewItem);
			ViewPager viewPager = (ViewPager) mActivity.findViewById(R.id.view_pager);
			viewPager.setAdapter(listAdapter);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected DocumentLibraryItem doInBackground(String... arg) {

		final SharepointListsClientWithFiles client = (SharepointListsClientWithFiles) mApplication.getCurrentListClient();
		final String listName = mApplication.getPreferences().getLibraryName();
		
		OfficeFuture<DocumentLibraryItem> item = client.getFileFromDocumentLibrary(listName,arg[0]);
		
		try {
			return mDocumentLibraryItem = item.get();
		}  catch (Exception e) {
			mThrowable = e;
			return null;
		}
	}
}