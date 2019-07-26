package sk.ttomovcik.quickly.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.ttomovcik.quickly.R;
import sk.ttomovcik.quickly.R2;
import sk.ttomovcik.quickly.adapters.TaskListAdapter;
import sk.ttomovcik.quickly.db.TaskDbHelper;
import sk.ttomovcik.quickly.views.NoScrollListView;

public class Home extends AppCompatActivity
{
    public static String KEY_ID = "id";
    public static String KEY_TASK = "task";

    TaskDbHelper taskDbHelper;
    ArrayList<HashMap<String, String>> taskListTodayHashMap = new ArrayList<>();
    ArrayList<HashMap<String, String>> taskListTomorrowHashMap = new ArrayList<>();
    ArrayList<HashMap<String, String>> taskListUpcomingHashMap = new ArrayList<>();

    // TextInputEditText -> addTask
    @BindView(R2.id.addTask)
    TextInputEditText addTask;

    // NoScrollListView -> taskListToday
    @BindView(R2.id.taskListToday)
    NoScrollListView taskListToday;

    // NoScrollListView -> taskListTomorrow
    @BindView(R2.id.taskListTomorrow)
    NoScrollListView taskListTomorrow;

    // NoScrollListView -> taskListUpcoming
    @BindView(R2.id.taskListUpcoming)
    NoScrollListView taskListUpcoming;

    // TextView -> titleToday
    @BindView(R2.id.titleToday)
    TextView titleToday;

    // TextView -> titleTomorrow
    @BindView(R2.id.titleTomorrow)
    TextView titleTomorrow;

    // TextView -> titleUpcoming
    @BindView(R2.id.titleUpcoming)
    TextView titleUpcoming;

    // Progressbar -> loader
    @BindView(R2.id.loader)
    ProgressBar loader;

    // NestedScrollView -> scrollView
    @BindView(R2.id.scrollView)
    NestedScrollView scrollView;

    @OnClick(R2.id.fab_addTask)
    void onClick()
    {
        Intent intent = new Intent(Home.this, AddTask.class);
        intent.putExtra("modifyTask", false);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        quickAddTask();
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
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    private void quickAddTask()
    {
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

    private static String generateColor(Random r)
    {
        final char[] hex = {'0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] s = new char[7];
        int n = r.nextInt(0x1000000);

        s[0] = '#';
        for (int i = 1; i < 7; i++)
        {
            s[i] = hex[n & 0xf];
            n >>= 4;
        }
        return new String(s);
    }

    @SuppressLint("StaticFieldLeak")
    class LoadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            taskListTodayHashMap.clear();
            taskListTomorrowHashMap.clear();
            taskListUpcomingHashMap.clear();
        }

        protected String doInBackground(String... args)
        {
            String xml = "";
            Cursor dataToday = taskDbHelper.getDataToday();
            loadDataList(dataToday, taskListTodayHashMap);
            Cursor dataTomorrow = taskDbHelper.getDataTomorrow();
            loadDataList(dataTomorrow, taskListTomorrowHashMap);
            Cursor dataUpcoming = taskDbHelper.getDataUpcoming();
            loadDataList(dataUpcoming, taskListUpcomingHashMap);
            Cursor dataUpcomingWithoutDate = taskDbHelper.getData();
            loadDataList(dataUpcomingWithoutDate, taskListUpcomingHashMap);
            return xml;
        }

        @Override
        protected void onPostExecute(String xml)
        {
            loadListView(taskListToday, taskListTodayHashMap);
            loadListView(taskListTomorrow, taskListTomorrowHashMap);
            loadListView(taskListUpcoming, taskListUpcomingHashMap);
            if (taskListTodayHashMap.size() > 0) titleToday.setVisibility(View.VISIBLE);
            else titleToday.setVisibility(View.GONE);
            if (taskListTomorrowHashMap.size() > 0) titleTomorrow.setVisibility(View.VISIBLE);
            else titleTomorrow.setVisibility(View.GONE);
            if (taskListUpcomingHashMap.size() > 0) titleUpcoming.setVisibility(View.VISIBLE);
            else titleUpcoming.setVisibility(View.GONE);
            loader.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
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
