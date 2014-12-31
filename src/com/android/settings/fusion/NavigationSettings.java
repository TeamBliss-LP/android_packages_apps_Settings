/*
 * Copyright (C) 2014 The Fusion Project
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

package com.android.settings.fusion;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.fusion.FusionUtils;

public class NavigationSettings extends SettingsPreferenceFragment
    implements OnPreferenceChangeListener {

    private static final String KEY_ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEYS_OVERFLOW_BUTTON = "keys_overflow_button";

    private boolean mCheckPreferences;

    private SwitchPreference mEnableNavigationBar;
    private SwitchPreference mNavigationBarLeftPref;
    private ListPreference mOverflowButtonMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fusion_navigation_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mOverflowButtonMode = (ListPreference) findPreference(KEYS_OVERFLOW_BUTTON);
        mOverflowButtonMode.setOnPreferenceChangeListener(this);

        // Navigation bar left
        mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);

        // Enable/disable navigation bar
        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;
        mEnableNavigationBar = (SwitchPreference) prefScreen.findPreference(KEY_ENABLE_NAVIGATION_BAR);
        mEnableNavigationBar.setChecked(enableNavigationBar);
        mEnableNavigationBar.setOnPreferenceChangeListener(this);

        updateNavBarSettings();
    }

    private void updateNavBarSettings() {
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW,
                FusionUtils.isNavBarDefault(getActivity()) ? 1 : 0) == 1;
        mEnableNavigationBar.setChecked(enableNavigationBar);

        String overflowButtonMode = Integer.toString(Settings.System.getInt(getContentResolver(),
                Settings.System.UI_OVERFLOW_BUTTON, 2));
        mOverflowButtonMode.setValue(overflowButtonMode);
        mOverflowButtonMode.setSummary(mOverflowButtonMode.getEntry());

        updateNavbarPreferences(enableNavigationBar);
    }

    private void updateNavbarPreferences(boolean show) {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mEnableNavigationBar) {
            mEnableNavigationBar.setEnabled(true);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                        ((Boolean) objValue) ? 1 : 0);
            // Enable overflow button
            Settings.System.putInt(getContentResolver(), Settings.System.UI_OVERFLOW_BUTTON, 2);
            if (mOverflowButtonMode != null) {
                mOverflowButtonMode.setSummary(mOverflowButtonMode.getEntries()[2]);
            }
            return true;
        } else if (preference == mOverflowButtonMode) {
            int val = Integer.parseInt((String) newValue);
            int index = mOverflowButtonMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.UI_OVERFLOW_BUTTON, val);
            mOverflowButtonMode.setSummary(mOverflowButtonMode.getEntries()[index]);
            Toast.makeText(getActivity(), R.string.keys_overflow_toast, Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private PreferenceScreen reloadSettings() {
        mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.fusion_navigation_settings);
        prefs = getPreferenceScreen();

        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) findPreference(KEY_BUTTON_BACKLIGHT);
        if (!backlight.isButtonSupported() && !backlight.isKeyboardSupported()) {
            prefs.removePreference(backlight);
        }

        mCheckPreferences = true;
        return prefs;
    }
}
