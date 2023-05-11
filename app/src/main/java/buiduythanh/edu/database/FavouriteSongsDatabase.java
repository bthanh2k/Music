package buiduythanh.edu.database;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import buiduythanh.edu.model.Music;

public class FavouriteSongsDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Music";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "FavouriteSongs";

    public static final String KEY_ID = "ID";
    public static final String KEY_ID_PROVIDER = "ID_PROVIDER";
    public static final String KEY_IS_FAVOURITE = "IS_FAVOURITE";
    public static final String KEY_COUNT_OF_PLAY = "COUNT_OF_PLAY";

    private ContentResolver myCR;

    public FavouriteSongsDatabase(@Nullable Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        assert context != null;
        myCR = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create_songs_table = String.format(
                "CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s INTEGER, %s INTEGER, %s INTEGER)"
                , TABLE_NAME, KEY_ID, KEY_ID_PROVIDER, KEY_IS_FAVOURITE, KEY_COUNT_OF_PLAY);
        sqLiteDatabase.execSQL(create_songs_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String drop_songs_table = String.format("DROP TABLE IF EXISTS %S", TABLE_NAME);
        sqLiteDatabase.execSQL(drop_songs_table);
        onCreate(sqLiteDatabase);
    }

    public void addAllSongs(ArrayList<Music> musicArrayList){
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i = 0; i<musicArrayList.size(); i++){
            ContentValues values = new ContentValues();
            values.put(KEY_ID_PROVIDER,musicArrayList.get(i).getmMusicId());
            values.put(KEY_IS_FAVOURITE,0);
            values.put(KEY_COUNT_OF_PLAY,0);
            db.insert(TABLE_NAME,null,values);
        }
        db.close();
    }
    
    public void addSong(Music music){
        SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_ID_PROVIDER,music.getmMusicId());
            values.put(KEY_IS_FAVOURITE,0);
            values.put(KEY_COUNT_OF_PLAY,0);
            db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public void setSongFavourite(int songId, int mFavourite){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_FAVOURITE,mFavourite);

        db.update(TABLE_NAME, values, KEY_ID_PROVIDER + " = ?", new String[] {String.valueOf(songId)});
        db.close();
    }

    public void setSongCountOfPlay(int songId, int mCount){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COUNT_OF_PLAY,mCount);

        db.update(TABLE_NAME, values, KEY_ID_PROVIDER + " = ?", new String[] {String.valueOf(songId)});
        db.close();
    }

    @SuppressLint("Recycle")
    public int getSongIsFavourite(int songId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, KEY_ID_PROVIDER + " = ?"
                , new String[] {String.valueOf(songId)},null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        assert cursor != null;
        return cursor.getInt(2);
    }

    @SuppressLint("Recycle")
    public int getSongCountOfPlay(int songId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, KEY_ID_PROVIDER + " = ?"
                , new String[] {String.valueOf(songId)},null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        assert cursor != null;
        return cursor.getInt(3);
    }

    public void deleteAllSongs(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

    public void deleteSong(int idSong){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHEN " + "ID_PROVIDER" + " = " + idSong);
        db.close();
    }
}
