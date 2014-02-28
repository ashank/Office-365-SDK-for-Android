/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.microsoft.office365.Action;
import com.microsoft.office365.ErrorCallback;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.Platform;

@SuppressLint("SetJavaScriptEnabled")
public class SharepointOnlineCredentials extends OAuthCredentials {

	private static final String AUTHORIZATION_CODE_REQUEST_URL_FORMAT = "%s_layouts/15/OAuthAuthorize.aspx?mobile=0&client_id=%s&scope=List.Write Web.Write&response_type=code&redirect_uri=%s";
	private static final String ACCESS_TOKEN_REQUEST_URL_FORMAT = "https://accounts.accesscontrol.windows.net/%s/tokens/OAuth/2";
	private static final String ACCESS_TOKEN_REQUEST_CONTENT_WITH_AUTHORIZATION_CODE = "grant_type=authorization_code&client_id=%s%%40%s&client_secret=%s&code=%s&redirect_uri=%s&resource=00000003-0000-0ff1-ce00-000000000000%%2F%s%%40%s";
	private static final String ACCESS_TOKEN_REQUEST_CONTENT_WITH_REFRESH_TOKEN= "grant_type=refresh_token&client_id=%s%%40%s&client_secret=%s&refresh_token=%s&redirect_uri=%s&resource=00000003-0000-0ff1-ce00-000000000000%%2F%s%%40%s";
	private static final String ACCESS_TOKEN_NODE =  "access_token";
	private static final String REFRESH_TOKEN_NODE =  "refresh_token";
	
	private String mToken;
	private String mRefreshToken;
	
	public SharepointOnlineCredentials(String oAuthToken, String refreshToken) {
		super(oAuthToken);
		mToken = oAuthToken;
		mRefreshToken = refreshToken;
	}
	
	@Override
	public String getToken() {
		return mToken;
	}
	
	public static OfficeFuture<SharepointOnlineCredentials> requestCredentials(Activity activity, 
						String siteUrl, final String clientId, final String redirectUrl, 
						final String office365Domain, final String clientSecret, final String refreshToken) throws MalformedURLException {
		
		final OfficeFuture<SharepointOnlineCredentials> credentialsFuture = new OfficeFuture<SharepointOnlineCredentials>();
		
		URL url = new URL(siteUrl);
		final String sharepointHost = url.getHost();
		
		String authCodeUrl = String.format(AUTHORIZATION_CODE_REQUEST_URL_FORMAT, siteUrl, clientId, redirectUrl);
		
		OfficeFuture<String> codeFuture;
		
		if (refreshToken == null) {
			codeFuture = showLoginForAcccessCode(activity, authCodeUrl, clientId, redirectUrl);
		} else {
			codeFuture = new OfficeFuture<String>();
			codeFuture.setResult(null);
		}
		
		codeFuture.done(new Action<String>() {
			
			@Override
			public void run(String accessCode) throws Exception {
				HttpConnection connection = Platform.createHttpConnection();
				
				Request get = new Request("POST");
				get.addHeader("Content-Type", "application/x-www-form-urlencoded");
				String accessTokenRequestUrl = String.format(ACCESS_TOKEN_REQUEST_URL_FORMAT, office365Domain);
				get.setUrl(accessTokenRequestUrl);
				
				String requestContent;
				
				if (refreshToken == null) {
					requestContent = String.format(ACCESS_TOKEN_REQUEST_CONTENT_WITH_AUTHORIZATION_CODE, encode(clientId), office365Domain, encode(clientSecret), accessCode, redirectUrl, sharepointHost, office365Domain);
				} else {
					requestContent = String.format(ACCESS_TOKEN_REQUEST_CONTENT_WITH_REFRESH_TOKEN, encode(clientId), office365Domain, encode(clientSecret), refreshToken, redirectUrl, sharepointHost, office365Domain);
				}
				
				get.setContent(requestContent);
				
				HttpConnectionFuture accessTokenFuture = connection.execute(get);
				
				accessTokenFuture.onError(new ErrorCallback() {
					
					@Override
					public void onError(Throwable error) {
						credentialsFuture.triggerError(error);
					}
				});
				
				accessTokenFuture.onTimeout(new ErrorCallback() {
					
					@Override
					public void onError(Throwable error) {
						credentialsFuture.triggerError(error);
					}
				});
				
				accessTokenFuture.done(new Action<Response>() {
					
					@Override
					public void run(Response accessTokenResponse) throws Exception {
						String content = accessTokenResponse.readToEnd();
						
						JSONObject json = new JSONObject(content);
						
						String accessToken = json.getString(ACCESS_TOKEN_NODE);
						
						String newRefreshToken;
						if (json.has(REFRESH_TOKEN_NODE)) {
							newRefreshToken = json.getString(REFRESH_TOKEN_NODE);
						} else {
							newRefreshToken = refreshToken;
						}
						
						credentialsFuture.setResult(new SharepointOnlineCredentials(accessToken, newRefreshToken));
					}
				});
			}
		});
		
		codeFuture.onError(new ErrorCallback() {
			
			@Override
			public void onError(Throwable error) {
				credentialsFuture.triggerError(error);
			}
		});
		
		return credentialsFuture;
	}
	

