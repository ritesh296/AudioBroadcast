package com.ccleeric.audiobroadcast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by ccleeric on 13/10/4.
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }


    public static class PrefsFragement extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals("3G")) {
                Preference connectionPref = findPreference(s);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(s, ""));
            }
        }
    }
}
