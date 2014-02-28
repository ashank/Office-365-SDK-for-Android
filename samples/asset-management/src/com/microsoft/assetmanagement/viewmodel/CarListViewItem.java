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

// TODO: Auto-generated Javadoc
/**
 * The Class CarListViewItem.
 */
public class CarListViewItem {

	/** The m car id. */
	private String mCarId;
	
	/** The m picture. */
	private byte[] mPicture;
	
 	/** The m list item. */
	 private SPListItem mListItem;

	/**
	 * Instantiates a new car list view item.
	 */
	public CarListViewItem() {
		mListItem = new SPListItem();
	}

	/**
	 * Instantiates a new car list view item.
	 *
	 * @param listItem the list item
	 * @param picture the picture
	 */
	public CarListViewItem(SPListItem listItem, byte[] picture) {
		mListItem = listItem;
		mPicture = picture;
	}

	/**
	 * Gets the list item.
	 *
	 * @return the list item
	 */
	public SPListItem getListItem() {
		return mListItem;
	}

	/**
	 * Populate.
	 */
	public void populate() {
		mCarId = getCarId();
	}

	/**
	 * Gets the car id.
	 *
	 * @return the car id
	 */
	public String getCarId() {
		mCarId = safeString(mListItem.getData("Id"));
		return mCarId;
	}
	
	/**
	 * Gets the data.
	 *
	 * @param key the key
	 * @return the data
	 */
	public String getData(String key) {
		return safeString(mListItem.getData(key));
	}

	/**
	 * Sets the car id.
	 *
	 * @param id the new car id
	 */
	public void setCarId(int id) {
		mListItem.setData("Id", id);
	}
	
	/**
	 * Sets the car title.
	 *
	 * @param title the new car title
	 */
	public void setCarTitle(String title) {
		mListItem.setData("Title", title);
	}
	
	/**
	 * Sets the car description.
	 *
	 * @param description the new car description
	 */
	public void setCarDescription(String description) {
		mListItem.setData("Description", description);
	}

	/**
	 * Gets the picture.
	 *
	 * @return the picture
	 */
	public byte[] getPicture() {
		return mPicture;
	}
	
	/**
	 * Gets the thumbnail.
	 *
	 * @return the thumbnail
	 */
	public Bitmap getThumbnail(){
		
		if (mPicture != null)
		{
			return generateThumbnail(mPicture);
		}
		return null;
	}

	/**
	 * Sets the picture.
	 *
	 * @param picture the new picture
	 */
	public void setPicture(byte[] picture) {
		mPicture = picture;
	}
	
	/**
	 * Safe string.
	 *
	 * @param object the object
	 * @return the string
	 */
	private String safeString(Object object) {
		if (object == null)
			return "";
		if (object.equals(JSONObject.NULL)) {
			return "";
		}
		return object.toString().trim();
	}
	
	/**
	 * Generate thumbnail.
	 *
	 * @param data the data
	 * @return the bitmap
	 */
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
