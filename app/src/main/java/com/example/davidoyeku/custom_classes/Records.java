package com.example.davidoyeku.custom_classes;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by DavidOyeku on 29/01/15.
 */
@Table(name = "Records")
public class Records extends Model {
    //colums for the database
    @Column(name = "title")
    public String title;
    @Column(name = "content")
    public String content;
    @Column(name = "shortSummary")
    public String shortSummary;
    @Column(name = "imagePath")
    public String imagePath;
    @Column(name = "vidPath")
    public String vidPath;
    @Column(name = "audio")
    public String audio;
    @Column(name = "location")
    public String location;
    @Column(name = "weather")
    public String weather;
    @Column(name = "music")
    public String music;
    @Column(name = "date")
    public Long date;
    @Column(name = "longitude")
    public String longitude;
    @Column(name = "latitude")
    public String latitude;


    public Records() {
        super();
    }


    public Records(String title,
                   String content,
                   String shortSummary,
                   String imagePath,
                   String audio,
                   String vidPath,
                   String location,
                   String weather,
                   String music,
                   long date) {
        super();
        //Assigning values;
        this.title = title;
        this.content = content;
        this.shortSummary = shortSummary;
        this.imagePath = imagePath;
        this.audio = audio;
        this.vidPath = vidPath;
        this.location = location;
        this.weather = weather;
        this.music = music;
        this.date = date;
    }

    /*gets every record in the database that contains the query, it then returns a cursor**/
    public static Cursor getKeyWordCursor(String query) {
        String tableName = Cache.getTableInfo(Records.class).getTableName();
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(Records.class).where("content like '%" + query + "%'").toSql();
        Log.d("Query", resultRecords);
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    /**
     * get every records in the database and it
     * returns a cursor*
     */
    public static Cursor getAllCursor() {
        String tableName = Cache.getTableInfo(Records.class).getTableName();
        //arrange in descending order
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(Records.class).orderBy("date DESC").toSql();
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    //get every records in the database but return it in a list
    public static List<Records> getAllList() {
        return new Select() //query
                .from(Records.class)
                .execute();
    }

    public static Cursor getRecordWithImages() {
        String tableName = Cache.getTableInfo(Records.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(Records.class).where("imagePath <> '' ").and("imagePath is not null").limit(5).toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    /**
     * takes in two dates as long, then compares what database record falls in between them
     */
    public static List<Records> getWithinDateRange(long start, long end) {
        return new Select()
                .from(Records.class)
                .where("date >= ?", start).and("date <= ?", end)
                .execute();
    }

    /**
     * clear every records within the database
     * *
     */
    public static void clearRecords() {
        String tableName = Cache.getTableInfo(Records.class).getTableName();
        SQLiteUtils.execSql("Delete from " + tableName);
    }

    //calculates the end of a day
    public static Date getEndDateOfDay(Date day) {
        if (day == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        return cal.getTime();
    }

    //calculate the start of the day
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    //returns the size of the database
    public static int countRecordSize() {
        return new Select().from(Records.class).execute().size();
    }

}