package sk.ttomovcik.quickly.activities

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import sk.ttomovcik.quickly.helpers.NoteDbHelper


class AddNote : AppCompatActivity() {

    var newIntent: Intent? = null
    var modifyTask: Boolean = false
    var id: String? = null
    var noteDbHelper: NoteDbHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_add_note)

        noteDbHelper = NoteDbHelper(this)
    }

    @Override
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (modifyTask) {
            menuInflater.inflate(R.menu.add_task_options, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    @Override
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                return true
            }
            R.id.action_removeTask -> {
                Snackbar.make(
                    findViewById<View>(android.R.id.content),
                    getString(R.string.toast_taskRemoved), Snackbar.LENGTH_SHORT
                )
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar?, event: Int) {
                            taskDbHelper.deleteTask(id)
                            finish()
                        }
                    })
                    .setAction("Undo") { view -> finish() }
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
