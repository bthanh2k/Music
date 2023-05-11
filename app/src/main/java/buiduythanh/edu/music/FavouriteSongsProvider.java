package buiduythanh.edu.music;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import buiduythanh.edu.database.FavouriteSongsDatabase;

public class FavouriteSongsProvider extends ContentProvider {
    private static final String AUTHORITY = "buiduythanh.edu.music.FavouriteSongsProvider";
    private static final String SONG_TABLE = "FavouriteSongs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SONG_TABLE);
    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;

    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mURIMatcher.addURI(AUTHORITY,SONG_TABLE,TYPE_1);
        mURIMatcher.addURI(AUTHORITY,SONG_TABLE + "/#",TYPE_2);
    }
    private FavouriteSongsDatabase mFavouriteSongsDatabase;

    public FavouriteSongsProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        mFavouriteSongsDatabase = new FavouriteSongsDatabase(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(FavouriteSongsDatabase.TABLE_NAME);
        int uriType = mURIMatcher.match(uri);
        switch (uriType){
            case TYPE_2:
                queryBuilder.appendWhere(FavouriteSongsDatabase.KEY_ID_PROVIDER + "="
                        + uri.getLastPathSegment());
                break;
            case TYPE_1:
                break;
            default:
                throw new IllegalArgumentException("Unknow URI");
        }
        Cursor cursor = queryBuilder.query(mFavouriteSongsDatabase.getReadableDatabase()
                ,projection,selection,selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = mURIMatcher.match(uri);
        SQLiteDatabase db = mFavouriteSongsDatabase.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType){
            case TYPE_1:
                rowsUpdated = db.update(FavouriteSongsDatabase.TABLE_NAME,values,selection,
                        selectionArgs);
                break;
            case TYPE_2:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsUpdated = db.update(FavouriteSongsDatabase.TABLE_NAME,values,
                            FavouriteSongsDatabase.KEY_ID_PROVIDER + "=" + id,null);
                }else{
                    rowsUpdated = db.update(FavouriteSongsDatabase.TABLE_NAME,values,
                            FavouriteSongsDatabase.KEY_ID_PROVIDER + "=" + id + "and" +
                                    selection,selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsUpdated;
    }
}