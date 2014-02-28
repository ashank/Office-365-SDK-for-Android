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
package com.example.office;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * Stores application public constants such as URLs to update configurations, default check back-in values, etc.
 */
public class Constants {

    /**
     * TEST Endpoint to retrieve list current messages from the inbox.
     */
    public static final String MAIL_MESSAGES_TEST = "https://outlook.office365.com/ews/odata";

    /**
     * Application logging TAG.
     */
    public static final String APP_TAG = "Office365Demo";

    /**
     * SIM number used as a stub when: <br/>
     * 1. real sim is not present (e.g. on the emulator) <b>AND</b> <br/>
     * 2. {@link Configuration#EMULATE_SIM_PRESENT} is set to <code>true</code>.
     */
    public static final String MOCK_SIM = "12345678910";

    /**
     * Url for Oauth2 authorization page.
     */
    public static final String AUTHORITY_URL = "https://login.windows-ppe.net/p365ppetap04.ccsctp.net";

    /**
     * Application unique ID for Oauth2 authorization.
     */
    public static final String CLIENT_ID = "a7558c9a-c964-4fbf-be19-2f277f78a586";

    /**
     * Resource id for authorization and where need get access.
     */
    public static final String RESOURCE_ID = "https://outlook.office365.com/";
    /**
     * Url application will be redirected after authentication.
     */
    public static final String REDIRECT_URL = "http://msopentech.com";
    
    /**
     * User name hint in authentication form.
     */
    public static final String USER_HINT = "Enter your login here";

    /**
     * Holds enumerations and constants related to application UI
     */
    public static class UI {

        /**
         * Unifies a number of screen references using some grouping criteria. Defines a common set of methods to make group operable.
         */
        public static interface IScreenGroup {
            /**
             * Retrieves members of the group.
             *
             * @return List of group members.
             */
            public EnumSet<Screen> getMembers();

            /**
             * Adds a member to the group
             *
             * @param member Screen to be added to the group.
             */
            public void addMember(Screen member);
        }

        /**
         * Splits all application screens in defined groups based on it's functionality.
         */
        public static enum ScreenGroup implements IScreenGroup {
            MAIL, // 'Box screens' that can contain email messages
            DRAWER, // References to screens that can be accessed via left sliding drawer.
            CONTACTS, EVENTS;

            /**
             * List of members of this group. It is initialized statically.
             */
            private List<Screen> members = new LinkedList<Screen>();

            static {
                // Forcing dependent enumeration initiation.
                try {
                    Class.forName(Screen.class.getName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Constants.ScreenGroup.static(): Class 'Screen' not found", e);
                }
            }

            @Override
            public EnumSet<Screen> getMembers() {
                return EnumSet.copyOf(members);
            }

            /**
             * Returns list of member names.
             *
             * @param context Application context.
             *
             * @return list of member names.
             */
            public List<String> getMemberNames(Context context) {
                List<String> names = new ArrayList<String>(members.size());
                for (Screen screen : members) {
                    names.add(screen.getName(context));
                }
                return names;
            }

            @Override
            public void addMember(Screen member) {
                members.add(member);
            }
        }

        /**
         * Enumerates application screens
         */
        public enum Screen {
            CONTACTS(R.string.screens_contacts, R.drawable.ic_contact, ScreenGroup.CONTACTS, ScreenGroup.MAIL, ScreenGroup.DRAWER),
            MAILBOX(R.string.screens_mailbox, R.drawable.mailbox, ScreenGroup.MAIL, ScreenGroup.DRAWER),
            EVENTS(R.string.screens_events, android.R.drawable.ic_menu_today, ScreenGroup.EVENTS, ScreenGroup.MAIL, ScreenGroup.DRAWER),
            HELP(R.string.screens_help, R.drawable.help, ScreenGroup.DRAWER);

            /**
             * Resource id holding the name for this screen.
             */
            private int titleId = -1;

            /**
             * Resource if holding the icon for this screen.
             */
            private int iconId = -1;

            /**
             * Internal constructor that is statically invoked by grouping class.
             *
             * @param screenGroupList Grouping class holding a list of its members.
             */
            private Screen(int titleId, int iconId, IScreenGroup... screenGroupList) {
                this.titleId = titleId;
                this.iconId = iconId;
                for (IScreenGroup group : screenGroupList) {
                    group.addMember(this);
                }
            }

            /**
             * Returns Screen name from resources.
             *
             * @param context Application context.
             *
             * @return Screen name from resources.
             */
            public String getName(Context context) {
                return context.getResources().getString(titleId);
            }

            /**
             * Returns icon from resources for current Screen.
             *
             * @param context Application context.
             *
             * @return Screen icon from resources.
             */
            public Drawable getIcon(Context context) {
                return context.getResources().getDrawable(iconId);
            }

            public int getIconId() {
                return iconId;
            }

            /**
             * Returns screen that has the same name as provided tag.
             *
             * @param tag Name of the screen we're looking for.
             * @param context Application context.
             *
             * @return Screen with specified name or <code>null</code> if tag is <code>null</code> or empty ot context is <code>null</code>
             *         or if no screen is found.
             */
            public static Screen getByTag(String tag, Context context) {
                if (TextUtils.isEmpty(tag) || context == null) return null;

                for (Screen screen : Screen.values()) {
                    if (screen.getName(context).equals(tag)) {
                        return screen;
                    }
                }

                return null;
            }

            /**
             * Tells if this screen belongs to the provided group.
             *
             * @param group Group to check if this dcreen belongs to it.
             *
             * @return <code>true</code> if screen belongs to the group. <code>false</code> otherwise.
             */
            public boolean in(IScreenGroup group) {
                for (Screen screen : group.getMembers()) {
                    if (screen.equals(this)) return true;
                }
                return false;
            }
        }
    }
}
