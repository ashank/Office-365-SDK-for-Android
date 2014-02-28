/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.example.office.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.office.Constants.UI.Screen;
import com.example.office.Constants.UI.ScreenGroup;
import com.example.office.R;
import com.example.office.logger.Logger;

public class SlidingDrawerAdapter extends ArrayAdapter<Screen> {

    /**
     * Layout inflater.
     */
    private LayoutInflater mInflater;

    /**
     * Resource id for single ListView item
     */
    private int mItemResource;

    /**
     * Resource id for delimiter.
     */
    private int mDelimResource;

    /**
     * List of delimiters positions in adapter.
     */
    private Collection<Integer> mDelimitersPositions;
    
    /**
     * View type for ordinal view.
     */
    private final int VIEW_TYPE_ITEM = 0;
    
    /**
     * View type for delimiter.
     */
    private final int VIEW_TYPE_DELIMITER = 1;

    /**
     * Default constructor.
     * 
     * @param context Application context.
     * @param itemResource Drawer item resource id.
     */
    public SlidingDrawerAdapter(Context context, int itemResource, int delimResource) {
        super(context, itemResource);
        try {
            mInflater = LayoutInflater.from(context);
            mItemResource = itemResource;
            mDelimResource = delimResource;
            mDelimitersPositions = new ArrayList<Integer>();

            for (Screen screen : Screen.values()) {
                if (screen.in(ScreenGroup.DRAWER)) {
                    add(screen);
                }
            }

            int delimPos = 0;
            for (ScreenGroup group : ScreenGroup.values()) {
                if (group.equals(ScreenGroup.DRAWER)) {
                    continue;
                }
                delimPos = group.getMembers().size() + delimPos;
                mDelimitersPositions.add(delimPos);
                ++delimPos;
            }

        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".SlidingDrawerAdapter(): Error.");
        }
    }

    private class ItemHolder {
        ImageView icon;
        TextView title;
        TextView count;
    }

    @Override
    public Screen getItem(int position) {
        return mDelimitersPositions.contains(position) ? null : super.getItem(getRealPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(getRealPosition(position));
    }

    @Override
    public int getCount() {
        // number of views = number of items (stored in superclass) + number of delimiters
        return super.getCount() + mDelimitersPositions.size();
    }
    
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    
    @Override
    public int getItemViewType(int position) {
        return mDelimitersPositions.contains(position) ? VIEW_TYPE_DELIMITER : VIEW_TYPE_ITEM;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return !mDelimitersPositions.contains(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ItemHolder holder;
            if (convertView == null) {
                holder = new ItemHolder();

                switch (getItemViewType(position)) {
                    case VIEW_TYPE_ITEM:
                        convertView = mInflater.inflate(mItemResource, null);
                        
                        holder.icon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
                        holder.title = (TextView) convertView.findViewById(R.id.drawer_item_title);
                        holder.count = (TextView) convertView.findViewById(R.id.drawer_item_count);
                        break;

                    case VIEW_TYPE_DELIMITER:
                        convertView = mInflater.inflate(mDelimResource, null);
                        break;
                }

                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            Screen screen = getItem(position);
            if (screen != null) {
                String title = screen.getName(getContext());
                Drawable icon = screen.getIcon(getContext());

                String count = "";

                holder.icon.setImageDrawable(icon);
                holder.title.setText(title);
                holder.count.setText(count);
            }

        } catch (Exception e) {}

        return convertView;
    }

    /**
     * Returns a real screen position considering delimiters.
     * 
     * @param position Position in ListView.
     * @return Position in adapter.
     */
    private int getRealPosition(int position) {
        int delimsBefore = 0;
        Iterator<Integer> iterator = mDelimitersPositions.iterator();
        while (iterator.hasNext()) {
            if (position > iterator.next()) {
                ++delimsBefore;
            }
        }

        return position - delimsBefore;
    }
}
