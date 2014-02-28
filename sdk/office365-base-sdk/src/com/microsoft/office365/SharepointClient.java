/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.microsoft.office365.http.*;

public class SharepointClient extends OfficeClient {
	private String mServerUrl;
	private String mSiteRelativeUrl;

	protected String getSiteUrl() {
		return mServerUrl + mSiteRelativeUrl;
	}

	protected String getServerUrl() {
		return mServerUrl;
	}

	protected String getSiteRelativeUrl() {
		return mSiteRelativeUrl;
	}

	public SharepointClient(String serverUrl, String siteRelativeUrl, Credentials credentials) {
		this(serverUrl, siteRelativeUrl, credentials, null);
	}

	public SharepointClient(String serverUrl, String siteRelativeUrl, Credentials credentials,
			Logger logger) {
		super(credentials, logger);
		
		if (serverUrl == null) {
			throw new IllegalArgumentException("serverUrl must not be null");
		}

		if (siteRelativeUrl == null) {
			throw new IllegalArgumentException("siteRelativeUrl must not be null");
		}

		
		mServerUrl = serverUrl;
		mSiteRelativeUrl = siteRelativeUrl;

		if (!mServerUrl.endsWith("/")) {
			mServerUrl += "/";
		}

		if (mSiteRelativeUrl.startsWith("/")) {
			mSiteRelativeUrl = mSiteRelativeUrl.substring(1);
		}

		if (!mSiteRelativeUrl.endsWith("/") && mSiteRelativeUrl.length() > 0) {
			mSiteRelativeUrl += "/";
		}

	}

	protected OfficeFuture<String> getFormDigest() {
		HttpConnection connection = Platform.createHttpConnection();

		Request request = new Request("POST");

		request.setUrl(getSiteUrl() + "_api/contextinfo");

		prepareRequest(request);

		log("Generate request for getFormDigest", LogLevel.Verbose);
		request.log(getLogger());

		final OfficeFuture<String> result = new OfficeFuture<String>();

		HttpConnectionFuture future = connection.execute(request);

		future.done(new Action<Response>() {

			@Override
			public void run(Response response) throws Exception {

				int statusCode = response.getStatus();
				if (isValidStatus(statusCode)) {
					String responseContent = response.readToEnd();

					JSONObject json = new JSONObject(responseContent);

					result.setResult(json.getJSONObject("d")
							.getJSONObject("GetContextWebInformation").getString("FormDigestValue"));
				} else {
					result.triggerError(new Exception("Invalid status code " + statusCode + ": "
							+ response.readToEnd()));
				}
			}
		});

		future.onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				log(error);
				result.triggerError(error);
			}
		});

		future.onTimeout(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				log(error);
				result.triggerError(error);
			}
		});

		return result;
	}

	/**
	 * Execute request json with digest.
	 * 
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param headers
	 *            the headers
	 * @param payload
	 *            the payload
	 * @return OfficeFuture<JSONObject>
	 */
	protected OfficeFuture<JSONObject> executeRequestJsonWithDigest(final String url,
			final String method, final Map<String, String> headers, final byte[] payload) {

		final OfficeFuture<JSONObject> result = new OfficeFuture<JSONObject>();

		OfficeFuture<String> digestFuture = getFormDigest();
		digestFuture.onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});

		digestFuture.done(new Action<String>() {

			@Override
			public void run(String digest) throws Exception {

				Map<String, String> finalHeaders = new HashMap<String, String>();

				if (headers != null) {
					for (String key : headers.keySet()) {
						finalHeaders.put(key, headers.get(key));
					}
				}

				finalHeaders.put("Content-Type", "application/json;odata=verbose");
				finalHeaders.put("X-RequestDigest", digest);

				OfficeFuture<JSONObject> request = executeRequestJson(url, method, finalHeaders,
						payload);

				request.done(new Action<JSONObject>() {

					@Override
					public void run(JSONObject json) throws Exception {
						result.setResult(json);
					}
				});

				copyFutureHandlers(request, result);
			}
		});
		return result;
	}

	public OfficeFuture<String> getWebTitle() {
		final OfficeFuture<String> result = new OfficeFuture<String>();

		OfficeFuture<JSONObject> request = executeRequestJson(mServerUrl + "_api/web/title", "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				result.setResult(json.getJSONObject("d").getString("Title"));
			}
		});

		copyFutureHandlers(request, result);
		return result;
	}
}
