package com.microsoft.office365.demo;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.microsoft.adal.AuthenticationCallback;
import com.microsoft.adal.AuthenticationContext;
import com.microsoft.adal.AuthenticationResult;
import com.microsoft.office365.OfficeClient;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.files.FileClient;
import com.microsoft.office365.http.OAuthCredentials;

public class DemoApplication extends Application {
	public AuthenticationContext getAuthenticationContext() {
	    AuthenticationContext context = null;
	    try {
            context = new AuthenticationContext(this, Constants.AUTHORITY_URL, false);
        } catch (Exception e) {
        }
	    
	    return context;
	}

	@Override
	public void onCreate() {
		Log.d("Asset Management", "onCreate");
		super.onCreate();
	}

	public OfficeFuture<OfficeClient> getOfficeClient(final Activity activity, String resourceId) {
        final OfficeFuture<OfficeClient> future = new OfficeFuture<OfficeClient>();

        try {
            getAuthenticationContext().acquireToken(activity, resourceId,
                    Constants.CLIENT_ID, Constants.REDIRECT_URL, "",
                    new AuthenticationCallback<AuthenticationResult>() {

                        @Override
                        public void onError(Exception exc) {
                            future.triggerError(exc);
                        }

                        @Override
                        public void onSuccess(AuthenticationResult result) {
                            OAuthCredentials credentials = new OAuthCredentials(result
                                    .getAccessToken());
                            
                            OfficeClient client = new OfficeClient(credentials);
                            future.setResult(client);
                        }
                    });

        } catch (Throwable t) {
            future.triggerError(t);
        }
        return future;
    }
	
	public OfficeFuture<FileClient> getFileClient(final Activity activity, String resourceId, final String sharepointUrl) {
        final OfficeFuture<FileClient> future = new OfficeFuture<FileClient>();

        try {
            getAuthenticationContext().acquireToken(activity, resourceId,
                    Constants.CLIENT_ID, Constants.REDIRECT_URL, "",
                    new AuthenticationCallback<AuthenticationResult>() {

                        @Override
                        public void onError(Exception exc) {
                            future.triggerError(exc);
                        }

                        @Override
                        public void onSuccess(AuthenticationResult result) {
                            OAuthCredentials credentials = new OAuthCredentials(result
                                    .getAccessToken());
                            
                            FileClient client = new FileClient(sharepointUrl, "", credentials);
                            future.setResult(client);
                        }
                    });

        } catch (Throwable t) {
            future.triggerError(t);
        }
        return future;
    }
}
