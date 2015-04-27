/*
 * Copyright (C) 2015 The Fusion Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSignalSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener { 

    private static final String KEY_CATEGORY_ACTIVITY = "signal_wifi_category_network_activity_icons";
    private static final String KEY_SHOW_ACTIVITY = "signal_wifi_show_network_activity";
    private static final String KEY_NETWORK_NORMAL_COLOR = "signal_wifi_network_icons_normal_color";
    private static final String KEY_NETWORK_FULLY_COLOR = "signal_wifi_network_icons_fully_color";
    private static final String KEY_ACTIVITY_NORMAL_COLOR = "signal_wifi_network_activity_icons_normal_color";
    private static final String KEY_ACTIVITY_FULLY_COLOR = "signal_wifi_network_activity_icons_fully_color";
    private static final String KEY_AIRPLANE_COLOR = "signal_wifi_airplane_mode_icon_color";

    private static final int DEFAULT_COLOR = 0xffffffff;
    private static final int DEFAULT_ACTIVITY_COLOR = 0xff000000;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private SwitchPreference mShowNetworkActivity;
    private ColorPickerPreference mNetworkNormalColor;
    private ColorPickerPreference mNetworkFullyColor;
    private ColorPickerPreference mActivityNormalColor;
    private ColorPickerPreference mActivityFullyColor;
    private ColorPickerPreference mAirplaneColor;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.bliss_status_bar_signal_settings);
        mResolver = getActivity().getContentResolver();

        boolean isNetworkActivityEnabled = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY, 0) == 1;

        int intColor;
        String hexColor;

        mShowNetworkActivity = (SwitchPreference) findPreference(
                    KEY_SHOW_ACTIVITY);
        mShowNetworkActivity.setChecked(isNetworkActivityEnabled);
        mShowNetworkActivity.setOnPreferenceChangeListener(this);

        mNetworkNormalColor = (ColorPickerPreference) findPreference(
                KEY_NETWORK_NORMAL_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_ICONS_NORMAL_COLOR,
                DEFAULT_COLOR); 
        mNetworkNormalColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mNetworkNormalColor.setSummary(hexColor);
        mNetworkNormalColor.setOnPreferenceChangeListener(this);
        mNetworkNormalColor.setAlphaSliderEnabled(true);

        mNetworkFullyColor = (ColorPickerPreference) findPreference(
                KEY_NETWORK_FULLY_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_ICONS_FULLY_COLOR,
                DEFAULT_COLOR); 
        mNetworkFullyColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mNetworkFullyColor.setSummary(hexColor);
        mNetworkFullyColor.setOnPreferenceChangeListener(this);
        mNetworkFullyColor.setAlphaSliderEnabled(true);

        PreferenceCategory activityCat = (PreferenceCategory) findPreference(KEY_CATEGORY_ACTIVITY);
        mActivityNormalColor = (ColorPickerPreference) findPreference(
                KEY_ACTIVITY_NORMAL_COLOR);
        mActivityFullyColor = (ColorPickerPreference) findPreference(
                KEY_ACTIVITY_FULLY_COLOR);
        if (isNetworkActivityEnabled) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_NORMAL_COLOR,
                    DEFAULT_ACTIVITY_COLOR); 
            mActivityNormalColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mActivityNormalColor.setSummary(hexColor);
            mActivityNormalColor.setOnPreferenceChangeListener(this);
            mActivityNormalColor.setAlphaSliderEnabled(true);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_FULLY_COLOR,
                    DEFAULT_ACTIVITY_COLOR); 
            mActivityFullyColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mActivityFullyColor.setSummary(hexColor);
            mActivityFullyColor.setOnPreferenceChangeListener(this);
            mActivityFullyColor.setAlphaSliderEnabled(true);
        } else {
            activityCat.removePreference(mActivityNormalColor);
            activityCat.removePreference(mActivityFullyColor);
            removePreference(KEY_CATEGORY_ACTIVITY);
        }

        mAirplaneColor = (ColorPickerPreference) findPreference(
                KEY_AIRPLANE_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_AIRPLANE_MODE_ICON_COLOR,
                DEFAULT_COLOR); 
        mAirplaneColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mAirplaneColor.setSummary(hexColor);
        mAirplaneColor.setOnPreferenceChangeListener(this);
        mAirplaneColor.setAlphaSliderEnabled(true);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_reset) // use the KitKat backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String hex;
        int intHex;

        if (preference == mShowNetworkActivity) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mNetworkNormalColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_ICONS_NORMAL_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mNetworkFullyColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_ICONS_FULLY_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mActivityNormalColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_NORMAL_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mActivityFullyColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_FULLY_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mAirplaneColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_AIRPLANE_MODE_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        }
        return false;


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        StatusBarSignalSettings getOwner() {
            return (StatusBarSignalSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_option_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.reset_android,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ICONS_NORMAL_COLOR,
                                DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ICONS_FULLY_COLOR,
                                DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_NORMAL_COLOR,
                                DEFAULT_ACTIVITY_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_FULLY_COLOR,
                                DEFAULT_ACTIVITY_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_AIRPLANE_MODE_ICON_COLOR,
                                DEFAULT_COLOR);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.reset_bliss,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ICONS_NORMAL_COLOR,
                                DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ICONS_FULLY_COLOR,
                                0xff33b5e5);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_NORMAL_COLOR,
                                DEFAULT_ACTIVITY_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_ACTIVITY_ICONS_FULLY_COLOR,
                                DEFAULT_ACTIVITY_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_AIRPLANE_MODE_ICON_COLOR,
                                DEFAULT_COLOR);
                            getOwner().refreshSettings();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }
}
