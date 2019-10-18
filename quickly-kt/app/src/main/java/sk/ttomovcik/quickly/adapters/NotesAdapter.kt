package sk.ttomovcik.quickly.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import sk.ttomovcik.quickly.R
import sk.ttomovcik.quickly.activities.Home
import java.util.*

class NotesAdapter(
    private val activity: Activity,
    private val data: ArrayList<HashMap<String, String>>
) : BaseAdapter() {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, targetView: View?, parent: ViewGroup): View? {
        var view = targetView
        val viewHolder: NotesViewHolder
        val stringHashMap: HashMap<String, String> = data[position]

        if (view == null) {
            viewHolder = NotesViewHolder()
            view = LayoutInflater.from(activity).inflate(
                R.layout.item_note, parent, false
            )
            viewHolder.noteTitle = view.findViewById(R.id.noteTitle)
            viewHolder.noteText = view.findViewById(R.id.noteText)
            view.tag = viewHolder
        } else viewHolder = view.tag as NotesViewHolder

        viewHolder.noteTitle!!.id = position
        viewHolder.noteTitle!!.text = stringHashMap[Home().noteKey]
        return view
    }
}

internal class NotesViewHolder {
    var noteTitle: TextView? = null
    var noteText: TextView? = null
}
