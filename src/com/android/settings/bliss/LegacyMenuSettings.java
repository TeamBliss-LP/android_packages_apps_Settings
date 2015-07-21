/*
 * Copyright (C) Axxion-Team ROMs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.bliss;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.ShortcutPickHelper;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LegacyMenuSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private ShortcutPickHelper mPicker;

    private static final String PREF_LEGACY_MENU_LEFT_ACTION = "legacy_menu_left_action";
    private static final String PREF_LEGACY_MENU_RIGHT_ACTION = "legacy_menu_right_action";
    private static final String PREF_LEGACY_MENU_LEFT_LONG_ACTION = "legacy_menu_left_long_action";
    private static final String PREF_LEGACY_MENU_RIGHT_LONG_ACTION = "legacy_menu_right_long_action";

    ListPreference mLegacyMenuLeftAction;
    ListPreference mLegacyMenuRightAction;
    ListPreference mLegacyMenuLeftLongAction;
    ListPreference mLegacyMenuRightLongAction;

    int APP_VALUE = 20;
    int LONG_APP_VALUE = 23;
    int app_id;
    int pref_id;
    String mSettingsUri;
    String mSettingsKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPicker = new ShortcutPickHelper(getActivity(), this);

        addPreferencesFromResource(R.xml.navbar_legacy_menu_settings);

        String mCustomLeftShortcutUri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LEGACY_MENU_LEFT_SHORTCUT_URI);
        String mCustomRightShortcutUri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LEGACY_MENU_RIGHT_SHORTCUT_URI);
        String mCustomLeftLongShortcutUri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LEGACY_MENU_LEFT_LONG_SHORTCUT_URI);
        String mCustomRightLongShortcutUri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LEGACY_MENU_RIGHT_LONG_SHORTCUT_URI);

        mLegacyMenuLeftAction = (ListPreference) findPreference(PREF_LEGACY_MENU_LEFT_ACTION);
        mLegacyMenuLeftAction.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LEGACY_MENU_LEFT_ACTION,
                0) + "");
        reloadSinglePressSummary(mLegacyMenuLeftAction, R.array.legacy_menu_action_entries,
                        R.array.legacy_menu_action_values,
                                mCustomLeftShortcutUri);
        mLegacyMenuLeftAction.setOnPreferenceChangeListener(this);

        mLegacyMenuRightAction = (ListPreference) findPreference(PREF_LEGACY_MENU_RIGHT_ACTION);
        mLegacyMenuRightAction.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LEGACY_MENU_RIGHT_ACTION,
                0) + "");
        reloadSinglePressSummary(mLegacyMenuRightAction, R.array.legacy_menu_action_entries,
                        R.array.legacy_menu_action_values,
                                mCustomRightShortcutUri);
        mLegacyMenuRightAction.setOnPreferenceChangeListener(this);

        mLegacyMenuLeftLongAction = (ListPreference) findPreference(PREF_LEGACY_MENU_LEFT_LONG_ACTION);
        mLegacyMenuLeftLongAction.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LEGACY_MENU_LEFT_LONG_ACTION,
                24) + "");
        reloadLongPressSummary(mLegacyMenuLeftLongAction, R.array.legacy_menu_long_action_entries,
                        R.array.legacy_menu_long_action_values,
                                mCustomLeftLongShortcutUri);
        mLegacyMenuLeftLongAction.setOnPreferenceChangeListener(this);

        mLegacyMenuRightLongAction = (ListPreference) findPreference(PREF_LEGACY_MENU_RIGHT_LONG_ACTION);
        mLegacyMenuRightLongAction.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LEGACY_MENU_RIGHT_LONG_ACTION,
                24) + "");
        reloadLongPressSummary(mLegacyMenuRightLongAction, R.array.legacy_menu_long_action_entries,
                        R.array.legacy_menu_long_action_values,
                                mCustomRightLongShortcutUri);
        mLegacyMenuRightLongAction.setOnPreferenceChangeListener(this);

    }

    public void reloadSinglePressSummary(ListPreference pref, int entryRes, int valueRes, String uri) {
        String[] entries = getResources().getStringArray(entryRes);
        String[] vals = getResources().getStringArray(valueRes);
        String currentVal = pref.getValue();
        int currentValue = Integer.parseInt(currentVal);
        String newEntry = "";
        String appName;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(currentVal)) {
                newEntry = entries[i];
                break;
            }
        }
        if (currentValue == 20) {
            appName = mPicker.getFriendlyNameForUri(uri);
            pref.setSummary(appName);
        } else {
            pref.setSummary(newEntry);
        }
    }

    public void reloadLongPressSummary(ListPreference pref, int entryRes, int valueRes, String uri) {
        String[] entries = getResources().getStringArray(entryRes);
        String[] vals = getResources().getStringArray(valueRes);
        String currentVal = pref.getValue();
        int currentValue = Integer.parseInt(currentVal);
        String newEntry = "";
        String appName;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(currentVal)) {
                newEntry = entries[i];
                break;
            }
        }
        if (currentValue == 23) {
            appName = mPicker.getFriendlyNameForUri(uri);
            pref.setSummary(appName);
        } else {
            pref.setSummary(newEntry);
        }
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (mSettingsKey == null || uri == null) {
            return;
        }
        // send uri
        Settings.System.putString(getActivity().getContentResolver(), mSettingsUri, uri);
        // send value
        Settings.System.putInt(getActivity().getContentResolver(),
                   mSettingsKey, app_id);
        // update summaries
        String shortcutname = mPicker.getFriendlyNameForUri(uri);
        switch (pref_id) {
            case 1:
                mLegacyMenuLeftAction.setSummary(shortcutname);
                break;
            case 2:
                mLegacyMenuRightAction.setSummary(shortcutname);
                break;
            case 3:
                mLegacyMenuLeftLongAction.setSummary(shortcutname);
                break;
            case 4:
                mLegacyMenuRightLongAction.setSummary(shortcutname);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            mPicker.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLegacyMenuLeftAction) {
            int index = mLegacyMenuLeftAction.findIndexOfValue((String) newValue);
            int value = Integer.parseInt((String) newValue);
            if (value == 20) {
                mSettingsUri = Settings.System.LEGACY_MENU_LEFT_SHORTCUT_URI;
                mSettingsKey = Settings.System.LEGACY_MENU_LEFT_ACTION;
                app_id = 20;
                pref_id = 1;
                mPicker.pickShortcut(null, null, getId());
            } else {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LEGACY_MENU_LEFT_ACTION, value);
            CharSequence entry = mLegacyMenuLeftAction.getEntries()[index];
            String action = entry.toString();
            preference.setSummary(action);
            }
            return true;
        } else if (preference == mLegacyMenuRightAction) {
            int index = mLegacyMenuRightAction.findIndexOfValue((String) newValue);
            int value = Integer.parseInt((String) newValue);
            if (value == 20) {
                mSettingsUri = Settings.System.LEGACY_MENU_RIGHT_SHORTCUT_URI;
                mSettingsKey = Settings.System.LEGACY_MENU_RIGHT_ACTION;
                app_id = 20;
                pref_id = 2;
                mPicker.pickShortcut(null, null, getId());
            } else {
                Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.LEGACY_MENU_RIGHT_ACTION, value);
                CharSequence entry = mLegacyMenuRightAction.getEntries()[index];
                String action = entry.toString();
                preference.setSummary(action);
            }
            return true;
        } else if (preference == mLegacyMenuLeftLongAction) {
            int index = mLegacyMenuLeftLongAction.findIndexOfValue((String) newValue);
            int value = Integer.parseInt((String) newValue);
            if (value == 23) {
                mSettingsUri = Settings.System.LEGACY_MENU_LEFT_LONG_SHORTCUT_URI;
                mSettingsKey = Settings.System.LEGACY_MENU_LEFT_LONG_ACTION;
                app_id = 23;
                pref_id = 3;
                mPicker.pickShortcut(null, null, getId());
            } else {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.LEGACY_MENU_LEFT_LONG_ACTION, value);
                CharSequence entry = mLegacyMenuLeftLongAction.getEntries()[index];
                String action = entry.toString();
                preference.setSummary(action);
            }
            return true;
        } else if (preference == mLegacyMenuRightLongAction) {
            int index = mLegacyMenuRightLongAction.findIndexOfValue((String) newValue);
            int value = Integer.parseInt((String) newValue);
            if (value == 23) {
                mSettingsUri = Settings.System.LEGACY_MENU_RIGHT_LONG_SHORTCUT_URI;
                mSettingsKey = Settings.System.LEGACY_MENU_RIGHT_LONG_ACTION;
                app_id = 23;
                pref_id = 4;
                mPicker.pickShortcut(null, null, getId());
            } else {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.LEGACY_MENU_RIGHT_LONG_ACTION, value);
                CharSequence entry = mLegacyMenuRightLongAction.getEntries()[index];
                String action = entry.toString();
                preference.setSummary(action);
            }
            return true;
        }
        return false;
    }
}
