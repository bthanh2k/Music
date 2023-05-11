package buiduythanh.edu.music;
import static android.util.Log.d;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import buiduythanh.edu.model.Music;


public class MediaPlaybackFragment extends Fragment implements Serializable{
    private View view;
    private SeekBar mSeekBar;
    private TextView mTxtMusicNamePlay;
    private TextView mTxtAlbumNamePlay;
    private TextView mTxtTimeEnd;
    private TextView mTxtTimeProgress;
    private ImageButton mBtnBack;
    private ImageView mImgLogoMini;
    private ImageView mImgLogoBig;
    private ImageButton mBtnPrevious;
    private ImageButton mBtnPause;
    private ImageButton mBtnNext;
    private ImageButton mBtnRepeat;
    private ImageButton mBtnShuffle;
    private ImageButton mBtnLike;
    private ImageButton mBtnDisLike;
    private Runnable mRunnableSeekBar;
    private final Handler handler = new Handler();
    private int REPEAT_MODE = 1;
    private final int REPEAT_END = 3;
    private ActivityMusic mActivityMusic;
    private Music lastMusic;

    public void setLastMusic(Music music){
        this.lastMusic = music;
    }

    public static int mSecondPlaying;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        defineViews();
        addEvents();
        return view;
    }

    private void addEvents() {
        mBtnBack.setOnClickListener(view -> {
            if(getFragmentManager() != null){
                getFragmentManager().popBackStack();
                ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
                actionBar.show();
            }
        });
        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityMusic.getAllSongsFragment().getMediaPlaybackService().playPrevious();
                mActivityMusic.getAllSongsFragment().updateImageNotification();
            }
        });
        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsPlaying()){
                    mActivityMusic.getAllSongsFragment().getMediaPlaybackService().pauseSong();
                    mActivityMusic.getAllSongsFragment().updateImageNotification();
                    mBtnPause.setImageResource(R.drawable.ic_play_circle);
                }else{
                    mBtnPause.setImageResource(R.drawable.ic_pause_circle);
                    mActivityMusic.getAllSongsFragment().getMediaPlaybackService().resumeSong();
                    mActivityMusic.getAllSongsFragment().updateImageNotification();
                }
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityMusic.getAllSongsFragment().getMediaPlaybackService().playNext();
                mActivityMusic.getAllSongsFragment().updateImageNotification();
            }
        });
        mBtnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (REPEAT_MODE >= REPEAT_END){
                    REPEAT_MODE = 1;
                }else{
                    REPEAT_MODE++;
                }
                switch (REPEAT_MODE){
                    case 2:
                        mBtnRepeat.setImageResource(R.drawable.ic_repeat_one);
                        mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsRepeateOne(true);
                        break;
                    case 3:
                        mBtnRepeat.setImageResource(R.drawable.ic_repeat_end);
                        mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsRepeateOne(false);
                        mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsRepeateAll(true);
                        break;
                    case 1:
                        mBtnRepeat.setImageResource(R.drawable.ic_repeat);
                        mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsRepeateOne(false);
                        break;
                }
            }
        });
        mBtnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsShuffleOn()){
                    mBtnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                    mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsShuffleOn(true);
                }else{
                    mBtnShuffle.setImageResource(R.drawable.ic_shuffle);
                    mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsShuffleOn(false);
                }
            }
        });
        mBtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSongIsLike();
                mActivityMusic.getAllSongsFragment().setLikeSong();
            }
        });
        mBtnDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSongIsDislike();
                mActivityMusic.getAllSongsFragment().setDisLikeSong();
            }
        });
    }

    private boolean shuffleOn;
    public boolean getShuffleOn(){
        return shuffleOn;
    }
    public void setShuffleIsOn(){
        mBtnShuffle.setImageResource(R.drawable.ic_shuffle_on);
        shuffleOn = true;
        mActivityMusic.getAllSongsFragment().setIsOldShuffleOn(true);
    }

    public void setRepeateOne(){
        mBtnRepeat.setImageResource(R.drawable.ic_repeat_one);
        mActivityMusic.getAllSongsFragment().setIsRepeatOne(true);
    }

    public void setRepeateAll(){
        mBtnRepeat.setImageResource(R.drawable.ic_repeat_end);
    }
    public void setUpView(Music music) {
        // Lấy bài hát được chọn và setup lên các controls:
        mTxtMusicNamePlay.setText(music.getmMusicName());
        mTxtAlbumNamePlay.setText(music.getmAlbumName());
        mTxtTimeEnd.setText(music.getmMusicDuraTion());
        if (music.getmMusicLogo() != null){
            mImgLogoBig.setImageBitmap(music.getmMusicLogo());
            mImgLogoMini.setImageBitmap(Bitmap.createScaledBitmap(music.getmMusicLogo()
                    ,AllSongsFragment.SIZE_LOGO_MINI,AllSongsFragment.SIZE_LOGO_MINI,false));
        }else{
            mImgLogoMini.setImageResource(R.drawable.ic_musicnode);
            mImgLogoBig.setImageResource(R.drawable.ic_musicnode);
        }
        setUpState();
    }

    public void setUpState(){
        if (mActivityMusic.getServiceState()){
            if (mActivityMusic.getServiceRestore().getIsPlaying()){
                mBtnPause.setImageResource(R.drawable.ic_pause_circle);
            }else{
                mBtnPause.setImageResource(R.drawable.ic_play_circle);
            }
        }else{
            if (mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsPlaying()){
                mBtnPause.setImageResource(R.drawable.ic_pause_circle);
            }else{
                mBtnPause.setImageResource(R.drawable.ic_play_circle);
            }
        }
    }

    public void setSongIsLike(){
        mBtnLike.setImageResource(R.drawable.ic_is_like);
        mBtnDisLike.setImageResource(R.drawable.ic_dislike);
    }

    public void setSongIsDislike(){
        mBtnLike.setImageResource(R.drawable.ic_like);
        mBtnDisLike.setImageResource(R.drawable.ic_is_dislike);
    }

    public void setDefFavourite(){
        mBtnLike.setImageResource(R.drawable.ic_like);
        mBtnDisLike.setImageResource(R.drawable.ic_dislike);
    }

    public static String REMOVE_SEEKBAR = "remove_seekbar";

    public void removeSeekBar(){
        if (mRunnableSeekBar != null){
            handler.removeCallbacks(mRunnableSeekBar);
        }
    }

    public void setUpSeekBar(MediaPlayer mediaPlayer){
        if (mRunnableSeekBar != null){
            handler.removeCallbacks(mRunnableSeekBar);
        }
        mSeekBar.setMax(mediaPlayer.getDuration());
        mRunnableSeekBar = new Runnable() {
            @Override
            public void run() {
                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(mCurrentPosition);
                handler.postDelayed(this, 1000);
            }
        };
        mRunnableSeekBar.run();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progressValue);
                }
                final long mMinutes = (progressValue / 1000) / 60;
                String mSeconds = String.valueOf(((progressValue / 1000) % 60));
                if (Long.parseLong(mSeconds) < 10) {
                    mSeconds = "0" + mSeconds;
                }
                mSecondPlaying = Integer.parseInt(mSeconds);
                mTxtTimeProgress.setText(mMinutes + ":" + mSeconds);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void defineViews() {
        mTxtMusicNamePlay = view.findViewById(R.id.mMusicName);
        mTxtAlbumNamePlay = view.findViewById(R.id.mAlbumName);
        mTxtTimeEnd = view.findViewById(R.id.mTxtTimeEnd);
        mBtnBack = view.findViewById(R.id.mBtnBack);
        mBtnPrevious = view.findViewById(R.id.mBtnPrevious);
        mBtnPause = view.findViewById(R.id.mBtnPlayBig);
        mBtnNext = view.findViewById(R.id.mBtnNext);
        mSeekBar = view.findViewById(R.id.mSbMusicDuration);
        mBtnRepeat = view.findViewById(R.id.mBtnRepeat);
        mBtnShuffle = view.findViewById(R.id.mBtnShuffle);
        mBtnLike = view.findViewById(R.id.mBtnLike);
        mBtnDisLike = view.findViewById(R.id.mBtnDislike);
        mTxtTimeProgress = view.findViewById(R.id.mTxtTimeProgress);
        mActivityMusic = (ActivityMusic) getActivity();
        if (mActivityMusic.getServiceRestore() != null){
            mTxtTimeProgress.setText(mActivityMusic.getServiceRestore().returnCurrentPosition());
        }
        mImgLogoMini = view.findViewById(R.id.mMusicLogoMini);
        mImgLogoBig = view.findViewById(R.id.mImgLogoBig);
        SharedPreferences preferences = requireActivity().getSharedPreferences(ActivityMusic.SAVE_FIRSTIME, Context.MODE_PRIVATE);
        if (preferences.getBoolean(ActivityMusic.IS_SHUFFLE,false)){
            setShuffleIsOn();
        }
        if (preferences.getBoolean(ActivityMusic.IS_REPEATE_ONE,false)){
            setRepeateOne();
        }
        if (preferences.getBoolean(ActivityMusic.IS_REPEATE_ALL,false)){
            setRepeateAll();
        }

    }
}