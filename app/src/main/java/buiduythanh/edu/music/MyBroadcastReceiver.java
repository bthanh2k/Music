package buiduythanh.edu.music;

import static android.util.Log.d;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

import buiduythanh.edu.model.Music;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int actionMusic = intent.getIntExtra(ActivityMusic.ACTION_MUSIC,-1);
        Intent intentService = new Intent(context,MediaPlaybackService.class);
        intentService.putExtra("action_music_receiver",actionMusic);

        context.startService(intentService);
    }
}