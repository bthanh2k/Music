package buiduythanh.edu.music;
import static android.util.Log.d;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import buiduythanh.edu.adapter.MusicListAdapter;
import buiduythanh.edu.model.Music;

public class FavouriteSongsFragment extends BaseSongListFragment implements MusicListAdapter.onItemClick{
    private ArrayList<Music> mMusicArray;
    private ArrayList<Music> mListFavourite;
    private RecyclerView mRecyclerView;
    private MusicListAdapter mAdapter;
    private LinearLayout mSlideUpFavourite;
    private TextView mNameOfSong;
    private TextView mNameOfAlbumn;
    private ImageButton mPauseOrResume;
    private ImageView mLogoOfSong;
    private ActivityMusic mActivityMusic;

    public FavouriteSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        super.readDataWhenResume(mMusicArray);
        mAdapter.notifyDataSetChanged();
    }

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_favourite_songs, container, false);
        defineViews();
        addEventClick();
        return view;
    }

    private void addEventClick() {
        mAdapter.setOnClick(this);
        mPauseOrResume.setOnClickListener(view -> {
            if(mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsPlaying()){
                if (mActivityMusic.getAllSongsFragment().getIsServiceConnected()){
                    mActivityMusic.getAllSongsFragment().getMediaPlaybackService().pauseSong();
                    mActivityMusic.getAllSongsFragment().updateImageNotification();
                    mPauseOrResume.setImageResource(R.drawable.ic_play);
                }
                mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsPlaying(false);
            }else{
                if (mActivityMusic.getAllSongsFragment().getIsServiceConnected()){
                    mActivityMusic.getAllSongsFragment().getMediaPlaybackService().resumeSong();
                    mActivityMusic.getAllSongsFragment().updateImageNotification();
                    mPauseOrResume.setImageResource(R.drawable.ic_pause);
                }
                mActivityMusic.getAllSongsFragment().getMediaPlaybackService().setIsPlaying(true);
            }
        });
        mSlideUpFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityMusic mActivityMusic = (ActivityMusic) getActivity();
                // Mở Fragment MediaPlayback:
                assert mActivityMusic != null;
                mActivityMusic.openMediaPlayBackFromFavouriteSong();
            }
        });
    }

    private void defineViews() {
        mMusicArray = new ArrayList<>();
        mListFavourite = new ArrayList<>();
        mActivityMusic = (ActivityMusic) getActivity();
        mAdapter = new MusicListAdapter(getContext(),mListFavourite);

        mRecyclerView = view.findViewById(R.id.mRecyclerViewFavourite);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSlideUpFavourite = view.findViewById(R.id.mSlideUpLayoutFavourite);
        mSlideUpFavourite.setVisibility(View.INVISIBLE);
        // Ánh xạ các view trong ô panel:
        mNameOfSong = view.findViewById(R.id.mMusicNamePlayFavourite);
        mNameOfAlbumn = view.findViewById(R.id.mAlbumNamePlayFavourite);
        mPauseOrResume = view.findViewById(R.id.mBtnStatusFavourite);
        mLogoOfSong = view.findViewById(R.id.mMusicLogoMiniFavourite);

        super.readDataFromExternalStorage(mMusicArray);
        if (mMusicArray.size() > 0){
            addSongsFromdatabase();
            sortArrayList();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshRecyclerView(){
        mAdapter.notifyDataSetChanged();
    }

    public MusicListAdapter getAdapter(){
        return mAdapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortArrayList(){
        Music music = new Music();
        for (int i = 0; i < mListFavourite.size() - 1; i++){
            for (int j = i + 1; j < mListFavourite.size(); j++){
                if(mListFavourite.get(i).getmMusicId() > mListFavourite.get(j).getmMusicId()){
                    music = mListFavourite.get(i);
                    mListFavourite.set(i,mListFavourite.get(j));
                    mListFavourite.set(j,music);
                }else if (mListFavourite.get(i).getmMusicId() == mListFavourite.get(j).getmMusicId()){
                    mListFavourite.remove(mListFavourite.get(i));
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void returnPositionWhenPlaying(){
        int tam = -1;
        for (int i = 0; i < mListFavourite.size(); i++){
            if (mListFavourite.get(i).getmMusicId()
                    == mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIdMusicWhenPlaying()){
                tam = i;
            }
        }
        mAdapter.setPositionFavouriteSong(tam);
    }

    public void addSongsFromdatabase() {
        Cursor cursor = requireContext().getContentResolver().query(AllSongsFragment.CONTENT_URI
                ,null, null,
                null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            if (cursor.getInt(2) == 2){
                addSongToArray(cursor.getInt(1));
            }
            cursor.moveToNext();
        }
        cursor.close();
        sortArrayList();
    }

    public void addSongToArray(int position){
        boolean isExist = false;
        if (mMusicArray.size() <= 1 && mListFavourite.size() <= 1){
            mListFavourite.add(mMusicArray.get(0));
        }else{
            for (int i = 0; i < mListFavourite.size(); i++){
                if (mListFavourite.get(i).getmMusicId() == position){
                    isExist = true;
                }
            }
            if (!isExist){
                mListFavourite.add(mMusicArray.get(position));
            }
        }
        sortArrayList();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void removeSong(int position){
        for (int i = 0; i < mListFavourite.size(); i++){
            if (i == position){
                mListFavourite.remove(mListFavourite.get(i));
                mActivityMusic.getDatabase().setSongFavourite(position,1);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void setUIWhenPlayMusic(int position){
        setPanelWhenPlayingMusic(position);
    }

    public void setPanelWhenPlayingMusic(int position){
        if (!mActivityMusic.isPortrait(requireActivity())){
            mSlideUpFavourite.setVisibility(View.INVISIBLE);
        }else{
            mSlideUpFavourite.setVisibility(View.VISIBLE);
        }
        mNameOfSong.setText(mMusicArray.get(position).getmMusicName());
        mPauseOrResume.setImageResource(R.drawable.ic_pause);
        if (!mActivityMusic.getAllSongsFragment().getMediaPlaybackService().getIsPlaying()){
            mPauseOrResume.setImageResource(R.drawable.ic_play);
        }
        mNameOfAlbumn.setText(mMusicArray.get(position).getmAlbumName());
        if (mMusicArray.get(position).getmMusicLogo() != null){
            mLogoOfSong.setImageBitmap(Bitmap.createScaledBitmap(mMusicArray.get(position).getmMusicLogo(),AllSongsFragment.SIZE_LOGO_MINI,AllSongsFragment.SIZE_LOGO_MINI,false));
        }else{
            mLogoOfSong.setImageResource(R.drawable.ic_musicnode);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemClick(int position) {
        mActivityMusic.getAllSongsFragment().sendInformationToService(mListFavourite.get(position).getmMusicId());
        mAdapter.setPositionFavouriteSong(position);
        mAdapter.notifyDataSetChanged();
    }
}