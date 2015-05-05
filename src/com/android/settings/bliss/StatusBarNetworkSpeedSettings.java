/*
 * Copyright (C) 2014 DarkKat
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
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNetworkSpeedSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_CAT_OPTIONS =
            "network_speed_cat_options";
    private static final String PREF_CAT_COLORS =
            "network_speed_cat_colors";
    private static final String PREF_INDICATOR =
            "network_speed_indicator";
    private static final String PREF_SHOW_ICON =
            "network_speed_show_icon";
    private static final String PREF_TRAFFIC_SUMMARY =
            "network_speed_traffic_summary";
    private static final String PREF_BIT_BYTE =
            "network_speed_bit_byte";
    private static final String PREF_HIDE_INDICATOR =
            "network_speed_hide_indicator";
    private static final String PREF_TEXT_COLOR =
            "network_speed_text_color";
    private static final String PREF_ICON_COLOR =
            "network_speed_icon_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ListPreference mIndicator;
    private SwitchPreference mShowIcon;
    private ListPreference mTrafficSummary; 
    private SwitchPreference mBitByte;
    private SwitchPreference mHideIndicator;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;

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

        int intColor = DEFAULT_COLOR;
        String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

        addPreferencesFromResource(R.xml.status_bar_network_speed_settings);
        mResolver = getActivity().getContentResolver();

        boolean isIndicatorEnabled = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_INDICATOR, 3) != 3;

        boolean showIcon = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_SHOW_ICON, 1) == 1;

        mIndicator =
                (ListPreference) findPreference(PREF_INDICATOR);
        int indicator = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_NETWORK_SPEED_INDICATOR, 2);
        mIndicator.setValue(String.valueOf(indicator));
        mIndicator.setSummary(mIndicator.getEntry());
        mIndicator.setOnPreferenceChangeListener(this);

        PreferenceCategory catOptions =
                (PreferenceCategory) findPreference(PREF_CAT_OPTIONS);
        PreferenceCategory catColor =
                (PreferenceCategory) findPreference(PREF_CAT_COLORS);
        mShowIcon =
                (SwitchPreference) findPreference(PREF_SHOW_ICON);
        mTrafficSummary =
                (ListPreference) findPreference(PREF_TRAFFIC_SUMMARY);
        mBitByte =
                (SwitchPreference) findPreference(PREF_BIT_BYTE);
        mHideIndicator =
                (SwitchPreference) findPreference(PREF_HIDE_INDICATOR);
        mTextColor =
                (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
        mIconColor =
                (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
        if (isIndicatorEnabled) {
            mShowIcon.setChecked(showIcon);
            mShowIcon.setOnPreferenceChangeListener(this);

            int trafficSummary = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, 3000);
            mTrafficSummary.setValue(String.valueOf(trafficSummary));
            mTrafficSummary.setSummary(mTrafficSummary.getEntry());
            mTrafficSummary.setOnPreferenceChangeListener(this);

            mBitByte.setChecked((Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NETWORK_SPEED_BIT_BYTE, 0) == 1));
            mBitByte.setOnPreferenceChangeListener(this);

            mHideIndicator.setChecked((Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NETWORK_SPEED_HIDE_TRAFFIC, 1) == 1));
            mHideIndicator.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NETWORK_SPEED_TEXT_COLOR, DEFAULT_COLOR); 
            mTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);

            if (showIcon) {
                intColor = Settings.System.getInt(mResolver,
                        Settings.System.STATUS_BAR_NETWORK_SPEED_ICON_COLOR, DEFAULT_COLOR); 
                mIconColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mIconColor.setSummary(hexColor);
                mIconColor.setOnPreferenceChangeListener(this);
            } else {
                catColor.removePreference(mIconColor);
            }
        } else {
            // Remove uneeded preferences if indicator is disabled
            catOptions.removePreference(mShowIcon);
            catOptions.removePreference(mTrafficSummary);
            catOptions.removePreference(mBitByte);
            catOptions.removePreference(mHideIndicator);
            catColor.removePreference(mTextColor);
            if (mIconColor != null) {
                catColor.removePreference(mIconColor);
            }
            removePreference(PREF_CAT_OPTIONS);
            removePreference(PREF_CAT_COLORS);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_menu_reset) // use the KitKat backup icon
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
        boolean value;
        int intValue;
        int index;
        int intHex;
        String hex;

        if (preference == mIndicator) {
            intValue = Integer.valueOf((String) newValue);
            index = mIndicator.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_INDICATOR, intValue);
            mIndicator.setSummary(mIndicator.getEntries()[index]);
            refreshSettings();
            return true;
        } else if (preference == mShowIcon) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_SHOW_ICON,
                value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mTrafficSummary) {
            intValue = Integer.valueOf((String) newValue);
            index = mTrafficSummary.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, intValue);
            mTrafficSummary.setSummary(mTrafficSummary.getEntries()[index]);
            return true;
        } else if (preference == mBitByte) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_BIT_BYTE,
                value ? 1 : 0);
            return true;
        } else if (preference == mHideIndicator) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_HIDE_TRAFFIC,
                value ? 1 : 0);
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(Integer.valueOf(
                    String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference ==  mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_SPEED_ICON_COLOR, intHex);
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

        StatusBarNetworkSpeedSettings getOwner() {
            return (StatusBarNetworkSpeedSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.dlg_reset_values_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.dlg_reset_android,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_INDICATOR, 3);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_SHOW_ICON, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, 3000);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_BIT_BYTE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_HIDE_TRAFFIC, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_TEXT_COLOR,
                                DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_ICON_COLOR,
                                DEFAULT_COLOR);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.dlg_reset_bliss,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_INDICATOR, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_SHOW_ICON, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, 3000);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_BIT_BYTE, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_HIDE_TRAFFIC, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_TEXT_COLOR,
                                0xffff0000);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_SPEED_ICON_COLOR,
                                0xff33b5e5);
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
