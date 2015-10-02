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

package com.android.settings;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SeekBarVolumizer;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.settings.notification.IncreasingRingVolumePreference;
import com.android.settings.notification.NotificationAccessSettings;
import com.android.settings.notification.VolumeSeekBarPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import cyanogenmod.hardware.CMHardwareManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SoundSettings extends SettingsPreferenceFragment implements Indexable,
        Preference.OnPreferenceChangeListener {
    private static final String TAG = SoundSettings.class.getSimpleName();

    private static final String KEY_SOUND = "sounds";
    private static final String KEY_VOLUMES = "volumes";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_MEDIA_VOLUME = "media_volume";
    private static final String KEY_ALARM_VOLUME = "alarm_volume";
    private static final String KEY_RING_VOLUME = "ring_volume";
    private static final String KEY_NOTIFICATION_VOLUME = "notification_volume";
    private static final String KEY_VOLUME_LINK_NOTIFICATION = "volume_link_notification";
    private static final String KEY_PHONE_RINGTONE = "ringtone";
    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_NOTIFICATION_ACCESS = "manage_notification_access";
    private static final String KEY_INCREASING_RING_VOLUME = "increasing_ring_volume";
    private static final String KEY_VOLUME_PANEL_TIMEOUT = "volume_panel_time_out";
    private static final String KEY_VIBRATION_INTENSITY = "vibration_intensity";
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrate_on_touch";

    private static final int SAMPLE_CUTOFF = 2000;  // manually cap sample playback at 2 seconds

    private final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();
    private final IncreasingRingVolumePreference.Callback mIncreasingRingVolumeCallback =
            new IncreasingRingVolumePreference.Callback() {
        @Override
        public void onStartingSample() {
            mVolumeCallback.stopSample();
            mHandler.removeMessages(H.STOP_SAMPLE);
            mHandler.sendEmptyMessageDelayed(H.STOP_SAMPLE, SAMPLE_CUTOFF);
        }
    };

    private final H mHandler = new H();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private final Receiver mReceiver = new Receiver();
    private final ArrayList<VolumeSeekBarPreference> mVolumePrefs = new ArrayList<>();

    private Context mContext;
    private PackageManager mPM;
    private boolean mVoiceCapable;
    private Vibrator mVibrator;
    private AudioManager mAudioManager;
    private VolumeSeekBarPreference mRingPreference;
    private VolumeSeekBarPreference mNotificationPreference;

    private TwoStatePreference mIncreasingRing;
    private IncreasingRingVolumePreference mIncreasingRingVolume;
    private ArrayList<DefaultRingtonePreference> mPhoneRingtonePreferences;
    private ListPreference mVolumePanelTimeOut;
    private Preference mNotificationRingtonePreference;
    private TwoStatePreference mVibrateWhenRinging;
    private Preference mNotificationAccess;
    private ComponentName mSuppressor;
    private int mRingerMode = -1;
    private SwitchPreference mVolumeLinkNotificationSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mPM = mContext.getPackageManager();
        mVoiceCapable = Utils.isVoiceCapable(mContext);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null && !mVibrator.hasVibrator()) {
            mVibrator = null;
        }

        addPreferencesFromResource(R.xml.sounds);

        final PreferenceCategory volumes = (PreferenceCategory) findPreference(KEY_VOLUMES);
        final PreferenceCategory sounds = (PreferenceCategory) findPreference(KEY_SOUND);
        final PreferenceCategory vibrate = (PreferenceCategory) findPreference(KEY_VIBRATE);

        initVolumePreference(KEY_MEDIA_VOLUME, AudioManager.STREAM_MUSIC,
                com.android.internal.R.drawable.ic_audio_vol_mute);
        initVolumePreference(KEY_ALARM_VOLUME, AudioManager.STREAM_ALARM,
                com.android.internal.R.drawable.ic_audio_alarm_mute);
        if (mVoiceCapable) {
            mRingPreference =
                    initVolumePreference(KEY_RING_VOLUME, AudioManager.STREAM_RING,
                            com.android.internal.R.drawable.ic_audio_ring_notif_mute);
            mVolumeLinkNotificationSwitch = (SwitchPreference)
                    volumes.findPreference(KEY_VOLUME_LINK_NOTIFICATION);
        } else {
            volumes.removePreference(volumes.findPreference(KEY_RING_VOLUME));
            volumes.removePreference(volumes.findPreference(KEY_VOLUME_LINK_NOTIFICATION));
        }

        CMHardwareManager hardware = CMHardwareManager.getInstance(mContext);
        if (!hardware.isSupported(CMHardwareManager.FEATURE_VIBRATOR)) {
            Preference preference = vibrate.findPreference(KEY_VIBRATION_INTENSITY);
            if (preference != null) {
                vibrate.removePreference(preference);
            }
        }

        initRingtones(sounds);
        initIncreasingRing(sounds);
        initVibrateWhenRinging(vibrate);

        mNotificationAccess = findPreference(KEY_NOTIFICATION_ACCESS);
        refreshNotificationListeners();
        updateRingerMode();
        updateEffectsSuppressor();

        mVolumePanelTimeOut = (ListPreference) findPreference(KEY_VOLUME_PANEL_TIMEOUT);
        int volumePanelTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.VOLUME_PANEL_TIMEOUT, 3000);
        mVolumePanelTimeOut.setValue(String.valueOf(volumePanelTimeOut));
        mVolumePanelTimeOut.setOnPreferenceChangeListener(this);
        updateVolumePanelTimeOutSummary(volumePanelTimeOut);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNotificationListeners();
        lookupRingtoneNames();
        updateNotificationPreferenceState();
        mSettingsObserver.register(true);
        mReceiver.register(true);
        updateRingOrNotificationPreference();
        updateEffectsSuppressor();
        for (VolumeSeekBarPreference volumePref : mVolumePrefs) {
            volumePref.onActivityResume();
        }
        if (mIncreasingRingVolume != null) {
            mIncreasingRingVolume.onActivityResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mVolumeCallback.stopSample();
        if (mIncreasingRingVolume != null) {
            mIncreasingRingVolume.stopSample();
        }
        mSettingsObserver.register(false);
        mReceiver.register(false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mVolumePanelTimeOut) {
            int volumePanelTimeOut = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.VOLUME_PANEL_TIMEOUT,
                    volumePanelTimeOut);
            updateVolumePanelTimeOutSummary(volumePanelTimeOut);
            return true;
        }
        return false;
    }

    private void updateVolumePanelTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.volume_panel_time_out_summary,
                value / 1000);
        mVolumePanelTimeOut.setSummary(summary);
    }

    // === Volumes ===

    private VolumeSeekBarPreference initVolumePreference(String key, int stream, int muteIcon) {
        final VolumeSeekBarPreference volumePref = (VolumeSeekBarPreference) findPreference(key);
        if (volumePref == null) return null;
        volumePref.setCallback(mVolumeCallback);
        volumePref.setStream(stream);
        mVolumePrefs.add(volumePref);
        volumePref.setMuteIcon(muteIcon);
        return volumePref;
    }

    private void updateRingOrNotificationPreference() {
        if (mRingPreference != null) {
            mRingPreference.showIcon(mSuppressor != null
                    ? com.android.internal.R.drawable.ic_audio_ring_notif_mute
                    : mRingerMode == AudioManager.RINGER_MODE_VIBRATE
                    ? com.android.internal.R.drawable.ic_audio_ring_notif_vibrate
                    : com.android.internal.R.drawable.ic_audio_ring_notif);
         }
    }

    private void updateRingerMode() {
        final int ringerMode = mAudioManager.getRingerModeInternal();
        if (mRingerMode == ringerMode) return;
        mRingerMode = ringerMode;
        updateRingOrNotificationPreference();
    }

    private void updateEffectsSuppressor() {
        final ComponentName suppressor = NotificationManager.from(mContext).getEffectsSuppressor();
        if (Objects.equals(suppressor, mSuppressor)) return;
        mSuppressor = suppressor;
        if (mRingPreference != null) {
            final String text = suppressor != null ?
                    mContext.getString(com.android.internal.R.string.muted_by,
                            getSuppressorCaption(suppressor)) : null;
            mRingPreference.setSuppressionText(text);
        }
        updateRingOrNotificationPreference();
    }

    private String getSuppressorCaption(ComponentName suppressor) {
        final PackageManager pm = mContext.getPackageManager();
        try {
            final ServiceInfo info = pm.getServiceInfo(suppressor, 0);
            if (info != null) {
                final CharSequence seq = info.loadLabel(pm);
                if (seq != null) {
                    final String str = seq.toString().trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error loading suppressor caption", e);
        }
        return suppressor.getPackageName();
    }

    private final class VolumePreferenceCallback implements VolumeSeekBarPreference.Callback {
        private SeekBarVolumizer mCurrent;

        @Override
        public void onSampleStarting(SeekBarVolumizer sbv) {
            if (mCurrent != null && mCurrent != sbv) {
                mCurrent.stopSample();
            }
            if (mIncreasingRingVolume != null) {
                mIncreasingRingVolume.stopSample();
            }
            mCurrent = sbv;
            if (mCurrent != null) {
                mHandler.removeMessages(H.STOP_SAMPLE);
                mHandler.sendEmptyMessageDelayed(H.STOP_SAMPLE, SAMPLE_CUTOFF);
            }
        }

        @Override
        public void onStreamValueChanged(int stream, int progress) {
            // noop
        }

        public void stopSample() {
            if (mCurrent != null) {
                mCurrent.stopSample();
            }
        }
    };


    // === Phone & notification ringtone ===

    private void initRingtones(PreferenceCategory root) {
        DefaultRingtonePreference phoneRingtonePreference =
                (DefaultRingtonePreference) root.findPreference(KEY_PHONE_RINGTONE);
        if (phoneRingtonePreference != null && !mVoiceCapable) {
            root.removePreference(phoneRingtonePreference);
            mPhoneRingtonePreferences = null;
        } else {
            mPhoneRingtonePreferences = new ArrayList<DefaultRingtonePreference>();
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(
                    Context.TELEPHONY_SERVICE);
            if (telephonyManager.isMultiSimEnabled()) {
                root.removePreference(phoneRingtonePreference);
                PreferenceCategory soundCategory = (PreferenceCategory) findPreference(KEY_SOUND);
                for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
                    DefaultRingtonePreference ringtonePreference =
                            new DefaultRingtonePreference(mContext, null);
                    String title = getString(R.string.sim_ringtone_title, i + 1);
                    ringtonePreference.setTitle(title);
                    ringtonePreference.setSubId(i);
                    ringtonePreference.setOrder(0);
                    ringtonePreference.setRingtoneType(RingtoneManager.TYPE_RINGTONE);
                    soundCategory.addPreference(ringtonePreference);
                    mPhoneRingtonePreferences.add(ringtonePreference);
                }
            } else {
                mPhoneRingtonePreferences.add(phoneRingtonePreference);
            }
        }
        mNotificationRingtonePreference = root.findPreference(KEY_NOTIFICATION_RINGTONE);
    }

    private void lookupRingtoneNames() {
        AsyncTask.execute(mLookupRingtoneNames);
    }

    private final Runnable mLookupRingtoneNames = new Runnable() {
        @Override
        public void run() {
            if (mPhoneRingtonePreferences != null) {
                ArrayList<CharSequence> summaries = new ArrayList<CharSequence>();
                for (DefaultRingtonePreference preference : mPhoneRingtonePreferences) {
                    CharSequence summary = updateRingtoneName(
                            mContext, RingtoneManager.TYPE_RINGTONE, preference.getSubId());
                    summaries.add(summary);
                }
                if (!summaries.isEmpty()) {
                    mHandler.obtainMessage(H.UPDATE_PHONE_RINGTONE, summaries).sendToTarget();
                }
            }
            if (mNotificationRingtonePreference != null) {
                final CharSequence summary = updateRingtoneName(
                        mContext, RingtoneManager.TYPE_NOTIFICATION, -1);
                if (summary != null) {
                    mHandler.obtainMessage(H.UPDATE_NOTIFICATION_RINGTONE, summary).sendToTarget();
                }
            }
        }
    };

    private static CharSequence updateRingtoneName(Context context, int type, int subId) {
        if (context == null) {
            Log.e(TAG, "Unable to update ringtone name, no context provided");
            return null;
        }
        Uri ringtoneUri;
        if (type != RingtoneManager.TYPE_RINGTONE || subId <= 0) {
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
        } else {
            ringtoneUri = RingtoneManager.getActualRingtoneUriBySubId(context, subId);
        }
        CharSequence summary = context.getString(com.android.internal.R.string.ringtone_unknown);
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            summary = context.getString(com.android.internal.R.string.ringtone_silent);
        } else {
            Cursor cursor = null;
            try {
                if (MediaStore.AUTHORITY.equals(ringtoneUri.getAuthority())) {
                    // Fetch the ringtone title from the media provider
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[] { MediaStore.Audio.Media.TITLE }, null, null, null);
                } else if (ContentResolver.SCHEME_CONTENT.equals(ringtoneUri.getScheme())) {
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }
                }
            } catch (SQLiteException sqle) {
                // Unknown title for the ringtone
            } catch (IllegalArgumentException iae) {
                // Some other error retrieving the column from the provider
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return summary;
    }

    // === Increasing ringtone ===

    private void initIncreasingRing(PreferenceCategory root) {
        mIncreasingRing = (TwoStatePreference)
                root.findPreference(Settings.System.INCREASING_RING);
        mIncreasingRingVolume = (IncreasingRingVolumePreference)
                root.findPreference(KEY_INCREASING_RING_VOLUME);

        if (!mVoiceCapable) {
            if (mIncreasingRing != null) {
                root.removePreference(mIncreasingRing);
                mIncreasingRing = null;
            }
            if (mIncreasingRingVolume != null) {
                root.removePreference(mIncreasingRingVolume);
                mIncreasingRingVolume = null;
            }
        } else {
            if (mIncreasingRingVolume != null) {
                mIncreasingRingVolume.setCallback(mIncreasingRingVolumeCallback);
            }
        }
    }

    // === Vibrate when ringing ===

    private void initVibrateWhenRinging(PreferenceCategory root) {
        mVibrateWhenRinging = (TwoStatePreference) root.findPreference(KEY_VIBRATE_WHEN_RINGING);
        if (mVibrateWhenRinging == null) {
            Log.i(TAG, "Preference not found: " + KEY_VIBRATE_WHEN_RINGING);
            return;
        }
        if (!mVoiceCapable) {
            root.removePreference(mVibrateWhenRinging);
            mVibrateWhenRinging = null;
            return;
        }
        mVibrateWhenRinging.setPersistent(false);
        updateVibrateWhenRinging();
        mVibrateWhenRinging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final boolean val = (Boolean) newValue;
                return Settings.System.putInt(getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING,
                        val ? 1 : 0);
            }
        });
    }

    private void updateVibrateWhenRinging() {
        if (mVibrateWhenRinging == null) return;
        mVibrateWhenRinging.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING, 0) != 0);
    }

    private void updateNotificationPreferenceState() {
        mNotificationPreference = initVolumePreference(KEY_NOTIFICATION_VOLUME,
                AudioManager.STREAM_NOTIFICATION,
                com.android.internal.R.drawable.ic_audio_ring_notif_mute);

        if (mVoiceCapable) {
            final boolean enabled = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.VOLUME_LINK_NOTIFICATION, 1) == 1;

            if (mNotificationPreference != null) {
                mNotificationPreference.setEnabled(!enabled);
                if (enabled) {
                    updateEffectsSuppressor();
                }
            }
            if (mVolumeLinkNotificationSwitch != null){
                mVolumeLinkNotificationSwitch.setChecked(enabled);
            }
        }
    }

    // === Notification listeners ===

    private void refreshNotificationListeners() {
        if (mNotificationAccess != null) {
            final int total = NotificationAccessSettings.getListenersCount(mPM);
            if (total == 0) {
                getPreferenceScreen().removePreference(mNotificationAccess);
            } else {
                final int n = NotificationAccessSettings.getEnabledListenersCount(mContext);
                if (n == 0) {
                    mNotificationAccess.setSummary(getResources().getString(
                            R.string.manage_notification_access_summary_zero));
                } else {
                    mNotificationAccess.setSummary(String.format(getResources().getQuantityString(
                            R.plurals.manage_notification_access_summary_nonzero,
                            n, n)));
                }
            }
        }
    }

    // === Callbacks ===

    private final class SettingsObserver extends ContentObserver {
        private final Uri VIBRATE_WHEN_RINGING_URI =
                Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING);
        private final Uri VOLUME_LINK_NOTIFICATION_URI =
                Settings.Secure.getUriFor(Settings.Secure.VOLUME_LINK_NOTIFICATION);

        public SettingsObserver() {
            super(mHandler);
        }

        public void register(boolean register) {
            final ContentResolver cr = getContentResolver();
            if (register) {
                cr.registerContentObserver(VIBRATE_WHEN_RINGING_URI, false, this);
                cr.registerContentObserver(VOLUME_LINK_NOTIFICATION_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                updateVibrateWhenRinging();
            }
            if (VOLUME_LINK_NOTIFICATION_URI.equals(uri)) {
                updateNotificationPreferenceState();
            }
        }
    }

    private final class H extends Handler {
        private static final int UPDATE_PHONE_RINGTONE = 1;
        private static final int UPDATE_NOTIFICATION_RINGTONE = 2;
        private static final int STOP_SAMPLE = 3;
        private static final int UPDATE_EFFECTS_SUPPRESSOR = 4;
        private static final int UPDATE_RINGER_MODE = 5;

        private H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PHONE_RINGTONE:
                    ArrayList<CharSequence> summaries = (ArrayList<CharSequence>) msg.obj;
                    for (int i = 0; i < summaries.size(); i++) {
                        Preference preference = mPhoneRingtonePreferences.get(i);
                        preference.setSummary(summaries.get(i));
                    }
                    break;
                case UPDATE_NOTIFICATION_RINGTONE:
                    mNotificationRingtonePreference.setSummary((CharSequence) msg.obj);
                    break;
                case STOP_SAMPLE:
                    mVolumeCallback.stopSample();
                    if (mIncreasingRingVolume != null) {
                        mIncreasingRingVolume.stopSample();
                    }
                    break;
                case UPDATE_EFFECTS_SUPPRESSOR:
                    updateEffectsSuppressor();
                    break;
                case UPDATE_RINGER_MODE:
                    updateRingerMode();
                    break;
            }
        }
    }

    private class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        public void register(boolean register) {
            if (mRegistered == register) return;
            if (register) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction(NotificationManager.ACTION_EFFECTS_SUPPRESSOR_CHANGED);
                filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
                mContext.registerReceiver(this, filter);
            } else {
                mContext.unregisterReceiver(this);
            }
            mRegistered = register;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NotificationManager.ACTION_EFFECTS_SUPPRESSOR_CHANGED.equals(action)) {
                mHandler.sendEmptyMessage(H.UPDATE_EFFECTS_SUPPRESSOR);
            } else if (AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION.equals(action)) {
                mHandler.sendEmptyMessage(H.UPDATE_RINGER_MODE);
            }
        }
    }

    // === Indexing ===

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public void prepare() {
            super.prepare();
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.sounds;
            return Arrays.asList(sir);
        }

        public List<String> getNonIndexableKeys(Context context) {
            final ArrayList<String> rt = new ArrayList<String>();
            if (Utils.isVoiceCapable(context)) {
                rt.add(KEY_NOTIFICATION_VOLUME);
            } else {
                rt.add(KEY_RING_VOLUME);
                rt.add(KEY_PHONE_RINGTONE);
                rt.add(KEY_VIBRATE_WHEN_RINGING);
            }
            Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vib == null || !vib.hasVibrator()) {
                rt.add(KEY_VIBRATE);
            }
            CMHardwareManager hardware = CMHardwareManager.getInstance(context);
            if (!hardware.isSupported(CMHardwareManager.FEATURE_VIBRATOR)) {
                rt.add(KEY_VIBRATION_INTENSITY);
            }

            return rt;
        }
    };
}
