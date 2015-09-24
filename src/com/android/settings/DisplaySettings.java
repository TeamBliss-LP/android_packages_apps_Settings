/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2014 The CyanogenMod Project
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

package com.android.settings;

import android.os.UserHandle;

import com.android.internal.view.RotationPolicy;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.Secure.DOZE_ENABLED;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import static cyanogenmod.hardware.CMHardwareManager.FEATURE_TAP_TO_WAKE;
import static cyanogenmod.hardware.CMHardwareManager.FEATURE_SWEEP_TO_WAKE;
import static cyanogenmod.hardware.CMHardwareManager.FEATURE_SWEEP_TO_SLEEP;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.cyanogenmod.DisplayRotation;
import com.android.settings.Utils;

import cyanogenmod.hardware.CMHardwareManager;

public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_CATEGORY_LIGHTS = "lights";
    private static final String KEY_CATEGORY_DISPLAY = "display";
    private static final String KEY_CATEGORY_INTERFACE = "interface";

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_LCD_DENSITY = "lcd_density";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SCREEN_SAVER = "screensaver";
    private static final String KEY_LIFT_TO_WAKE = "lift_to_wake";
    private static final String KEY_AUTO_BRIGHTNESS = "auto_brightness";
    private static final String KEY_SWEEP_TO_SLEEP = "sweep_sleep_gesture";
    private static final String KEY_SWEEP_TO_WAKE = "sweep_wake_gesture";
    private static final String KEY_TAP_TO_WAKE = "double_tap_wake_gesture";
    private static final String KEY_PROXIMITY_WAKE = "proximity_on_wake";
    private static final String KEY_DISPLAY_ROTATION = "display_rotation";
    private static final String KEY_WAKE_WHEN_PLUGGED_OR_UNPLUGGED = "wake_when_plugged_or_unplugged";
    private static final String KEY_DOZE = "doze";
    private static final String KEY_DOZE_FRAGMENT = "doze_fragment";
    private static final String KEY_NOTIFICATION_LIGHT = "notification_light";
    private static final String KEY_BATTERY_LIGHT = "battery_light";

    private static final String KEY_SCREEN_OFF_GESTURE_SETTINGS = "screen_off_gesture_settings";
    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;

    private ListPreference mLcdDensityPreference;
    private FontDialogPreference mFontSizePref;
    private PreferenceScreen mDisplayRotationPreference;

    private final Configuration mCurConfig = new Configuration();

    private ListPreference mScreenTimeoutPreference;
    private Preference mScreenSaverPreference;
    private SwitchPreference mAccelerometer;
    private SwitchPreference mLiftToWakePreference;
    private SwitchPreference mDozePreference;
    private PreferenceScreen mDozeFragement;
    private SwitchPreference mAutoBrightnessPreference;
    private SwitchPreference mSweepToSleep;
    private SwitchPreference mSweepToWake;
    private SwitchPreference mTapToWake;
    private SwitchPreference mWakeWhenPluggedOrUnplugged;
    private SwitchPreference mProximityWakePreference;

    private CMHardwareManager mHardware;

    private ContentObserver mAccelerometerRotationObserver =
            new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateDisplayRotationPreferenceDescription();
            updateAccelerometerRotationSwitch();
        }
    };

    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            updateDisplayRotationPreferenceDescription();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
        mHardware = CMHardwareManager.getInstance(activity);

        addPreferencesFromResource(R.xml.display);

        PreferenceCategory displayPrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_DISPLAY);
        PreferenceCategory interfacePrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_INTERFACE);

        mDisplayRotationPreference = (PreferenceScreen) findPreference(KEY_DISPLAY_ROTATION);
        mAccelerometer = (SwitchPreference) findPreference(DisplayRotation.KEY_ACCELEROMETER);
        if (mAccelerometer != null) {
            mAccelerometer.setPersistent(false);
        }

        mScreenSaverPreference = findPreference(KEY_SCREEN_SAVER);
        if (mScreenSaverPreference != null
                && getResources().getBoolean(
                        com.android.internal.R.bool.config_dreamsSupported) == false) {
            interfacePrefs.removePreference(mScreenSaverPreference);
        }

        mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        final long currentTimeout = Settings.System.getLong(resolver, SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);
        updateDisplayRotationPreferenceDescription();

        mSweepToSleep = (SwitchPreference) findPreference(KEY_SWEEP_TO_SLEEP);
        if (displayPrefs != null && mSweepToSleep != null
                && !mHardware.isSupported(FEATURE_SWEEP_TO_SLEEP)) {
            displayPrefs.removePreference(mSweepToSleep);
            mSweepToSleep = null;
        }

        mSweepToWake = (SwitchPreference) findPreference(KEY_SWEEP_TO_WAKE);
        if (displayPrefs != null && mSweepToWake != null
                && !mHardware.isSupported(FEATURE_SWEEP_TO_WAKE)) {
            displayPrefs.removePreference(mSweepToWake);
            mSweepToWake = null;
        }

        mTapToWake = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
        if (displayPrefs != null && mTapToWake != null
                && !mHardware.isSupported(FEATURE_TAP_TO_WAKE)) {
            displayPrefs.removePreference(mTapToWake);
            mTapToWake = null;
        }

        mLcdDensityPreference = (ListPreference) findPreference(KEY_LCD_DENSITY);
        if (mLcdDensityPreference != null) {
            int defaultDensity = DisplayMetrics.DENSITY_DEVICE_DEFAULT;
            int currentDensity = DisplayMetrics.DENSITY_PREFERRED;
            if (currentDensity < 200 || currentDensity >= 1000) {
                // Unsupported value, force default
                currentDensity = defaultDensity;
            }

            int factor = defaultDensity >= 480 ? 40 : 20;
            int minimumDensity = defaultDensity - 4 * factor;
            int currentIndex = -1;
            String[] densityEntries = new String[7];
            String[] densityValues = new String[7];
            for (int idx = 0; idx < 7; ++idx) {
                int val = minimumDensity + factor * idx;
                int valueFormatResId = val == defaultDensity
                        ? R.string.lcd_density_default_value_format
                        : R.string.lcd_density_value_format;

                densityEntries[idx] = getString(valueFormatResId, val);
                densityValues[idx] = Integer.toString(val);
                if (currentDensity == val) {
                    currentIndex = idx;
                }
            }
            mLcdDensityPreference.setEntries(densityEntries);
            mLcdDensityPreference.setEntryValues(densityValues);
            if (currentIndex != -1) {
                mLcdDensityPreference.setValueIndex(currentIndex);
            }
            mLcdDensityPreference.setOnPreferenceChangeListener(this);
            updateLcdDensityPreferenceDescription(currentDensity);
        }

        mFontSizePref = (FontDialogPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mFontSizePref.setOnPreferenceClickListener(this);

        mAutoBrightnessPreference = (SwitchPreference) findPreference(KEY_AUTO_BRIGHTNESS);
        if (mAutoBrightnessPreference != null && isAutomaticBrightnessAvailable(getResources())) {
            mAutoBrightnessPreference.setOnPreferenceChangeListener(this);
        } else {
            if (displayPrefs != null && mAutoBrightnessPreference != null) {
                displayPrefs.removePreference(mAutoBrightnessPreference);
                mAutoBrightnessPreference = null;
            }
        }

        mLiftToWakePreference = (SwitchPreference) findPreference(KEY_LIFT_TO_WAKE);
        if (mLiftToWakePreference != null && isLiftToWakeAvailable(activity)) {
            mLiftToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            if (displayPrefs != null && mLiftToWakePreference != null) {
                displayPrefs.removePreference(mLiftToWakePreference);
                mLiftToWakePreference = null;
            }
        }

        mDozePreference = (SwitchPreference) findPreference(KEY_DOZE);
        if (mDozePreference != null && Utils.isDozeAvailable(activity)) {
            mDozePreference.setOnPreferenceChangeListener(this);
        } else {
            if (displayPrefs != null && mDozePreference != null) {
                displayPrefs.removePreference(mDozePreference);
            }
        }

        mDozeFragement = (PreferenceScreen) findPreference(KEY_DOZE_FRAGMENT);
        if (displayPrefs != null && mDozeFragement != null && !Utils.isDozeAvailable(activity)) {
            displayPrefs.removePreference(mDozeFragement);
        }

        if (!isDeviceHandlerInstalled()) {
            displayPrefs.removePreference(findPreference(KEY_SCREEN_OFF_GESTURE_SETTINGS));
        }

        mProximityWakePreference = (SwitchPreference) findPreference(KEY_PROXIMITY_WAKE);
        boolean proximityCheckOnWake = getResources().getBoolean(
                com.android.internal.R.bool.config_proximityCheckOnWake);
        if (mProximityWakePreference != null && proximityCheckOnWake) {
            mProximityWakePreference.setOnPreferenceChangeListener(this);
        } else {
            if (displayPrefs != null && mProximityWakePreference != null) {
                displayPrefs.removePreference(mProximityWakePreference);
                Settings.System.putInt(getContentResolver(), Settings.System.PROXIMITY_ON_WAKE, 0);
            }
        }

        mWakeWhenPluggedOrUnplugged =
                (SwitchPreference) findPreference(KEY_WAKE_WHEN_PLUGGED_OR_UNPLUGGED);

        initPulse((PreferenceCategory) findPreference(KEY_CATEGORY_LIGHTS));
    }

    private static boolean allowAllRotations(Context context) {
        return Resources.getSystem().getBoolean(
                com.android.internal.R.bool.config_allowAllRotations);
    }

    private static boolean isLiftToWakeAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_WAKE_GESTURE) != null;
    }

    private static boolean isAutomaticBrightnessAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_automatic_brightness_available);
    }

    private void updateAccelerometerRotationSwitch() {
        if (mAccelerometer != null) {
            mAccelerometer.setChecked(!RotationPolicy.isRotationLocked(getActivity()));
        }
    }

    private void updateDisplayRotationPreferenceDescription() {
        if (mDisplayRotationPreference == null) {
            // The preference was removed, do nothing
            return;
        }

        // We have a preference, lets update the summary
        boolean rotationEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) != 0;

        if (!rotationEnabled) {
            mDisplayRotationPreference.setSummary(R.string.display_rotation_disabled);
            return;
        }

        StringBuilder summary = new StringBuilder();
        int mode = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES,
                DisplayRotation.ROTATION_0_MODE
                | DisplayRotation.ROTATION_90_MODE
                | DisplayRotation.ROTATION_270_MODE);
        ArrayList<String> rotationList = new ArrayList<String>();
        String delim = "";

        if ((mode & DisplayRotation.ROTATION_0_MODE) != 0) {
            rotationList.add("0");
        }
        if ((mode & DisplayRotation.ROTATION_90_MODE) != 0) {
            rotationList.add("90");
        }
        if ((mode & DisplayRotation.ROTATION_180_MODE) != 0) {
            rotationList.add("180");
        }
        if ((mode & DisplayRotation.ROTATION_270_MODE) != 0) {
            rotationList.add("270");
        }
        for (int i = 0; i < rotationList.size(); i++) {
            summary.append(delim).append(rotationList.get(i));
            if ((rotationList.size() - i) > 2) {
                delim = ", ";
            } else {
                delim = " & ";
            }
        }
        summary.append(" " + getString(R.string.display_rotation_unit));
        mDisplayRotationPreference.setSummary(summary);
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        summary = "";
        if (currentTimeout >= 0) {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries != null && entries.length > 0) {
                int maxEntries = Math.min(entries.length, values.length);
                int best = 0;
                for (int i = 0; i < maxEntries; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                if (best >= entries.length) {
                  best = entries.length - 1;
                }
                summary = preference.getContext().getString(R.string.screen_timeout_summary,
                        entries[best]);
            }
        }
        preference.setSummary(summary);
    }

    private void updateLcdDensityPreferenceDescription(int currentDensity) {
        final int summaryResId = currentDensity == DisplayMetrics.DENSITY_DEVICE
                ? R.string.lcd_density_default_value_format : R.string.lcd_density_value_format;
        mLcdDensityPreference.setSummary(getString(summaryResId, currentDensity));
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            final int userPreference = Integer.parseInt(screenTimeoutPreference.getValue());
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else if (revisedValues.size() > 0
                    && Long.parseLong(revisedValues.get(revisedValues.size() - 1).toString())
                    == maxTimeout) {
                // If the last one happens to be the same as the max timeout, select that
                screenTimeoutPreference.setValue(String.valueOf(maxTimeout));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisplayRotationPreferenceDescription();

        if (mSweepToSleep != null) {
            mSweepToSleep.setChecked(mHardware.get(FEATURE_SWEEP_TO_SLEEP));
        }

        if (mSweepToWake != null) {
            mSweepToWake.setChecked(mHardware.get(FEATURE_SWEEP_TO_WAKE));
        }

        if (mTapToWake != null) {
            mTapToWake.setChecked(mHardware.get(FEATURE_TAP_TO_WAKE));
        }

        RotationPolicy.registerRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        final ContentResolver resolver = getContentResolver();

        // Display rotation observer
        resolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true,
                mAccelerometerRotationObserver);

        // Default value for wake-on-plug behavior from config.xml
        boolean wakeUpWhenPluggedOrUnpluggedConfig = getResources().getBoolean(
                com.android.internal.R.bool.config_unplugTurnsOnScreen);

        if (mWakeWhenPluggedOrUnplugged != null) {
            mWakeWhenPluggedOrUnplugged.setChecked(Settings.Global.getInt(getContentResolver(),
                    Settings.Global.WAKE_WHEN_PLUGGED_OR_UNPLUGGED,
                    (wakeUpWhenPluggedOrUnpluggedConfig ? 1 : 0)) == 1);
        }

        updateState();
        updateAccelerometerRotationSwitch();
    }

    @Override
    public void onPause() {
        super.onPause();

        RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        // Display rotation observer
        getContentResolver().unregisterContentObserver(mAccelerometerRotationObserver);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                            mFontSizePref.click();
                        }
                    });
        }
        return null;
    }

    private void updateState() {
        readFontSizePreference(mFontSizePref);
        updateScreenSaverSummary();

        // Update auto brightness if it is available.
        if (mAutoBrightnessPreference != null) {
            int brightnessMode = Settings.System.getInt(getContentResolver(),
                    SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
            mAutoBrightnessPreference.setChecked(brightnessMode != SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        // Update lift-to-wake if it is available.
        if (mLiftToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), WAKE_GESTURE_ENABLED, 0);
            mLiftToWakePreference.setChecked(value != 0);
        }

        // Update doze if it is available.
        if (mDozePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOZE_ENABLED,
                    getActivity().getResources().getBoolean(
                    com.android.internal.R.bool.config_doze_enabled_by_default) ? 1 : 0);
            mDozePreference.setChecked(value != 0);
        }

        // Update proximity wake if it is available.
        if (mProximityWakePreference != null) {
            boolean defaultValue = getResources().getBoolean(
                com.android.internal.R.bool.config_proximityCheckOnWakeEnabledByDefault);
            boolean enabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.PROXIMITY_ON_WAKE, defaultValue ? 1 : 0) == 1;
            mProximityWakePreference.setChecked(enabled);
        }
    }

    private void updateScreenSaverSummary() {
        if (mScreenSaverPreference != null) {
            mScreenSaverPreference.setSummary(
                    DreamSettings.getSummaryTextWithDreamName(getActivity()));
        }
    }

    private void writeLcdDensityPreference(final Context context, int value) {
        try {
            SystemProperties.set("persist.sys.lcd_density", Integer.toString(value));
        } catch (RuntimeException e) {
            Log.e(TAG, "Unable to save LCD density");
            return;
        }
        final IActivityManager am = ActivityManagerNative.asInterface(
                ServiceManager.checkService("activity"));
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                ProgressDialog dialog = new ProgressDialog(context);
                dialog.setMessage(getResources().getString(R.string.restarting_ui));
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
            }
            @Override
            protected Void doInBackground(Void... params) {
                // Give the user a second to see the dialog
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore
                }
                // Restart the UI
                try {
                    am.restart();
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to restart");
                }
                return null;
            }
        };
        task.execute();
    }

    // === Pulse notification light ===

    private void initPulse(PreferenceCategory parent) {
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_intrusiveNotificationLed)) {
            parent.removePreference(parent.findPreference(KEY_NOTIFICATION_LIGHT));
        }
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_intrusiveBatteryLed)
                || UserHandle.myUserId() != UserHandle.USER_OWNER) {
            parent.removePreference(parent.findPreference(KEY_BATTERY_LIGHT));
        }
        if (parent.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(parent);
        }
    }

    /**
     * Reads the current font size and sets the value in the summary text
     */
    public void readFontSizePreference(Preference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // report the current size in the summary text
        final Resources res = getResources();
        String fontDesc = FontDialogPreference.getFontSizeDescription(res, mCurConfig.fontScale);
        pref.setSummary(getString(R.string.summary_font_size, fontDesc));
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTapToWake) {
            return mHardware.set(FEATURE_TAP_TO_WAKE, mTapToWake.isChecked());
        } else if (preference == mSweepToWake) {
            return mHardware.set(FEATURE_SWEEP_TO_WAKE, mSweepToWake.isChecked());
        } else if (preference == mSweepToSleep) {
            return mHardware.set(FEATURE_SWEEP_TO_SLEEP, mSweepToSleep.isChecked());
        } else if (preference == mWakeWhenPluggedOrUnplugged) {
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.WAKE_WHEN_PLUGGED_OR_UNPLUGGED,
                    mWakeWhenPluggedOrUnplugged.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mAccelerometer) {
            RotationPolicy.setRotationLockForAccessibility(getActivity(),
                    !mAccelerometer.isChecked());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            try {
                int value = Integer.parseInt((String) objValue);
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                updateTimeoutPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (KEY_LCD_DENSITY.equals(key)) {
            try {
                int value = Integer.parseInt((String) objValue);
                writeLcdDensityPreference(preference.getContext(), value);
                updateLcdDensityPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist display density setting", e);
            }
        }
        if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }
        if (preference == mAutoBrightnessPreference) {
            boolean auto = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE,
                    auto ? SCREEN_BRIGHTNESS_MODE_AUTOMATIC : SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        if (preference == mLiftToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), WAKE_GESTURE_ENABLED, value ? 1 : 0);
        }
        if (preference == mDozePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOZE_ENABLED, value ? 1 : 0);
        }
        if (preference == mProximityWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), Settings.System.PROXIMITY_ON_WAKE,
                    value ? 1 : 0);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return true;
            } else {
                mFontSizePref.click();
            }
        }
        return false;
    }


    /**
     * Restore the properties associated with this preference on boot
     *
     * @param ctx A valid context
     */
    public static void restore(Context ctx) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        CMHardwareManager cmHardwareManager = CMHardwareManager.getInstance(ctx);
        if (cmHardwareManager.isSupported(FEATURE_TAP_TO_WAKE)) {
            final boolean enabled = prefs.getBoolean(KEY_TAP_TO_WAKE,
                cmHardwareManager.get(FEATURE_TAP_TO_WAKE));

            if (!cmHardwareManager.set(FEATURE_TAP_TO_WAKE, enabled)) {
                Log.e(TAG, "Failed to restore tap-to-wake settings.");
            } else {
                Log.d(TAG, "Tap-to-wake settings restored.");
            }
        }

        if (cmHardwareManager.isSupported(FEATURE_SWEEP_TO_WAKE)) {
            final boolean enabled = prefs.getBoolean(KEY_SWEEP_TO_WAKE,
                cmHardwareManager.get(FEATURE_SWEEP_TO_WAKE));

            if (!cmHardwareManager.set(FEATURE_SWEEP_TO_WAKE, enabled)) {
                Log.e(TAG, "Failed to restore sweep-to-wake settings.");
            } else {
                Log.d(TAG, "Sweep-to-wake settings restored.");
            }
        }

        if (cmHardwareManager.isSupported(FEATURE_SWEEP_TO_SLEEP)) {
            final boolean enabled = prefs.getBoolean(KEY_SWEEP_TO_SLEEP,
                cmHardwareManager.get(FEATURE_SWEEP_TO_SLEEP));

            if (!cmHardwareManager.set(FEATURE_SWEEP_TO_SLEEP, enabled)) {
                Log.e(TAG, "Failed to restore sweep-to-sleep settings.");
            } else {
                Log.d(TAG, "Sweep-to-sleep settings restored.");
            }
        }
    }

    private boolean isDeviceHandlerInstalled() {
        boolean ret = true;
        final PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.slim.device", PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            ret = false;
        }
        return ret;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.display;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    CMHardwareManager cmHardwareManager = CMHardwareManager.getInstance(context);
                    ArrayList<String> result = new ArrayList<String>();
                    if (!context.getResources().getBoolean(
                            com.android.internal.R.bool.config_dreamsSupported)) {
                        result.add(KEY_SCREEN_SAVER);
                    }
                    if (!context.getResources().getBoolean(
                            com.android.internal.R.bool.config_intrusiveNotificationLed)) {
                        result.add(KEY_NOTIFICATION_LIGHT);
                    }
                    if (!context.getResources().getBoolean(
                            com.android.internal.R.bool.config_intrusiveBatteryLed)) {
                        result.add(KEY_BATTERY_LIGHT);
                    }
                    if (!context.getResources().getBoolean(
                            com.android.internal.R.bool.config_proximityCheckOnWake)) {
                        result.add(KEY_PROXIMITY_WAKE);
                    }
                    if (!cmHardwareManager.isSupported(FEATURE_TAP_TO_WAKE)) {
                        result.add(KEY_TAP_TO_WAKE);
                    }
                    if (!cmHardwareManager.isSupported(FEATURE_SWEEP_TO_WAKE)) {
                        result.add(KEY_SWEEP_TO_WAKE);
                    }
                    if (!cmHardwareManager.isSupported(FEATURE_SWEEP_TO_SLEEP)) {
                        result.add(KEY_SWEEP_TO_SLEEP);
                    }
                    if (!isAutomaticBrightnessAvailable(context.getResources())) {
                        result.add(KEY_AUTO_BRIGHTNESS);
                    }
                    if (!isLiftToWakeAvailable(context)) {
                        result.add(KEY_LIFT_TO_WAKE);
                    }
                    if (!Utils.isDozeAvailable(context)) {
                        result.add(KEY_DOZE);
                        result.add(KEY_DOZE_FRAGMENT);
                    }
                    return result;
                }
            };
}
