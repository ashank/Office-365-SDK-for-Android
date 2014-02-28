/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.assetmanagement.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.assetmanagement.R;
import com.microsoft.assetmanagement.viewmodel.CarListViewItem;

public class CarItemAdapter extends BaseAdapter {

	private Activity mActivity;
	private List<CarListViewItem> mData;
	private static LayoutInflater inflater = null;

	public CarItemAdapter(Activity activity, List<CarListViewItem> data) {
		mActivity = activity;
		mData = data;
		inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null)
			view = inflater.inflate(R.layout.activity_sharepoint_list_item, null);

		TextView carTitle = (TextView) view.findViewById(R.id.carTitle);
		TextView carDescription = (TextView)view.findViewById(R.id.carDescription);
		ImageView thumbnail = (ImageView) view.findViewById(R.id.list_image);

		CarListViewItem item = mData.get(position);
		carTitle.setText(item.getData("Title"));
		carDescription.setText(item.getData("Description"));
		byte[] imageData = item.getPicture();
		if (imageData != null && thumbnail != null) {
			thumbnail.setImageBitmap(item.getThumbnail());
		}
		return view;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
