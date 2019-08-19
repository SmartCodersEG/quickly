package sk.ttomovcik.quickly.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import sk.ttomovcik.quickly.R;

public class Settings extends AppCompatActivity
{
    static int ANDROID_API_VERSION = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference _appTheme = findPreference("appTheme");
            Objects.requireNonNull(_appTheme).setOnPreferenceClickListener(preference ->
            {
                String[] APP_THEMES_PRE_Q = {getString(R.string.pref_appTheme_setByBatterySaver), getString(R.string.pref_appTheme_light), getString(R.string.pref_appTheme_dark)};
                String[] APP_THEMES_Q = {getString(R.string.pref_appTheme_systemDefault), getString(R.string.pref_appTheme_light), getString(R.string.pref_appTheme_dark)};
                String[] APP_THEMES_TARGET = ANDROID_API_VERSION >= 29 ? APP_THEMES_Q : APP_THEMES_PRE_Q;
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle(getString(R.string.title_appTheme));
                builder.setItems(APP_THEMES_TARGET, (dialog, which) ->
                {
                    switch (which)
                    {
                        case 0: // Set by battery saver or system default
                            applyTheme(0);
                            break;
                        case 1: // Light theme
                            applyTheme(1);
                            break;
                        case 2: // Dark theme
                            applyTheme(2);
                            break;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            });
        }

        private void applyTheme(int themeId)
        {
            switch (themeId)
            {
                case 0: // Set by battery saver or system default
                    if (ANDROID_API_VERSION >= 29)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                    break;
                case 1: // Light theme
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case 2: // Dark theme
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
            }
        }
    }
}