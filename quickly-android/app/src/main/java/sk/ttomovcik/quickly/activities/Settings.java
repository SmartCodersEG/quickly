package sk.ttomovcik.quickly.activities;

import android.annotation.SuppressLint;
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
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class Settings extends AppCompatActivity {

    static int ANDROID_API_VERSION = Build.VERSION.SDK_INT;
    SharedPreferences preferences;
    static SharedPreferences.Editor sharedPrefEditor;


    @SuppressLint("CommitPrefEdits")
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
        }


        preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        sharedPrefEditor = preferences.edit();
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

            /*
            TODO: Add stuff below asap.
            Preference pref_cloud_signIn = findPreference("cloud_signIn");
            Preference pref_cloud_allowSync = findPreference("cloud_allowSync");
            Preference pref_cloud_snapFingers = findPreference("cloud_snapFingers");
            Preference pref_tasks_archived = findPreference("tasks_viewArchived");
            Preference pref_db_export = findPreference("db_export");
            Preference pref_db_import = findPreference("db_import");
            */

            Preference pref_db_deleteAll = findPreference("db_deleteAll");
            Preference pref_app_language = findPreference("app_language");
            Preference pref_app_theme = findPreference("app_theme");
            Preference pref_app_help = findPreference("app_help");
            Preference pref_app_resetShowcase = findPreference("app_resetShowcase");
            Preference pref_app_thirdPartyLibs = findPreference("app_thirdPartyLibs");
            Preference pref_app_about = findPreference("app_about");

            Objects.requireNonNull(pref_db_deleteAll).setOnPreferenceClickListener(preference ->
            {
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setIcon(R.drawable.ic_delete_forever_24dp)
                        .setTitle(getString(R.string.dialog_areYouSure))
                        .setMessage("Are you sure you want to close without saving this task?")
                        .setNegativeButton(getString(R.string.btn_action_cancel), null)
                        .setPositiveButton(getString(R.string.btn_action_delete), (dialog, id) -> {
                            Objects.requireNonNull(getContext()).deleteDatabase("tasks.db");
                            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),
                                    getString(R.string.toast_allTasksRemoved), Snackbar.LENGTH_SHORT).show();
                        }).show();
                return false;
            });

            Objects.requireNonNull(pref_app_language).setOnPreferenceClickListener(preference ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setItems(R.array.languages, (dialog, which) -> {
                    switch (which) {
                        case 0: // English
                            setLocale("en");
                            sharedPrefEditor.putString("appLang", "en").apply();
                            break;
                        case 1: // Slovak
                            setLocale("sk");
                            sharedPrefEditor.putString("appLang", "sk").apply();
                            break;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            });

            Objects.requireNonNull(pref_app_theme).setOnPreferenceClickListener(preference ->
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
                            sharedPrefEditor.putInt("appTheme", 0).apply();
                            sharedPrefEditor.apply();
                            break;
                        case 1: // Light theme
                            applyTheme(1);
                            sharedPrefEditor.putInt("appTheme", 1).apply();
                            sharedPrefEditor.apply();
                            break;
                        case 2: // Dark theme
                            applyTheme(2);
                            sharedPrefEditor.putInt("appTheme", 2).apply();
                            sharedPrefEditor.apply();
                            break;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            });

            Objects.requireNonNull(pref_app_help).setOnPreferenceClickListener(preference ->
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://github.com/ttomovcik/quickly/wiki")));
                return false;
            });

            Objects.requireNonNull(pref_app_resetShowcase).setOnPreferenceClickListener(preference ->
            {
                MaterialShowcaseView.resetSingleUse(Objects.requireNonNull(getContext()), "seqId");
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),
                        getString(R.string.toast_allTasksRemoved), Snackbar.LENGTH_SHORT).show();
                return false;
            });

            Objects.requireNonNull(pref_app_thirdPartyLibs).setOnPreferenceClickListener(preference ->
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

            Objects.requireNonNull(pref_app_about).setOnPreferenceClickListener(preference ->
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://github.com/ttomovcik/quickly/")));
                return false;
            });


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
    }
}