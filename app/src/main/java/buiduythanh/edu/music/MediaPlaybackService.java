package buiduythanh.edu.music;

import static android.util.Log.d;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import buiduythanh.edu.model.Music;

public class MediaPlaybackService extends Service implements Serializable {
    private int mMusicPosition;
    private int mSizeMusicArray;
    private transient MediaPlayer mMediaPlayer;

    private ArrayList<Music> mMusicArrayList;
    private boolean isFirstTimeGet = true;
    private boolean isPlaying;
    private boolean isRepeateOne;
    private boolean isRepeateAll;
    private boolean isShuffleOn;
    private boolean serviceState;
    private transient RemoteViews notifyDefault;

    private final int ACTION_PAUSE = 0;
    private final int ACTION_RESUME = 1;
    private final int ACTION_PRE = 2;
    private final int ACTION_NEXT = 3;
    private final int ACTION_UPDATE_NOTIFICATION = 4;
    private final String CHANNEL_ID = "MUSIC_NOTIFICATION_CHANNEL";

    private Music musicPlay;
    private transient SendInfor sendInfor;

    public void setSendInfor(SendInfor sendInfor){
        this.sendInfor = sendInfor;
    }

    public MediaPlaybackService() { }

    public MediaPlayer getMediaPlayer(){
        return mMediaPlayer;
    }

    public void setIsPlaying(boolean b){
        this.isPlaying = b;
    }

    public boolean getIsPlaying(){
        return isPlaying;
    }

    public boolean getServiceState(){
        return serviceState;
    }

    public void setServiceState(boolean b){
        this.serviceState = b;
    }

    private final MyBinder mBinder = new MyBinder();

    public class MyBinder extends Binder implements Serializable{
        MediaPlaybackService getMediaPlaybackService(){
            return MediaPlaybackService.this;
        }
    }

    public boolean getIsRepeateOne(){
        return isRepeateOne;
    }

    public void setIsRepeateOne(boolean b){
        this.isRepeateOne = b;
    }

    public boolean getIsShuffleOn(){
        return isShuffleOn;
    }

    public void setIsShuffleOn(boolean b){
        this.isShuffleOn = b;
    }

    public void setIsRepeateAll(boolean b){
        this.isRepeateAll = b;
    }

