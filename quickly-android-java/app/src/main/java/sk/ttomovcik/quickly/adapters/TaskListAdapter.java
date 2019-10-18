package sk.ttomovcik.quickly.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import sk.ttomovcik.quickly.R;
import sk.ttomovcik.quickly.activities.Home;

public class TaskListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public TaskListAdapter(Activity activity,
                           ArrayList<HashMap<String, String>> data)
    {
        this.activity = activity;
        this.data = data;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        TaskListViewHolder holder;
        if (view == null) {
            holder = new TaskListViewHolder();
            view = LayoutInflater.from(activity).inflate(
                    R.layout.item_task, parent, false);
            holder.tvTaskName = view.findViewById(R.id.taskName);
            view.setTag(holder);
        } else {
            holder = (TaskListViewHolder) view.getTag();
        }
        holder.tvTaskName.setId(position);

        HashMap<String, String> stringHashMap;
        stringHashMap = data.get(position);
        holder.tvTaskName.setText(stringHashMap.get(Home.KEY_TASK));
        return view;
    }
}

class TaskListViewHolder {
    TextView tvTaskName;
}
