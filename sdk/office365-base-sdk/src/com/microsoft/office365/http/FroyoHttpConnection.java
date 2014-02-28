/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.http;

import android.annotation.SuppressLint;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import com.microsoft.office365.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Froyo HttpConnection implementation, based on AndroidHttpClient and 
 * AsyncTask for async operations
 */
public class FroyoHttpConnection implements HttpConnection {

	@Override
	public HttpConnectionFuture execute(final Request request) {

		final HttpConnectionFuture future = new HttpConnectionFuture();
		
		final RequestTask requestTask = new RequestTask() {

			AndroidHttpClient mClient;
			InputStream mResponseStream;
			
			@Override
			protected Void doInBackground(Void... voids) {
				if (request == null) {
					future.triggerError(new IllegalArgumentException("request"));
				}
				
				mClient = AndroidHttpClient.newInstance(Platform.getUserAgent());
				mResponseStream = null;
				URI uri;

				try {
					HttpRequest realRequest = createRealRequest(request);
					uri = new URI(request.getUrl());

					HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

					HttpResponse response;
					
					try {
						response = mClient.execute(host, realRequest);
					} catch (SocketTimeoutException timeoutException) {
						closeStreamAndClient();
					
						future.triggerTimeout(timeoutException);
						
						return null;
					}
					
					mResponseStream = response.getEntity().getContent();
					Header[] headers = response.getAllHeaders();
					Map<String, List<String>> headersMap = new HashMap<String, List<String>>();
					for (Header header : headers) {
						String headerName = header.getName();
						if (headersMap.containsKey(headerName)) {
							headersMap.get(headerName).add(header.getValue());
						} else {
							List<String> headerValues = new ArrayList<String>();
							headerValues.add(header.getValue());
							headersMap.put(headerName, headerValues);
						}
					}
					
					future.setResult(new StreamResponse(mResponseStream, response.getStatusLine().getStatusCode(), headersMap));
					closeStreamAndClient();
				} catch (Exception e) {
					closeStreamAndClient();
					
					future.triggerError(e);
				}

				return null;
			}
			
			protected void closeStreamAndClient() {
				if (mResponseStream != null) {
					try {
						mResponseStream.close();
					} catch (IOException e) {
					}
				}

				if (mClient != null) {
					mClient.close();
				}
			}
		};
		
		future.onCancelled(new Runnable() {
			
			@Override
			public void run() {
				AsyncTask<Void, Void, Void> cancelTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						requestTask.closeStreamAndClient();
						return null;
					}
				};
				
				executeTask(cancelTask);
			}
		});
		
		executeTask(requestTask);
		
		return future;
	}
	
	@SuppressLint("NewApi")
	private void executeTask(AsyncTask<Void, Void, Void> task) {
		// If it's running with Honeycomb or greater, it must execute each
		// request in a different thread
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	
	/**
	 * Internal class to represent an async operation that can close a stream
	 */
	private abstract class RequestTask extends AsyncTask<Void, Void, Void> {
		
		/**
		 * Closes the internal stream and http client, if they exist
		 */
		abstract protected void closeStreamAndClient();
	}

	/**
	 * Creates a request that can be accepted by the AndroidHttpClient
	 * @param request The request information
	 * @throws UnsupportedEncodingException
	 */
	private static BasicHttpEntityEnclosingRequest createRealRequest(Request request) throws UnsupportedEncodingException {
		BasicHttpEntityEnclosingRequest realRequest = new BasicHttpEntityEnclosingRequest(request.getVerb(), request.getUrl());

		if (request.getContent() != null) {
			realRequest.setEntity(new ByteArrayEntity(request.getContent()));
		}

		Map<String, String> headers = request.getHeaders();

		for (String key : headers.keySet()) {
			realRequest.addHeader(key, headers.get(key));
		}

		return realRequest;
	}
}
