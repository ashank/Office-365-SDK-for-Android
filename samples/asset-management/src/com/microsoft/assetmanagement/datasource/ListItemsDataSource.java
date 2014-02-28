/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.microsoft.assetmanagement.AssetApplication;
import com.microsoft.assetmanagement.R;
import com.microsoft.assetmanagement.files.SharepointListsClientWithFiles;
import com.microsoft.assetmanagement.files.SharepointListsClientWithFiles.DocumentLibraryItem;
import com.microsoft.assetmanagement.files.SharepointListsClientWithFiles.SPFile;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.Query;
import com.microsoft.office365.lists.SPList;
import com.microsoft.office365.lists.SPListItem;
import com.microsoft.office365.lists.SharepointListsClient;

public class ListItemsDataSource {

	private AssetApplication mApplication;

	public ListItemsDataSource(AssetApplication application) {
		mApplication = application;
	}

	private SharepointListsClient getListsClient() {
		return mApplication.getCurrentListClient();
	}

	public ArrayList<CarListViewItem> getDefaultListViewItems() throws Exception {
		final ArrayList<CarListViewItem> items = new ArrayList<CarListViewItem>();

		final SharepointListsClientWithFiles client = (SharepointListsClientWithFiles) getListsClient();
		final String listName = mApplication.getPreferences().getLibraryName();
		int topCount = mApplication.getPreferences().getListDisplaySize();
		
		String[] columns = mApplication.getApplicationContext().getResources()
				.getStringArray(R.array.visibleListColumns);
		List<SPListItem> listItems = client.getListItems(listName,
				new Query().select(columns).top(topCount)).get();

		List<OfficeFuture<DocumentLibraryItem>> futures = new ArrayList<OfficeFuture<DocumentLibraryItem>>();
		for (final SPListItem carItem : listItems) {
			int pictureId = Integer.parseInt(carItem.getData("ID").toString());
			futures.add(client.getFileFromDocumentLibrary(listName, String.valueOf(pictureId), true));
		}

		List<DocumentLibraryItem> pictures = OfficeFuture.all(futures).get();

		for (final SPListItem carItem : listItems) {
			for (DocumentLibraryItem picture : pictures) {
				if (String.valueOf(carItem.getId()).equals(picture.getItemId())) {
					items.add(new CarListViewItem(carItem, picture.getContent()));
					break;
				}
			}
		}

		return items;
	}

	public void updateSelectedCar(CarListViewItem carViewItem) throws Exception {
		updatePicture(carViewItem);
		updateCarData(carViewItem);
	}

	public int saveNewCar(final CarListViewItem carViewItem) throws Exception {
		int pictureId = saveNewPicture(carViewItem);
		carViewItem.setCarId(pictureId);
		updateCarData(carViewItem);

		return pictureId;
	}

	private void updatePicture(CarListViewItem carViewItem) throws Exception {
		SharepointListsClientWithFiles client = (SharepointListsClientWithFiles) getListsClient();
		String listName = mApplication.getPreferences().getLibraryName();
		if (client != null) {
			SPFile spFile = client.getSPFileFromPictureLibrary(listName, carViewItem.getCarId())
					.get();
			client.uploadFile(listName, spFile.getData("Name").toString(), carViewItem.getPicture())
					.get();
		}
	}

	private int saveNewPicture(CarListViewItem carViewItem) throws Exception {
		SharepointListsClientWithFiles client = (SharepointListsClientWithFiles) getListsClient();
		String listName = mApplication.getPreferences().getLibraryName();

		SPFile file = client.uploadFile(listName, UUID.randomUUID().toString() + ".png",
				carViewItem.getPicture()).get();
		String pictureUrl = file.getData("ServerRelativeUrl").toString();
		String id = client.getListItemIdForFileByServerRelativeUrl(pictureUrl).get();
		return Integer.parseInt(id);
	}

	private void updateCarData(CarListViewItem carViewItem) throws Exception {
		SharepointListsClient client = getListsClient();
		String listName = mApplication.getPreferences().getLibraryName();
		SPList carList = client.getList(listName).get();
		SPListItem item = carViewItem.getListItem();
		client.updateListItem(item, carList).get();
	}

	public void deleteCar(CarListViewItem mCarViewItem) {
		SharepointListsClient client = getListsClient();
		String listName = mApplication.getPreferences().getLibraryName();

		try {
			client.deleteListItem(mCarViewItem.getListItem(), listName).get();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
