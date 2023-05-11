package buiduythanh.edu.music;

import static android.util.Log.d;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import buiduythanh.edu.database.FavouriteSongsDatabase;
import buiduythanh.edu.model.Music;

public class ActivityMusic extends AppCompatActivity implements Serializable {
    private final int MY_PERMISSION_REQUEST = 1;
    private final String SONG_STATE = "song_state";
    private final String SERVICE_RESTORE = "service_restore";
    private boolean isOpenMediaPlaybackFragment;
    private boolean songStateRestore;
    private MediaPlaybackService serviceRestore;

    private AllSongsFragment allSongsFragment;
    private MediaPlaybackFragment mediaPlaybackFragment;
    private FavouriteSongsFragment favouriteSongsFragment;

    private Music mMusicSelected = new Music();

    private final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    private final FragmentManager mFragmentManager = getSupportFragmentManager();

    private final String IS_FIRST = "isfirst";
    private final String POSITION_SERVICE = "position_from_service";
    public static final String IS_PAUSE = "is_pause";

    public static String IS_SHUFFLE = "IS_SHUFFLE";
    public static String IS_REPEATE_ONE = "IS_REPEATE_ONE";
    public static String IS_REPEATE_ALL = "IS_REPEATE_ALL";

    public static String ACTION_MUSIC = "action_music";

