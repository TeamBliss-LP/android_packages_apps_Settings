/*
 * Copyright (C) 2014 The NamelessRom project
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

import android.os.Bundle;
import android.preference.Preference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NavigationBarSettings extends SettingsPreferenceFragment {

    private static final String CATEGORY_NAV_BAR_SIMULATE = "navigation_bar_simulate";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar_settings);

        final boolean hasRealNavigationBar = getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        if (hasRealNavigationBar) { // only disable on devices with REAL navigation bars
            final Preference pref = findPreference(CATEGORY_NAV_BAR_SIMULATE);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    }

}
