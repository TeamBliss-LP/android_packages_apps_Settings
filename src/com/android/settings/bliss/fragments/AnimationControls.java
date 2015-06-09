/*
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

package com.android.settings.bliss.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SlimSeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import com.android.internal.util.bliss.AwesomeAnimationHelper;

import java.util.Arrays;

public class AnimationControls extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String ACTIVITY_OPEN = "activity_open";
    private static final String ACTIVITY_CLOSE = "activity_close";
    private static final String TASK_OPEN = "task_open";
    private static final String TASK_CLOSE = "task_close";
    private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
    private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
    private static final String ANIMATION_DURATION = "animation_duration";
    private static final String WALLPAPER_OPEN = "wallpaper_open";
    private static final String WALLPAPER_CLOSE = "wallpaper_close";
    private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
    private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
    private static final String TASK_OPEN_BEHIND = "task_open_behind";

    SlimSeekBarPreference mAnimationDuration;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    private ListPreference[] mAllEnumPrefs = new ListPreference[11];
    private final int maxEnumIndex = 10;
    private ContentResolver mContentRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_animation_controls);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_animation_controls);

        mContentRes = getActivity().getContentResolver();

        PreferenceScreen prefs = getPreferenceScreen();
        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(mContext, mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }

        mAllEnumPrefs[0] = (ListPreference) findPreference(ACTIVITY_OPEN);
        mAllEnumPrefs[1] = (ListPreference) findPreference(ACTIVITY_CLOSE);
        mAllEnumPrefs[2] = (ListPreference) findPreference(TASK_OPEN);
        mAllEnumPrefs[3] = (ListPreference) findPreference(TASK_CLOSE);
        mAllEnumPrefs[4] = (ListPreference) findPreference(TASK_MOVE_TO_FRONT);
        mAllEnumPrefs[5] = (ListPreference) findPreference(TASK_MOVE_TO_BACK);
        mAllEnumPrefs[6] = (ListPreference) findPreference(WALLPAPER_OPEN);
        mAllEnumPrefs[7] = (ListPreference) findPreference(WALLPAPER_CLOSE);
        mAllEnumPrefs[8] = (ListPreference) findPreference(WALLPAPER_INTRA_OPEN);
        mAllEnumPrefs[9] = (ListPreference) findPreference(WALLPAPER_INTRA_CLOSE);
        mAllEnumPrefs[10] = (ListPreference) findPreference(TASK_OPEN_BEHIND);

        String summary, res;
        for (int j = 0; j <= maxEnumIndex; j++) {
            if (mAllEnumPrefs[j] != null) {
                summary = getProperSummaryByIndex(j);
                mAllEnumPrefs[j].setOnPreferenceChangeListener(this);
                mAllEnumPrefs[j].setSummary(summary);
                mAllEnumPrefs[j].setEntries(mAnimationsStrings);
                mAllEnumPrefs[j].setEntryValues(mAnimationsNum);
                res = getProperValByIndex(j);
                if (res == null) {
                    Settings.System.putInt(mContentRes,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[j], 0);
                    res = "0";
                }
                mAllEnumPrefs[j].setValue(res);
            }
        }

        int defaultDuration = Settings.System.getInt(mContentRes,
            Settings.System.ANIMATION_CONTROLS_DURATION, 25);
        mAnimationDuration = (SlimSeekBarPreference) findPreference(ANIMATION_DURATION);
        if (mAnimationDuration != null) {
            mAnimationDuration.setInitValue((int) (defaultDuration));
            mAnimationDuration.setOnPreferenceChangeListener(this);
        }
    }

    private String getProperValByIndex(int index) {
        if (index < 0 || index > maxEnumIndex) return null;
        String mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[index];
        if (mString != null && !"".equals(mString)) {
            return Settings.System.getString(mContentRes, mString);
        }
        return null;
    }

    private String getProperSummaryByIndex(int index) {
        if (index < 0 || index > maxEnumIndex) index = 0;
        int mNum = Settings.System.getInt(mContentRes,
            Settings.System.ACTIVITY_ANIMATION_CONTROLS[index], 0);
        return mAnimationsStrings[mNum];
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mAnimationDuration) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.ANIMATION_CONTROLS_DURATION,
                    val);
        } else {
            int val = Integer.parseInt((String) newValue);
            for (int j = 0; j <= maxEnumIndex; j++) {
                if (preference == mAllEnumPrefs[j]) {
                    result = Settings.System.putInt(mContentRes,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[j], val);
                    preference.setSummary(getProperSummaryByIndex(j));
                    break;
                }
            }
        }
        return result;
    }

}
