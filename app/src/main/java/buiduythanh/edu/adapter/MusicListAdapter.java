package buiduythanh.edu.adapter;
import static android.util.Log.d;

import android.annotation.SuppressLint;
import android.content.Context;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import buiduythanh.edu.model.Music;
import buiduythanh.edu.music.ActivityMusic;
import buiduythanh.edu.music.AllSongsFragment;
import buiduythanh.edu.music.MediaPlaybackService;
import buiduythanh.edu.music.R;
import es.claucookie.miniequalizerlibrary.EqualizerView;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> implements Serializable {
    private ArrayList<Music> mMusicArray;
    private LayoutInflater mInflater;
    private int checkedPosition = -1;
    private Music mMusicSelected;
    private int mPositionService;
    private int positionFavouriteSong;
    private boolean serviceState;
    private boolean isFavouriteSongsFragment;
    private boolean isRestoreApp;
    private int lastPosition;

    public void setIsRestoreApp(boolean b){
        this.isRestoreApp = b;
    }

    public void setLastPosition(int lastPosition){
        this.lastPosition = lastPosition;
    }

    public void setIsFavouriteSongsFragment(boolean b){
        this.isFavouriteSongsFragment = b;
    }

    public boolean getIsFavouriteSongsFragment(){
        return isFavouriteSongsFragment;
    }

    public void setServiceState(boolean b){
        serviceState = b;
    }

    public void setPositionFavouriteSong(int position){
        this.positionFavouriteSong = position;
    }

    public void setPositionService(int position){
        mPositionService = position;
    }
    public interface ISendMusicSelected{
        void sendMusicSelected(Music music);
    }

    public interface onItemClick{
        void onItemClick(int position);
    }

    public interface sendCommandFavourite{
        void sendCommand(String s, int position);
    }

    private static sendCommandFavourite iSendCommandFavourite;
    private ISendMusicSelected mISendMusicSelectedToAllSongs;
    private onItemClick onClick;

    public MusicListAdapter(Context context, ArrayList<Music> mMusicArray) {
        mInflater = LayoutInflater.from(context);
        this.mMusicArray = mMusicArray;
    }

    public MusicListAdapter(Context context, ArrayList<Music> mMusicArray, ISendMusicSelected mISendMusicSelected
            , onItemClick onClick, sendCommandFavourite iSendCommandFavourite) {
        mInflater = LayoutInflater.from(context);
        this.mMusicArray = mMusicArray;
        this.mISendMusicSelectedToAllSongs = mISendMusicSelected;
        this.onClick = onClick;
        this.iSendCommandFavourite = iSendCommandFavourite;
    }

    class MusicViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        private final TextView mMusicSerial;
        private final TextView mMusicName;
        private final TextView mMusicDuration;
        private final EqualizerView mEqualizer;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            mMusicSerial = itemView.findViewById(R.id.mSerial);
            mMusicName = itemView.findViewById(R.id.mMusicName);
            mMusicDuration = itemView.findViewById(R.id.mMusicDuration);
            ImageButton mButtonMore = itemView.findViewById(R.id.mOption);
            mEqualizer = itemView.findViewById(R.id.mEqualizer);
            mButtonMore.setOnClickListener(this::showPopupMenu);
        }

        public void showPopupMenu(View view){
            PopupMenu popupMenu = new PopupMenu(itemView.getContext(),view);
            popupMenu.inflate(R.menu.menu_more);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        public void setItemWhenMusicSelected(){
            checkedPosition = -1;
            mMusicName.setTypeface(null,Typeface.BOLD);
            mMusicSerial.setVisibility(View.INVISIBLE);
            mEqualizer.setVisibility(View.VISIBLE);
            mEqualizer.animateBars();
        }
        public void setItemWhenMusicUnselect(){
            mMusicName.setTypeface(null,Typeface.NORMAL);
            mMusicSerial.setVisibility(View.VISIBLE);
            mEqualizer.stopBars();
            mEqualizer.setVisibility(View.INVISIBLE);
        }

        public void setItemPause(){
            mEqualizer.stopBars();
        }

        public void setItemResume(){
            mEqualizer.animateBars();
        }

        public void handleSelectSingleMusic() {
            if (checkedPosition == -1) {
                setItemWhenMusicUnselect();
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    setItemWhenMusicSelected();
                } else {
                    setItemWhenMusicUnselect();
                }
            }
            itemView.setOnClickListener(view -> {
                itemView.setSelected(true);
                checkedPosition = -1;
                setItemWhenMusicSelected();
                onClick.onItemClick(getAdapterPosition());
                if (checkedPosition != getAdapterPosition()) {
                    notifyItemChanged(checkedPosition);
                }
            });
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String ACTION_REMOVE = "Đã xóa khỏi danh sách yêu thích";
            switch (menuItem.getItemId()){
                case R.id.item_like:
                    if (!isFavouriteSongsFragment){
                        iSendCommandFavourite.sendCommand(AllSongsFragment.IS_LIKE, getAdapterPosition());
                        String ACTION_FAVOURITE = "Đã thêm vào danh sách yêu thích";
                        Toast.makeText(itemView.getContext(), ACTION_FAVOURITE, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.item_remove:
                    iSendCommandFavourite.sendCommand(AllSongsFragment.IS_DISLIKE, getAdapterPosition());
                    Toast.makeText(itemView.getContext(), ACTION_REMOVE, Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    }
    private int oldPosition = -1;
    public void setOldPosition(int position){
        this.oldPosition = position;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(mItemView);
    }
    private boolean isReloadMusic;
    public void setIsReloadMusic(boolean b){
        isReloadMusic = b;
    }

    private int mPositionFromService;

    public void setMPositionFromService(int position){
        this.mPositionFromService = position;
    }

    private String mMusicNameFromService;

    public void setMusicNameFromService(String s){
        this.mMusicNameFromService = s;
    }

    private boolean oldPause;

    public void setOldPause(boolean b){
        this.oldPause = b;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        mMusicSelected = mMusicArray.get(position);
        // +1 vì để hiển thị số thứ tự từ 1:
        holder.mMusicSerial.setText(String.valueOf(mMusicSelected.getmMusicId() + 1));
        holder.mMusicName.setText(mMusicSelected.getmMusicName());
        holder.mMusicDuration.setText(mMusicSelected.getmMusicDuraTion());
        holder.handleSelectSingleMusic();
        // Cập nhật giao diện khi vào lại app:
        if (isRestoreApp){
            checkedPosition = lastPosition;
            holder.handleSelectSingleMusic();
            if (isPause){
                holder.setItemPause();
            }
            isRestoreApp = false;
        }
        // Cập nhật giao diện Recycler View khi ArrayList thay đổi bài hát:
        if (isReloadMusic){
            int tam = -1;
            for (int i = 0; i < mMusicArray.size(); i++){
                if (mMusicArray.get(i).getmMusicData().equals(mMusicNameFromService)){
                    tam = mMusicArray.get(i).getmMusicId();
                }
            }
            mPositionService = tam;
            holder.setItemResume();
            isReloadMusic = false;
        }
        // Cập nhật giao diện FavouriteSong:
        if (isFavouriteSongsFragment){
            if (serviceState){
                checkedPosition = positionFavouriteSong;
                holder.handleSelectSingleMusic();
            }else{
                checkedPosition = positionFavouriteSong;
                holder.handleSelectSingleMusic();
            }
        }
        // Cập nhật item khi chơi bài hát mới AllSongsFragment:
        if (oldPosition != -1 && position == oldPosition){
            holder.setItemWhenMusicSelected();
            mPositionService = oldPosition;
            if (oldPause){
                holder.setItemPause();
            }
            oldPosition = -1;
        }else if (position == mPositionService && serviceState){
            if (oldPosition == -1){
                holder.setItemWhenMusicSelected();
            }
        }
        if (isPause && position == mPositionService){
            holder.setItemPause();
        }else{
            holder.setItemResume();
        }
    }

    private boolean isPause;
    public void setIsPause(boolean b){
        this.isPause = b;
    }

    public void setOnClick(onItemClick onClick){
        this.onClick = onClick;
    }

    @Override
    public int getItemCount() {
        return mMusicArray.size();
    }
}

