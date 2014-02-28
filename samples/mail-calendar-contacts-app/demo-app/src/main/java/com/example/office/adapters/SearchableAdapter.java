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
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.office.logger.Logger;

/**
 * Adapter to display and search/filter items in ListView.
 */
public abstract class SearchableAdapter<T> extends ArrayAdapter<T> {

    /**
     * Layout inflater
     */
    protected LayoutInflater mInflater;

    /**
     * Resource id for single ListView item
     */
    protected int mItemResource;

    /**
     * Current filter query;
     */
    protected CharSequence mCurrentFilter;

    /**
     * A vector that contains an actual set of mSuggestions. It is updated each time the content of mUrlEditTextView is changed.
     */
    protected List<T> mFilteredData;
    /**
     * A data reference which is used to populate mSuggestions. It is never allocated internally and always refers to the merged storage of
     * PersistenceManager.
     */
    protected List<T> mOriginalData;

    /**
     * Default constructor.
     *
     * @param context Application context.
     * @param resource List item resource id.
     * @param data Data to populate.
     */
    public SearchableAdapter(Context context, int resource, List<T> data) {
        super(context, resource, data != null ? data : new ArrayList<T>());
        try {
            mInflater = LayoutInflater.from(context);
            mItemResource = resource;
            mOriginalData = new ArrayList<T>(data);
            mFilteredData = new ArrayList<T>();
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".constructor(): Error.");
        }
    }

    /**
     * Updates adapter with new underlying data. No update takes place if provided data is <code>null</code>. Provide an empty list if you
     * would like to clean up all current items.
     *
     * @return <code>true</code> if there were no errors, <code>false</code> otherwise.
     */
    public boolean update(List<T> data) {
        try {
            if (data != null) {
                mOriginalData = new ArrayList<T>(data);
                mFilteredData = new ArrayList<T>();

                return updateAndNotify(mOriginalData);
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".update(): Error.");
        }
        return false;
    }

    /**
     * Clears current data, updates it with a new one and notifies adapter of the change.
     *
     * @param data Data update.
     */
    private boolean updateAndNotify(List<T> data) {
        try {
            clear();
            if (data != null) {
                for (T item : data) {
                    add(item);
                }
            }
            notifyDataSetChanged();
            return true;
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".updateAndNotify(): Error.");
        }
        return false;
    }

    /**
     * Sets a text for a view with additional verification.
     *
     * @param view TextView to set text into.
     * @param text String to set.
     */
    public void setViewText(TextView view, String text) {
        try {
            if (view != null && text != null) {
                view.setText(text);
            }
        } catch (Exception e) {}
    };

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * Constructs and returns View for filling ListView
     */
    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    /**
     * Removes item from the selected position in the adapter.
     *
     * @param position Item position.
     */
    public void remove(int position) {
        try {
            remove(getItem(position));
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + "remove(): Error.");
        }
    }

    /**
     * Defines if provided item matches provided constraint and should be present in a filtered data set.
     *
     * @param item Item to filter.
     * @param constraint Constraint to check item against.
     *
     * @return <code>true</code> if item should be present in the filtered data set, <code>false</code> otherwise.
     */
    protected abstract boolean isMatch(T item, CharSequence constraint);

    /**
     * An instance of Filter for handling mSuggestions. No filtering would be applied in case of error.
     */
    Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            try {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    if (constraint.equals(mCurrentFilter)) {
                        // No need to double-filter.
                        return null;
                    } else if (constraint.equals("") && !TextUtils.isEmpty(mCurrentFilter)) {
                        // Check if this request is an empty string while previous filter is NOT so original data has to be restored.
                        mCurrentFilter = constraint;
                        return getOriginalFilter();
                    }
                }

                mCurrentFilter = constraint;
                if (!TextUtils.isEmpty(constraint)) {
                    mFilteredData.clear();
                    for (T item : mOriginalData) {
                        if(isMatch(item, constraint)) {
                            mFilteredData.add(item);
                        }
                    }

                    filterResults.values = mFilteredData;
                    filterResults.count = mFilteredData.size();

                    return filterResults;
                } else {
                    return getOriginalFilter();
                }
            } catch (Exception e) {
                Logger.logApplicationException(e, getClass().getSimpleName() + ".mFilter.performFiltering(): Error.");
            }
            return getOriginalFilter();
        }

        /**
         * Returns filter that imposes no filtering on original data.
         *
         * @return List filter, or <code>null</code> in case of error.
         */
        private FilterResults getOriginalFilter() {
            try {
                FilterResults filterResults = new FilterResults();
                filterResults.values = mOriginalData;
                filterResults.count = mOriginalData.size();
                return filterResults;
            } catch (Exception e) {
                Logger.logApplicationException(e, getClass().getSimpleName() + ".mFilter.getOriginalFilter(): Error.");
            }
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                if (results != null) {
                    @SuppressWarnings("unchecked")
                    List<T> list = (List<T>) results.values;
                    updateAndNotify(list);
                }
            } catch (Exception e) {
                Logger.logApplicationException(e, getClass().getSimpleName() + ".mFilter.publishResults(): Error.");
            }
        }
    };

}
