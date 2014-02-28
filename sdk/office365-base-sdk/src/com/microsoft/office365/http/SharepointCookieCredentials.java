/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.http;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.microsoft.office365.Action;
import com.microsoft.office365.ErrorCallback;
import com.microsoft.office365.OfficeFuture;

public class SharepointCookieCredentials extends CookieCredentials {
    
    public static OfficeFuture<CookieCredentials> requestCredentials(String sharepointSiteUrl, Activity activity) {
        final OfficeFuture<CookieCredentials> future = new OfficeFuture<CookieCredentials>();
        
        showLoginForCookies(activity, sharepointSiteUrl)
            .done(new Action<String>() {
                
                @Override
                public void run(String cookies) throws Exception {
                    future.setResult(new CookieCredentials(cookies));
                }
            })
            .onError(new ErrorCallback() {
                
                @Override
                public void onError(Throwable error) {
                    future.triggerError(error);
                }
            });
        
        return future;
    }
    
    protected static OfficeFuture<String> showLoginForCookies(Activity activity, final String startUrl) {
        
        final OfficeFuture<String> codeFuture = new OfficeFuture<String>();
        
        if (startUrl == null || startUrl == "") {
            throw new IllegalArgumentException(
                    "startUrl can not be null or empty");
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
        
        
        //wv.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36");
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
                    if (cookieWasFound(view) && !mResultReturned) {
                        mResultReturned = true;
                        
                        //CookieSyncManager syncManager = CookieSyncManager.createInstance(view.getContext());
                        CookieManager cookieManager = CookieManager.getInstance();
                        String cookie = cookieManager.getCookie(url);
                        dialog.dismiss();
                        codeFuture.setResult(cookie);
                    }

                    super.onPageStarted(view, url, favicon);                    
                }
            }

            private boolean cookieWasFound(WebView view) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookie = cookieManager.getCookie(startUrl);
                
                if (cookie != null && cookie.contains("rtFa")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        wv.loadUrl(startUrl);
        dialog.show();
        
        return codeFuture;
    }
}
