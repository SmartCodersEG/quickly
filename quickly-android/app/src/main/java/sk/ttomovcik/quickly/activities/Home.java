package sk.ttomovcik.quickly.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.ttomovcik.quickly.BuildConfig;
import sk.ttomovcik.quickly.R;
import sk.ttomovcik.quickly.adapters.TaskListAdapter;
import sk.ttomovcik.quickly.db.TaskDbHelper;
import sk.ttomovcik.quickly.views.NoScrollListView;

public class Home extends AppCompatActivity
{
    public static String KEY_ID = "id";
    public static String KEY_TASK = "task";

    int ANDROID_API_VERSION = Build.VERSION.SDK_INT;

    TaskDbHelper taskDbHelper;
    ArrayList<HashMap<String, String>> taskListHashMap = new ArrayList<>();

    // TextInputEditText -> addTask
    @BindView(R.id.addTask)
    TextInputEditText addTask;

    // NoScrollListView -> taskListUpcoming
    @BindView(R.id.taskList)
    NoScrollListView taskListUpcoming;

    // Progressbar -> loader
    @BindView(R.id.loader)
    ProgressBar loader;

    @BindView(R.id.getStartedHint)
    TextView getStartedHint;

    // NestedScrollView -> scrollView
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;

    @OnClick(R.id.fab_addTask)
    void onClick()
    {
        Intent intent = new Intent(Home.this, AddTask.class);
        intent.putExtra("modifyTask", false);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
    }

    @OnClick(R.id.changeTheme)
    void onClickChangeTheme()
    {
        String[] APP_THEMES_PRE_Q = {getString(R.string.pref_appTheme_setByBatterySaver), getString(R.string.pref_appTheme_light), getString(R.string.pref_appTheme_dark)};
        String[] APP_THEMES_Q = {getString(R.string.pref_appTheme_systemDefault), getString(R.string.pref_appTheme_light), getString(R.string.pref_appTheme_dark)};
        String[] APP_THEMES_TARGET = ANDROID_API_VERSION >= 29 ? APP_THEMES_Q : APP_THEMES_PRE_Q;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        int storedTheme = sharedPref.getInt("appTheme", 0);
        Log.i("themeManager", String.valueOf(storedTheme));
        if (storedTheme == 2)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        initQuickAddTask();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("appTheme", AppCompatDelegate.getDefaultNightMode());
        editor.apply();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        populateData();
    }

    private void populateData()
    {
        taskDbHelper = new TaskDbHelper(this);
        scrollView.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        getStartedHint.setVisibility(View.VISIBLE);
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    private void initQuickAddTask()
    {
        addTask.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        addTask.setOnEditorActionListener((v, actionId, event) ->
        {
            if (event != null && event.getKeyCode()
                    == KeyEvent.KEYCODE_ENTER
                    || actionId == EditorInfo.IME_ACTION_DONE)
            {
                TaskDbHelper taskDbHelper = new TaskDbHelper(this);
                String _taskName = String.valueOf(addTask.getText());
                taskDbHelper.addTask(
                        _taskName,
                        "",
                        "",
                        "",
                        "");
                Objects.requireNonNull(addTask.getText()).clear();
                populateData();
                return true;
            }
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

    @SuppressLint("StaticFieldLeak")
    class LoadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            taskListHashMap.clear();
        }

        protected String doInBackground(String... args)
        {
            Cursor taskData = taskDbHelper.getData();
            loadDataList(taskData, taskListHashMap);
            return "";
        }

        @Override
        protected void onPostExecute(String xml)
        {
            loadListView(taskListUpcoming, taskListHashMap);
            loader.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            if (!taskListHashMap.isEmpty()) getStartedHint.setVisibility(View.GONE);
        }
    }

    public void loadDataList(Cursor cursor, ArrayList<HashMap<String, String>> dataList)
    {
        if (cursor != null)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(KEY_ID, cursor.getString(0));
                hashMap.put(KEY_TASK, cursor.getString(1));
                dataList.add(hashMap);
                cursor.moveToNext();
            }
        }
    }

    public void loadListView(ListView listView, final ArrayList<HashMap<String, String>> dataList)
    {
        TaskListAdapter adapter = new TaskListAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            Intent i = new Intent(this, AddTask.class);
            i.putExtra("modifyTask", true);
            i.putExtra("id", dataList.get(+position).get(KEY_ID));
            i.putExtra("task", dataList.get(+position).get(KEY_TASK));
            startActivity(i);
        });
    }
}
