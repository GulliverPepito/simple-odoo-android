package com.eiqui.eiqui.activities;

/**
 * Created by uchar on 23/09/16.
 */

import android.app.Activity;
import android.os.Bundle;

import com.eiqui.eiqui.fragments.SettingsFragment;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}