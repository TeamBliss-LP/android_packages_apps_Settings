/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
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
package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.SeekBarPreferenceCham;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "StatusBar";

    private static final String KEY_BLISS_LOGO_COLOR = "status_bar_bliss_logo_color";
    private static final String KEY_STATUS_BAR_GREETING = "status_bar_greeting";
    private static final String KEY_STATUS_BAR_GREETING_TIMEOUT = "status_bar_greeting_timeout";
    private static final String KEY_CARRIERLABEL_PREFERENCE = "carrier_options";

    private ColorPickerPreference mBlissLogoColor;
    private SwitchPreference mStatusBarGreeting;
    private SeekBarPreferenceCham mStatusBarGreetingTimeout;
    private PreferenceScreen mCarrierLabel;

    private String mCustomGreetingText = "";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.status_bar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        if (TelephonyManager.getDefault().getPhoneCount() <= 1) {
            removePreference(Settings.System.STATUS_BAR_MSIM_SHOW_EMPTY_ICONS);
        }

        // Bliss logo color
        mBlissLogoColor =
            (ColorPickerPreference) prefSet.findPreference(KEY_BLISS_LOGO_COLOR);
        mBlissLogoColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_BLISS_LOGO_COLOR, 0xffffffff);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBlissLogoColor.setSummary(hexColor);
            mBlissLogoColor.setNewPreviewColor(intColor);
			mBlissLogoColor.setAlphaSliderEnabled(true);

        // Greeting
        mStatusBarGreeting = (SwitchPreference) prefSet.findPreference(KEY_STATUS_BAR_GREETING);
        mCustomGreetingText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_GREETING);
        boolean greeting = mCustomGreetingText != null && !TextUtils.isEmpty(mCustomGreetingText);
        mStatusBarGreeting.setChecked(greeting);

        mStatusBarGreetingTimeout =
                (SeekBarPreferenceCham) prefSet.findPreference(KEY_STATUS_BAR_GREETING_TIMEOUT);
        int statusBarGreetingTimeout = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_GREETING_TIMEOUT, 1000);
        mStatusBarGreetingTimeout.setValue(statusBarGreetingTimeout / 1);
        mStatusBarGreetingTimeout.setOnPreferenceChangeListener(this);

        mCarrierLabel = (PreferenceScreen) prefSet.findPreference(KEY_CARRIERLABEL_PREFERENCE);
        if (Utils.isWifiOnly(getActivity()) || TelephonyManager.getDefault().isMultiSimEnabled()) {
            prefSet.removePreference(mCarrierLabel);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mBlissLogoColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BLISS_LOGO_COLOR, intHex);
            return true;
        } else if (preference == mStatusBarGreetingTimeout) {
            int timeout = (Integer) newValue;
            if (timeout < 100) {
              timeout = 100;
            }
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_GREETING_TIMEOUT, timeout * 1);
            return true;
        }  
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
       if (preference == mStatusBarGreeting) {
           boolean enabled = mStatusBarGreeting.isChecked();
           if (enabled) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle(R.string.status_bar_greeting_title);
                alert.setMessage(R.string.status_bar_greeting_dialog);

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                input.setText(mCustomGreetingText != null ? mCustomGreetingText :
                        getResources().getString(R.string.status_bar_greeting_main));
                alert.setView(input);
                alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = ((Spannable) input.getText()).toString();
                        Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_GREETING, value);
                        updateCheckState(value);
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            } else {
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_GREETING, "");
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateCheckState(String value) {
        if (value == null || TextUtils.isEmpty(value)) mStatusBarGreeting.setChecked(false);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.status_bar_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
