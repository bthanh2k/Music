package buiduythanh.edu.music;

import static android.util.Log.d;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;
import java.util.ArrayList;
import buiduythanh.edu.model.Music;

public class BaseSongListFragment extends Fragment {

    public BaseSongListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_song_list, container, false);
    }

    @SuppressLint({"Range", "NotifyDataSetChanged"})
    protected void readDataFromExternalStorage(ArrayList<Music> mMusicArray) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null,selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            int id = 0;
            while (cursor.moveToNext()) {
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String durationTotal = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String duration = formatDuration(Long.parseLong(durationTotal));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                Bitmap bitmap = null;
                // Lấy hình ảnh qua Uri
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), albumArtUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMusicArray.add(new Music(id, data, title, album, bitmap, duration, 0,0));
                id++;
            }
            cursor.close();
            ActivityMusic mActivityMusic = (ActivityMusic) getActivity();
            if (mActivityMusic.getIsFirstTimeAddToDatabase()){
                addSongsToDatabaseFavourite(mMusicArray);
                mActivityMusic.setIsFirstTimeAddToDatabase(false);
            }
        }
    }

    public void addSongsToDatabaseFavourite(ArrayList<Music> mMusicArray){
        ActivityMusic mActivityMusic = (ActivityMusic) getActivity();
        mActivityMusic.getDatabase().deleteAllSongs();
        mActivityMusic.getDatabase().addAllSongs(mMusicArray);
        mActivityMusic.setIsFirstTimeAddToDatabase(false);
    }

    @SuppressLint({"Range","Recycle"})
    public void readDataWhenResume(ArrayList<Music> mListMusic){
        ContentResolver contentResolver = requireActivity().getContentResolver();
        ArrayList<Music> mArrayList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null,selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            int id = 0;
            while (cursor.moveToNext()) {
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String durationTotal = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String duration = formatDuration(Long.parseLong(durationTotal));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                Bitmap bitmap = null;
                // Lấy hình ảnh qua Uri
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), albumArtUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mArrayList.add(new Music(id, data, title, album, bitmap, duration, 0,0));
                id++;
            }
        }
        int n = mArrayList.size() - mListMusic.size();
        ActivityMusic mActivityMusic = (ActivityMusic) getActivity();
        if (n > 0){
            mActivityMusic.getAllSongsFragment().setIsReloadMusic(true);
            mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setServiceState(false);
            handleAddSongFromDatabase(mArrayList,mListMusic);
            mActivityMusic.getAllSongsFragment().setIsFirstTimeSend(true);
            mActivityMusic.getAllSongsFragment().getMusicAdapter().setIsReloadMusic(true);
            if (mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsPlaying()){
                mActivityMusic.getAllSongsFragment().getMusicAdapter()
                        .setMusicNameFromService(mActivityMusic.getAllSongsFragment()
                                .getMediaPlaybackService().getDataSongPlaying());
            }
        } else if (n < 0){
            mActivityMusic.getAllSongsFragment().setIsReloadMusic(true);
            mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setServiceState(false);
            handleRemoveSongToDatabase(mArrayList,mListMusic);
            mActivityMusic.getAllSongsFragment().setIsFirstTimeSend(true);
            mActivityMusic.getAllSongsFragment().getMusicAdapter().setIsReloadMusic(true);
            if (mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsPlaying()){
                mActivityMusic.getAllSongsFragment().getMusicAdapter()
                        .setMusicNameFromService(mActivityMusic.getAllSongsFragment()
                                .getMediaPlaybackService().getDataSongPlaying());
            }
        }
    }

    private void handleRemoveSongToDatabase(ArrayList<Music> mArrayList, ArrayList<Music> mListMusic) {
        ActivityMusic mActivityMusic = (ActivityMusic) getActivity();
        if (mArrayList.size() > 0){
            for (int i = mArrayList.size() - 1; i < mListMusic.size(); i++){
                mListMusic.remove(mListMusic.get(i));
            }
            for (int i = 0; i< mArrayList.size(); i++){
                mListMusic.set(i,mArrayList.get(i));
                mActivityMusic.getDatabase().addSong(mListMusic.get(i));
            }
            addSongsToDatabaseFavourite(mListMusic);
            mActivityMusic.setIsFirstTimeAddToDatabase(false);
        }
    }


    private void handleAddSongFromDatabase(ArrayList<Music> mArrayList, ArrayList<Music> mListMusic) {
        Music music = new Music();
        ActivityMusic mActivityMusic = (ActivityMusic) getActivity();
        for (int i = mListMusic.size(); i < mArrayList.size(); i++){
            mListMusic.add(music);
        }
        for (int i = 0; i< mArrayList.size(); i++){
            mListMusic.set(i,mArrayList.get(i));
            mActivityMusic.getDatabase().addSong(mListMusic.get(i));
        }
        addSongsToDatabaseFavourite(mListMusic);
        mActivityMusic.setIsFirstTimeAddToDatabase(false);
    }

    public static String formatDuration(long milliseconds) {
        String finalTimerString = "";
        String secondsString;

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }
}