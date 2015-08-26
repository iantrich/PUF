package com.example.gesturesapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Display the fragment as the main content
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PUFPreferencesFragment()).commit();
	}

	/**
	 * This fragment shows the preferences for ToolTray
	 */
	public static class PUFPreferencesFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			//Make sure default values are applied
			PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

			//Load preferences from XML resource
			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