	protected static String encode(String clientId) {
		@SuppressWarnings("deprecation")
		String encoded = URLEncoder.encode(clientId);
		return encoded;
	}

	protected static OfficeFuture<String> showLoginForAcccessCode(Activity activity,
			final String startUrl, String clientId, final String endUrl) {
		
		final OfficeFuture<String> codeFuture = new OfficeFuture<String>();
		
		if (startUrl == null || startUrl == "") {
			throw new IllegalArgumentException(
					"startUrl can not be null or empty");
		}

		if (endUrl == null || endUrl == "") {
			throw new IllegalArgumentException(
					"endUrl can not be null or empty");
		}

		if (activity == null) {
			throw new IllegalArgumentException("activity can not be null");
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		// Create the Web View to show the login page
		final WebView wv = new WebView(activity);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				codeFuture.triggerError(new Exception("User cancelled"));
			}
		});
		
		
		wv.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36");
		wv.getSettings().setJavaScriptEnabled(true);
		
		wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		wv.getSettings().setLoadWithOverviewMode(true);
		wv.getSettings().setUseWideViewPort(true);
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int webViewHeight = displaymetrics.heightPixels;
		
		wv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, webViewHeight));
		

		wv.requestFocus(View.FOCUS_DOWN);
		wv.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN
						|| action == MotionEvent.ACTION_UP) {
					if (!view.hasFocus()) {
						view.requestFocus();
					}
				}

				return false;
			}
		});

		// Create a LinearLayout and add the WebView to the Layout
		LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(wv);

		// Add a dummy EditText to the layout as a workaround for a bug
		// that prevents showing the keyboard for the WebView on some devices
		EditText dummyEditText = new EditText(activity);
		dummyEditText.setVisibility(View.GONE);
		layout.addView(dummyEditText);

		// Add the layout to the dialog
		builder.setView(layout);

		final AlertDialog dialog = builder.create();

		wv.setWebViewClient(new WebViewClient() {
			
			boolean mResultReturned = false;
			Object mSync = new Object();

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				synchronized (mSync) {
					// If the URL of the started page matches with the final URL
					// format, the login process finished
					if (isFinalUrl(url) && !mResultReturned) {
						mResultReturned = true;
						String code = url.replace(endUrl + "?code=", "");
						dialog.dismiss();
						codeFuture.setResult(code);
					}

					super.onPageStarted(view, url, favicon);					
				}
			}

			// Checks if the given URL matches with the final URL's format
			private boolean isFinalUrl(String url) {
				if (url == null) {
					return false;
				}

				return url.startsWith(endUrl);
			}

			/*
			// Checks if the given URL matches with the start URL's format
			private boolean isStartUrl(String url) {
				if (url == null) {
					return false;
				}

				return url.startsWith(startUrl);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (isStartUrl(url)) {
					if (externalCallback != null) {
						externalCallback
								.onCompleted(
										null,
										new MobileServiceException(
												"Logging in with the selected authentication provider is not enabled"));
					}

					dialog.dismiss();
				}
			}*/
		});

		wv.loadUrl(startUrl);
		dialog.show();
		
		return codeFuture;
	}

	public String getRefreshToken() {
		return mRefreshToken;
	}

	public void setRefreshToken(String mRefreshToken) {
		this.mRefreshToken = mRefreshToken;
	}
	
}
