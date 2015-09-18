/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import com.android.settings.ChooseLockGeneric;
import com.android.settings.DataUsageSummary;
import com.android.settings.DateTimeSettings;
import com.android.settings.DevelopmentSettings;
import com.android.settings.DeviceInfoSettings;
import com.android.settings.DisplaySettings;
import com.android.settings.HomeSettings;
import com.android.settings.ScreenPinningSettings;
import com.android.settings.PrivacySettings;
import com.android.settings.SecuritySettings;
import com.android.settings.WirelessSettings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.deviceinfo.Memory;
import com.android.settings.deviceinfo.UsbSettings;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.bliss.AnimationSettings;
import com.android.settings.bliss.BatteryStyle;
import com.android.settings.bliss.BlissDisplaySettings;
import com.android.settings.bliss.InterfaceSettings;
import com.android.settings.bliss.LockScreenSettings;
import com.android.settings.bliss.NavbarSettings;
import com.android.settings.bliss.NavigationSettings;
import com.android.settings.bliss.BlissSoundSettings;
import com.android.settings.bliss.SmartControl;
import com.android.settings.bliss.StatusBarClockStyle;
import com.android.settings.bliss.StatusBarBatteryStatusSettings;
import com.android.settings.bliss.RecentsSettings;
import com.android.settings.net.DataUsageMeteredSettings;
import com.android.settings.notification.NotificationManagerSettings;
import com.android.settings.SoundSettings;
import com.android.settings.notification.OtherSoundSettings;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.sim.SimSettings;
import com.android.settings.users.UserSettings;
import com.android.settings.voice.VoiceInputSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;

import java.util.HashMap;

/**
 * Utility class for dealing with Search Ranking.
 */
public final class Ranking {

    public static final int RANK_WIFI = 1;
    public static final int RANK_BT = 2;
    public static final int RANK_SIM = 3;
    public static final int RANK_DATA_USAGE = 4;
    public static final int RANK_WIRELESS = 5;
    public static final int RANK_ANIMATION = 6;
    public static final int RANK_INTERFACE = 7;
    public static final int RANK_BATTERYSTYLE = 8;
    public static final int RANK_DISPLAYSETTINGS = 9;
    public static final int RANK_LOCKSCREEN = 10;
    public static final int RANK_NAVBAR = 11;
    public static final int RANK_NAVIGATION = 12;
    public static final int RANK_SOUNDSETTINGS = 13;
    public static final int RANK_CLOCKSTYLE = 14;
    public static final int RANK_BATTERYSTATUS = 15;
    public static final int RANK_RECENTS = 16;
    public static final int RANK_HOME = 17;
    public static final int RANK_DISPLAY = 18;
    public static final int RANK_NOTIFICATIONS = 19;
    public static final int RANK_SMART = 20;
    public static final int RANK_STORAGE = 21;
    public static final int RANK_POWER_USAGE = 22;
    public static final int RANK_USERS = 23;
    public static final int RANK_LOCATION = 24;
    public static final int RANK_SECURITY = 25;
    public static final int RANK_IME = 26;
    public static final int RANK_PRIVACY = 27;
    public static final int RANK_DATE_TIME = 28;
    public static final int RANK_ACCESSIBILITY = 29;
    public static final int RANK_PRINTING = 30;
    public static final int RANK_DEVELOPEMENT = 31;
    public static final int RANK_DEVICE_INFO = 32;

    public static final int RANK_UNDEFINED = -1;
    public static final int RANK_OTHERS = 1024;
    public static final int BASE_RANK_DEFAULT = 2048;

    public static int sCurrentBaseRank = BASE_RANK_DEFAULT;

    private static HashMap<String, Integer> sRankMap = new HashMap<String, Integer>();
    private static HashMap<String, Integer> sBaseRankMap = new HashMap<String, Integer>();

