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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.ShareActionProvider;

import com.example.office.Constants.UI;
import com.example.office.Constants.UI.Screen;
import com.example.office.Constants.UI.ScreenGroup;
import com.example.office.OfficeApplication;
import com.example.office.R;
import com.example.office.adapters.SlidingDrawerAdapter;
import com.example.office.logger.Logger;
import com.example.office.mail.ui.box.EventsFragment;
import com.example.office.mail.ui.box.DraftsFragment;
import com.example.office.mail.ui.box.ContactsFragment;

/**
 * Activity that common application UI logic related to Action Bar, Sliding Drawer and Fragments providing main content.
 */
public class Office365DemoActivity extends BaseActivity implements SearchView.OnQueryTextListener, IFragmentNavigator {

    /**
     * Default email box to be used.
     */
    private static final UI.Screen DEFAULT_BOX = Screen.MAILBOX;

    /**
     * Tag to pass current fragment.
     */
    private static final String STATE_FRAGMENT_TAG = "current_fragment_tag";

    /**
     * Search view.
     */
    private SearchView mSearchView;

    /**
     * Left-side drawer.
     */
    private DrawerLayout mDrawerLayout;

    /**
     * List view to populate the drawer.
     */
    private ListView mDrawerList;

    /**
     * Toggle t ohandle the Drawer
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Title displayed in the action bar.
     */
    private CharSequence mTitle;

    /**
     * Title displayed when the drawer is shown.
     */
    private CharSequence mDrawerTitle;

    /**
     * Current fragment tag.
     */
    private static String mCurrentFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mTitle = mDrawerTitle = getTitle();

        // Setting up Action Bar and Tabs.
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setLogo(R.drawable.ic_action_mail);

        Tab tab = actionBar.newTab().setText(UI.Screen.CONTACTS.getName(this)).setTag(UI.Screen.CONTACTS.getName(this))
                .setTabListener(new TabListener<ContactsFragment>(this, UI.Screen.CONTACTS.getName(this), ContactsFragment.class));
        actionBar.addTab(tab);

        tab = actionBar.newTab().setText(UI.Screen.MAILBOX.getName(this)).setTag(UI.Screen.MAILBOX.getName(this))
                .setTabListener(new TabListener<DraftsFragment>(this, UI.Screen.MAILBOX.getName(this), DraftsFragment.class));
        actionBar.addTab(tab, true);

        tab = actionBar.newTab().setText(UI.Screen.EVENTS.getName(this)).setTag(UI.Screen.EVENTS.getName(this))
                .setTabListener(new TabListener<EventsFragment>(this, UI.Screen.EVENTS.getName(this), EventsFragment.class));
        actionBar.addTab(tab);

