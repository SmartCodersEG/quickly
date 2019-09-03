package sk.ttomovcik.quickly.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.Locale;
import java.util.Objects;

import sk.ttomovcik.quickly.BuildConfig;
import sk.ttomovcik.quickly.R;

public class Settings extends AppCompatActivity {

    static int ANDROID_API_VERSION = Build.VERSION.SDK_INT;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference _appTheme = findPreference("appTheme");
            Objects.requireNonNull(_appTheme).setOnPreferenceClickListener(preference ->
            {
                String[] APP_THEMES_PRE_Q = {getString(R.string.pref_appTheme_setByBatterySaver), getString(R.string.pref_appTheme_light), getString(R.string.pref_appTheme_dark)};
                String[] APP_THEMES_Q = {getString(R.string.pref_appTheme_systemDefault), getString(R.string.pref_appTheme_light), getString(R.string.pref_appTheme_dark)};
                String[] APP_THEMES_TARGET = ANDROID_API_VERSION >= 29 ? APP_THEMES_Q : APP_THEMES_PRE_Q;
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle(getString(R.string.title_appTheme));
                builder.setIcon(R.drawable.ic_palette_24dp);
                builder.setItems(APP_THEMES_TARGET, (dialog, which) ->
                {
                    switch (which) {
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

            Preference _help = findPreference("help");
            Objects.requireNonNull(_help).setOnPreferenceClickListener(preference ->
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://github.com/ttomovcik/quickly/wiki")));
                return false;
            });

            Preference _thirdParty = findPreference("thirdParty");
            Objects.requireNonNull(_thirdParty).setOnPreferenceClickListener(preference ->
            {
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT)
                        .withAboutAppName(getString(R.string.app_name))
                        .withAboutDescription(getString(R.string.aboutLibraries_description_text))
                        .withAutoDetect(true)
                        .withLicenseShown(false)
                        .withActivityTitle(getString(R.string.pref_desc_about))
                        .start(Objects.requireNonNull(getContext()));
                return false;
            });

            Preference _about = findPreference("about");
            Objects.requireNonNull(_about).setOnPreferenceClickListener(preference ->
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://github.com/ttomovcik/quickly/")));
                return false;
            });

            Preference _clearAll = findPreference("clearAll");
            Objects.requireNonNull(_clearAll).setOnPreferenceClickListener(preference ->
            {
                // Creates empty task after deleting DB.
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setIcon(R.drawable.ic_delete_forever_24dp)
                        .setTitle(getString(R.string.dialog_areYouSure))
                        .setMessage("Are you sure you want to close without saving this task?")
                        .setNegativeButton(getString(R.string.btn_action_cancel), null)
                        .setPositiveButton(getString(R.string.btn_action_delete), (dialog, id) -> {
                            String DB_NAME = BuildConfig.APPLICATION_ID + ".todo.db";
                            Objects.requireNonNull(getContext()).deleteDatabase(DB_NAME);
                            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),
                                    getString(R.string.toast_allTasksRemoved), Snackbar.LENGTH_SHORT).show();
                        }).show();
                return false;
            });

            Preference _language = findPreference("language");
            Objects.requireNonNull(_language).setOnPreferenceClickListener(preference ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setItems(R.array.languages, (dialog, which) -> {
                    switch (which) {
                        case 0: // English
                            setLocale("en");
                            break;
                        case 1: // Slovak
                            setLocale("sk");
                            break;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            });
        }

        private void applyTheme(int themeId) {
            switch (themeId) {
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


        private void setLocale(String lang) {
            Locale myLocale = new Locale(lang);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(getActivity(), Home.class);
            startActivity(refresh);
        }
    }
}