package sk.ttomovcik.quickly.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
    boolean handled = false;

    @BindView(R.id.addTask)
    TextInputEditText textInputEditTextAddTask;
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

        // init settings
        sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        if (sharedPref.getInt("app_theme", 0) == 2)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        initShowcase(this, this);

        // load stored tasks and init textInput for new tasks
        taskDbHelper = new TaskDbHelper(this);
        reloadTasks.setOnRefreshListener(this);
        textInputEditTextAddTask.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        textInputEditTextAddTask.setOnEditorActionListener((v, id, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    || id == EditorInfo.IME_ACTION_DONE) {
                quickStoreTask();
                handled = true;
            }
            return handled;
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

    private boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    private void populateData() {
        nestedScrollView.setVisibility(View.GONE);
        LoadStoredTasks loadStoredTasks = new LoadStoredTasks();
        loadStoredTasks.execute();
    }

    private void quickStoreTask() {
        String _taskName = String.valueOf(textInputEditTextAddTask.getText());
        if (isEmpty(_taskName)) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "No task name", Snackbar.LENGTH_SHORT);
        } else {
            taskDbHelper.addTask(_taskName, "", "", "", "");
            Objects.requireNonNull(textInputEditTextAddTask.getText()).clear();
            populateData();
        }
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

    //
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

        // Open AddTask.class in Edit mode
        listView.setOnItemClickListener((parent, view, position, id) ->
                startActivity(new Intent(this, AddTask.class)
                        .putExtra("modifyTask", true)
                        .putExtra("id", dataList.get(+position).get(KEY_ID))
                        .putExtra("task", dataList.get(+position).get(KEY_TASK))));
    }

    @SuppressLint("StaticFieldLeak")
    class LoadStoredTasks extends AsyncTask<String, Void, String> {
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