    private FavouriteSongsDatabase mDatabase;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private int oldPosition = -1;
    private boolean oldPause;
    private int servicePosition;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null){
                return;
            }
                int command = bundle.getInt(getString(R.string.actionmediaplayback));
                boolean b = bundle.getBoolean(getString(R.string.state));
                boolean isPause = bundle.getBoolean(getString(R.string.ispase));
                allSongsFragment.getMusicAdapter().setServiceState(b);
                servicePosition = bundle.getInt(getString(R.string.position));
                if (b){
                    setUIAllSongsPlayingMusic(bundle.getInt(getString(R.string.position)));
                    setUIMediaPlayback(command);
                    setUIFavouriteSongs(bundle.getInt(getString(R.string.position)));
                    allSongsFragment.getMusicAdapter().setIsPause(isPause);
                    favouriteSongsFragment.getAdapter().setIsPause(isPause);
                    favouriteSongsFragment.getAdapter().notifyDataSetChanged();
                }
        }
    };

    private void setCountPlay(int position) {
        if (position == 1){
            allSongsFragment.countPlayToDatabase(servicePosition);
        }
    }

    private void setUIFavouriteSongs(int position) {
        favouriteSongsFragment.setUIWhenPlayMusic(position);
    }

    @SuppressLint("NonConstantResourceId")
    private void setUIMediaPlayback(int command) {
        switch (command){
            case R.string.ACTION_SETUP_SEEKBAR:
                mediaPlaybackFragment.setUpSeekBar(allSongsFragment.getMediaPlaybackService().getMediaPlayer());
                break;
            case R.string.ACTION_SETUP_STATE:
                mediaPlaybackFragment.setUpState();
                break;
        }
    }

    public FavouriteSongsDatabase getDatabase(){
        return mDatabase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences(SAVE_FIRSTIME,MODE_PRIVATE);
        isFirstTimeAddToDatabse = preferences.getBoolean(IS_FIRST,true);
        setUpNavigation();
        mediaPlaybackFragment = new MediaPlaybackFragment();
        allSongsFragment = new AllSongsFragment();
        favouriteSongsFragment = new FavouriteSongsFragment();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getString(R.string.sendtoactivity)));
        requestPermissionsForApp();
        mDatabase = new FavouriteSongsDatabase(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUIAllSongsPlayingMusic(int position){
        allSongsFragment.setMPosition(position);
        allSongsFragment.getMusicAdapter().setPositionService(position);
        allSongsFragment.setUIWhenPlayingMusic(position);
        allSongsFragment.getMusicAdapter().notifyDataSetChanged();
        setUpFavouriteSongsMedia(position);
    }

    private void setUpFavouriteSongsMedia(int position) {
        switch (mDatabase.getSongIsFavourite(position)){
            case 2:
                mediaPlaybackFragment.setSongIsLike();
                break;
            case 1:
                mediaPlaybackFragment.setSongIsDislike();
                break;
            case 0:
                mediaPlaybackFragment.setDefFavourite();
                break;
        }
    }

    public AllSongsFragment getAllSongsFragment(){
        return allSongsFragment;
    }

    public MediaPlaybackFragment getMediaPlaybackFragment(){
        return mediaPlaybackFragment;
    }

    public FavouriteSongsFragment getFavouriteSongsFragment(){
        return favouriteSongsFragment;
    }

    public boolean getSongStateRestore(){
        return songStateRestore;
    }

    public void setSongStateRestore(boolean b){
        this.songStateRestore = b;
    }

    public MediaPlaybackService getServiceRestore(){
        return serviceRestore;
    }

    private boolean isFirstTimeAddToDatabse = true;
    public boolean getIsFirstTimeAddToDatabase(){
        return isFirstTimeAddToDatabse;
    }
    public void setIsFirstTimeAddToDatabase(boolean b){
        this.isFirstTimeAddToDatabse = b;
    }
    public static String SAVE_FIRSTIME = "save_first_time";
    public static String IS_RESTORE = "is_restore";

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getSharedPreferences(SAVE_FIRSTIME,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IS_FIRST,isFirstTimeAddToDatabse);
        editor.putBoolean(IS_SHUFFLE,allSongsFragment.getMediaPlaybackService().getIsShuffleOn());
        editor.putBoolean(IS_REPEATE_ONE,allSongsFragment.getMediaPlaybackService().getIsRepeateOne());
        editor.putBoolean(IS_REPEATE_ALL,allSongsFragment.getMediaPlaybackService().getIsRepeateAll());
        editor.putBoolean(IS_PAUSE,allSongsFragment.getMediaPlaybackService().getIsPause());
        if (allSongsFragment.getMediaPlaybackService().getServiceState()){
            editor.putInt(getString(R.string.positionlast),allSongsFragment.getMediaPlaybackService().returnMusicPosition());
            editor.putBoolean(IS_RESTORE,true);
        }else{
            editor.putInt(getString(R.string.positionlast),-1);
        }
        editor.commit();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_more,menu);
    }

    private void setUpNavigation(){
        Toolbar mToolBar = findViewById(R.id.mToolBar);
        setSupportActionBar(mToolBar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.mNavigation);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout, mToolBar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setCheckedItem(R.id.item_allSongs);
        addEventClickNavigation();
    }

    private void addEventClickNavigation() {
        mNavigationView.setNavigationItemSelectedListener(item -> {
            selectDrawerItem(item);
            return false;
        });
    }

    private void setUncheckItem(int idItem){
        for (int i = 0; i < mNavigationView.getMenu().size(); i++){
            if (mNavigationView.getMenu().getItem(i).getItemId() == idItem){
                mNavigationView.getMenu().getItem(i).setChecked(false);
            }
        }
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    private void selectDrawerItem(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_allSongs:
                item.setChecked(true);
                setUncheckItem(R.id.item_favouriteSongs);
                allSongsFragment.getMusicAdapter().setIsFavouriteSongsFragment(false);
                FragmentTransaction fragmentTransactionOpenAllSongs = getSupportFragmentManager().beginTransaction();
                Objects.requireNonNull(getSupportActionBar()).show();
                if (!isPortrait(this)){
                    fragmentTransactionOpenAllSongs.hide(favouriteSongsFragment);
                    fragmentTransactionOpenAllSongs.show(allSongsFragment);
                }else{
                    fragmentTransactionOpenAllSongs.show(allSongsFragment);
                    fragmentTransactionOpenAllSongs.hide(mediaPlaybackFragment);
                    fragmentTransactionOpenAllSongs.hide(favouriteSongsFragment);
                }
                fragmentTransactionOpenAllSongs.commit();
                break;
            case R.id.item_favouriteSongs:
                item.setChecked(true);
                setUncheckItem(R.id.item_allSongs);
                favouriteSongsFragment.getAdapter().setIsFavouriteSongsFragment(true);
                favouriteSongsFragment.refreshRecyclerView();
                Objects.requireNonNull(getSupportActionBar()).show();
                if (allSongsFragment.getMediaPlaybackService().getServiceState()){
                    favouriteSongsFragment.returnPositionWhenPlaying();
                }
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.layout_favourite_songs, favouriteSongsFragment,FavouriteSongsFragment.class.getName());
                fragmentTransaction.show(favouriteSongsFragment);
                if (!isPortrait(this)){
                    fragmentTransaction.hide(allSongsFragment);
                }else{
                    fragmentTransaction.hide(allSongsFragment);
                    fragmentTransaction.hide(mediaPlaybackFragment);
                }
                fragmentTransaction.addToBackStack(FavouriteSongsFragment.class.getName());
                fragmentTransaction.commit();
                break;
        }
        mDrawerLayout.closeDrawers();
    }
    private boolean serviceState;
    public boolean getServiceState(){
        return serviceState;
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceState = savedInstanceState.getBoolean(SONG_STATE,false);
        if (serviceState){
            serviceRestore = (MediaPlaybackService) savedInstanceState.getSerializable(SERVICE_RESTORE);
            serviceRestore.setSeekBar();
            oldPosition = savedInstanceState.getInt(POSITION_SERVICE,-1);
            oldPause = savedInstanceState.getBoolean(IS_PAUSE,false);
            allSongsFragment.setUIWhenPlayingMusic(serviceRestore.returnMusicPosition());
            allSongsFragment.getMusicAdapter().setOldPosition(oldPosition);
            allSongsFragment.getMusicAdapter().setOldPause(oldPause);
            songStateRestore = true;
            allSongsFragment.setIsRestoreApp(false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SONG_STATE,allSongsFragment.getMediaPlaybackService().getServiceState());
        if (allSongsFragment.getMediaPlaybackService().getServiceState()){
            outState.putSerializable(SERVICE_RESTORE, allSongsFragment.getMediaPlaybackService());
            outState.putInt(POSITION_SERVICE, allSongsFragment.getMediaPlaybackService().returnMusicPosition());
            if (allSongsFragment.getMediaPlaybackService().getIsPause()){
                outState.putBoolean(IS_PAUSE, true);
            }
        }
        super.onSaveInstanceState(outState);
    }

    public boolean isPortrait(Context context){
        return context.getResources().getBoolean(R.bool.isPortrait);
    }
    private void openAllSongsFragment() {
        if(mFragmentManager.findFragmentById(R.id.layout_allsongs) != null && mFragmentManager.findFragmentById(R.id.layout_mediaplayback) != null){
            fragmentTransaction.replace(R.id.layout_allsongs, allSongsFragment,AllSongsFragment.class.getName());
            fragmentTransaction.replace(R.id.layout_mediaplayback,mediaPlaybackFragment,MediaPlaybackFragment.class.getName());
            fragmentTransaction.replace(R.id.layout_favourite_songs, favouriteSongsFragment,FavouriteSongsFragment.class.getName());
        }else {
            fragmentTransaction.add(R.id.layout_allsongs, allSongsFragment,AllSongsFragment.class.getName());
            fragmentTransaction.add(R.id.layout_mediaplayback, mediaPlaybackFragment,MediaPlaybackFragment.class.getName());
            fragmentTransaction.add(R.id.layout_favourite_songs, favouriteSongsFragment,FavouriteSongsFragment.class.getName());
        }
        if (isPortrait(this)){
            fragmentTransaction.hide(mediaPlaybackFragment);
            fragmentTransaction.hide(favouriteSongsFragment);
        }else{
            fragmentTransaction.hide(favouriteSongsFragment);
        }
        fragmentTransaction.commit();
    }

    private void requestPermissionsForApp() {
        if(ContextCompat.checkSelfPermission
                (this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }else{
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }else{
            openAllSongsFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    String ALLOW_PERMISSION = getString(R.string.allowreadstorage);
                    Toast.makeText(this, ALLOW_PERMISSION, Toast.LENGTH_SHORT).show();
                    openAllSongsFragment();
                }
            } else {
                String DONT_ALLOW_PERMISSION = getString(R.string.notallowreadstorage);
                Toast.makeText(this, DONT_ALLOW_PERMISSION, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void  goToMediaPlaybackFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.show(mediaPlaybackFragment);
        fragmentTransaction.hide(allSongsFragment);
        fragmentTransaction.addToBackStack(MediaPlaybackFragment.class.getName());
        fragmentTransaction.commit();
        Objects.requireNonNull(getSupportActionBar()).hide();
        isOpenMediaPlaybackFragment = true;
    }

    public void openMediaPlayBackFromFavouriteSong(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.show(mediaPlaybackFragment);
        fragmentTransaction.hide(favouriteSongsFragment);
        fragmentTransaction.addToBackStack(MediaPlaybackFragment.class.getName());
        fragmentTransaction.commit();
        Objects.requireNonNull(getSupportActionBar()).hide();
        isOpenMediaPlaybackFragment = true;
    }

    @Override
    public void onBackPressed() {
        if (isOpenMediaPlaybackFragment){
            Objects.requireNonNull(getSupportActionBar()).show();
            isOpenMediaPlaybackFragment = false;
            super.onBackPressed();
        }else{
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search,menu);
        return super.onCreateOptionsMenu(menu);
    }
}