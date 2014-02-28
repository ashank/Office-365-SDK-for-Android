/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.lists;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.microsoft.office365.*;

/**
 * The Class SharepointListsClient.
 */
public class SharepointListsClient extends SharepointClient {

	/**
	 * Instantiates a new sharepoint lists client.
	 * 
	 * @param siteUrl
	 *            the site url
	 * @param credentials
	 *            the credentials
	 */
	public SharepointListsClient(String serverUrl, String siteRelativeUrl, Credentials credentials) {
		super(serverUrl, siteRelativeUrl, credentials);
	}

	/**
	 * Instantiates a new sharepoint lists client.
	 * 
	 * @param siteUrl
	 *            the site url
	 * @param credentials
	 *            the credentials
	 * @param logger
	 *            the logger
	 */
	public SharepointListsClient(String serverUrl, String siteRelativeUrl, Credentials credentials,
			Logger logger) {
		super(serverUrl, siteRelativeUrl, credentials, logger);
	}

	/**
	 * Gets the lists.
	 * 
	 * @param query
	 *            the query
	 * @return the lists
	 */
	public OfficeFuture<List<SPList>> getLists(Query query) {
		final OfficeFuture<List<SPList>> result = new OfficeFuture<List<SPList>>();

		String queryOData = generateODataQueryString(query);
		String getListsUrl = getSiteUrl() + "_api/web/lists/?" + queryEncode(queryOData);
		OfficeFuture<JSONObject> request = executeRequestJson(getListsUrl, "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				List<SPList> list = SPList.listFromJson(json);
				result.setResult(list);
			}
		});

		copyFutureHandlers(request, result);

		return result;
	}

	/**
	 * Gets the list.
	 * 
	 * @param listName
	 *            the list name
	 * @return the list
	 */
	public OfficeFuture<SPList> getList(String listName) {

		final OfficeFuture<SPList> result = new OfficeFuture<SPList>();
		String getListUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')";
		getListUrl = String.format(getListUrl, urlEncode(listName));
		OfficeFuture<JSONObject> request = executeRequestJson(getListUrl, "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				SPList list = new SPList();
				list.loadFromJson(json, true);
				result.setResult(list);
			}
		});

		copyFutureHandlers(request, result);
		return result;
	}

	/**
	 * Gets the list items.
	 * 
	 * @param listName
	 *            the list name
	 * @param query
	 *            the query
	 * @return the list items
	 */
	public OfficeFuture<List<SPListItem>> getListItems(String listName, Query query) {
		final OfficeFuture<List<SPListItem>> result = new OfficeFuture<List<SPListItem>>();

		String listNamePart = String.format("_api/web/lists/GetByTitle('%s')/Items?",
				urlEncode(listName));
		String getListUrl = getSiteUrl() + listNamePart + generateODataQueryString(query);
		OfficeFuture<JSONObject> request = executeRequestJson(getListUrl, "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				result.setResult(SPListItem.listFromJson(json));
			}
		});

		copyFutureHandlers(request, result);
		return result;
	}

	/**
	 * Gets the list fields.
	 * 
	 * @param listName
	 *            the list name
	 * @param query
	 *            the query
	 * @return the list fields
	 */
	public OfficeFuture<List<SPListField>> getListFields(String listName, Query query) {
		final OfficeFuture<List<SPListField>> result = new OfficeFuture<List<SPListField>>();

		String getListUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')/Fields?"
				+ generateODataQueryString(query);
		getListUrl = String.format(getListUrl, urlEncode(listName));
		OfficeFuture<JSONObject> request = executeRequestJson(getListUrl, "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				result.setResult(SPListField.listFromJson(json));
			}
		});

		copyFutureHandlers(request, result);

		return result;
	}

	/**
	 * Insert list item.
	 * 
	 * @param listItem
	 *            the list item
	 * @param list
	 *            the list
	 * @return the office future
	 */
	public OfficeFuture<Void> insertListItem(final SPListItem listItem, final SPList list) {
		final OfficeFuture<Void> result = new OfficeFuture<Void>();

		String getListUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')/Items";
		getListUrl = String.format(getListUrl, urlEncode(list.getTitle()));

		try {
			JSONObject payload = new JSONObject();
			JSONObject metadata = new JSONObject();
			metadata.put("type", list.getListItemEntityTypeFullName());
			payload.put("__metadata", metadata);

			for (String key : listItem.getValues().keySet()) {

				Object object = listItem.getValues().get(key);
				// we assume you're trying to store a value on a linked
				// sharepoint list
				if (object instanceof JSONArray) {
					JSONObject container = new JSONObject();
					container.put("results", object);
					payload.put(key + "Id", container);
				} else {
					payload.put(key, object);
				}
			}

			OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(getListUrl, "POST",
					null, getBytes(payload.toString()));

			request.done(new Action<JSONObject>() {

				@Override
				public void run(JSONObject json) throws Exception {
					result.setResult(null);
				}
			});

			copyFutureHandlers(request, result);
		} catch (Throwable t) {
			result.triggerError(t);
		}

		return result;
	}

	/**
	 * Update list item.
	 * 
	 * @param listItem
	 *            the list item
	 * @param list
	 *            the list
	 * @return the office future
	 */
	public OfficeFuture<Void> updateListItem(final SPListItem listItem, final SPList list) {
		final OfficeFuture<Void> result = new OfficeFuture<Void>();

		String getListUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')/items("
				+ listItem.getId() + ")";
		getListUrl = String.format(getListUrl, urlEncode(list.getTitle()));

		try {
			JSONObject payload = new JSONObject();
			JSONObject metadata = new JSONObject();
			metadata.put("type", list.getListItemEntityTypeFullName());
			payload.put("__metadata", metadata);

			for (String key : listItem.getValues().keySet()) {
				Object object = listItem.getValues().get(key);
				// we assume you're trying to store a value on a linked
				// sharepoint list
				if (object instanceof JSONArray) {
					JSONObject container = new JSONObject();
					container.put("results", object);
					payload.put(key + "Id", container);
				} else {
					payload.put(key, object);
				}
			}

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("X-HTTP-Method", "MERGE");
			headers.put("If-Match", "*");

			OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(getListUrl, "POST",
					headers, getBytes(payload.toString()));

			request.done(new Action<JSONObject>() {

				@Override
				public void run(JSONObject json) throws Exception {
					result.setResult(null);
				}
			});

			copyFutureHandlers(request, result);
		} catch (Throwable t) {
			result.triggerError(t);
		}

		return result;
	}

	/**
	 * Delete list item.
	 * 
	 * @param listItem
	 *            the list item
	 * @param list
	 *            the list
	 * @return the office future
	 */
	public OfficeFuture<Void> deleteListItem(final SPListItem listItem, final String listName) {
		final OfficeFuture<Void> result = new OfficeFuture<Void>();

		String getListUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')/items("
				+ listItem.getId() + ")";
		getListUrl = String.format(getListUrl, urlEncode(listName));

		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("X-HTTP-Method", "DELETE");
			headers.put("If-Match", "*");

			OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(getListUrl, "POST",
					headers, null);

			request.done(new Action<JSONObject>() {

				@Override
				public void run(JSONObject json) throws Exception {
					result.setResult(null);
				}
			});

			copyFutureHandlers(request, result);
		} catch (Throwable t) {
			result.triggerError(t);
		}

		return result;
	}

	public OfficeFuture<List<String>> getColumnsFromDefaultView(final String listName) {
		final OfficeFuture<List<String>> result = new OfficeFuture<List<String>>();
		String getViewUrl = getSiteUrl()
				+ String.format("_api/web/lists/GetByTitle('%s')/defaultView/viewfields",
						urlEncode(listName));
		OfficeFuture<JSONObject> request = executeRequestJson(getViewUrl, "GET");
		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {

				JSONObject container = json.getJSONObject("d");
				JSONArray results = container.getJSONObject("Items").getJSONArray("results");
				ArrayList<String> columnNames = new ArrayList<String>();

				for (int i = 0; i < results.length(); i++) {
					columnNames.add(results.get(i).toString());
				}
				result.setResult(columnNames);
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});
		return result;
	}

	public OfficeFuture<String> getUserProperties() {
		final OfficeFuture<String> result = new OfficeFuture<String>();

		String url = getSiteUrl() + "/_api/SP.UserProfiles.PeopleManager/GetMyProperties";

		OfficeFuture<JSONObject> request = executeRequestJson(url, "GET");
		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject obj) throws Exception {
				result.setResult(obj.toString());
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.setResult(error.getMessage());
			}
		});

		return result;
	}

	/**
	 * Gets the bytes from a given string.
	 * 
	 * @param s
	 *            the s
	 * @return the bytes
	 */
	private byte[] getBytes(String s) {
		try {
			return s.getBytes(Constants.UTF8_NAME);
		} catch (UnsupportedEncodingException e) {
			return s.getBytes();
		}
	}
}
