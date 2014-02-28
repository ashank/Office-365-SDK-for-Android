/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.files;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.microsoft.office365.Action;
import com.microsoft.office365.Constants;
import com.microsoft.office365.Credentials;
import com.microsoft.office365.ErrorCallback;
import com.microsoft.office365.Logger;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.SharepointClient;

/**
 * The Class FileClient.
 */
public class FileClient extends SharepointClient {

	/**
	 * Instantiates a new file API client.
	 * 
	 * @param serverUrl
	 * @param siteUrl
	 * @param credentials
	 */
	public FileClient(String serverUrl, String siteRelativeUrl, Credentials credentials) {
		super(serverUrl, siteRelativeUrl, credentials);
	}

	/**
	 * Instantiates a new file client.
	 * 
	 * @param serverUrl
	 * @param siteRelativeUrl
	 * @param credentials
	 * @param logger
	 */
	public FileClient(String serverUrl, String siteRelativeUrl, Credentials credentials,
			Logger logger) {
		super(serverUrl, siteRelativeUrl, credentials, logger);
	}

	/**
	 * Gets a list of FileSystemItem from the default Document Library
	 * 
	 * @return OfficeFuture<List<FileSystemItem>>
	 */
	public OfficeFuture<List<FileSystemItem>> getFileSystemItems() {

		return getFileSystemItems(null, null);
	}

