/*
 * Copyright (C) 2015 DarkKat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.qs.QSTiles;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class QSColors extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_QS_BACKGROUND_COLOR = "qs_background_color";
    private static final String PREF_QS_ICON_COLOR = "qs_icon_color";
    private static final String PREF_QS_TEXT_COLOR = "qs_text_color";
    private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
    private static final String PREF_QS_COLOR_SWITCH = "qs_color_switch";
    private static final String PREF_SHOW_WEATHER = "expanded_header_show_weather";
    private static final String PREF_SHOW_LOCATION = "expanded_header_show_weather_location";
    private static final String PREF_BG_COLOR = "expanded_header_background_color";
    private static final String PREF_CLOCK_COLOR = "expanded_header_clock_color";
    private static final String PREF_DATE_COLOR = "expanded_header_date_color";
    private static final String PREF_ALARM_COLOR = "expanded_header_alarm_color";
    private static final String PREF_AMPM_COLOR = "expanded_header_ampm_color";
    private static final String PREF_WEATHER1_COLOR = "expanded_header_weather1_color";
    private static final String PREF_WEATHER2_COLOR = "expanded_header_weather2_color";
    private static final String PREF_ICON_COLOR = "expanded_header_icon_color";
    private static final String PREF_CLEAR_ALL_ICON_COLOR = "notification_drawer_clear_all_icon_color";
    private static final String PREF_QS_RIPPLE_COLOR = "qs_ripple_color";
    private static final String PREF_CUSTOM_HEADER_DEFAULT = "status_bar_custom_header_default";

    private static final int DEFAULT_BACKGROUND_COLOR = 0xff263238;
    private static final int WHITE = 0xffffffff;
    private static final int BLISS_BLUE = 0xff1976D2;
    private static final int DEFAULT_COLOR = 0xffffffff;
    private static final int DEFAULT_BG_COLOR = 0xff384248;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ColorPickerPreference mQSBackgroundColor;
    private ColorPickerPreference mQSIconColor;
    private ColorPickerPreference mQSTextColor;
    private SwitchPreference mQSShadeTransparency;
    private SwitchPreference mQSSSwitch;
    private SwitchPreference mShowWeather;
    private SwitchPreference mShowLocation;
    private ColorPickerPreference mBackgroundColor;
    private ColorPickerPreference mClockColor;
    private ColorPickerPreference mAmPmColor;
    private ColorPickerPreference mDateColor;
    private ColorPickerPreference mAlarmColor;
    private ColorPickerPreference mWeather1Color;
    private ColorPickerPreference mWeather2Color;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mClearAllIconColor;
    private ColorPickerPreference mQSRippleColor;
    private SwitchPreference mCustomHeader;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.qs_color_settings);
        mResolver = getActivity().getContentResolver();

        boolean showWeather = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER, 0) == 1;

        int intColor;
        String hexColor;

        mQSBackgroundColor =
                (ColorPickerPreference) findPreference(PREF_QS_BACKGROUND_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.QS_BACKGROUND_COLOR,
                DEFAULT_BACKGROUND_COLOR);
        mQSBackgroundColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQSBackgroundColor.setSummary(hexColor);
        mQSBackgroundColor.setAlphaSliderEnabled(true);
        mQSBackgroundColor.setOnPreferenceChangeListener(this);

        mQSIconColor =
                (ColorPickerPreference) findPreference(PREF_QS_ICON_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.QS_ICON_COLOR, WHITE); 
        mQSIconColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQSIconColor.setSummary(hexColor);
        mQSIconColor.setOnPreferenceChangeListener(this);

        mQSTextColor =
                (ColorPickerPreference) findPreference(PREF_QS_TEXT_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.QS_TEXT_COLOR, WHITE); 
        mQSTextColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQSTextColor.setSummary(hexColor);
        mQSTextColor.setOnPreferenceChangeListener(this);

        mQSShadeTransparency = (SwitchPreference) findPreference(PREF_QS_TRANSPARENT_SHADE);
        mQSShadeTransparency.setChecked((Settings.System.getInt(mResolver,
                Settings.System.QS_TRANSPARENT_SHADE, 0) == 1));
        mQSShadeTransparency.setOnPreferenceChangeListener(this);

        mQSSSwitch = (SwitchPreference) findPreference(PREF_QS_COLOR_SWITCH);
        mQSSSwitch.setChecked((Settings.System.getInt(mResolver,
                Settings.System.QS_COLOR_SWITCH, 0) == 1));
        mQSSSwitch.setOnPreferenceChangeListener(this);

        mShowWeather = (SwitchPreference) findPreference(PREF_SHOW_WEATHER);
        mShowWeather.setChecked(showWeather);
        mShowWeather.setOnPreferenceChangeListener(this);

        mWeather1Color = (ColorPickerPreference) findPreference(PREF_WEATHER1_COLOR);
            mWeather2Color = (ColorPickerPreference) findPreference(PREF_WEATHER2_COLOR);

        if (showWeather) {
            mShowLocation = (SwitchPreference) findPreference(PREF_SHOW_LOCATION);
            mShowLocation.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER_LOCATION, 1) == 1);
            mShowLocation.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER1_COLOR,
                    DEFAULT_COLOR);
            mWeather1Color.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mWeather1Color.setSummary(hexColor);
            mWeather1Color.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER2_COLOR,
                    DEFAULT_COLOR);
            mWeather2Color.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mWeather2Color.setSummary(hexColor);
            mWeather2Color.setOnPreferenceChangeListener(this);

        } else {
            removePreference(PREF_SHOW_LOCATION);
            removePreference(PREF_WEATHER1_COLOR);
            removePreference(PREF_WEATHER2_COLOR);
        }

        mBackgroundColor =
                (ColorPickerPreference) findPreference(PREF_BG_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_BG_COLOR,
                DEFAULT_BG_COLOR);
        mBackgroundColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBackgroundColor.setSummary(hexColor);
        mBackgroundColor.setDefaultColors(DEFAULT_BG_COLOR, DEFAULT_BG_COLOR);
        mBackgroundColor.setOnPreferenceChangeListener(this);
		mBackgroundColor.setAlphaSliderEnabled(true);

        mClockColor = (ColorPickerPreference) findPreference(PREF_CLOCK_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_CLOCK_COLOR,
                DEFAULT_COLOR);
        mClockColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mClockColor.setSummary(hexColor);
        mClockColor.setOnPreferenceChangeListener(this);

        mDateColor = (ColorPickerPreference) findPreference(PREF_DATE_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_DATE_COLOR,
                DEFAULT_COLOR);
        mDateColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mDateColor.setSummary(hexColor);
        mDateColor.setOnPreferenceChangeListener(this);

        mAlarmColor = (ColorPickerPreference) findPreference(PREF_ALARM_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_ALARM_COLOR,
                DEFAULT_COLOR);
        mAlarmColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mAlarmColor.setSummary(hexColor);
        mAlarmColor.setOnPreferenceChangeListener(this);

        mAmPmColor = (ColorPickerPreference) findPreference(PREF_AMPM_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_AMPM_COLOR,
                DEFAULT_COLOR);
        mAmPmColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mAmPmColor.setSummary(hexColor);
        mAmPmColor.setOnPreferenceChangeListener(this);

        mIconColor = (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_ICON_COLOR,
                DEFAULT_COLOR);
        mIconColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mIconColor.setSummary(hexColor);
        mIconColor.setOnPreferenceChangeListener(this);

        mClearAllIconColor =
                (ColorPickerPreference) findPreference(PREF_CLEAR_ALL_ICON_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR, WHITE);
        mClearAllIconColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mClearAllIconColor.setSummary(hexColor);
        mClearAllIconColor.setDefaultColors(WHITE, BLISS_BLUE);
        mClearAllIconColor.setOnPreferenceChangeListener(this);

        mQSRippleColor =
                (ColorPickerPreference) findPreference(PREF_QS_RIPPLE_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.QS_RIPPLE_COLOR, WHITE); 
        mQSRippleColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQSRippleColor.setSummary(hexColor);
        mQSRippleColor.setDefaultColors(WHITE, BLISS_BLUE);
        mQSRippleColor.setOnPreferenceChangeListener(this);

        // Status bar custom header default
        mCustomHeader = (SwitchPreference) findPreference(PREF_CUSTOM_HEADER_DEFAULT);
        mCustomHeader.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_DEFAULT, 0) == 1));
        mCustomHeader.setOnPreferenceChangeListener(this);

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

        if (preference == mQSBackgroundColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.QS_BACKGROUND_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mQSIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.QS_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mQSTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.QS_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mQSShadeTransparency) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.QS_TRANSPARENT_SHADE, value ? 1 : 0);
            return true;
        } else if (preference == mQSSSwitch) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.QS_COLOR_SWITCH, value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mShowWeather) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER,
                value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mShowLocation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER_LOCATION,
                value ? 1 : 0);
            return true;
        } else if (preference == mCustomHeader) {
           boolean value = (Boolean) newValue;
           Settings.System.putInt(mResolver,
                   Settings.System.STATUS_BAR_CUSTOM_HEADER_DEFAULT, value ? 1 : 0);
            return true;
        } else if (preference == mBackgroundColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_BG_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mClockColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_CLOCK_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mDateColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_DATE_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mAlarmColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_ALARM_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mAmPmColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_AMPM_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mWeather1Color) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER1_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mWeather2Color) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER2_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_EXPANDED_HEADER_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mClearAllIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mQSRippleColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
               Settings.System.QS_RIPPLE_COLOR, intHex);
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

        QSColors getOwner() {
            return (QSColors) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.reset_android,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_BACKGROUND_COLOR,
                                    DEFAULT_BACKGROUND_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_ICON_COLOR, WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_TEXT_COLOR, WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_TRANSPARENT_SHADE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER_LOCATION, 1);
                             Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_BG_COLOR,
                                    DEFAULT_BG_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_CLOCK_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_DATE_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_ALARM_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_AMPM_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER1_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER2_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_ICON_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR,
                                    WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_RIPPLE_COLOR,
                                    WHITE);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.reset_bliss,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_BACKGROUND_COLOR,
                                    0xff1b1f23);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_ICON_COLOR,
                                    BLISS_BLUE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_TEXT_COLOR,
                                    BLISS_BLUE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_TRANSPARENT_SHADE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_SHOW_WEATHER_LOCATION, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_BG_COLOR,
                                    0xff000000);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_CLOCK_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_DATE_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_ALARM_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_AMPM_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER1_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_WEATHER2_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_EXPANDED_HEADER_ICON_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR,
                                    WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.QS_RIPPLE_COLOR,
                                    WHITE);
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
