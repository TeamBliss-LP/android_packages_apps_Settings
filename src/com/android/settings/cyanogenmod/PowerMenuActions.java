/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;

import com.android.settings.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.cm.PowerMenuConstants;
import com.android.settings.bliss.NumberPickerPreference;

import static com.android.internal.util.cm.PowerMenuConstants.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class PowerMenuActions extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    final static String TAG = "PowerMenuActions";

    private static final String KEY_SCREENRECORD = "power_menu_screenrecord";
    private static final String KEY_SCREENSHOT_DELAY = "screenshot_delay";    

    private SwitchPreference mScreenrecordPref;
    private SwitchPreference mPowerPref;
    private SwitchPreference mRebootPref;
    private SwitchPreference mScreenshotPref;
    private SwitchPreference mProfilePref;
    private SwitchPreference mAirplanePref;
    private SwitchPreference mUsersPref;
    private SwitchPreference mSettingsPref;
    private SwitchPreference mLockdownPref;
    private SwitchPreference mSilentPref;
    
    private NumberPickerPreference mScreenshotDelay;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private static final int MIN_DELAY_VALUE = 1;
    private static final int MAX_DELAY_VALUE = 30;    

    Context mContext;
    private ArrayList<String> mLocalUserConfig = new ArrayList<String>();
    private String[] mAvailableActions;
    private String[] mAllActions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu_settings);
        mContext = getActivity().getApplicationContext();
        
        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        final ContentResolver resolver = getActivity().getContentResolver();

        mAvailableActions = getActivity().getResources().getStringArray(
                R.array.power_menu_actions_array);
        mAllActions = PowerMenuConstants.getAllActions();

        mScreenrecordPref = (SwitchPreference) findPreference(KEY_SCREENRECORD);
        mScreenrecordPref.setChecked(Settings.System.getInt(resolver,
                Settings.System.POWER_MENU_SCREENRECORD_ENABLED, 0) == 1);
        mScreenrecordPref.setOnPreferenceChangeListener(this);
        
        mScreenshotDelay = (NumberPickerPreference) mPrefSet.findPreference(KEY_SCREENSHOT_DELAY);
        mScreenshotDelay.setOnPreferenceChangeListener(this);
        mScreenshotDelay.setMinValue(MIN_DELAY_VALUE);
        mScreenshotDelay.setMaxValue(MAX_DELAY_VALUE);
        int ssDelay = Settings.System.getInt(mCr,
                Settings.System.SCREENSHOT_DELAY, 1);
        mScreenshotDelay.setCurrentValue(ssDelay);

        for (String action : mAllActions) {
        // Remove preferences not present in the overlay
            if (!isActionAllowed(action)) {
                getPreferenceScreen().removePreference(findPreference(action));
                continue;
            }

            if (action.equals(GLOBAL_ACTION_KEY_POWER)) {
                mPowerPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_POWER);
            } else if (action.equals(GLOBAL_ACTION_KEY_REBOOT)) {
                mRebootPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_REBOOT);
            } else if (action.equals(GLOBAL_ACTION_KEY_SCREENSHOT)) {
                mScreenshotPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SCREENSHOT);
            } else if (action.equals(GLOBAL_ACTION_KEY_PROFILE)) {
                mProfilePref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_PROFILE);
            } else if (action.equals(GLOBAL_ACTION_KEY_AIRPLANE)) {
                mAirplanePref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_AIRPLANE);
            } else if (action.equals(GLOBAL_ACTION_KEY_USERS)) {
                mUsersPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_USERS);
            } else if (action.equals(GLOBAL_ACTION_KEY_SETTINGS)) {
                mSettingsPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SETTINGS);
            } else if (action.equals(GLOBAL_ACTION_KEY_LOCKDOWN)) {
                mLockdownPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_LOCKDOWN);
            } else if (action.equals(GLOBAL_ACTION_KEY_SILENT)) {
                mSilentPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SILENT);
            }
        }

        getUserConfig();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mPowerPref != null) {
            mPowerPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_POWER));
        }

        if (mRebootPref != null) {
            mRebootPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_REBOOT));
        }

        if (mScreenshotPref != null) {
            mScreenshotPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SCREENSHOT));
        }

        if (mProfilePref != null) {
            mProfilePref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_PROFILE));
        }

        if (mAirplanePref != null) {
            mAirplanePref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_AIRPLANE));
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                getPreferenceScreen().removePreference(findPreference(GLOBAL_ACTION_KEY_USERS));
            } else {
                List<UserInfo> users = ((UserManager) mContext.getSystemService(
                        Context.USER_SERVICE)).getUsers();
                boolean enabled = (users.size() > 1);
                mUsersPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_USERS) && enabled);
                mUsersPref.setEnabled(enabled);
            }
        }

        if (mSettingsPref != null) {
            mSettingsPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SETTINGS));
        }

        if (mLockdownPref != null) {
            mLockdownPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_LOCKDOWN));
        }

        if (mSilentPref != null) {
            mSilentPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SILENT));
        }

        updatePreferences();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScreenrecordPref) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_SCREENRECORD_ENABLED,
                    Boolean.TRUE.equals((Boolean)newValue) ? 1 : 0);
            return true;
        } else if (preference == mScreenshotDelay) {
            int value = Integer.parseInt(newValue.toString());
            Settings.System.putInt(mCr, Settings.System.SCREENSHOT_DELAY,
                    value);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mPowerPref) {
            value = mPowerPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_POWER);

        } else if (preference == mRebootPref) {
            value = mRebootPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_REBOOT);

        } else if (preference == mScreenshotPref) {
            value = mScreenshotPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);

        } else if (preference == mProfilePref) {
            value = mProfilePref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_PROFILE);

        } else if (preference == mAirplanePref) {
            value = mAirplanePref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);

        } else if (preference == mUsersPref) {
            value = mUsersPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);

        } else if (preference == mSettingsPref) {
            value = mSettingsPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SETTINGS);

        } else if (preference == mLockdownPref) {
            value = mLockdownPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_LOCKDOWN);

        } else if (preference == mSilentPref) {
            value = mSilentPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SILENT);

        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    private boolean settingsArrayContains(String preference) {
        return mLocalUserConfig.contains(preference);
    }

    private boolean isActionAllowed(String action) {
        if (Arrays.asList(mAvailableActions).contains(action)) {
            return true;
        }
        return false;
    }

    private void updateUserConfig(boolean enabled, String action) {
        if (enabled) {
            if (!settingsArrayContains(action)) {
                mLocalUserConfig.add(action);
            }
        } else {
            if (settingsArrayContains(action)) {
                mLocalUserConfig.remove(action);
            }
        }
        saveUserConfig();
    }

    private void updatePreferences() {
        boolean profiles = Settings.System.getInt(getContentResolver(),
                Settings.System.SYSTEM_PROFILES_ENABLED, 1) != 0;

        if (mProfilePref != null) {
            mProfilePref.setEnabled(profiles);
            if (profiles) {
                mProfilePref.setSummary(null);
            } else {
                mProfilePref.setSummary(R.string.power_menu_profiles_disabled);
            }
        }
    }

    private void getUserConfig() {
        mLocalUserConfig.clear();
        String[] defaultActions;
        String savedActions = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                Settings.Secure.POWER_MENU_ACTIONS, UserHandle.USER_CURRENT);

        if (savedActions == null) {
            defaultActions = mContext.getResources().getStringArray(
                    com.android.internal.R.array.config_globalActionsList);
            for (String action : defaultActions) {
                mLocalUserConfig.add(action);
            }
        } else {
            for (String action : savedActions.split("\\|")) {
                mLocalUserConfig.add(action);
            }
        }
    }

    private void saveUserConfig() {
        StringBuilder s = new StringBuilder();

        // TODO: Use DragSortListView
        ArrayList<String> setactions = new ArrayList<String>();
        for (String action : mAllActions) {
            if (settingsArrayContains(action) && isActionAllowed(action)) {
                setactions.add(action);
            } else {
                continue;
            }
        }

        for (int i = 0; i < setactions.size(); i++) {
            s.append(setactions.get(i).toString());
            if (i != setactions.size() - 1) {
                s.append("|");
            }
        }

        Settings.Secure.putStringForUser(getContentResolver(),
                 Settings.Secure.POWER_MENU_ACTIONS, s.toString(), UserHandle.USER_CURRENT);
        updatePowerMenuDialog();
    }

    private void updatePowerMenuDialog() {
        Intent u = new Intent();
        u.setAction(Intent.UPDATE_POWER_MENU);
        mContext.sendBroadcastAsUser(u, UserHandle.ALL);
    }
}
