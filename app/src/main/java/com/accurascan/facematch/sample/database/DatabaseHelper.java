package com.accurascan.facematch.sample.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.accurascan.facematch.sample.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserManager.db";
    private static final String TABLE_USER = "user";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_PROFILE = "user_profile";
    private static final String COLUMN_USER_F_ARRAY = "user_f_array";

    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_PROFILE + " TEXT,"+ COLUMN_USER_F_ARRAY +" TEXT"+")";

    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE);
        // Create tables again
        onCreate(db);
    }

    public void addUser(UserModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getUserName());
        values.put(COLUMN_USER_PROFILE, user.getUserImage());
        values.put(COLUMN_USER_F_ARRAY, user.getFloatArrayAsString());
        db.insert(TABLE_USER, null, values);
        db.close();
    }


    public List<UserModel> getAllUser() {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
                COLUMN_USER_PROFILE,
                COLUMN_USER_F_ARRAY
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<UserModel> userList = new ArrayList<UserModel>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            do {
                UserModel user = new UserModel();
                user.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))));
                user.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)));
                user.setUserImage(cursor.getString(cursor.getColumnIndex(COLUMN_USER_PROFILE)));
                user.setFloatArrayAsString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_F_ARRAY)));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public float[][] getFeatureList(){
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
                /*COLUMN_USER_PROFILE,*/
                COLUMN_USER_F_ARRAY
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<UserModel> userList = new ArrayList<UserModel>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            do {
                UserModel user = new UserModel();
                user.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))));
                user.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)));
//                user.setUserImage(cursor.getString(cursor.getColumnIndex(COLUMN_USER_PROFILE)));
                user.setFloatArrayAsString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_F_ARRAY)));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        float[][] floats = new float[0][];
        if (userList!=null) {
            floats = new float[userList.size()][];
        }
        int i = 0;
        for (UserModel userModel: userList) {
            floats[i++] = userModel.getFloatArray();
        }
        return floats;
    }

    public void updateUser(UserModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getUserName());
        values.put(COLUMN_USER_PROFILE, user.getUserImage());
        values.put(COLUMN_USER_F_ARRAY,user.getFloatArrayAsString());
        // updating row
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

    public void deleteUser(UserModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }
}

