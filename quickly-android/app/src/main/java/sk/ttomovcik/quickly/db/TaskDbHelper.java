package sk.ttomovcik.quickly.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static sk.ttomovcik.quickly.db.Constants.DB_NAME;
import static sk.ttomovcik.quickly.db.Constants.DB_TABLE;
import static sk.ttomovcik.quickly.db.Constants.DB_VERSION;

public class TaskDbHelper extends SQLiteOpenHelper
{
    /**
     * @param ctx Context
     */
    public TaskDbHelper(Context ctx)
    {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDb)
    {
        sqlDb.execSQL("CREATE TABLE " + DB_TABLE +
                "(id INTEGER PRIMARY KEY," +
                " taskName TEXT," +
                " taskNote TEXT," +
                " taskColor TEXT," +
                " taskFinishDate TEXT," +
                " taskReminder TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDb, int i, int i1)
    {
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
                        String taskFinishDate, String taskReminder)
    {
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
                           String taskFinishDate, String taskReminder)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("taskName", taskName);
        contentValues.put("taskNote", taskNote);
        contentValues.put("taskColor", taskColor);
        contentValues.put("taskFinishDate", taskFinishDate);
        contentValues.put("taskReminder", taskReminder);
        db.update(DB_TABLE, contentValues, "id = ? ", new String[]{id});
    }

    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE + " order by id desc", null);
    }

    public Cursor getDataSpecific(String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE + " WHERE id = '" + id + "' order by id desc", null);
    }

    public Cursor getDataToday()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE +
                " WHERE date(datetime(taskFinishDate / 1000 , 'unixepoch', 'localtime')) " +
                "= date('now', 'localtime') order by id desc", null);
    }

    public Cursor getDataTomorrow()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE +
                " WHERE date(datetime(taskFinishDate / 1000 , 'unixepoch', 'localtime')) " +
                "= date('now', '+1 day', 'localtime')  order by id desc", null);
    }

    public Cursor getDataUpcoming()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_TABLE +
                " WHERE date(datetime(taskFinishDate / 1000 , 'unixepoch', 'localtime'))" +
                " > date('now', '+1 day', 'localtime') order by id desc", null);
    }
}
