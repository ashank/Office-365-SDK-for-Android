/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.microsoft.office.integration.test;

import android.test.ActivityInstrumentationTestCase2;


import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.content.Context;
import android.view.ContextThemeWrapper;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.office.integration.*;
import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.engine.client.ODataV3Client;
import com.msopentech.odatajclient.engine.client.ODataV4Client;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.format.ODataFormat;
import com.msopentech.odatajclient.engine.format.ODataPubFormat;
import com.msopentech.odatajclient.proxy.api.AsyncCall;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;

public class AbstractTest extends ActivityInstrumentationTestCase2<TestActivity> {

    private static final String PASSWORD_FOR_ADAL_LOGIN = "Enter your password here";
    private static final String ENTER_PASSWORD_COMMAND = "javascript: document.getElementById('cred_password_inputtext').value = '%s';";
    private static final String SEND_AUTHENTICATION_REQUEST_COMMAND = "Post.SubmitCreds();";

    private static final long WAIT_ADAL_ACTIVITY_TIMEOUT = 30 * 1000;
    private static final long WAIT_OPEN_DIALOG_TIMEOUT = 20 * 1000;
    private static final long WAIT_CLOSE_DIALOG_TIMEOUT = 3 * 60 * 1000;
    private static final long SMALL_DELAY = 250;

    protected static ODataV3Client v3Client;

    protected static ODataV4Client v4Client;

    protected static final String username = "Enter your username here";

    /** Flag indicating if current test execution is the first for this session or not. */
    private static boolean wasStarted = false;

    /** Message for fatal error. null means there are no fatal errors. */
    private static String fatalErrorMessage = null;

    Activity mainActivity = null;
    Activity adalActivity = null;
    ActivityMonitor activityMonitor = null;

    String windowManagerString = null;
    Class<?> windowManager = null;

    public AbstractTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        if (fatalErrorMessage != null) {
            fail(fatalErrorMessage);
        }

        if (wasStarted) {
            return;
        }

        super.setUp();

        prepare();

        final Future<Void> emails = new AsyncCall<Void>(ODataClientFactory.getV4().getConfiguration()) {
            @Override
            public Void call() {
                Me.getDrafts();
                return null;
            }
        };
        Thread th = new Thread(new Runnable() {

            public void run() {
                try {
                    emails.get(120, TimeUnit.SECONDS);
                } catch (Exception e) {
                    fail(e.toString());
                }
            }

        });
        th.start();
        try {
            if (!waitForAdalActivity(WAIT_ADAL_ACTIVITY_TIMEOUT)) {
                fatalErrorMessage = "Adal activity not opened";
                fail(fatalErrorMessage);
            }
            // In case when adal authorized on this devices spinner isn't displayed.
            if (waitForDialogToOpen(WAIT_OPEN_DIALOG_TIMEOUT)) {
                if (!waitForDialogToClose(WAIT_CLOSE_DIALOG_TIMEOUT)) {
                    fatalErrorMessage = "Adal activity not opened";
                    fail(fatalErrorMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final WebView webView = getWebView(adalActivity.getWindow().getDecorView().getRootView());
        assertNotNull(webView);
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                webView.loadUrl(String.format(ENTER_PASSWORD_COMMAND, PASSWORD_FOR_ADAL_LOGIN) + SEND_AUTHENTICATION_REQUEST_COMMAND);
            }
        });

        TestActivity.available.acquire();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        EntityContainerFactory.getContext().detachAll();
    }

    private void prepare() {
        wasStarted = true;

        // Sets field name for reflection to work with various android version.
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            windowManagerString = "sDefaultWindowManager";
        } else if (android.os.Build.VERSION.SDK_INT >= 13) {
            windowManagerString = "sWindowManager";
        } else {
            windowManagerString = "mWindowManager";
        }

