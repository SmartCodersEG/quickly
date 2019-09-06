package sk.ttomovcik.quickly.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.ttomovcik.quickly.R;
import sk.ttomovcik.quickly.R2;
import sk.ttomovcik.quickly.helpers.TaskDbHelper;

public class AddTask extends AppCompatActivity {

    TaskDbHelper taskDbHelper;
    Intent intent;
    boolean modifyTask;
    String id;

    @BindView(R2.id.toolbar)
    MaterialToolbar materialToolbar;
    @BindView(R2.id.title)
    TextView title;
    @BindView(R2.id.tv_taskName)
    TextView tvTaskName;
    @BindView(R2.id.taskName)
    TextInputEditText taskNameBox;
    @BindView(R2.id.taskNote)
    TextInputEditText taskNoteBox;
    @BindView(R2.id.fab_addTask)
    ExtendedFloatingActionButton addTask;

    @OnClick(R2.id.fab_addTask)
    void onClickAddOrModify() {
        finishEditingTask();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideAnimation();
        setContentView(R.layout.activity_add_task);
        ButterKnife.bind(this);

        intent = getIntent();
        id = intent.getStringExtra("id");
        modifyTask = intent.getBooleanExtra("modifyTask", false);

        // Add toolbar stuff
        setSupportActionBar(materialToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        taskDbHelper = new TaskDbHelper(this);
        if (modifyTask) setModifyTask();

        // TODO: Set max height
        displayTaskName();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_task_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                displayWarningOnExit();
                return true;
            case R.id.action_removeTask:
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.toast_taskRemoved), Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            public void onDismissed(Snackbar snackbar, int event) {
                                taskDbHelper.deleteTask(id);
                                finish();
                            }
                        })
                        .setAction("Undo", view -> finish())
                        .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        displayWarningOnExit();
    }

    public void setModifyTask() {
        title.setText(R.string.title_updateTask);
        addTask.setText(R.string.title_updateTask);
        populatePlaceholdersFromStoredData(id);
    }

    private void finishEditingTask() {
        String _taskName = String.valueOf(taskNameBox.getText());
        String _taskNote = String.valueOf(taskNoteBox.getText());

        if (modifyTask) taskDbHelper.updateTask(id, _taskName, _taskNote, "", "", "");
        else {
            if (isEmpty(_taskNote))
                taskDbHelper.addTask(getString(R.string.task_untitled), _taskNote, "#000000", "", "");
            else
                taskDbHelper.addTask(_taskName, _taskNote, "", "", "");
        }
        finish();
    }

    private void displayWarningOnExit() {
        if (!isEmpty(String.valueOf(taskNameBox.getText()))
                || !isEmpty(String.valueOf(taskNoteBox.getText()))) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_playlist_add_24dp)
                    .setTitle("Unsaved changes")
                    .setMessage("Are you sure you want to close without saving this task?")
                    .setPositiveButton("Yep", (dialog, which) -> finish())
                    .setNegativeButton("NAOW", null)
                    .show();
        } else finish();
    }

    public void setSlideAnimation() {
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.END);
        slide.setDuration(200);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(slide);
        getWindow().setEnterTransition(slide);
    }

    private void displayTaskName() {
        taskNameBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvTaskName.setMovementMethod(ScrollingMovementMethod.getInstance());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvTaskName.setText(taskNameBox.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEmpty(taskNameBox.toString())) {
                    tvTaskName.setText(getString(R.string.title_noTaskName));
                }
            }
        });
    }

    private void populatePlaceholdersFromStoredData(String id) {
        Cursor cTaskData = taskDbHelper.getDataFromId(id);
        String _taskName = null, _taskNote = null;
        if (cTaskData != null) {
            cTaskData.moveToFirst();
            _taskName = cTaskData.getString(cTaskData.getColumnIndex("taskName"));
            _taskNote = cTaskData.getString(cTaskData.getColumnIndex("taskNote"));
        }
        if (!isEmpty(_taskName)) taskNameBox.setText(_taskName);
        if (!isEmpty(_taskName)) tvTaskName.setText(_taskName);
        if (!isEmpty(_taskNote)) taskNameBox.setText(_taskNote);
    }

    private boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }
}