    static {
        // Wi-Fi
        sRankMap.put(WifiSettings.class.getName(), RANK_WIFI);
        sRankMap.put(AdvancedWifiSettings.class.getName(), RANK_WIFI);
        sRankMap.put(SavedAccessPointsWifiSettings.class.getName(), RANK_WIFI);

        // BT
        sRankMap.put(BluetoothSettings.class.getName(), RANK_BT);

        // SIM Cards
        sRankMap.put(SimSettings.class.getName(), RANK_SIM);

        // DataUsage
        sRankMap.put(DataUsageSummary.class.getName(), RANK_DATA_USAGE);
        sRankMap.put(DataUsageMeteredSettings.class.getName(), RANK_DATA_USAGE);

        // Other wireless settinfs
        sRankMap.put(WirelessSettings.class.getName(), RANK_WIRELESS);

        // Bliss Interface
        sRankMap.put(AnimationSettings.class.getName(), RANK_ANIMATION);

        // Bliss Interface
        sRankMap.put(InterfaceSettings.class.getName(), RANK_INTERFACE);

        // Bliss Battery Bar
        sRankMap.put(BatteryStyle.class.getName(), RANK_BATTERYSTYLE);

        // Bliss Advanced Dispaly Options
        sRankMap.put(BlissDisplaySettings.class.getName(), RANK_DISPLAYSETTINGS);

        // Bliss Lockscreen Options
        sRankMap.put(LockScreenSettings.class.getName(), RANK_LOCKSCREEN);

        // Bliss Navigation Bar 
        sRankMap.put(NavbarSettings.class.getName(), RANK_NAVBAR);

        // Bliss Navigation Options
        sRankMap.put(NavigationSettings.class.getName(), RANK_NAVIGATION);

        // Bliss Advanced Sound Settings
        sRankMap.put(BlissSoundSettings.class.getName(), RANK_SOUNDSETTINGS);

        // Bliss Clock Style Options
        sRankMap.put(StatusBarClockStyle.class.getName(), RANK_CLOCKSTYLE);

        // Bliss Battery Icon Options
        sRankMap.put(StatusBarBatteryStatusSettings.class.getName(), RANK_BATTERYSTATUS);

        // Bliss Recents Options
        sRankMap.put(RecentsSettings.class.getName(), RANK_RECENTS);

        // Home
        sRankMap.put(HomeSettings.class.getName(), RANK_HOME);

        // Display
        sRankMap.put(DisplaySettings.class.getName(), RANK_DISPLAY);

        // Notifications
        sRankMap.put(SoundSettings.class.getName(), RANK_NOTIFICATIONS);
        sRankMap.put(NotificationManagerSettings.class.getName(), RANK_NOTIFICATIONS);
        sRankMap.put(OtherSoundSettings.class.getName(), RANK_NOTIFICATIONS);
        sRankMap.put(ZenModeSettings.class.getName(), RANK_NOTIFICATIONS);

        // Bliss SmartControl
        sRankMap.put(SmartControl.class.getName(), RANK_SMART);

        // Storage
        sRankMap.put(Memory.class.getName(), RANK_STORAGE);
        sRankMap.put(UsbSettings.class.getName(), RANK_STORAGE);

        // Battery
        sRankMap.put(PowerUsageSummary.class.getName(), RANK_POWER_USAGE);
        sRankMap.put(BatterySaverSettings.class.getName(), RANK_POWER_USAGE);

        // Users
        sRankMap.put(UserSettings.class.getName(), RANK_USERS);

        // Location
        sRankMap.put(LocationSettings.class.getName(), RANK_LOCATION);

        // Security
        sRankMap.put(SecuritySettings.class.getName(), RANK_SECURITY);
        sRankMap.put(LockScreenSettings.class.getName(), RANK_SECURITY);
        sRankMap.put(ChooseLockGeneric.ChooseLockGenericFragment.class.getName(), RANK_SECURITY);
        sRankMap.put(ScreenPinningSettings.class.getName(), RANK_SECURITY);

        // IMEs
        sRankMap.put(InputMethodAndLanguageSettings.class.getName(), RANK_IME);
        sRankMap.put(VoiceInputSettings.class.getName(), RANK_IME);

        // Privacy
        sRankMap.put(PrivacySettings.class.getName(), RANK_PRIVACY);
        sRankMap.put(com.android.settings.cyanogenmod.PrivacySettings.class.getName(), RANK_PRIVACY);

        // Date / Time
        sRankMap.put(DateTimeSettings.class.getName(), RANK_DATE_TIME);

        // Accessibility
        sRankMap.put(AccessibilitySettings.class.getName(), RANK_ACCESSIBILITY);

        // Print
        sRankMap.put(PrintSettingsFragment.class.getName(), RANK_PRINTING);

        // Development
        sRankMap.put(DevelopmentSettings.class.getName(), RANK_DEVELOPEMENT);

        // Device infos
        sRankMap.put(DeviceInfoSettings.class.getName(), RANK_DEVICE_INFO);

        sBaseRankMap.put("com.android.settings", 0);
    }

    public static int getRankForClassName(String className) {
        Integer rank = sRankMap.get(className);
        return (rank != null) ? (int) rank: RANK_OTHERS;
    }

    public static int getBaseRankForAuthority(String authority) {
        synchronized (sBaseRankMap) {
            Integer base = sBaseRankMap.get(authority);
            if (base != null) {
                return base;
            }
            sCurrentBaseRank++;
            sBaseRankMap.put(authority, sCurrentBaseRank);
            return sCurrentBaseRank;
        }
    }
}
