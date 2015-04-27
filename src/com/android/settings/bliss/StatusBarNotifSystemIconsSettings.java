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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.util.bliss.DeviceUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNotifSystemIconsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_COLORS = "notif_system_icons_category_colors";
    private static final String KEY_COLORIZE_NOTIF_ICONS = "notif_system_icons_colorize_notif_icons";
    private static final String KEY_SHOW_TICKER = "notif_system_icons_show_ticker";
    private static final String KEY_SHOW_COUNT = "notif_system_icons_show_count";
    private static final String KEY_ICON_COLOR = "notif_system_icons_icon_color";
    private static final String KEY_NOTIF_TEXT_COLOR = "notif_system_icons_notif_text_color";
    private static final String KEY_COUNT_ICON_COLOR = "notif_system_icons_count_icon_color";
    private static final String KEY_COUNT_TEXT_COLOR = "notif_system_icons_count_text_color";

    private static final int DEFAULT_COLOR = 0xffffffff;
    private static final int DEFAULT_COUNT_ICON_COLOR = 0xffE5350D;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private SwitchPreference mColorizeNotifIcons;
    private SwitchPreference mShowTicker;
    private SwitchPreference mShowCount;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mNotifTextColor;
    private ColorPickerPreference mCountIconColor;
    private ColorPickerPreference mCountTextColor;

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

        addPreferencesFromResource(R.xml.bliss_status_bar_notif_system_icons_settings);

        mResolver = getActivity().getContentResolver();
        int intColor = DEFAULT_COLOR;
        String hexColor = String.format("#%08x", (0xffffffff & intColor));

        boolean showTicker = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 1;
        boolean showCount = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_SHOW_NOTIF_COUNT, 0) == 1;

        mColorizeNotifIcons = (SwitchPreference) findPreference(KEY_COLORIZE_NOTIF_ICONS);
        mColorizeNotifIcons.setChecked(Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS, 0) == 1);
        /*if (DeviceUtils.isPhone(getActivity())) {
            mColorizeNotifIcons.setTitle(R.string.notif_system_icons_colorize_notif_icons_title_phone);
        }*/
        mColorizeNotifIcons.setOnPreferenceChangeListener(this);

        mShowTicker = (SwitchPreference) findPreference(KEY_SHOW_TICKER);
        mShowTicker.setChecked(showTicker);
        mShowTicker.setOnPreferenceChangeListener(this);

        mShowCount = (SwitchPreference) findPreference(KEY_SHOW_COUNT);
        mShowCount.setChecked(showCount);
        mShowCount.setOnPreferenceChangeListener(this);

        mIconColor = (ColorPickerPreference) findPreference(KEY_ICON_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR,
                DEFAULT_COLOR); 
        mIconColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mIconColor.setSummary(hexColor);
        mIconColor.setOnPreferenceChangeListener(this);
        mIconColor.setAlphaSliderEnabled(true);

        PreferenceCategory catColors = (PreferenceCategory) findPreference(KEY_CATEGORY_COLORS);
        mNotifTextColor = (ColorPickerPreference) findPreference(KEY_NOTIF_TEXT_COLOR);
        if (showTicker) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR,
                    DEFAULT_COLOR); 
            mNotifTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mNotifTextColor.setSummary(hexColor);
            mNotifTextColor.setOnPreferenceChangeListener(this);
            mNotifTextColor.setAlphaSliderEnabled(true);
        } else {
            // Remove uneeded preferences if ticker is disabled
            catColors.removePreference(mNotifTextColor);
        }

        mCountIconColor = (ColorPickerPreference) findPreference(KEY_COUNT_ICON_COLOR);
        mCountTextColor = (ColorPickerPreference) findPreference(KEY_COUNT_TEXT_COLOR);
        if (showCount) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR,
                    DEFAULT_COUNT_ICON_COLOR); 
            mCountIconColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mCountIconColor.setSummary(hexColor);
            mCountIconColor.setOnPreferenceChangeListener(this);
            mCountIconColor.setAlphaSliderEnabled(true);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR,
                    DEFAULT_COLOR); 
            mCountTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mCountTextColor.setSummary(hexColor);
            mCountTextColor.setOnPreferenceChangeListener(this);
            mCountTextColor.setAlphaSliderEnabled(true);
        } else {
            // Remove uneeded preferences if notification count is disabled
            catColors.removePreference(mCountIconColor);
            catColors.removePreference(mCountTextColor);
        }

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
        boolean value;
        int intHex;
        String hex;

        if (preference == mColorizeNotifIcons) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowTicker) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER, value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mShowCount) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_NOTIF_COUNT, value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mNotifTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mCountIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mCountTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        }

        return false;
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

        StatusBarNotifSystemIconsSettings getOwner() {
            return (StatusBarNotifSystemIconsSettings) getTargetFragment();
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
                                    Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_TICKER, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_NOTIF_COUNT, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR,
                                    DEFAULT_COUNT_ICON_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR,
                                    DEFAULT_COLOR);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.reset_bliss,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_TICKER, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_NOTIF_COUNT, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR,
                                    0xff33b5e5);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR,
                                    0xffff0000);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR,
                                    0xff33b5e5);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR,
                                    0xffff0000);
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
