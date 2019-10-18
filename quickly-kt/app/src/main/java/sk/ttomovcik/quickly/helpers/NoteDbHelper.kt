package sk.ttomovcik.quickly.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDbHelper(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, 1) {

    val data: Cursor
        get() {
            val db = this.readableDatabase
            return db.rawQuery(
                "select * from " + DB_TABLE
                        + " order by id desc", null
            )
        }

    override fun onCreate(sqlDb: SQLiteDatabase) {
        sqlDb.execSQL(
            ("CREATE TABLE " + DB_TABLE +
                    "(id INTEGER PRIMARY KEY," +
                    " noteName TEXT," +
                    " noteNote TEXT," +
                    " noteFinishDate TEXT," +
                    " noteReminder TEXT)")
        )
    }

    override fun onUpgrade(sqlDb: SQLiteDatabase, i: Int, i1: Int) {
        sqlDb.execSQL("DROP TABLE IF EXISTS $DB_TABLE")
        onCreate(sqlDb)
    }

    /**
     * @param noteName       name of the note
     * @param noteNote       additional notes
     * @param noteFinishDate finish date
     * @param noteReminder   reminder. will be set with alarm after saving
     */
    fun addNote(
        noteName: String, noteNote: String,
        noteFinishDate: String, noteReminder: String
    ) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("noteName", noteName)
        contentValues.put("noteNote", noteNote)
        contentValues.put("noteFinishDate", noteFinishDate)
        contentValues.put("noteReminder", noteReminder)
        db.insert(DB_TABLE, null, contentValues)
    }

    /**
     * @param noteName       name of the note
     * @param noteNote       additional notes
     * @param noteFinishDate finish date
     * @param noteReminder   reminder. will be set with alarm after saving
     */
    fun updateNote(
        id: String, noteName: String, noteNote: String,
        noteFinishDate: String, noteReminder: String
    ) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("noteName", noteName)
        contentValues.put("noteNote", noteNote)
        contentValues.put("noteFinishDate", noteFinishDate)
        contentValues.put("noteReminder", noteReminder)
        db.update(DB_TABLE, contentValues, "id = ? ", arrayOf(id))
    }

    fun deleteNote(id: String) {
        val db = this.writableDatabase
        db.delete(DB_TABLE, "ID=?", arrayOf(id))
    }

    fun getNotes() : Cursor {
        val db = this.readableDatabase
        return db.rawQuery("select * from " + DB_TABLE
                + " order by id desc", null)
    }

    companion object {
        private const val DB_NAME = "notes.db"
        private const val DB_TABLE = "notes"
    }
}