    public boolean getIsRepeateAll(){
        return  isRepeateAll;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!serviceState){
            getInformation(intent);
            playSong();
            serviceState = true;
        }
        int actionMusic = intent.getIntExtra(getString(R.string.actionmusicreceiver), -1);
        handleActionNotification(actionMusic);
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendInfor.sendCommand(MediaPlaybackFragment.REMOVE_SEEKBAR);
        stopSelf();
    }

    private void handleActionNotification(int actionMusic) {
        switch (actionMusic){
            case ACTION_PAUSE:
                pauseSong();
                notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_pause_notification);
                break;
                case ACTION_RESUME:
                    resumeSong();
                    notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_play_notification);
                    break;
                    case ACTION_PRE:
                        playPrevious();
                        updateNotification(mMusicArrayList.get(mMusicPosition));
                        break;
                        case ACTION_NEXT:
                            playNext();
                            updateNotification(mMusicArrayList.get(mMusicPosition));
                            break;
                            case ACTION_UPDATE_NOTIFICATION:
                                sendBigStyleNotification(mMusicArrayList.get(mMusicPosition));
                                break;
        }
    }

    public void setIsFirstTimeGet(boolean b){
        this.isFirstTimeGet = b;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        serviceState = false;
    }

    public int returnMusicPosition(){
        return mMusicPosition;
    }

    public int getIdMusicWhenPlaying(){
        return mMusicArrayList.get(mMusicPosition).getmMusicId();
    }

    public String getDataSongPlaying(){
        return mMusicArrayList.get(mMusicPosition).getmMusicData();
    }

    public String returnCurrentPosition(){
        if (mMediaPlayer != null){
            String time = AllSongsFragment.formatDuration(mMediaPlayer.getCurrentPosition());
            final long mMinutes = (mMediaPlayer.getCurrentPosition() / 1000) / 60;
            String mSeconds = String.valueOf(((mMediaPlayer.getCurrentPosition() / 1000) % 60));
            if (Long.parseLong(mSeconds) < 10) {
                mSeconds = "0" + mSeconds;
            }
            return time;
        }else{
            return "0:00";
        }
    }

    public void getPositionFromIntent(Intent intent){
        Bundle bundle = intent.getExtras();
        if (bundle != null){
            mMusicPosition = bundle.getInt(AllSongsFragment.POSITION_SELECTED);
        }
    }

    public void getInformation(Intent intent) {
        if (isFirstTimeGet){
            mMusicArrayList = new ArrayList<Music>();
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                mMusicPosition = bundle.getInt(AllSongsFragment.POSITION_SELECTED);
                mSizeMusicArray = bundle.getInt(AllSongsFragment.SIZE_MUSIC_ARRAY);
                for (int i = 0; i < mSizeMusicArray; i++){
                    mMusicArrayList.add((Music) bundle.getSerializable(AllSongsFragment.PLAY_SONG + i));
                }
            }
            isFirstTimeGet = false;
        }else{
            getPositionFromIntent(intent);
        }
    }

    public void createNotificationChannel(){
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,getString(R.string.playmusic),NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(true);
            notificationChannel.setDescription(getString(R.string.notifiplaymusic));
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null,null);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }
    public void updateNotification(Music music){
        if(music.getmMusicLogo() != null){
            notifyDefault.setImageViewBitmap(R.id.mNotificationLogo
                    ,Bitmap.createScaledBitmap(music.getmMusicLogo()
                            ,AllSongsFragment.SIZE_LOGO_MINI,AllSongsFragment.SIZE_LOGO_MINI,false) );
        }
        if (isPlaying){
            notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_pause_notification);
        }else{
            notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_play_notification);
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    public void sendNotification(Music music) {
        createNotificationChannel();
        this.musicPlay = music;
        notifyDefault = new RemoteViews(getPackageName(),R.layout.notification_default);
        if(music.getmMusicLogo() != null){
            notifyDefault.setImageViewBitmap(R.id.mNotificationLogo
                    ,Bitmap.createScaledBitmap(music.getmMusicLogo()
                            ,AllSongsFragment.SIZE_LOGO_MINI,AllSongsFragment.SIZE_LOGO_MINI,false) );
        }
        if (isPlaying){
            notifyDefault.setOnClickPendingIntent(R.id.mNotificationPauseButton, getPendingIntent(this,ACTION_PAUSE));
            notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_pause_notification);
        }else{
            notifyDefault.setOnClickPendingIntent(R.id.mNotificationPauseButton, getPendingIntent(this,ACTION_RESUME));
            notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_play_notification);
        }
        notifyDefault.setOnClickPendingIntent(R.id.mNotificationPreButton,getPendingIntent(this,ACTION_PRE));
        notifyDefault.setOnClickPendingIntent(R.id.mNotificationNextButton,getPendingIntent(this,ACTION_NEXT));
        notifyDefault.setOnClickPendingIntent(R.id.mNotificationBigStyle,getPendingIntent(this,ACTION_UPDATE_NOTIFICATION));
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_album)
                .setCustomContentView(notifyDefault)
                .build();
        startForeground(1,notification);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void sendBigStyleNotification(Music music) {
        music = musicPlay;
        Intent intent = new Intent(this,ActivityMusic.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        notifyDefault = new RemoteViews(getPackageName(),R.layout.notification_big_style);
        if(music.getmMusicLogo() != null){
            int SIZE_LOGO_BIG_STYLE = 232;
            notifyDefault.setImageViewBitmap(R.id.mNotificationLogo
                    ,Bitmap.createScaledBitmap(music.getmMusicLogo()
                            , SIZE_LOGO_BIG_STYLE, SIZE_LOGO_BIG_STYLE,false) );
        }
        notifyDefault.setTextViewText(R.id.NameOfAlbumn, music.getmAlbumName());
        notifyDefault.setTextViewText(R.id.NameOfSong, music.getmMusicName());
        if (isPlaying){
            notifyDefault.setOnClickPendingIntent(R.id.mNotificationPauseButton, getPendingIntent(this,ACTION_PAUSE));
            notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_pause_notification);
        }else{
            notifyDefault.setOnClickPendingIntent(R.id.mNotificationPauseButton, getPendingIntent(this,ACTION_RESUME));
            notifyDefault.setImageViewResource(R.id.mNotificationPauseButton,R.drawable.ic_play_notification);
        }
        notifyDefault.setOnClickPendingIntent(R.id.mNotificationPreButton,getPendingIntent(this,ACTION_PRE));
        notifyDefault.setOnClickPendingIntent(R.id.mNotificationNextButton,getPendingIntent(this,ACTION_NEXT));
        notifyDefault.setOnClickPendingIntent(R.id.mNotificationBigStyle,pendingIntent);
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_album)
                .setCustomContentView(notifyDefault)
                .build();
        startForeground(1,notification);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getPendingIntent(Context context, int action){
        Intent intent = new Intent(this,MyBroadcastReceiver.class);
        intent.putExtra(ActivityMusic.ACTION_MUSIC,action);
        return PendingIntent.getBroadcast(context.getApplicationContext(),action,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setSeekBar(){
        sendActionToActivity(String.valueOf(R.string.ACTION_SETUP_SEEKBAR));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void playSong() {
        mMediaPlayer = new MediaPlayer();
        serviceState = true;
        isPause = false;
//        ActivityMusic.allSongsFragment.getMusicAdapter().setPosition(mMusicPosition);
//        ActivityMusic.allSongsFragment.countPlayToDatabase(mMusicPosition);
//        ActivityMusic.allSongsFragment.getMusicAdapter().notifyDataSetChanged();
        sendToActivity(mMusicPosition);
        try {
            mMediaPlayer.setDataSource(mMusicArrayList.get(mMusicPosition).getmMusicData());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            setSeekBar();
            isPlaying = true;
//            ActivityMusic.allSongsFragment.setUIWhenPlayingMusic(mMusicPosition);
            sendToActivity(mMusicPosition);
//            setWhenSongFavourite();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (!isRepeateOne){
                if (mMusicPosition == mMusicArrayList.size() - 1){
                    mMusicPosition = 0;
//                    ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
                    sendToActivity(mMusicPosition);
                }else{
                    mMusicPosition++;
//                    ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
                    sendToActivity(mMusicPosition);
                }
            }
            if (isShuffleOn){
                Random rd = new Random();
                mMusicPosition = rd.nextInt(mSizeMusicArray - 1);
            }
            sendToActivity(mMusicPosition);
            playSong();
        });
    }
    private boolean isPause;
    public boolean getIsPause(){
        return isPause;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void pauseSong(){
        if (mMediaPlayer != null && isPlaying){
            isPlaying = false;
            isPause = true;
//            ActivityMusic.allSongsFragment.getMusicAdapter().notifyDataSetChanged();
            mMediaPlayer.pause();
            sendToActivity(mMusicPosition);
//            ActivityMusic.allSongsFragment.setPanelViewWhenSelectedMusic(mMusicPosition);
//            ActivityMusic.mediaPlaybackFragment.setUpState();
            sendActionToActivity(String.valueOf(R.string.ACTION_SETUP_STATE));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resumeSong(){
        if (mMediaPlayer != null && !isPlaying){
            isPlaying = true;
            isPause = false;
//            ActivityMusic.allSongsFragment.getMusicAdapter().notifyDataSetChanged();
            mMediaPlayer.start();
            sendToActivity(mMusicPosition);
//            ActivityMusic.allSongsFragment.setPanelViewWhenSelectedMusic(mMusicPosition);
//            ActivityMusic.mediaPlaybackFragment.setUpState();
            sendActionToActivity(String.valueOf(R.string.ACTION_SETUP_STATE));
        }
    }

    public void stopSong(){
        if (isPlaying){
            isPlaying = false;
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    public void stopSong(MediaPlayer mediaPlayer){
        if (isPlaying){
            isPlaying = false;
            mediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    public void playPrevious(){
        stopSong();
        if (MediaPlaybackFragment.mSecondPlaying >= 3){
            playSong();
//            ActivityMusic.allSongsFragment.setPanelViewWhenSelectedMusic(mMusicPosition);
//            ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
            sendToActivity(mMusicPosition);
        }else{
            if (mMusicPosition == 0){
                mMusicPosition = mMusicArrayList.size() - 1;
//                ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
                sendToActivity(mMusicPosition);
            }else{
                mMusicPosition--;
//                ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
                sendToActivity(mMusicPosition);
            }
//            ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
            sendToActivity(mMusicPosition);
            playSong();
//            ActivityMusic.allSongsFragment.setPanelViewWhenSelectedMusic(mMusicPosition);
        }
    }

    public void playNext(){
        stopSong();
        if (mMusicPosition == mMusicArrayList.size() - 1){
            mMusicPosition = 0;
        }else{
            mMusicPosition++;
//            ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
        }
//        ActivityMusic.allSongsFragment.setMPosition(mMusicPosition);
        sendToActivity(mMusicPosition);
        playSong();
//        ActivityMusic.allSongsFragment.setPanelViewWhenSelectedMusic(mMusicPosition);
    }


    private String sendToActivity = "send_to_activity";
    public static String sendCommand = "send_command";

    private final transient Intent intent = new Intent(sendToActivity);
    private transient Bundle bundle = new Bundle();

    private void sendToActivity(int mPosition){
        bundle.putInt(getString(R.string.position),mPosition);
        bundle.putBoolean(getString(R.string.state),serviceState);
        bundle.putBoolean(getString(R.string.ispause),isPause);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendActionToActivity(String action){
        bundle.putInt(getString(R.string.actionmediaplayback),Integer.parseInt(action));
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}