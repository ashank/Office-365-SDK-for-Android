/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.microsoft.assetmanagement.files.BitmapResizer;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;

// TODO: Auto-generated Javadoc
/**
 * The Class DisplayCarAdapter.
 */
public class DisplayCarAdapter extends PagerAdapter {

	/** The m activity. */
	private Activity mActivity;
	
	/** The m data. */
	private List<CarListViewItem> mData;
	
	/** The m resizer. */
	private BitmapResizer mResizer;

	/**
	 * Instantiates a new display car adapter.
	 *
	 * @param activity the activity
	 */
	public DisplayCarAdapter(Activity activity) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivity = activity;
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mResizer = new BitmapResizer(displayMetrics);
		mData = new ArrayList<CarListViewItem>();
	}

	/**
	 * Instantiates a new display car adapter.
	 *
	 * @param activity the activity
	 * @param data the data
	 */
	public DisplayCarAdapter(Activity activity, List<CarListViewItem> data) {
		this(activity);
		mData = data;
	}

	/**
	 * Instantiates a new display car adapter.
	 *
	 * @param activity the activity
	 * @param item the item
	 */
	public DisplayCarAdapter(Activity activity, CarListViewItem item) {
		this(activity);
		mData.add(item);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#isViewFromObject(android.view.View, java.lang.Object)
	 */
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((ImageView) object);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		CarListViewItem item = mData.get(position);
		ImageView imageView = null;

		byte[] picture = item.getPicture();
		imageView = new ImageView(mActivity);
		try {

			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setImageBitmap(mResizer.getBitmapFrom(picture));
			((ViewPager) container).addView(imageView, 0);
		} catch (Exception e) {
			Log.e("Asset", e.getMessage());
		}
		return imageView;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.ViewGroup, int, java.lang.Object)
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((ImageView) object);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mData.size();
	}
}
