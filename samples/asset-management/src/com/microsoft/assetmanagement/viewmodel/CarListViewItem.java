/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.viewmodel;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.microsoft.office365.lists.SPListItem;

public class CarListViewItem {

	private String mCarId;
	private byte[] mPicture;
	
 	private SPListItem mListItem;

	public CarListViewItem() {
		mListItem = new SPListItem();
	}

	public CarListViewItem(SPListItem listItem, byte[] picture) {
		mListItem = listItem;
		mPicture = picture;
	}

	public SPListItem getListItem() {
		return mListItem;
	}

	public void populate() {
		mCarId = getCarId();
	}

	public String getCarId() {
		mCarId = safeString(mListItem.getData("Id"));
		return mCarId;
	}
	
	public String getData(String key) {
		return safeString(mListItem.getData(key));
	}

	public void setCarId(int id) {
		mListItem.setData("Id", id);
	}
	
	public void setCarTitle(String title) {
		mListItem.setData("Title", title);
	}
	
	public void setCarDescription(String description) {
		mListItem.setData("Description", description);
	}

	public byte[] getPicture() {
		return mPicture;
	}
	
	public Bitmap getThumbnail(){
		
		if (mPicture != null)
		{
			return generateThumbnail(mPicture);
		}
		return null;
	}

	public void setPicture(byte[] picture) {
		mPicture = picture;
	}
	
	private String safeString(Object object) {
		if (object == null)
			return "";
		if (object.equals(JSONObject.NULL)) {
			return "";
		}
		return object.toString().trim();
	}
	
	protected Bitmap generateThumbnail(byte[] data) {

		Bitmap bitmap = null;
		try {

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, opts);

			final int REQUIRED_SIZE = 70;
			int width_tmp = opts.outWidth, height_tmp = opts.outHeight;
			int scale = 1;

			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeByteArray(data, 0, data.length, o2);
		} catch (Exception e) {
			Log.e("getBitmap", e.getMessage());
		}
		return bitmap;
	}
}
