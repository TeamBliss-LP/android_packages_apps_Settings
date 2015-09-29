/*
 * Copyright (C) 2014 The Dirty Unicorns Project
 * Copyright (C) 2014 BOSP 
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

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class Download extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    /* Preference mContest; */
    private Preference mSupport;
    private Preference mBanksGapps;
    private Preference mBlissOfficial;
    private Preference mBlissNightly;
    private Preference mPAGapps;
    private Preference mKernels;
    private Preference mAddons;
    private String mBlissDevice = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.download);

        final ContentResolver resolver = getActivity().getContentResolver();

        /* mContest = findPreference("contest_bliss"); */
        mSupport = findPreference("support_bliss");
        mBanksGapps = findPreference("banks_gapps");
        mBlissOfficial = findPreference("bliss_official");
        mBlissNightly = findPreference("bliss_nightly");
        mPAGapps = findPreference("pa_gapps");
        mKernels = findPreference("bliss_kernels");
        mAddons = findPreference("various_addons");

        try {
            mBlissDevice = SystemProperties.get("ro.bliss.device", "");
        } catch (RuntimeException e) { }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSupport) {
            Uri uri = Uri.parse("http://goo.gl/tymMFo");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mBanksGapps) {
            Uri uri = Uri.parse("http://fitsnugly.euroskank.com/?rom=banks&device=gapps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mPAGapps) {
            Uri uri = Uri.parse("http://forum.xda-developers.com/android/software/gapps-google-apps-minimal-edition-t2943330");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mBlissOfficial) {
            Uri uri = Uri.parse("http://downloads.blissroms.com/BlissPop/Official/"+mBlissDevice);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mBlissNightly) {
            Uri uri = Uri.parse("http://downloads.blissroms.com/BlissPop/Nightlies/"+mBlissDevice);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mKernels) {
            Uri uri = Uri.parse("http://downloads.blissroms.com/Kernels/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mAddons) {
            Uri uri = Uri.parse("http://downloads.blissroms.com/Add-ons/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
         return true;
    }

    public static class DeviceAdminLockscreenReceiver extends DeviceAdminReceiver {}

}
