package com.ccleeric.audiobroadcast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ccleeric on 13/10/4.
 */
public class SettingsActivity extends Activity {
    private PrefsFragement mPrefsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mPrefsFragment = new PrefsFragement();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mPrefsFragment).commit();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }


    public static class PrefsFragement extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String CONNECTIVITY_PREF = "ConnectivityPref";
        public static final String AUDIO_SOURCE_PREF = "AudioSourcePref";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            restoreSettings();
        }

        //Restore Settings Values
        public void restoreSettings() {
            SharedPreferences settingsPref = getPreferenceScreen().getSharedPreferences();

            Preference connectivityPref = findPreference(CONNECTIVITY_PREF);
            connectivityPref.setSummary(settingsPref.getString(CONNECTIVITY_PREF, ""));

            Preference audioSourcePref = findPreference(AUDIO_SOURCE_PREF);
            audioSourcePref.setSummary(settingsPref.getString(AUDIO_SOURCE_PREF, ""));
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
            if (s.equals(CONNECTIVITY_PREF)) {
                Preference connectivityPref = findPreference(s);
                // Set summary to be the user-description for the selected value
                connectivityPref.setSummary(sharedPreferences.getString(s, ""));
            } else if (s.equals(AUDIO_SOURCE_PREF)) {
                Preference audioSourcePref = findPreference(s);
                // Set summary to be the user-description for the selected value
                audioSourcePref.setSummary(sharedPreferences.getString(s, ""));
            }
        }
    }
}
