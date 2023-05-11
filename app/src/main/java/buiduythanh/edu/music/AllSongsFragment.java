package buiduythanh.edu.music;

import static android.util.Log.d;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import buiduythanh.edu.adapter.MusicListAdapter;
import buiduythanh.edu.database.FavouriteSongsDatabase;
import buiduythanh.edu.model.Music;

public class AllSongsFragment extends BaseSongListFragment implements MusicListAdapter.ISendMusicSelected, MusicListAdapter.onItemClick,
        Serializable, MusicListAdapter.sendCommandFavourite, SendInfor {
    private View view;
    private RecyclerView mRecyclerView;
    private MusicListAdapter mMusicAdapter;
    private LinearLayout mSlideUp;

    private ImageButton mButtonPause;
    private TextView mMusicNamePlaying;
    private TextView mAlumNamePlaying;

    private ImageView mMusicLogoPlaying;

    private Music mMusicSelected;

    private ArrayList<Music> mMusicArray;
    private boolean isFirstTimeSend;
    private int mPosition;

    public static int SIZE_LOGO_MINI = 80;
    public static String PLAY_SONG = "play_song";
    public static String POSITION_SELECTED = "position_song";
    public static String SIZE_MUSIC_ARRAY = "size_of_music_array";
    private static final String AUTHORITY = "buiduythanh.edu.music.FavouriteSongsProvider";
    private static final String SONG_TABLE = "FavouriteSongs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SONG_TABLE);


    private Intent intent;
    private MediaPlaybackService mMediaPlaybackService;
    private boolean isServiceConnected;
    private boolean isOldShuffleOn;
    private boolean isRepeatOne;
    private boolean isRestoreApp;
    public void setIsRestoreApp(boolean b){
        this.isRestoreApp = b;
    }
    public void setIsOldShuffleOn(boolean b){
        this.isOldShuffleOn = b;
    }

    public void setIsRepeatOne(boolean b){
        this.isRepeatOne = b;
    }

    public static final String IS_LIKE = "islike";
    public static final String IS_DISLIKE = "isdislike";

    public MediaPlaybackService getMediaPlaybackService(){
        return mMediaPlaybackService;
    }

    @Override
    public void sendCommand(String s, int position) {
        if (s.equals(IS_LIKE)){
            setLikeSongPosition(position);
        }else if (s.equals(IS_DISLIKE)){
            setDisLikeSongPosition(position);
        }
    }

    @Override
    public void sendCommand(String command) {
        if (command.equals(MediaPlaybackFragment.REMOVE_SEEKBAR)){
            mActivityMusic.getMediaPlaybackFragment().removeSeekBar();
        }
    }

    public interface sendMusic{
        void sendMusic(Music music);
    }
    private sendMusic iSendMusic;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        iSendMusic = this::sendMusicSelected;
    }

    public MusicListAdapter getMusicAdapter(){
        return mMusicAdapter;
    }

    public boolean getIsServiceConnected(){
        return isServiceConnected;
    }

    public void setMPosition(int p){
        this.mPosition = p;
        if (p == mMusicArray.size()){
            p--;
        }
        mMediaPlaybackService.sendNotification(mMusicArray.get(p));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        super.readDataWhenResume(mMusicArray);
        mMusicAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public AllSongsFragment() {
    }

    @Override
    public void sendMusicSelected(Music music) {
        mMusicSelected = new Music();
        // Lấy bài hát được chọn từ MusicListAdapter:
        mMusicSelected = music;
        // Chuyển music được chọn đến Mediaplayback Fragment
        iSendMusic.sendMusic(mMusicArray.get(mPosition));
    }

    public void setIsFirstTimeSend(boolean isFirstTimeSend){
        this.isFirstTimeSend = isFirstTimeSend;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private int lastPosition;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        // Khai báo các view:
        defineViews();
        requireActivity().bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);
        // Xử lý events click panel:
        addEventsOnClickViews();
        SharedPreferences preferences = requireContext().getSharedPreferences(ActivityMusic.SAVE_FIRSTIME, Context.MODE_PRIVATE);
        lastPosition = preferences.getInt(getString(R.string.positionlast),-1);
        if (lastPosition > -1 && mMusicArray.size() > 0){
            setUIWhenRestoreApp(lastPosition);
            isRestoreApp = preferences.getBoolean(ActivityMusic.IS_RESTORE,false);
        }
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUIWhenRestoreApp(int lastPosition) {
        setPanelRestoreApp(lastPosition);
        isFirstTimeSend = false;
    }

    private void setPanelRestoreApp(int lastPosition) {
        if (!mActivityMusic.isPortrait(requireActivity())){
            mSlideUp.setVisibility(View.INVISIBLE);
        }else{
            mSlideUp.setVisibility(View.VISIBLE);
        }
        mMusicNamePlaying.setText(mMusicArray.get(lastPosition).getmMusicName());

        mButtonPause.setImageResource(R.drawable.ic_play);
        mAlumNamePlaying.setText(mMusicArray.get(lastPosition).getmAlbumName());

        if (mMusicArray.get(lastPosition).getmMusicLogo() != null){
            mMusicLogoPlaying.setImageBitmap(Bitmap.createScaledBitmap(mMusicArray.get(lastPosition).getmMusicLogo(),SIZE_LOGO_MINI,SIZE_LOGO_MINI,false));
        }else{
            mMusicLogoPlaying.setImageResource(R.drawable.ic_musicnode);
        }
    }

    private void addEventsOnClickViews() {
        handleSetOnClickViewPanel();
        mButtonPause.setOnClickListener(view -> {
            if (isRestoreApp){
                handleStartPlayMusic(lastPosition);
                isRestoreApp = false;
            }else{
                if(mMediaPlaybackService.getIsPlaying()){
                    if (isServiceConnected){
                        mMediaPlaybackService.pauseSong();
                        mMediaPlaybackService.setIsPlaying(false);
                        mMediaPlaybackService.updateNotification(mMusicArray.get(mPosition));
                        mButtonPause.setImageResource(R.drawable.ic_play);
                    }
                }else{
                    if (isServiceConnected){
                        mMediaPlaybackService.resumeSong();
                        mMediaPlaybackService.setIsPlaying(true);
                        mMediaPlaybackService.updateNotification(mMusicArray.get(mPosition));
                        mButtonPause.setImageResource(R.drawable.ic_pause);
                    }
                }
            }
        });
        // Xử lý sự kiện OnClick item RecyclerView:
        mMusicAdapter.setOnClick(this);
    }
    private ActivityMusic mActivityMusic;
    private void defineViews() {
        mRecyclerView = view.findViewById(R.id.mRecyclerView);
        mMusicArray = new ArrayList<>();
        intent = new Intent(getContext(),MediaPlaybackService.class);
        isFirstTimeSend = true;
        mActivityMusic = (ActivityMusic) getActivity();
        // Thiết lập Panel:
        mSlideUp = view.findViewById(R.id.mSlideUpLayout);
        mSlideUp.setVisibility(View.GONE);

        // Thiết lập RecyclerView music
        mMusicAdapter = new MusicListAdapter(getContext(),mMusicArray, this,this,this);
        mRecyclerView.setAdapter(mMusicAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Ánh xạ các View trong ô SlidingUpPanel:
        mButtonPause = view.findViewById(R.id.mBtnStatus);
        mMusicNamePlaying = view.findViewById(R.id.mMusicNamePlay);
        mAlumNamePlaying = view.findViewById(R.id.mAlbumNamePlay);
        mMusicLogoPlaying = view.findViewById(R.id.mMusicLogoMini);
        // Đọc dữ liệu từ bộ nhớ vào mMusicArray:
        super.readDataFromExternalStorage(mMusicArray);
    }

    public void handleOpenMediaPlaybackFragment(){
        if (isRestoreApp){
            handleStartPlayMusic(lastPosition);
            mActivityMusic.getMediaPlaybackFragment().setUpView(mMusicArray.get(lastPosition));
            // Mở Fragment MediaPlayback:
            mActivityMusic.goToMediaPlaybackFragment();
            isRestoreApp = false;
        }else{
            mActivityMusic.getMediaPlaybackFragment().setUpView(mMusicArray.get(mMediaPlaybackService.returnMusicPosition()));
            mActivityMusic.getMediaPlaybackFragment().setUpSeekBar(mMediaPlaybackService.getMediaPlayer());
            // Mở Fragment MediaPlayback:
            mActivityMusic.goToMediaPlaybackFragment();
        }
    }

    private void handleSetOnClickViewPanel() {
        mSlideUp.setOnClickListener(view -> handleOpenMediaPlaybackFragment());
    }

    @Override
    public void onItemClick(int position) {
        isRestoreApp = false;
        handleStartPlayMusic(position);
    }

    private void handleStartPlayMusic(int position){
        if (mActivityMusic.getSongStateRestore()){
            mActivityMusic.getServiceRestore().stopSong(mActivityMusic.getServiceRestore().getMediaPlayer());
            mActivityMusic.setSongStateRestore(false);
            mMediaPlaybackService.setServiceState(false);
        }
        mPosition = position;
        sendInformationToService(position);
        updateImageNotification();
        mMediaPlaybackService.setIsPlaying(true);
        mMediaPlaybackService.setIsShuffleOn(isOldShuffleOn);
        mMediaPlaybackService.setIsRepeateOne(isRepeatOne);
    }

    public void setLikeSongPosition(int position){
        mActivityMusic.getDatabase().setSongFavourite(position,2);
        mActivityMusic.getFavouriteSongsFragment().addSongToArray(position);
    }

    public void setDisLikeSongPosition(int position){
        mActivityMusic.getDatabase().setSongFavourite(position,1);
        mActivityMusic.getFavouriteSongsFragment().removeSong(position);
    }

    public void setLikeSong(){
        mActivityMusic.getDatabase().setSongFavourite(mPosition,2);
        mActivityMusic.getFavouriteSongsFragment().addSongToArray(mPosition);
    }

    public void setDisLikeSong(){
        mActivityMusic.getDatabase().setSongFavourite(mPosition,1);
        mActivityMusic.getFavouriteSongsFragment().removeSong(mPosition);
    }

    public void countPlayToDatabase(int position) {
        Cursor cursor = requireContext().getContentResolver().query(CONTENT_URI
                ,null, null,
                null,null);
        ContentValues valueClick = new ContentValues();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            if (cursor.getInt(1) == position) {
                valueClick.put(FavouriteSongsDatabase.KEY_COUNT_OF_PLAY, cursor.getInt(3) + 1);
            }
            cursor.moveToNext();
        }
        cursor.close();
        int update = requireContext().getContentResolver().update(CONTENT_URI, valueClick,
                FavouriteSongsDatabase.KEY_ID_PROVIDER  + " = " + position, null);
        if (mActivityMusic.getDatabase().getSongCountOfPlay(position) >= 3 && mActivityMusic.getDatabase().getSongIsFavourite(position) != 1){
            mActivityMusic.getDatabase().setSongFavourite(position,2);
            mActivityMusic.getFavouriteSongsFragment().addSongToArray(mPosition);
        }
    }

    public void updateImageNotification(){
        mMediaPlaybackService.sendNotification(mMusicArray.get(mPosition));
        setPanelViewWhenSelectedMusic(mPosition);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlaybackService.MyBinder myBinder = (MediaPlaybackService.MyBinder) iBinder;
            mMediaPlaybackService = myBinder.getMediaPlaybackService();
            mMediaPlaybackService.setSendInfor(AllSongsFragment.this);
            isServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMediaPlaybackService = null;
            isServiceConnected = false;
        }
    };
    private boolean isReloadMusic;
    public void setIsReloadMusic(boolean b){
        this.isReloadMusic = b;
    }
    public boolean getIsReloadMusic(){
        return isReloadMusic;
    }

    public void sendInformationToService(int position) {
        if (isFirstTimeSend){
            if (mMediaPlaybackService.getServiceState()){
                mMediaPlaybackService.stopSong();
            }
            if (isReloadMusic && mMediaPlaybackService.getIsPlaying()){
                mMediaPlaybackService.stopSong();
            }
            mMediaPlaybackService.setIsFirstTimeGet(true);
            for(int i = 0; i < mMusicArray.size(); i++){
                intent.putExtra(PLAY_SONG + i, mMusicArray.get(i));
            }
            intent.putExtra(SIZE_MUSIC_ARRAY,mMusicArray.size());
            intent.putExtra(POSITION_SELECTED,position);
            requireActivity().startService(intent);
            isFirstTimeSend = false;
        } else{
            if (!mActivityMusic.getSongStateRestore()){
                if (mMediaPlaybackService != null){
                    if (mMediaPlaybackService.getMediaPlayer() != null){
                        mMediaPlaybackService.stopSong();
                    }else{
                        isFirstTimeSend = true;
                        sendInformationToService(position);
                        return;
                    }
                }
            }
            intent.putExtra(POSITION_SELECTED,position);
            mMediaPlaybackService.getPositionFromIntent(intent);
            mMediaPlaybackService.playSong();
        }
    }

    public void setUIWhenPlayingMusic(int mPosition){
        mActivityMusic.getMediaPlaybackFragment().setUpView(mMusicArray.get(mPosition));
        if (mPosition == mMusicArray.size()){
            mPosition--;
        }
        setPanelViewWhenSelectedMusic(mPosition);
    }

    public void setPanelViewWhenSelectedMusic(int position){
        if (!mActivityMusic.isPortrait(requireActivity())){
                mSlideUp.setVisibility(View.GONE);
        }else{
            mSlideUp.setVisibility(View.VISIBLE);
        }
        mMusicNamePlaying.setText(mMusicArray.get(position).getmMusicName());
        mButtonPause.setImageResource(R.drawable.ic_pause);
        if (mActivityMusic.getServiceState()){
            if (!mActivityMusic.getServiceRestore().getIsPlaying()){
                mButtonPause.setImageResource(R.drawable.ic_play);
            }
        }else{
            if (!mMediaPlaybackService.getIsPlaying()){
                mButtonPause.setImageResource(R.drawable.ic_play);
            }
        }
        mAlumNamePlaying.setText(mMusicArray.get(position).getmAlbumName());
        if (mMusicArray.get(position).getmMusicLogo() != null){
            mMusicLogoPlaying.setImageBitmap(Bitmap.createScaledBitmap(mMusicArray.get(position).getmMusicLogo(),SIZE_LOGO_MINI,SIZE_LOGO_MINI,false));
        }else{
            mMusicLogoPlaying.setImageResource(R.drawable.ic_musicnode);
        }
    }
}