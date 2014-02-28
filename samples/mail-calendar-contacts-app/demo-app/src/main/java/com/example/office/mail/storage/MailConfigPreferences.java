package com.example.office.mail.storage;

import com.example.office.OfficeApplication;
import com.example.office.logger.Logger;
import com.example.office.mail.data.MailItem;
import com.example.office.mail.data.MailConfig;
import com.example.office.storage.LocalPersistence;
import com.example.office.utils.NetworkUtils;

/**
 * Implements logic to persist {@link MailConfig} preferences.
 */
public class MailConfigPreferences {

    /**
     * Mails configuration file.
     */
    private static final String PREFERENCE_FILE = "com.example.office.mail.mails_config";

    /**
     * Interface to get notifications when Mails configuration is updated.
     */
    public interface OnMailConfigUpdatedListener {

        /**
         * Notifies the listener that Mails configuration was updated.
         * 
         * @param MailsConfig Updated Mails configuration.
         */
        public void onMailsConfigUpdated(MailConfig MailsConfig);
    }

    /**
     * Retrieves saved Mails configuration.
     * 
     * @return Saved Mails configuration.
     */
    public static MailConfig loadConfig() {
        try {
            MailConfig mailConfig = (MailConfig) LocalPersistence.readObjectFromFile(OfficeApplication.getContext(), PREFERENCE_FILE);
            if (mailConfig == null) return null;

            String curentSimNumber = NetworkUtils.getCurrentSimCardNumber();
            String savedSimNumber = mailConfig.getSimNumber();
            if (curentSimNumber == null || savedSimNumber == null || !curentSimNumber.contentEquals(savedSimNumber)) return null;
            return mailConfig;
        } catch (Exception e) {
            Logger.logApplicationException(e, MailConfigPreferences.class.getSimpleName() + ".loadConfig(): Failed.");
        }
        return null;
    }

    /**
     * Updates mail configuration and saves it to shared preferences. New (server) content has a priority. Each server item 'id' is used to
     * look up local item with the same 'id'. If match is found 'box' value of local item is copied into a server item. Resulting updated
     * server list is saved as a new configuration.
     * 
     * @param newConfig New configuration.
     */
    public static void updateConfiguration(MailConfig newConfig) {
        try {
            MailConfig localConfig = loadConfig();
            if (localConfig != null && newConfig != null && newConfig.getMails() != null) {
                for (MailItem serverItem : newConfig.getMails()) {
                    MailItem localItem = localConfig.getMailById(serverItem.getId());
                    if (localItem != null) {
                        newConfig.updateMailById(serverItem.getId(), (MailItem)serverItem.setBox(localItem.getBox())/*.
                                setIsRead(localItem.getIsRead()).setImportance(localItem.getImportance())*/);
                    }
                }
            }

            saveConfiguration(newConfig);
        } catch (Exception e) {
            Logger.logApplicationException(e, MailConfigPreferences.class.getSimpleName() + ".updateConfiguration(): Failed.");
        }
    }

    /**
     * Saves Mails configuration to shared preferences. Doesn't notify listeners.
     * 
     * @param mailConfig Mails configuration.
     */
    public static void saveConfiguration(MailConfig mailConfig) {
        try {
            LocalPersistence.writeObjectToFile(OfficeApplication.getContext(), mailConfig, PREFERENCE_FILE);
        } catch (Exception e) {
            Logger.logApplicationException(e, MailConfigPreferences.class.getSimpleName() + ".saveConfiguration(): Failed.");
        }
    }
}