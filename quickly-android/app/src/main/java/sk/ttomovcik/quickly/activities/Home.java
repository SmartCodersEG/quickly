package sk.ttomovcik.quickly.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.snackbar.Snackbar;
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
import sk.ttomovcik.quickly.helpers.TaskDbHelper;
import sk.ttomovcik.quickly.views.NoScrollListView;

public class Home extends AppCompatActivity {
    public static String KEY_ID = "id";
    public static String KEY_TASK = "task";

    // TextInputEditText -> TextInputEditText_AddTask
    @BindView(R.id.addTask)
    TextInputEditText TextInputEditText_AddTask;

    TaskDbHelper taskDbHelper;
    ArrayList<HashMap<String, String>> taskListHashMap = new ArrayList<>();
    @BindView(R.id.title)
    TextView tv_windowTitle;

    // NoScrollListView -> taskListUpcoming
    @BindView(R.id.taskList)
    NoScrollListView taskListUpcoming;

    // Progressbar -> loader
    @BindView(R.id.loader)
    ProgressBar loader;

    @BindView(R.id.getStartedHint)
    RelativeLayout getStartedHint;

    // NestedScrollView -> scrollView
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;

    @OnClick(R.id.fab_addTask)
    void onClickFabAddTask() {
        Intent intent = new Intent(Home.this, AddTask.class)
                .putExtra("modifyTask", false);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
    }

    @OnClick(R.id.openSettings)
    void openSettings() {
        startActivity(new Intent(this, Settings.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("appTheme", AppCompatDelegate.getDefaultNightMode());
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateData();
    }

    private void populateData() {
        taskDbHelper = new TaskDbHelper(this);
        scrollView.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        getStartedHint.setVisibility(View.VISIBLE);
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    private void initQuickAddTask() {
        TextInputEditText_AddTask.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        TextInputEditText_AddTask.setOnEditorActionListener((v, actionId, event) ->
        {
            if (event != null && event.getKeyCode()
                    == KeyEvent.KEYCODE_ENTER
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                TaskDbHelper taskDbHelper = new TaskDbHelper(this);
                String _taskName = String.valueOf(TextInputEditText_AddTask.getText());
                taskDbHelper.addTask(_taskName, "", "", "", "");
                Objects.requireNonNull(TextInputEditText_AddTask.getText()).clear();
                populateData();
                return true;
            }
            return false;
        });
    }

    public void loadDataList(Cursor cursor, ArrayList<HashMap<String, String>> dataList) {
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(KEY_ID, cursor.getString(0));
                hashMap.put(KEY_TASK, cursor.getString(1));
                dataList.add(hashMap);
                cursor.moveToNext();
            }
        }
    }

    public void loadListView(ListView listView, final ArrayList<HashMap<String, String>> dataList) {
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

    @SuppressLint("StaticFieldLeak")
    class LoadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskListHashMap.clear();
        }

        protected String doInBackground(String... args) {
            Cursor taskData = taskDbHelper.getData();
            loadDataList(taskData, taskListHashMap);
            return "";
        }

        @Override
        protected void onPostExecute(String xml) {
            loadListView(taskListUpcoming, taskListHashMap);
            loader.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            tv_windowTitle.setOnLongClickListener(view ->
            {
                Snackbar.make(getWindow().getDecorView().getRootView(), "meow", Snackbar.LENGTH_SHORT).show();
                return true;
            });
            if (!taskListHashMap.isEmpty()) getStartedHint.setVisibility(View.GONE);
        }
    }
}
