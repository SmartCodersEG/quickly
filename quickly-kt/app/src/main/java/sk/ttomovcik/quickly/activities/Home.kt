package sk.ttomovcik.quickly.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_home.*
import sk.ttomovcik.quickly.adapters.NotesAdapter
import sk.ttomovcik.quickly.helpers.NoteDbHelper


@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class Home : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    var noteId = "id"
    var noteKey = "note"
    var noteDbHelper: NoteDbHelper? = null
    var notesHashMap: ArrayList<HashMap<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(sk.ttomovcik.quickly.R.layout.activity_home)
        noteDbHelper = NoteDbHelper(this)
        LoadNotes().execute()

    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onRefresh() {
        LoadNotes().execute()
        Handler().postDelayed(
            {
                content.isRefreshing = false
            }, 500
        )
    }

    private fun setupApp() {
        addNote.setOnClickListener {
            startActivity(
                Intent(this@Home, AddNote::class.java)
                    .putExtra("modifyTask", false),
                ActivityOptions.makeSceneTransitionAnimation(this@Home).toBundle()
            )
        }
    }

    fun loadListView(listView: ListView, dataList: ArrayList<HashMap<String, String>>) {
        val adapter = NotesAdapter(this, dataList)
        listView.adapter = adapter

        // Open AddTask.class in Edit mode
        listView.setOnItemClickListener { parent, view, position, id ->
            startActivity(
                Intent(this, AddNote::class.java)
                    .putExtra("modifyTask", true)
                    .putExtra("id", dataList[+position][noteId])
                    .putExtra("task", dataList[+position][noteKey])
            )
        }
    }

    fun loadDataList(cursor: Cursor?, dataList: ArrayList<HashMap<String, String>>) {
        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val hashMap = HashMap<String, String>()
                hashMap[noteId] = cursor.getString(0)
                hashMap[noteKey] = cursor.getString(1)
                dataList.add(hashMap)
                cursor.moveToNext()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class LoadNotes : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            notesHashMap.clear()
        }

        override fun doInBackground(vararg args: String): String {
            val taskData = noteDbHelper?.getNotes()
            loadDataList(taskData, notesHashMap)
            return ""
        }

        override fun onPostExecute(xml: String) {
            loadListView(taskList, notesHashMap)
            scrollView.visibility = View.VISIBLE
        }
    }
}
