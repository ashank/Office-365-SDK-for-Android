/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.files;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.microsoft.office365.Action;
import com.microsoft.office365.Credentials;
import com.microsoft.office365.ErrorCallback;
import com.microsoft.office365.Logger;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.OfficeEntity;
import com.microsoft.office365.lists.SharepointListsClient;

/**
 * This class will be replaced when the new Files API is released to production
 */

public class SharepointListsClientWithFiles extends SharepointListsClient {

	public SharepointListsClientWithFiles(String serverUrl, String siteRelativeUrl,
			Credentials credentials) {
		super(serverUrl, siteRelativeUrl, credentials);
	}

	public SharepointListsClientWithFiles(String serverUrl, String siteRelativeUrl,
			Credentials credentials, Logger logger) {
		super(serverUrl, siteRelativeUrl, credentials, logger);
	}

	public class SPFile extends OfficeEntity {

	}

	/**
	 * Gets the file.
	 * 
	 * @param listName
	 *            the list name
	 * @param itemId
	 *            the item id
	 * @return the file
	 */
	public OfficeFuture<DocumentLibraryItem> getFileFromDocumentLibrary(String listName,
			final String itemId) {
		return getFileFromDocumentLibrary(listName, itemId, false);
	}

	/**
	 * Gets the file.
	 * 
	 * @param listName
	 *            the list name
	 * @param itemId
	 *            the item id
	 * @param thumbnail
	 *            if true, the method will retrieve a thumbnail
	 * @return the file
	 */
	public OfficeFuture<DocumentLibraryItem> getFileFromDocumentLibrary(String listName,
			final String itemId, final boolean thumbnail) {

		final OfficeFuture<DocumentLibraryItem> result = new OfficeFuture<DocumentLibraryItem>();
		String queryPath = String.format(
				"_api/web/lists/GetByTitle('%s')/items('%s')/File?$select=ServerRelativeUrl",
				urlEncode(listName), itemId);
		String filePathUrl = getSiteUrl() + queryPath;
		OfficeFuture<JSONObject> request = executeRequestJson(filePathUrl, "GET");

		request.done(new Action<JSONObject>() {
			@Override
			public void run(JSONObject json) throws Exception {

				String filePath = json.getJSONObject("d").getString("ServerRelativeUrl");

				if (filePath == null) {
					throw new IllegalStateException("File path missing");
				}

				if (filePath.startsWith("/")) {
					filePath = filePath.substring(1);
				}

				String path = filePath.replaceAll("\\s", "%20");

				if (thumbnail) {
					path = path.substring(0, path.lastIndexOf("/") + 1) + "_t"
							+ path.substring(path.lastIndexOf("/")).replace(".", "_") + ".jpg";
				}

				final String completePath = getServerUrl() + path;
				OfficeFuture<byte[]> file = executeRequest(completePath, "GET");

				file.done(new Action<byte[]>() {
					@Override
					public void run(byte[] payload) throws Exception {
						result.setResult(new DocumentLibraryItem(payload, itemId));
					}
				});
			}
		});
		return result;
	}

	public class DocumentLibraryItem {
		private byte[] mContent;
		private String mItemId;

		public DocumentLibraryItem(byte[] content, String itemId) {
			setContent(content);
			setItemId(itemId);
		}

		public byte[] getContent() {
			return mContent;
		}

		public void setContent(byte[] content) {
			this.mContent = content;
		}

		public String getItemId() {
			return mItemId;
		}

		public void setItemId(String itemId) {
			this.mItemId = itemId;
		}
	}

	public OfficeFuture<SPFile> getSPFileFromPictureLibrary(final String library, final String id) {

		final OfficeFuture<SPFile> result = new OfficeFuture<SPFile>();
		String getListUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')/items('%s')/File";
		getListUrl = String.format(getListUrl, urlEncode(library), id);

		try {
			OfficeFuture<JSONObject> request = executeRequestJson(getListUrl, "GET");

			request.done(new Action<JSONObject>() {

				@Override
				public void run(JSONObject json) throws Exception {
					SPFile file = new SPFile();
					file.loadFromJson(json);
					result.setResult(file);
				}
			});

			copyFutureHandlers(request, result);
		} catch (Throwable t) {
			result.triggerError(t);
		}

		return result;
	}

	public OfficeFuture<SPFile> uploadFile(final String documentLibraryName, final String fileName,
			final byte[] fileContent) {
		final OfficeFuture<SPFile> result = new OfficeFuture<SPFile>();

		//The name of the library not always matches the title, here is how we get the real path
		String getRootFolderUrl = getSiteUrl() + String.format("_api/web/lists/GetByTitle('%s')/RootFolder", urlEncode(documentLibraryName));
		
		executeRequestJson(getRootFolderUrl, "GET")
			.done(new Action<JSONObject>() {
				
				@Override
				public void run(JSONObject json) throws Exception {
					try {

						String libraryServerRelativeUrl = json.getJSONObject("d").getString("ServerRelativeUrl");

						String getListUrl = getSiteUrl()
								+ "_api/web/GetFolderByServerRelativeUrl('%s')/Files/add(url='%s',overwrite=true)";
						getListUrl = String.format(getListUrl,  urlEncode(libraryServerRelativeUrl), urlEncode(fileName));

						Map<String, String> headers = new HashMap<String, String>();
						headers.put("Content-Type", "application/json;odata=verbose");
						OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(getListUrl, "POST",
								headers, fileContent);

						request.done(new Action<JSONObject>() {

							@Override
							public void run(JSONObject json) throws Exception {
								SPFile file = new SPFile();
								file.loadFromJson(json);
								result.setResult(file);
							}
						});

						copyFutureHandlers(request, result);
					} catch (Throwable t) {
						result.triggerError(t);
					}
				}
			})
			.onError(new ErrorCallback() {
				
				@Override
				public void onError(Throwable error) {
					result.triggerError(error);
				}
			});
		
		

		return result;
	}

	public OfficeFuture<String> getListItemIdForFileByServerRelativeUrl(String serverRelativeUrl) {
		final OfficeFuture<String> result = new OfficeFuture<String>();

		String getListUrl = getSiteUrl()
				+ "_api/Web/GetFileByServerRelativeUrl('%s')/ListItemAllFields?$select=id";
		getListUrl = String.format(getListUrl, serverRelativeUrl);

		try {
			OfficeFuture<JSONObject> request = executeRequestJson(getListUrl, "GET");

			request.done(new Action<JSONObject>() {

				@Override
				public void run(JSONObject json) throws Exception {
					result.setResult(json.getJSONObject("d").getString("ID"));
				}
			});

			copyFutureHandlers(request, result);
		} catch (Throwable t) {
			result.triggerError(t);
		}

		return result;
	}
}