        // Setting up sliding drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        SlidingDrawerAdapter drawerAdapter = new SlidingDrawerAdapter(OfficeApplication.getContext(), R.layout.drawer_list_item,
                R.layout.drawer_delimiter);
        mDrawerList.setAdapter(drawerAdapter);

        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Screen[] drawerScreens = ScreenGroup.DRAWER.getMembers().toArray(new Screen[0]);
                    Screen currentScreen = DEFAULT_BOX;

                    // use id instead of position here because some positions used by delimiters, id contains real index of clicked item
                    if (drawerScreens != null && drawerScreens.length - 1 >= id) {
                        currentScreen = drawerScreens[(int) id];
                    }
                    switchScreen(currentScreen);
                } catch (Exception e) {
                    Logger.logApplicationException(e, getClass().getSimpleName() + "onItemClick(): Error.");
                }
            }
        });

        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* drawer navigation image replacing '<' */
        R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            switchScreen(DEFAULT_BOX);
        } else {
            // This is not used mostly as we're going back to this activity when it
            // is at the top of the back stack. So (as it does have 'singleTop' in parameters) it is
            // simply restored and onNewIntent() is called instead of onCreate().
            // So savedInstanceState will usually be null. This is added to anticipate other future use cases.
            String tag = savedInstanceState.getString(STATE_FRAGMENT_TAG);
            switchScreen(Screen.getByTag(tag, this));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // This activity is 'singleTop' so all 'reopenings' should be handled here.
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FRAGMENT_TAG, mCurrentFragmentTag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inbox, menu);

        MenuItem searchItem = menu.findItem(R.id.inbox_menu_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);

        MenuItem shareItem = menu.findItem(R.id.inbox_menu_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        shareActionProvider.setShareIntent(getShareIntent());

        searchItem.setOnActionExpandListener(new OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true; // Return true to expand action view
            }
        });

        return true;
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.inbox_menu_search: {
                mSearchView.setIconified(false);
                return true;
            }
            case R.id.inbox_menu_settings: {
                return false;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Hide search if drawer is open
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.inbox_menu_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BaseFragment fragment = getCurrentFragment();
            if (fragment != null) {
                boolean result;
                    result = fragment.onKeyDown(keyCode, event);

                return result ? true : super.onKeyDown(keyCode, event);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Choose one of the available screens to display (via appropriate Fragment).
     *
     * @param newScreen Screen to be shown.
     */
    private void switchScreen(UI.Screen newScreen) {
        try {
            ActionBar actionBar = getActionBar();

            mDrawerList.setItemChecked(newScreen.ordinal(), true);
            setTitle(newScreen.getName(this));
            actionBar.setLogo(newScreen.getIcon(this));

            if (newScreen.in(ScreenGroup.MAIL)) {
                Screen currentScreen = Screen.getByTag(mCurrentFragmentTag, this);
                if (!currentScreen.in(ScreenGroup.MAIL)) {
                    Fragment newFragment;
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    switch (newScreen) {
                        case CONTACTS:
                            newFragment = new ContactsFragment();
                            break;
                        case MAILBOX:
                            newFragment = new DraftsFragment();
                            break;
                        case EVENTS: {
                            newFragment = new EventsFragment();
                            break;
                        }
                        default: {
                            newFragment = new DraftsFragment();
                            break;
                        }
                    }
                    fragmentTransaction.add(R.id.content_pane, newFragment, newScreen.getName(this));
                    fragmentTransaction.commit();
                }
                actionBar.selectTab(actionBar.getTabAt(newScreen.ordinal()));
                mCurrentFragmentTag = newScreen.getName(this);
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".switchBox(): Error.");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        try {
            mTitle = title;
            getActionBar().setTitle(mTitle);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".setTitle(): Error.");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            // Sync state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".onPostCreate(): Error.");
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            mDrawerToggle.onConfigurationChanged(newConfig);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".onConfigurationChanged(): Error.");
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        try {
            ListFragment<?, ?> fragment = (ListFragment<?, ?>) getCurrentFragment();
            if (fragment != null) {
                fragment.onQueryTextChange(query);
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + "onQueryTextSubmit(): Error.");
        }
        return false;
    }

    private BaseFragment getCurrentFragment() {
        FragmentManager manager = getFragmentManager();
        if (!TextUtils.isEmpty(mCurrentFragmentTag)) {
            Fragment fragment = manager.findFragmentByTag(mCurrentFragmentTag);
            if (fragment != null) {
                return (BaseFragment) fragment;
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        BaseFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Manages tab interaction and content.
     *
     * @param <T> Class extending {@link Fragment} to be managed as a tab content.
     */
    private static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final BaseActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /**
         * Constructor used each time a new tab is created.
         *
         * @param activity The host Activity, used to instantiate the fragment
         * @param tag The identifier tag for the fragment
         * @param clazz The fragment's Class, used to instantiate the fragment
         */
        public TabListener(BaseActivity activity, String tag, Class<T> clazz) {
            mActivity = activity;
            mTag = tag;
            mClass = clazz;
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction transaction) {
            try {
                if (mFragment == null) {
                    mFragment = Fragment.instantiate(mActivity, mClass.getName());
                    transaction.add(R.id.content_pane, mFragment, mTag);
                } else {
                    transaction.attach(mFragment);
                }

                ActionBar actionBar = mActivity.getActionBar();
                actionBar.setIcon(Screen.getByTag(mTag, mActivity).getIcon(mActivity));
                actionBar.setTitle(Screen.getByTag(mTag, mActivity).getName(mActivity));

                mCurrentFragmentTag = mTag;
            } catch (Exception e) {
                Logger.logApplicationException(e, getClass().getSimpleName() + ".onTabSelected(): Error.");
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            try {
                if (mFragment != null) {
                    ft.detach(mFragment);
                }
            } catch (Exception e) {
                Logger.logApplicationException(e, getClass().getSimpleName() + ".onTabUnselected(): Error.");
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {}
    }

    @Override
    public void setCurrentFragmentTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            mCurrentFragmentTag = tag;
        }
    }

    @Override
    public String getCurrentFragmentTag() {
        return mCurrentFragmentTag;
    }
}
