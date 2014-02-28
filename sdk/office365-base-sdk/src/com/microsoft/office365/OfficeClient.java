package com.microsoft.office365;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import com.microsoft.office365.http.HttpConnection;
import com.microsoft.office365.http.HttpConnectionFuture;
import com.microsoft.office365.http.Request;
import com.microsoft.office365.http.Response;

public class OfficeClient {

	Credentials mCredentials;
	Logger mLogger;

	public OfficeClient(Credentials credentials) {
		this(credentials, null);
	}

	public OfficeClient(Credentials credentials, Logger logger) {
		if (credentials == null) {
			throw new IllegalArgumentException("credentials must not be null");
		}

		if (logger == null) {
			// add an empty logger
			mLogger = new Logger() {

				@Override
				public void log(String message, LogLevel level) {
				}
			};
		} else {
			mLogger = logger;
		}

		mCredentials = credentials;
	}

	protected void log(String message, LogLevel level) {
		getLogger().log(message, level);
	}

	protected void log(Throwable error) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		String stackTrace = sw.toString();

		getLogger().log(error.toString() + "\nStack Trace: " + stackTrace, LogLevel.Critical);
	}

	protected Logger getLogger() {
		return mLogger;
	}

	protected Credentials getCredentials() {
		return mCredentials;
	}

	protected String generateODataQueryString(Query query) {
		StringBuilder sb = new StringBuilder();
		if (query != null) {
			query.ensureIdProperty();
			sb.append("$filter=");
			sb.append(queryEncode(query.toString()));

			String rowSetModifiers = query.getRowSetModifiers().trim();
			if (rowSetModifiers != "") {

				if (!rowSetModifiers.startsWith("&")) {
					sb.append("&");
				}
				sb.append(rowSetModifiers);
			}
		}

		return sb.toString();
	}

	protected OfficeFuture<byte[]> executeRequest(String url, String method) {
		return executeRequest(url, method, null, null);
	}

	protected OfficeFuture<byte[]> executeRequest(String url, String method,
			Map<String, String> headers, byte[] payload) {
		HttpConnection connection = Platform.createHttpConnection();

		Request request = new Request(method);

		if (headers != null) {
			for (String key : headers.keySet()) {
				request.addHeader(key, headers.get(key));
			}
		}

		request.setUrl(url);
		request.setContent(payload);
		prepareRequest(request);

		log("Generate request for " + url, LogLevel.Verbose);
		request.log(getLogger());

		final OfficeFuture<byte[]> result = new OfficeFuture<byte[]>();
		HttpConnectionFuture future = connection.execute(request);

		future.done(new Action<Response>() {

			@Override
			public void run(Response response) throws Exception {

				int statusCode = response.getStatus();
				if (isValidStatus(statusCode)) {
					byte[] responseContentBytes = response.readAllBytes();
					result.setResult(responseContentBytes);
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

	protected OfficeFuture<JSONObject> executeRequestJson(String url, String method) {
		return executeRequestJson(url, method, null, null);
	}

	protected OfficeFuture<JSONObject> executeRequestJson(String url, String method,
			Map<String, String> headers, byte[] payload) {
		final OfficeFuture<JSONObject> result = new OfficeFuture<JSONObject>();

		executeRequest(url, method, headers, payload).done(new Action<byte[]>() {

			@Override
			public void run(byte[] b) throws Exception {
				String string = new String(b, Constants.UTF8_NAME);
				if (string == null || string.length() == 0) {
					result.setResult(null);
				} else {
					JSONObject json = new JSONObject(string);
					result.setResult(json);
				}
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		}).onCancelled(new Runnable() {

			@Override
			public void run() {
				result.cancel();
			}
		});

		return result;
	}

	protected void copyFutureHandlers(OfficeFuture<?> source, final OfficeFuture<?> target) {
		source.onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				log(error);
				target.triggerError(error);
			}
		});

		source.onCancelled(new Runnable() {

			@Override
			public void run() {
				log("Operation cancelled", LogLevel.Critical);
				target.cancel();
			}
		});
	}

	public OfficeFuture<List<DiscoveryInformation>> getDiscoveryInfo() {
		return getDiscoveryInfo("https://api.office.com/discovery/me/services");
	}

	public OfficeFuture<List<DiscoveryInformation>> getDiscoveryInfo(String discoveryEndpoint) {
		final OfficeFuture<List<DiscoveryInformation>> result = new OfficeFuture<List<DiscoveryInformation>>();

		OfficeFuture<JSONObject> request = executeRequestJson(discoveryEndpoint, "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				List<DiscoveryInformation> discoveryInfo = DiscoveryInformation.listFromJson(json,
						DiscoveryInformation.class);
				result.setResult(discoveryInfo);
			}
		});

		copyFutureHandlers(request, result);
		return result;
	}

	protected void prepareRequest(Request request) {
		request.addHeader("Accept", "application/json;odata=verbose");
		int contentLength = 0;
		if (request.getContent() != null) {
		    contentLength = request.getContent().length;
		}
		request.addHeader("Content-Length", String.valueOf(contentLength));
		mCredentials.prepareRequest(request);
	}

	protected static boolean isValidStatus(int status) {
		return status >= 200 && status <= 299;
	}

	protected String queryEncode(String query) {

		String encoded = null;

		try {
			encoded = query.replaceAll("\\s", "+");
		} catch (Exception e) {
			encoded = query;
		}
		return encoded;
	}

	protected String urlEncode(String str) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(str, Constants.UTF8_NAME);
		} catch (UnsupportedEncodingException e) {
			encoded = str;
		}

		encoded = encoded.replaceAll("\\+", "%20").replaceAll("\\%21", "!")
				.replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")")
				.replaceAll("\\%7E", "~");
		return encoded;
	}

	protected String UUIDtoString(UUID id) {
		return id.toString().replace("-", "");
	}

}
