package buiduythanh.edu.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Music implements Serializable {
    private int mMusicId;
    private String mMusicData;
    private String mMusicName;
    private String mAlbumName;
    private transient Bitmap mMusicLogo;
    private String mMusicDuraTion;
    private int countClick;
    private int isFavourite;

    public Music() {
        this.mMusicId = -1;
        this.mMusicData = "";
        this.mMusicName = "Tên bài hát";
        this.mAlbumName = "Tên Albumn";
        this.mMusicLogo = null;
        this.mMusicDuraTion = "0:00";
        this.countClick = 0;
        this.isFavourite = 0;
    }

    public Music(int mMusicId){
        this.mMusicId = mMusicId;
    }

    public Music(int mMusicId, String mMusicData, String mMusicName, String mAlbumName,
                 Bitmap mMusicLogo, String mMusicDuraTion, int countClick, int isFavourite) {
        this.mMusicId = mMusicId;
        this.mMusicData = mMusicData;
        this.mMusicName = mMusicName;
        this.mAlbumName = mAlbumName;
        this.mMusicLogo = mMusicLogo;
        this.mMusicDuraTion = mMusicDuraTion;
        this.countClick = countClick;
        this.isFavourite = isFavourite;
    }

    public int getmMusicId() {
        return mMusicId;
    }

    public void setmMusicId(int mMusicId) {
        this.mMusicId = mMusicId;
    }


    public String getmMusicName() {
        return mMusicName;
    }

    public void setmMusicName(String mMusicName) {
        this.mMusicName = mMusicName;
    }

    public String getmAlbumName() {
        return mAlbumName;
    }

    public void setmAlbumName(String mAlbumName) {
        this.mAlbumName = mAlbumName;
    }

    public String getmMusicData() {
        return mMusicData;
    }

    public void setmMusicData(String mMusicData) {
        this.mMusicData = mMusicData;
    }

    public Bitmap getmMusicLogo() {
        return mMusicLogo;
    }

    public void setmMusicLogo(Bitmap mMusicLogo) {
        this.mMusicLogo = mMusicLogo;
    }

    public String getmMusicDuraTion() {
        return mMusicDuraTion;
    }

    public void setmMusicDuraTion(String mMusicDuraTion) {
        this.mMusicDuraTion = mMusicDuraTion;
    }

    public int getCountClick() {
        return countClick;
    }

    public void setCountClick(int countClick) {
        this.countClick = countClick;
    }

    public int getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(int isFavourite) {
        this.isFavourite = isFavourite;
    }
}
