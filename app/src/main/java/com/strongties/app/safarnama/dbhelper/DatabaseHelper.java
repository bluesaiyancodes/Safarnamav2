package com.strongties.app.safarnama.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.strongties.app.safarnama.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DBname = "safarnama";
    private static final int version = 1;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DBname, null, version);

        this.context = context.getApplicationContext();
    }

    public static ArrayList<String> customSplitSpecific(String s)
    {
        ArrayList<String> words = new ArrayList<String>();
        boolean notInsideComma = true;
        int start =0, end=0;
        for(int i=0; i<s.length()-1; i++)
        {
            if(s.charAt(i)==',' && notInsideComma)
            {
                words.add(s.substring(start,i));
                start = i+1;
            }
            else if(s.charAt(i)=='"')
                notInsideComma=!notInsideComma;
        }
        words.add(s.substring(start));
        return words;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS LANDMARKS");
        db.execSQL("DROP TABLE IF EXISTS INDIAINFO");
        onCreate(db);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create Table for Local Landmark
        String sql = "CREATE TABLE LANDMARKS (id INTEGER PRIMARY KEY AUTOINCREMENT, place_id TEXT, name TEXT, lat REAL, lon REAL, state TEXT, district TEXT, city TEXT, type TEXT, url TEXT)";
        db.execSQL(sql);

        //Read from Landmarks csv file and insert into LANDMARKS
        insertStatedataLocal(db, R.raw.landmarks_odisha);
        insertStatedataLocal(db, R.raw.landmarks_delhi);
        insertStatedataLocal(db, R.raw.landmarks_goa);


        //Create Table for India State Information
        String sql2 = "CREATE TABLE INDIAINFO (id INTEGER PRIMARY KEY AUTOINCREMENT, state TEXT, district TEXT, lat REAL, lon REAL, statetype TEXT)";
        db.execSQL(sql2);

        //Read from Landmarks csv file and insert into INDIAINFO
        insertStateInformation(db, R.raw.india_info);

    }


    private void insertStatedataLocal(SQLiteDatabase db, int csvID){

        InputStream is = context.getResources().openRawResource(csvID);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
        String line = "";
        int linecounter = 0;

        try {

            while ((line = reader.readLine()) != null) {
                // Split the line into different tokens (using the comma as a separator excluding commas inside quotes).
                ArrayList<String> tokens = customSplitSpecific(line);
                //count lines
                linecounter++;
                //exclude the first line as it contains headers
                if (linecounter == 1) {
                    continue;
                }
                insertdataLandmark(tokens.get(0), tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), Double.parseDouble(tokens.get(5)), Double.parseDouble(tokens.get(6)), tokens.get(7), tokens.get(11), db);

                Log.d("DatabaseHelper", "Landmark CSV read line ->" + linecounter);
            }
        } catch (IOException e1) {
            Log.e("DatabaseHelper", "Error" + line, e1);
            e1.printStackTrace();
        }

    }

    private  void insertStateInformation(SQLiteDatabase db, int csvID){
        InputStream is2 = context.getResources().openRawResource(csvID);
        BufferedReader reader2 = new BufferedReader(
                new InputStreamReader(is2, StandardCharsets.UTF_8));
        String line2 = "";
        int linecounter2 = 0;


        try {

            while ((line2 = reader2.readLine()) != null) {
                // Split the line into different tokens (using the comma as a separator excluding commas inside quotes).
                ArrayList<String> tokens = customSplitSpecific(line2);
                //count lines
                linecounter2++;
                //exclude the first line as it contains headers
                if (linecounter2 == 1) {
                    continue;
                }

                try {
                    insertdataIndiaInfo(tokens.get(0), tokens.get(1), Double.parseDouble(tokens.get(2)), Double.parseDouble(tokens.get(3)), tokens.get(4), db);
                    Log.d("DatabaseHelper", "IndiaInfo CSV read -> " + tokens.toString());
                } catch (NumberFormatException e) {
                    Log.d("DatabaseHelper", "IndiaInfo CSV read -> " + tokens.toString());
                }

            }
        } catch (IOException e1) {
            Log.e("DatabaseHelper", "Error" + line2, e1);
            e1.printStackTrace();
        }
    }


    private void insertdataLandmark(String name, String place_id, String state, String district, String city, double lat, double lon, String type, String url, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put("place_id", place_id);
        values.put("name", name);
        values.put("state", state);
        values.put("district", district);
        values.put("city", city);
        values.put("lat", lat);
        values.put("lon", lon);
        values.put("type", type);
        values.put("url", url);

        database.insert("LANDMARKS", null, values);

    }

    private void insertdataIndiaInfo(String state, String district, double lat, double lon, String statetype, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put("state", state);
        values.put("district", district);
        values.put("lat", lat);
        values.put("lon", lon);
        values.put("statetype", statetype);

        database.insert("INDIAINFO", null, values);
    }
}
