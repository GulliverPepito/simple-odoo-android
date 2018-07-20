package com.eiqui.eiqui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.eiqui.eiqui.R;
import com.eiqui.eiqui.utils.Constants;

/**
 * Created by uchar on 23/09/16.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    static public final String TAG = "SETTINGS";

    public static final String KEY_PREF_SYNC_REFRESH_DELAY = "pref_sync_refresh_delay";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_SYNC_REFRESH_DELAY)) {
            getActivity().sendBroadcast(new Intent(Constants.ACTION_INIT_SERVICE));
        }

    }
}
