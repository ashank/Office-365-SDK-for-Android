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
package com.example.office.ui;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.office.adapters.SearchableAdapter;
import com.example.office.logger.Logger;

/**
 * Inbox fragment containing logic related to managing inbox emails.
 */
public abstract class ListFragment<T, A extends SearchableAdapter<T>> extends BaseFragment {

    /**
     * Adapter for ListView containing items
     */
    protected A mAdapter;

    /**
     * Constructor.
     */
    public ListFragment() {}

    /**
     * Retrieves the list of items to display in the list.
     *
     * @return List of items to display in the list.
     */
    protected abstract List<T> getListData();

    /**
     * Sets up the list. Supposed to init List UI if necessary and fill it with data so it can be displayed.
     */
    protected abstract void initList();

    /**
     * Resource id of the layout used to draw a list item.
     *
     * @return Resource id.
     */
    protected abstract int getListItemLayoutId();

    /**
     * Resource id of the a List view in the layout.
     *
     * @return Resource id.
     */
    protected abstract int getListViewId();

    /**
     * Resource id of the an infinite progress bar that will be shown instead of list.
     *
     * @return Resource id.
     */
    protected abstract int getProgressViewId();

    /**
     * Resource id of the container holding all the view elements of the fragment except for progress bar.
     *
     * @return Resource id.
     */
    protected abstract int getContentContainerId();

    @Override
    protected abstract int getFragmentLayoutId();

    /**
     * Gets/creates singleton adapter used to back the list. If there is none - creates one and instantiates dedicated class field. Should be overridden. Returns instance of {@link ArrayAdapter} by
     * default. Data from {@link #getListData()} is used.
     *
     * @return List adapter, or <code>null</code> in case of error.
     */
    protected A getListAdapterInstance() {
        try {
            return getListAdapterInstance(null);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".getListAdapter(): Error.");
        }
        return null;
    }

    /**
     * Gets/creates singleton adapter used to back the list. If there is none - creates one and instantiates dedicated calss field. Should be overridden. Returns instance of {@link ArrayAdapter} by
     * default. Data from {@link #getListData()} is used if if supplied data is <code>null</code>.
     *
     * @param data Data to back the adapter.
     *
     * @return List adapter, or <code>null</code> in case of error.
     */
    protected abstract A getListAdapterInstance(List<T> data);

    /**
     * Gets/creates singleton view to add as a footer to the list. If there is none - creates one and instantiates dedicated class field.
     *
     * @return Footer view, or <code>null</code> in case of error.
     */
    protected abstract View getListFooterViewInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        try {
            // Setting up adapter
            BaseAdapter adapter = getListAdapterInstance();
            final ListView listView = (ListView) rootView.findViewById(getListViewId());
            listView.setAdapter(adapter);

            // Adding footer if any.
            View view = getListFooterViewInstance();
            if (view != null) {
                listView.addFooterView(view);
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".onCreateView(): Error.");
        }

        return rootView;
    }

    /**
     * Called by parent activity to propagate coressponding event. Perfroms filtering based on the adapter
     * implementation returned by {@link #getListAdapterInstance()}.
     *
     * @param query Filter query.
     *
     * @return <code>false</code>.
     */
    public boolean onQueryTextChange(String query) {
        try {
            SearchableAdapter<T> adapter = (SearchableAdapter<T>) getListAdapterInstance();
            if (adapter != null && adapter.getFilter() != null) {
                adapter.getFilter().filter(query);
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + "onQueryTextSubmit(): Error.");
        }
        return false;
    }

    /**
     * Toggles between showing either progress indicator or a content pane with actual data.
     *
     * @param isWorkInProgress Progress status.
     * @param showContentProgress Defines if progress bar should be displayed on the content pane of the fragment.
     */
    protected void showWorkInProgress(boolean isWorkInProgress, boolean showContentProgress) {
        try {
            View rootView = getView();
            if ((showContentProgress || !isWorkInProgress) && rootView != null) {
                View progressIndicator = rootView.findViewById(getProgressViewId());
                View contentPane = rootView.findViewById(getListViewId());
                progressIndicator.setVisibility(isWorkInProgress ? View.VISIBLE : View.GONE);
                contentPane.setVisibility(isWorkInProgress ? View.GONE : View.VISIBLE);
            }

            // Separate progress indicator in ActionBar.
            getActivity().setProgressBarIndeterminateVisibility(isWorkInProgress);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".showWorkInProgress(): Error.");
        }
    }

}
