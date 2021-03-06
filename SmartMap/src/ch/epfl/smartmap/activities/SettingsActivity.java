package ch.epfl.smartmap.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.ServiceContainer;

/**
 * A {@link PreferenceActivity} to handle our settings.
 *
 * @author SpicyCH
 */
public class SettingsActivity extends PreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new PreferenceListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setupActionBar();
        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (SettingsActivity.this.getString(R.string.settings_key_events_show_public).equals(key)) {
                    // Notify the event listeners if the show public events is enabled or disabled.
                    ServiceContainer.getCache().notifyEventListeners();
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        // Show the Up button in the action bar.
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        // Set action bar color to main color
        this.getActionBar().setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.main_blue)));
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary
     * (line of text below the preference title) is updated to reflect the value. The summary is also immediately
     * updated upon calling this method. The exact display format is dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows all the settings for simplicity's sake. We can add some other ones if we have many settings
     * in the future and want them to be split by headers.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.pref_general);

            bindPreferenceSummaryToValue(this.findPreference(this.getString(R.string.settings_key_refresh_frequency)));
            bindPreferenceSummaryToValue(this.findPreference(this.getString(R.string.settings_key_last_seen_max)));
            bindPreferenceSummaryToValue(this.findPreference(this
                    .getString(R.string.settings_key_max_distance_fetch_events)));
        }
    }

    /**
     * A listener on the preferences that helps bind a summary to its value.
     *
     * @author SpicyCH
     */
    static class PreferenceListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    }
}