package com.fhx.microphone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by fhx on 2/4/16.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference channelConfig = findPreference("channel_config");
        channelConfig.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String strValue = newValue.toString();
                Preference primaryAudioSource = findPreference("primary_audio_source");
                Preference secondaryAudioSource = findPreference("secondary_audio_source");
                if(strValue.equals("2")){
                    if(AudioRecorder.isStereoRecordingSupported()) {
                        primaryAudioSource.setEnabled(false);
                        secondaryAudioSource.setEnabled(true);
                    } else {
                        Toast.makeText(getActivity(),
                                "Stereo recording is not supported in this device",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }else{
                    primaryAudioSource.setEnabled(true);
                    secondaryAudioSource.setEnabled(false);
                }
                setSummary(preference, newValue);
                return true;
            }
        });
        channelConfig.getOnPreferenceChangeListener().onPreferenceChange(channelConfig,
                PreferenceManager
                        .getDefaultSharedPreferences(channelConfig.getContext())
                        .getString(channelConfig.getKey(), ""));

        bindPreferenceSummaryToValue("sample_rate");
        bindPreferenceSummaryToValue("primary_audio_source");
        bindPreferenceSummaryToValue("secondary_audio_source");
    }

    private void bindPreferenceSummaryToValue(String preferenceKey){
        Preference preference = findPreference(preferenceKey);
        preference.setOnPreferenceChangeListener(updateSummary);
        updateSummary.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preferenceKey, ""));
    }

    private Preference.OnPreferenceChangeListener updateSummary = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            setSummary(preference, newValue);
            return true;
        }
    };

    public void setSummary(Preference preference, Object newValue){
        String strValue = newValue.toString();
        if(preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference)preference;
            int index = listPreference.findIndexOfValue(strValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);
        } else {
            preference.setSummary(strValue);
        }
    }
}
