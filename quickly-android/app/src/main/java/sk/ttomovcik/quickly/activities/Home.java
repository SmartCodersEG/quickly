package sk.ttomovcik.quickly.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.ttomovcik.quickly.BuildConfig;
import sk.ttomovcik.quickly.R;
import sk.ttomovcik.quickly.adapters.TaskListAdapter;
import sk.ttomovcik.quickly.helpers.TaskDbHelper;
import sk.ttomovcik.quickly.views.NoScrollListView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class Home
        extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    public static String KEY_ID = "id";
    public static String KEY_TASK = "task";

    ArrayList<HashMap<String, String>> taskListHashMap = new ArrayList<>();
    TaskDbHelper taskDbHelper;
    MaterialShowcaseSequence mSSeq;
    ShowcaseConfig scCfg;
    SharedPreferences sharedPref;
    TaskListAdapter adapter;

    @BindView(R.id.addTask)
    TextInputEditText textInputEditTextAddTask;
    @BindView(R.id.title)
    TextView tv_windowTitle;
    @BindView(R.id.taskList)
    NoScrollListView nsvTaskList;
    @BindView(R.id.getStartedHint)
    RelativeLayout hintLayout;
    @BindView(R.id.scrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.openSettings)
    ImageButton imgBtn_openSettings;
    @BindView(R.id.fab_addTask)
    ExtendedFloatingActionButton fab_addTask;
    @BindView(R.id.reloadTasks)
    SwipeRefreshLayout reloadTasks;

    @OnClick(R.id.fab_addTask)
    void onClickFabAddTask() {
        startActivity(new Intent(Home.this, AddTask.class)
                        .putExtra("modifyTask", false),
                ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
    }

    @OnClick(R.id.openSettings)
    void openSettings() {
        startActivity(new Intent(this, Settings.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);

        int appTheme = sharedPref.getInt("appTheme", 0);
        String appLang = sharedPref.getString("appLang", "en");

        if (appTheme == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        setLocale(appLang);
        setContentView(R.layout.activity_home);

        tv_windowTitle.setOnLongClickListener(view ->
        {
            Snackbar.make(getWindow().getDecorView().getRootView(), "meow", Snackbar.LENGTH_SHORT).show();
            return true;
        });

        ButterKnife.bind(this);
        initShowcase(this, this);
        taskDbHelper = new TaskDbHelper(this);
        reloadTasks.setOnRefreshListener(this);

        runOnUiThread(() -> {
            textInputEditTextAddTask.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            textInputEditTextAddTask.setOnEditorActionListener((v, id, event) -> {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (id == KeyEvent.ACTION_DOWN)) {
                    quickStoreTask();
                    return true;
                }
                return false;
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateData();
    }

    @Override
    public void onRefresh() {
        populateData();
        new Handler().postDelayed(() -> reloadTasks.setRefreshing(false), 500);
    }

    private void populateData() {
        nestedScrollView.setVisibility(View.GONE);
        hintLayout.setVisibility(View.VISIBLE);
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    private void quickStoreTask() {
        String _taskName = String.valueOf(textInputEditTextAddTask.getText());
        taskDbHelper.addTask(_taskName, "", "", "", "");
        Objects.requireNonNull(textInputEditTextAddTask.getText()).clear();
        populateData();
    }

    private void initShowcase(Context mCtx, Activity activity) {
        mSSeq = new MaterialShowcaseSequence(activity, "seqId");
        scCfg = new ShowcaseConfig();
        scCfg.setDelay(500);
        scCfg.setMaskColor(ContextCompat.getColor(mCtx, R.color.colorShowcase));
        mSSeq.setConfig(scCfg);
        mSSeq.addSequenceItem(textInputEditTextAddTask, getString(R.string.showcase_quicklyAddTaks), getString(R.string.btn_gotIt));
        mSSeq.addSequenceItem(fab_addTask, getString(R.string.showcase_addTask), getString(R.string.btn_gotIt));
        mSSeq.addSequenceItem(imgBtn_openSettings, getString(R.string.showcase_openSettings), getString(R.string.btn_gotIt));
        mSSeq.start();
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
        adapter = new TaskListAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) ->
                startActivity(new Intent(this, AddTask.class)
                        .putExtra("modifyTask", true)
                        .putExtra("id", dataList.get(+position).get(KEY_ID))
                        .putExtra("task", dataList.get(+position).get(KEY_TASK))));
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
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
            loadListView(nsvTaskList, taskListHashMap);
            nestedScrollView.setVisibility(View.VISIBLE);
            if (!taskListHashMap.isEmpty()) hintLayout.setVisibility(View.GONE);
        }
    }
}
