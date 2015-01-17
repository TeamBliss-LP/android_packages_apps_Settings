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
import android.graphics.Color;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
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

public class BatteryStatusSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_CAT_CIRCLE_OPTIONS =
            "battery_status_cat_circle_options";
    private static final String PREF_CAT_COLORS =
            "battery_status_cat_colors";
    private static final String PREF_STYLE =
            "battery_status_style";
    private static final String PREF_PERCENT_STYLE =
            "battery_status_percent_style";
    private static final String PREF_CHARGE_ANIMATION_SPEED =
            "battery_status_charge_animation_speed";
    private static final String PREF_SHOW_CIRCLE_DOTTED =
            "battery_status_show_circle_dotted";
    private static final String PREF_CIRCLE_DOT_LENGTH =
            "battery_status_circle_dot_length";
    private static final String PREF_CIRCLE_DOT_INTERVAL =
            "battery_status_circle_dot_interval";
    private static final String PREF_BATTERY_COLOR =
            "battery_status_battery_color";
    private static final String PREF_TEXT_COLOR =
            "battery_status_text_color";

    private static final int DEFAULT_BATTERY_COLOR = 0xffffffff;
    private static final int DEFAULT_TEXT_COLOR = 0xff000000;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private static final int BATTERY_STATUS_CIRCLE = 2;
    private static final int BATTERY_STATUS_TEXT = 3;
    private static final int BATTERY_STATUS_HIDDEN = 4;

    private static final int CIRCLE_DOTTED = 1;

    private ListPreference mStyle;
    private ListPreference mPercentStyle;
    private ListPreference mChargeAnimationSpeed;
    private SwitchPreference mShowCircleDotted;
    private ListPreference mCircleDotLength;
    private ListPreference mCircleDotInterval;
    private ColorPickerPreference mBatteryColor;
    private ColorPickerPreference mTextColor;

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

        addPreferencesFromResource(R.xml.bliss_battery_status);
        mResolver = getActivity().getContentResolver();

        int intColor = 0xffffffff;
        String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

        mStyle =
                (ListPreference) findPreference(PREF_STYLE);
        int style = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_BATTERY_STATUS_STYLE, 0);
        mStyle.setValue(String.valueOf(style));
        mStyle.setSummary(mStyle.getEntry());
        mStyle.setOnPreferenceChangeListener(this);

        boolean batteryStatusVisible = style != BATTERY_STATUS_HIDDEN;
        boolean isCircle = style == BATTERY_STATUS_CIRCLE;
        boolean isTextOnly = style == BATTERY_STATUS_TEXT;
        boolean isCircleDotted = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_BATTERY_STATUS_SHOW_CIRCLE_DOTTED,
                0) == CIRCLE_DOTTED;

        PreferenceCategory catCircleOptions =
                (PreferenceCategory) findPreference(PREF_CAT_CIRCLE_OPTIONS);
        PreferenceCategory catColors =
                (PreferenceCategory) findPreference(PREF_CAT_COLORS);
        mShowCircleDotted =
                (SwitchPreference) findPreference(PREF_SHOW_CIRCLE_DOTTED);
        mCircleDotLength =
                (ListPreference) findPreference(PREF_CIRCLE_DOT_LENGTH);
        mCircleDotInterval =
                (ListPreference) findPreference(PREF_CIRCLE_DOT_INTERVAL);
        mBatteryColor =
            (ColorPickerPreference) findPreference(PREF_BATTERY_COLOR);
        mTextColor =
                (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);

        if (batteryStatusVisible && !isTextOnly) {
            mPercentStyle =
                    (ListPreference) findPreference(PREF_PERCENT_STYLE);
            int percentStyle = Settings.System.getInt(mResolver,
                   Settings.System.STATUS_BAR_BATTERY_STATUS_PERCENT_STYLE, 2);
            mPercentStyle.setValue(String.valueOf(percentStyle));
            mPercentStyle.setSummary(mPercentStyle.getEntry());
            mPercentStyle.setOnPreferenceChangeListener(this);

            mChargeAnimationSpeed =
                    (ListPreference) findPreference(PREF_CHARGE_ANIMATION_SPEED);
            int chargeAnimationSpeed = Settings.System.getInt(mResolver,
                   Settings.System.STATUS_BAR_BATTERY_STATUS_CHARGING_ANIMATION_SPEED, 3);
            mChargeAnimationSpeed.setValue(String.valueOf(chargeAnimationSpeed));
            mChargeAnimationSpeed.setSummary(mChargeAnimationSpeed.getEntry());
            mChargeAnimationSpeed.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_PERCENT_STYLE);
            removePreference(PREF_CHARGE_ANIMATION_SPEED);
        }

        if (batteryStatusVisible && isCircle) {
            mShowCircleDotted.setChecked(isCircleDotted);
            mShowCircleDotted.setOnPreferenceChangeListener(this);
        } else {
            catCircleOptions.removePreference(mShowCircleDotted);
        }

        if (batteryStatusVisible && isCircle && isCircleDotted) {
            int circleDotLength = Settings.System.getInt(mResolver,
                   Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_LENGTH, 3);
            mCircleDotLength.setValue(String.valueOf(circleDotLength));
            mCircleDotLength.setSummary(mCircleDotLength.getEntry());
            mCircleDotLength.setOnPreferenceChangeListener(this);

            int circleDotInterval = Settings.System.getInt(mResolver,
                   Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_INTERVAL, 2);
            mCircleDotInterval.setValue(String.valueOf(circleDotInterval));
            mCircleDotInterval.setSummary(mCircleDotInterval.getEntry());
            mCircleDotInterval.setOnPreferenceChangeListener(this);
        } else {
            catCircleOptions.removePreference(mCircleDotLength);
            catCircleOptions.removePreference(mCircleDotInterval);
        }

        if (batteryStatusVisible && !isTextOnly) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_BATTERY_STATUS_COLOR,
                    DEFAULT_BATTERY_COLOR);
            mBatteryColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryColor.setSummary(hexColor);
            mBatteryColor.setOnPreferenceChangeListener(this);
        } else {
            catColors.removePreference(mBatteryColor);
        }

        if (batteryStatusVisible) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_BATTERY_STATUS_TEXT_COLOR,
                    DEFAULT_TEXT_COLOR); 
            mTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);
        } else {
            catColors.removePreference(mTextColor);
        }

        if (!batteryStatusVisible || !isCircle) {
            removePreference(PREF_CAT_CIRCLE_OPTIONS);
        }
        if (!batteryStatusVisible) {
            removePreference(PREF_CAT_COLORS);
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
        int intValue;
        int index;
        int intHex;
        String hex;

        if (preference == mStyle) {
            intValue = Integer.valueOf((String) newValue);
            index = mStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_BATTERY_STATUS_STYLE, intValue);
            mStyle.setSummary(mStyle.getEntries()[index]);
            refreshSettings();
            return true;
        } else if (preference == mPercentStyle) {
            intValue = Integer.valueOf((String) newValue);
            index = mPercentStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_BATTERY_STATUS_PERCENT_STYLE, intValue);
            mPercentStyle.setSummary(mPercentStyle.getEntries()[index]);
            refreshSettings();
            return true;
        } else if (preference == mChargeAnimationSpeed) {
            intValue = Integer.valueOf((String) newValue);
            index = mChargeAnimationSpeed.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_BATTERY_STATUS_CHARGING_ANIMATION_SPEED, intValue);
            mChargeAnimationSpeed.setSummary(mChargeAnimationSpeed.getEntries()[index]);
            return true;
        } else if (preference == mShowCircleDotted) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_BATTERY_STATUS_SHOW_CIRCLE_DOTTED,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mCircleDotLength) {
            intValue = Integer.valueOf((String) newValue);
            index = mCircleDotLength.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_LENGTH, intValue);
            mCircleDotLength.setSummary(mCircleDotLength.getEntries()[index]);
            return true;
        } else if (preference == mCircleDotInterval) {
            intValue = Integer.valueOf((String) newValue);
            index = mCircleDotInterval.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_INTERVAL, intValue);
            mCircleDotInterval.setSummary(mCircleDotInterval.getEntries()[index]);
            return true;
        } else if (preference == mBatteryColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_BATTERY_STATUS_COLOR,
                    intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_BATTERY_STATUS_TEXT_COLOR, intHex);
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

        BatteryStatusSettings getOwner() {
            return (BatteryStatusSettings) getTargetFragment();
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
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_STYLE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_PERCENT_STYLE, 2);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_CHARGING_ANIMATION_SPEED, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_SHOW_CIRCLE_DOTTED, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_LENGTH, 3);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_INTERVAL, 2);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_COLOR,
                                    DEFAULT_BATTERY_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_TEXT_COLOR,
                                    DEFAULT_TEXT_COLOR);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.dlg_reset_bliss,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                             Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_STYLE, 2);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_PERCENT_STYLE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_CHARGING_ANIMATION_SPEED, 3);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_SHOW_CIRCLE_DOTTED, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_LENGTH, 3);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_CIRCLE_DOT_INTERVAL, 2);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_COLOR,
                                    0xff33b5e5);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_BATTERY_STATUS_TEXT_COLOR,
                                    DEFAULT_BATTERY_COLOR);
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
