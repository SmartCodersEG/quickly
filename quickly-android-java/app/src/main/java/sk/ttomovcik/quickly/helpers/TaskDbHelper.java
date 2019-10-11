package sk.ttomovcik.quickly.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "tasks.db";
    private static final String DB_TABLE = "tasks";
    private static final int DB_VERSION = 1;

    /**
     * @param ctx Context
     */
    public TaskDbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDb) {
        sqlDb.execSQL("CREATE TABLE " + DB_TABLE +
                "(id INTEGER PRIMARY KEY," +
                " taskName TEXT," +
                " taskNote TEXT," +
                " taskColor TEXT," +
                " taskFinishDate TEXT," +
                " taskReminder TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDb, int i, int i1) {
        sqlDb.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        onCreate(sqlDb);
    }

    /**
     * @param taskName       name of the task
     * @param taskNote       additional notes
     * @param taskColor      accent color of the task
     * @param taskFinishDate finish date
     * @param taskReminder   reminder. will be set with alarm after saving
     */
    public void addTask(String taskName, String taskNote, String taskColor,
                        String taskFinishDate, String taskReminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("taskName", taskName);
        contentValues.put("taskNote", taskNote);
        contentValues.put("taskColor", taskColor);
        contentValues.put("taskFinishDate", taskFinishDate);
        contentValues.put("taskReminder", taskReminder);
        db.insert(DB_TABLE, null, contentValues);
    }

    /**
     * @param taskName       name of the task
     * @param taskNote       additional notes
     * @param taskColor      accent color of the task
     * @param taskFinishDate finish date
     * @param taskReminder   reminder. will be set with alarm after saving
     */
    public void updateTask(String id, String taskName, String taskNote, String taskColor,
                           String taskFinishDate, String taskReminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("taskName", taskName);
        contentValues.put("taskNote", taskNote);
        contentValues.put("taskColor", taskColor);
        contentValues.put("taskFinishDate", taskFinishDate);
        contentValues.put("taskReminder", taskReminder);
        db.update(DB_TABLE, contentValues, "id = ? ", new String[]{id});
    }

    public void deleteTask(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DB_TABLE, "ID=?", new String[]{id});
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE
                + " order by id desc", null);
    }

    public Cursor getDataFromId(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE
                + " WHERE id = '" + id
                + "' order by id desc", null);

    }
}