	/**
	 * Gets children folder with a given path
	 * 
	 * @param path
	 * @return OfficeFuture<FileSystemItem>
	 */
	public OfficeFuture<List<FileSystemItem>> getFileSystemItems(String path, String library) {

		final OfficeFuture<List<FileSystemItem>> result = new OfficeFuture<List<FileSystemItem>>();

		String getPath;

		if (library == null) {
			if (path == null || path.length() == 0) {
				getPath = getSiteUrl() + "_api/Files";
			} else {
				getPath = getSiteUrl()
						+ String.format("_api/Files('%s')/children", urlEncode(path));
			}
		} else {
			if (path == null || path.length() == 0) {
				getPath = getSiteUrl()
						+ String.format("_api/web/lists/GetByTitle('%s')/files", urlEncode(library));
			} else {
				getPath = getSiteUrl()
						+ String.format("_api/web/lists/GetByTitle('%s')/files('%s')/children",
								urlEncode(library), urlEncode(path));
			}
		}

		OfficeFuture<JSONObject> request = executeRequestJson(getPath, "GET");

		request.done(new Action<JSONObject>() {
			@Override
			public void run(JSONObject json) throws Exception {
				List<FileSystemItem> item;
				try {
					item = FileSystemItem.listFrom(json);
					result.setResult(item);
				} catch (Throwable e) {
					result.triggerError(e);
				}
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});
		return result;
	}

	public OfficeFuture<FileSystemItem> getFileSystemItem(String path) {
		return getFileSystemItem(path, null);
	}

	/**
	 * Get a FileSystemItem from a path in a document library
	 * 
	 * @param library
	 *            the document library
	 * @param path
	 *            the path
	 * @return OfficeFuture<List<FileSystemItem>>
	 */
	public OfficeFuture<FileSystemItem> getFileSystemItem(String path, final String library) {

		final OfficeFuture<FileSystemItem> files = new OfficeFuture<FileSystemItem>();

		String getFilesUrl;
		if (library != null) {
			getFilesUrl = getSiteUrl() + "_api/web/lists/GetByTitle('%s')/files(%s)";
			getFilesUrl = String.format(getFilesUrl, urlEncode(library), getUrlPath(path));
		} else {
			getFilesUrl = getSiteUrl() + String.format("_api/files(%s)", getUrlPath(path));
		}

		try {
			OfficeFuture<JSONObject> request = executeRequestJson(getFilesUrl, "GET");
			request.done(new Action<JSONObject>() {

				@Override
				public void run(JSONObject json) throws Exception {
					try {
						FileSystemItem item = new FileSystemItem();
						item.loadFromJson(json);
						files.setResult(item);
					} catch (Throwable e) {
						files.triggerError(e);
					}
				}
			});

			request.onError(new ErrorCallback() {

				@Override
				public void onError(Throwable error) {
					files.triggerError(error);
				}
			});

		} catch (Throwable t) {
			files.triggerError(t);
		}
		return files;
	}

	/**
	 * Retrieves the value of property with a given path and library
	 * 
	 * @param property
	 * @param path
	 * @param library
	 * @return
	 */
	public OfficeFuture<Object> getProperty(final String property, String path, String library) {
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("Path cannot be null or empty");
		}

		if (property == null || property.length() == 0) {
			throw new IllegalArgumentException("Property cannot be null or empty");
		}

		String getPropertyUrl;
		if (library == null) {
			getPropertyUrl = getSiteUrl()
					+ String.format("_api/files('%s')/%s", urlEncode(path), property);
		} else {
			String url = getSiteUrl() + "_api/web/Lists/GetByTitle('%s')/files('%s')/%s";
			getPropertyUrl = String.format(url, urlEncode(library.trim()), urlEncode(path),
					property);
		}

		final OfficeFuture<Object> result = new OfficeFuture<Object>();
		OfficeFuture<JSONObject> request = executeRequestJson(getPropertyUrl, "GET");

		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {
				Object propertyResult = json.getJSONObject("d").get(property);
				result.setResult(propertyResult);
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});
		return result;
	}

	/**
	 * Gets the value of a given property with a given path
	 * 
	 * @param path
	 * @param property
	 * @return OfficeFuture<Object>
	 */
	public OfficeFuture<Object> getProperty(final String property, String path) {
		return getProperty(property, path, null);
	}

	/**
	 * Gets the file.
	 * 
	 * @param path
	 * @return OfficeFuture<byte[]>
	 */
	public OfficeFuture<byte[]> getFile(String path) {
		return getFile(path, null);
	}

	/**
	 * Gets the file.
	 * 
	 * @param path
	 * @return OfficeFuture<byte[]>
	 */
	public OfficeFuture<byte[]> getFile(String path, String library) {
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("Path cannot be null or empty");
		}

		String getFileUrl;
		if (library == null) {
			getFileUrl = getSiteUrl() + String.format("_api/files('%s')/$value", urlEncode(path));
		} else {
			getFileUrl = getSiteUrl()
					+ String.format("_api/web/Lists/GetByTitle('%s')/files('%s')/$value",
							urlEncode(library), urlEncode(path));
		}
		return executeRequest(getFileUrl, "GET");
	}

	/**
	 * Creates the folder with a given path
	 * 
	 * @param path
	 * @return OfficeFuture<FileSystemItem>
	 */
	public OfficeFuture<FileSystemItem> createFolder(String path) {

		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("path cannot be null or empty");
		}
		final OfficeFuture<FileSystemItem> fileMetadata = createEmpty(path, null,
				FileConstants.FOLDER_CREATE);
		return fileMetadata;
	}

	/**
	 * Creates a folder with a given path and library
	 * 
	 * @param path
	 * @param library
	 * @return OfficeFuture<FileSystemItem>
	 */
	public OfficeFuture<FileSystemItem> createFolder(String path, String library) {

		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("path cannot be null or empty");
		}

		if (library == null || library.length() == 0) {
			throw new IllegalArgumentException("library name cannot be null or empty");
		}

		final OfficeFuture<FileSystemItem> fileMetadata = createEmpty(path, library,
				FileConstants.FOLDER_CREATE);
		return fileMetadata;
	}

	/**
	 * Creates an empty file.
	 * 
	 * @param fileName
	 * @return OfficeFuture<FileSystemItem>
	 */
	public OfficeFuture<FileSystemItem> createFile(String fileName) {

		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("fileName cannot be null or empty");
		}

		final OfficeFuture<FileSystemItem> fileMetadata = createEmpty(fileName, null,
				FileConstants.FILE_CREATE);
		return fileMetadata;
	}

	/**
	 * Creates an empty file.
	 * 
	 * @param fileName
	 * @return OfficeFuture<FileSystemItem>
	 */
	public OfficeFuture<FileSystemItem> createFile(String fileName, String library) {
		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("fileName cannot be null or empty");
		}

		if (library == null || fileName.length() == 0) {
			throw new IllegalArgumentException("libraryName cannot be null or empty");
		}

		final OfficeFuture<FileSystemItem> fileMetadata = createEmpty(fileName, library,
				FileConstants.FILE_CREATE);
		return fileMetadata;
	}

	/**	
	 * Creates a file with a given path inside a given library
	 * 
	 * @param fileName
	 * @param library
	 * @param overwrite
	 * @param content
	 * @return OfficeFuture<FileSystemItem> 
	 */
	public OfficeFuture<FileSystemItem> createFile(String fileName, String library,
			boolean overwrite, byte[] content) {

		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("fileName cannot be null or empty");
		}

		String urlPart = urlEncode(String.format("Add(name='%s', overwrite='%s')", fileName,
				Boolean.toString(overwrite)));

		String url;
		if (library == null || library.length() == 0) {
			url = getSiteUrl() + "_api/files/" + urlPart;
		} else {
			url = getSiteUrl()
					+ String.format("_api/web/lists/getbytitle('%s')/files/", urlEncode(library))
					+ urlPart;
		}
		final OfficeFuture<FileSystemItem> result = new OfficeFuture<FileSystemItem>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/octet-stream");

		OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(url, "POST", headers,
				content);
		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject json) throws Exception {

				FileSystemItem item = new FileSystemItem();
				item.loadFromJson(json, true);
				result.setResult(item);
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});
		return result;
	}

	/**
	 * Creates the file with a given file name and content
	 * 
	 * @param fileName The file 
	 * @param overwrite True to overwrite
	 * @param content The content
	 * @return OfficeFuture<FileSystemItem>
	 */
	public OfficeFuture<FileSystemItem> createFile(String fileName, boolean overwrite,
			byte[] content) {

		return createFile(fileName, null, overwrite, content);
	}

	/**
	 * Delete a file/folder with a given path
	 * 
	 * @param path
	 * @return OfficeFuture<Void>
	 */
	public OfficeFuture<Void> delete(String path) {

		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("path cannot be null or empty");
		}

		return delete(path, null);
	}

	/**
	 * Deletes a file/folder with a given path and library
	 * @param path The path
	 * @param library The library
	 * @return
	 */
	public OfficeFuture<Void> delete(String path, String library) {

		final OfficeFuture<Void> result = new OfficeFuture<Void>();

		String deleteUrl;
		if (library == null) {
			deleteUrl = getSiteUrl() + String.format("_api/Files('%s')", urlEncode(path));
		} else {
			deleteUrl = getSiteUrl()
					+ String.format("_api/web/Lists/GetByTitle('%s')/files('%s')",
							urlEncode(library), urlEncode(path));
		}

		OfficeFuture<JSONObject> request = executeRequestJson(deleteUrl, "DELETE");
		request.done(new Action<JSONObject>() {

			@Override
			public void run(JSONObject obj) throws Exception {
				result.setResult(null);
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});
		return result;
	}

	/**
	 * Moves an item from the given sourcePath to the given destinationPath.
	 * Returns the destination path when succeeds
	 * 
	 * @param sourcePath
	 * @param destinationPath
	 * @param overwrite
	 * @return OfficeFuture<String>
	 */
	public OfficeFuture<Void> move(String sourcePath, String destinationPath, boolean overwrite) {
		if (sourcePath == null) {
			throw new IllegalArgumentException("sourcePath cannot be null or empty");
		}

		if (destinationPath == null) {
			throw new IllegalArgumentException("destinationPath cannot be null or empty");
		}
		OfficeFuture<Void> result = fileOp("MoveTo", sourcePath, destinationPath, overwrite, null);
		return result;
	}

	/**
	 * Moves an item from the given sourcePath to the given destinationPath.
	 * Returns the destination path when succeeds
	 * 
	 * @param source
	 *            Path
	 * @param destination
	 *            Path
	 * @param overwrite
	 *            flag
	 * @return OfficeFuture<String>
	 */
	public OfficeFuture<Void> move(String sourcePath, String destinationPath, boolean overwrite,
			String library) {
		if (sourcePath == null) {
			throw new IllegalArgumentException("sourcePath cannot be null or empty");
		}

		if (destinationPath == null) {
			throw new IllegalArgumentException("destinationPath cannot be null or empty");
		}
		OfficeFuture<Void> result = fileOp("MoveTo", sourcePath, destinationPath, overwrite,
				library);
		return result;
	}

	/**
	 * Copies an item from the given sourcePath to the given destinationPath.
	 * Returns the destination path when succeeds
	 * 
	 * @param sourcePath
	 * @param destinationPath
	 *            the destination path
	 * @param overwrite
	 * @return OfficeFuture<String>
	 */
	public OfficeFuture<Void> copy(String sourcePath, String destinationPath, boolean overwrite) {
		if (sourcePath == null) {
			throw new IllegalArgumentException("sourcePath cannot be null or empty");
		}

		if (destinationPath == null) {
			throw new IllegalArgumentException("destinationPath cannot be null or empty");
		}
		OfficeFuture<Void> result = fileOp("CopyTo", sourcePath, destinationPath, overwrite, null);
		return result;
	}

	/**
	 * Copies an item from the given sourcePath to the given destinationPath.
	 * Returns the destination path when succeeds
	 * 
	 * @param sourcePath
	 * @param destinationPath
	 *            the destination path
	 * @param overwrite
	 * @return OfficeFuture<String>
	 */
	public OfficeFuture<Void> copy(String sourcePath, String destinationPath, boolean overwrite,
			String library) {
		if (sourcePath == null) {
			throw new IllegalArgumentException("sourcePath cannot be null or empty");
		}

		if (destinationPath == null) {
			throw new IllegalArgumentException("destinationPath cannot be null or empty");
		}
		OfficeFuture<Void> result = fileOp("CopyTo", sourcePath, destinationPath, overwrite,
				library);
		return result;
	}

	/**
	 * Creates the empty.
	 * 
	 * @param path
	 * @param metadata
	 *            content for the file
	 * @return OfficeFuture<FileSystemItem>
	 */
	private OfficeFuture<FileSystemItem> createEmpty(String path, String library, String metadata) {

		final OfficeFuture<FileSystemItem> result = new OfficeFuture<FileSystemItem>();

		String postUrl = null;
		if (library == null) {
			postUrl = getSiteUrl() + "_api/files";
		} else {
			postUrl = getSiteUrl()
					+ String.format("_api/web/lists/GetByTitle('%s')/files", urlEncode(library));
		}

		byte[] payload = null;
		try {
			String completeMetada = String.format(metadata, path);
			payload = completeMetada.getBytes(Constants.UTF8_NAME);
			OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(postUrl, "POST", null,
					payload);

			request.done(new Action<JSONObject>() {
				@Override
				public void run(JSONObject json) throws Exception {
					FileSystemItem item = new FileSystemItem();
					item.loadFromJson(json, true);
					result.setResult(item);
				}
			}).onError(new ErrorCallback() {

				@Override
				public void onError(Throwable error) {
					result.triggerError(error);
				}
			});
		} catch (UnsupportedEncodingException e) {
			result.triggerError(e);
		}
		return result;
	}

	private OfficeFuture<Void> fileOp(final String operation, String source, String destination,
			boolean overwrite, String library) {
		final OfficeFuture<Void> result = new OfficeFuture<Void>();
		String url;

		String targetEncoded = urlEncode("target='" + destination + "', overwrite="
				+ Boolean.toString(overwrite));

		if (library == null || library.length() == 0) {
			url = getSiteUrl()
					+ String.format("_api/files('%s')/%s(%s)", urlEncode(source), operation,
							targetEncoded);
		} else {
			url = getSiteUrl()
					+ String.format("_api/web/lists/getbytitle('%s')/files('%s')/%s(%s)",
							urlEncode(library), urlEncode(source), operation, targetEncoded);
		}

		OfficeFuture<JSONObject> request = executeRequestJsonWithDigest(url, "POST", null, null);
		request.done(new Action<JSONObject>() {
			@Override
			public void run(JSONObject json) throws Exception {
				result.setResult(null);
			}
		}).onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				result.triggerError(error);
			}
		});
		return result;
	}

	/**
	 * Returns the URL component for a path
	 */
	private String getUrlPath(String path) {
		if (path == null) {
			path = "";
		}

		String urlPath;
		if (path.length() == 0) {
			urlPath = "";
		} else {
			urlPath = String.format("'%s'", urlEncode(path));
		}
		return urlPath;
	}
}