        // Prepare reflection specific window manager for various android version.
        String windowManagerClassName;
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            windowManagerClassName = "android.view.WindowManagerGlobal";
        } else {
            windowManagerClassName = "android.view.WindowManagerImpl";
        }
        try {
            windowManager = Class.forName(windowManagerClassName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up odata clients.
        v3Client = ODataClientFactory.getV3();
        v4Client = ODataClientFactory.getV4();

        // Initialize activity monitor.
        activityMonitor = new ActivityMonitor((IntentFilter) null, null, false);
        getInstrumentation().addMonitor(activityMonitor);

        // Start and check main test activity.
        mainActivity = getActivity();
        assertNotNull(mainActivity);
    }

    private boolean waitForAdalActivity(long timeout) {
        final long endTime = SystemClock.uptimeMillis() + timeout;

        while (SystemClock.uptimeMillis() < endTime) {
            Activity activity = activityMonitor.getLastActivity();
            // Wait for authentication activity (not the test activity).
            if ((activity != null) && !(activity instanceof TestActivity)) {
                adalActivity = activity;
                return true;
            }
            try {
                Thread.sleep(SMALL_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adalActivity = null;
        return false;
    }

    public boolean waitForDialogToClose(long timeout) {
        final long endTime = SystemClock.uptimeMillis() + timeout;

        while (SystemClock.uptimeMillis() < endTime) {
            if (!isDialogOpen()) {
                return true;
            }
            try {
                Thread.sleep(SMALL_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean waitForDialogToOpen(long timeout) {
        final long endTime = SystemClock.uptimeMillis() + timeout;

        if (isDialogOpen()) {
            return true;
        }

        while (SystemClock.uptimeMillis() < endTime) {

            if (isDialogOpen()) {
                return true;
            }
            try {
                Thread.sleep(SMALL_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private WebView getWebView(View rootView) {
        if (rootView instanceof WebView) {
            return (WebView) rootView;
        } else if (rootView instanceof ViewGroup) {
            ViewGroup rootViewGroup = (ViewGroup) rootView;
            for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
                View view = getWebView(rootViewGroup.getChildAt(i));
                if (view != null) {
                    return (WebView) view;
                }
            }
        }
        return null;
    }

    private View[] getWindowDecorViews() {
        try {
            Field viewsField = windowManager.getDeclaredField("mViews");
            Field instanceField = windowManager.getDeclaredField(windowManagerString);
            viewsField.setAccessible(true);
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                return ((ArrayList<View>) viewsField.get(instance)).toArray(new View[0]);
            } else {
                return (View[]) viewsField.get(instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isDialog(Activity activity, View decorView) {
        if (decorView == null || !decorView.isShown()) {
            return false;
        }
        Context viewContext = null;
        if (decorView != null) {
            viewContext = decorView.getContext();
        }

        if (viewContext instanceof ContextThemeWrapper) {
            ContextThemeWrapper ctw = (ContextThemeWrapper) viewContext;
            viewContext = ctw.getBaseContext();
        }
        Context activityContext = activity;
        Context activityBaseContext = activity.getBaseContext();
        return (activityContext.equals(viewContext) || activityBaseContext.equals(viewContext)) && (decorView != activity.getWindow().getDecorView());
    }

    private boolean isDialogOpen() {
        View[] views = getWindowDecorViews();
        for (View v : views) {
            if (isDialog(adalActivity, v)) {
                return true;
            }
        }
        return false;
    }

    protected ODataClient getClient() {
        return v4Client;
    }

    protected ODataEntity getEntityFromResource(String resourceFileName) {
        try {
            InputStream input = getInstrumentation().getContext().getAssets().open(resourceFileName);
            return getClient().getBinder().getODataEntity(getClient().getDeserializer().toEntry(input, getClient().getResourceFactory().entryClassForFormat(ODataPubFormat.JSON)));
        } catch (Exception e) {
            return null;
        }
    }

    protected byte[] getImageInByteFromResource(String path) {
        InputStream input = null;
        try {
            input = getInstrumentation().getContext().getAssets().open(path);
            return IOUtils.toByteArray(input);
        } catch (Exception e) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {}
            }
        }
    }

    protected static void assertArrayEquals(byte[] expected, byte[] actual) {
        assertTrue(Arrays.equals(expected, actual));
    }

    protected String getSuffix(final ODataPubFormat format) {
        return format == ODataPubFormat.ATOM ? "xml" : "json";
    }

    protected String getSuffix(final ODataFormat format) {
        return format == ODataFormat.XML ? "xml" : "json";
    }
}
