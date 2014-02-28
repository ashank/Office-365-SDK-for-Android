package com.microsoft.office365.test.integration.android;

import java.util.concurrent.Future;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.microsoft.office365.Action;
import com.microsoft.office365.LogLevel;
import com.microsoft.office365.Logger;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.files.FileClient;
import com.microsoft.office365.http.CookieCredentials;
import com.microsoft.office365.http.SharepointCookieCredentials;
import com.microsoft.office365.lists.SharepointListsClient;
import com.microsoft.office365.test.integration.TestPlatformContext;
import com.microsoft.office365.test.integration.framework.TestCase;
import com.microsoft.office365.test.integration.framework.TestExecutionCallback;
import com.microsoft.office365.test.integration.framework.TestResult;

public class AndroidTestPlatformContext implements TestPlatformContext {

	private static Activity mActivity;

	public AndroidTestPlatformContext(Activity activity) {
		mActivity = activity;
	}

	@Override
	public Logger getLogger() {
		return new Logger() {

			@Override
			public void log(String message, LogLevel level) {
				Log.d(Constants.TAG, level.toString() + ": " + message);
			}
		};
	}

	@Override
	public String getServerUrl() {
		return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(
				Constants.PREFERENCE_SHAREPOINT_URL, "");
	}

	@Override
	public String getTestListName() {
		return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(
				Constants.PREFERENCE_LIST_NAME, "");
	}

	@Override
	public String getSiteRelativeUrl() {
		return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(
				Constants.PREFERENCE_SITE_URL, "");
	}

	@Override
	public Future<Void> showMessage(final String message) {
		final OfficeFuture<Void> result = new OfficeFuture<Void>();

		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

				builder.setTitle("Message");
				builder.setMessage(message);
				builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.setResult(null);
					}
				});

				builder.create().show();
			}
		});

		return result;
	}

	@Override
	public void executeTest(final TestCase testCase, final TestExecutionCallback callback) {
		AsyncTask<Void, Void, TestResult> task = new AsyncTask<Void, Void, TestResult>() {

			@Override
			protected TestResult doInBackground(Void... params) {
				return testCase.executeTest();
			}

			@Override
			protected void onPostExecute(TestResult result) {
				callback.onTestComplete(testCase, result);
			}
		};

		task.execute();
	}

	@Override
	public void sleep(int seconds) throws Exception {
		Thread.sleep(seconds * 1000);
	}

	@Override
	public SharepointListsClient getListsClient() {

		final OfficeFuture<SharepointListsClient> clientFuture = new OfficeFuture<SharepointListsClient>();

		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				OfficeFuture<CookieCredentials> future = SharepointCookieCredentials
						.requestCredentials(getServerUrl(), mActivity);

				future.done(new Action<CookieCredentials>() {

					@Override
					public void run(CookieCredentials credentials) throws Exception {
						SharepointListsClient client = new SharepointListsClient(getServerUrl(),
								getSiteRelativeUrl(), credentials, getLogger());
						clientFuture.setResult(client);
					}
				});

			}
		});

		try {
			return clientFuture.get();
		} catch (Throwable t) {
			Log.e(Constants.TAG, t.getMessage());
			return null;
		}
	}

	@Override
	public FileClient getFileClient() {
		
		final OfficeFuture<FileClient> clientFuture = new OfficeFuture<FileClient>();

		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				OfficeFuture<CookieCredentials> future = SharepointCookieCredentials
						.requestCredentials(getServerUrl(), mActivity);

				future.done(new Action<CookieCredentials>() {

					@Override
					public void run(CookieCredentials credentials) throws Exception {
						FileClient client = new FileClient(getServerUrl(),
								getSiteRelativeUrl(), credentials, getLogger());
						clientFuture.setResult(client);
					}
				});

			}
		});

		try {
			return clientFuture.get();
		} catch (Throwable t) {
			Log.e(Constants.TAG, t.getMessage());
			return null;
		}
	}
}
