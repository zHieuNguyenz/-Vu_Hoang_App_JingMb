package com.example.jingmb3.view.offline.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.ActivityPlayerSongBinding;
import com.example.jingmb3.model.offline.FavoriteDatabase;
import com.example.jingmb3.model.offline.FavoriteObject;
import com.example.jingmb3.model.offline.MyAlbumDatabase;
import com.example.jingmb3.model.offline.MyAlbumObject;
import com.example.jingmb3.model.offline.MyArtistDatabase;
import com.example.jingmb3.model.offline.MyArtistObject;
import com.example.jingmb3.model.offline.MyMediaPlayer;
import com.example.jingmb3.model.offline.MySongObject;
import com.example.jingmb3.model.offline.MySongsDatabase;
import com.example.jingmb3.view.offline.fragment.MyMusic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.core.Flowable;

public class PlayerSong extends AppCompatActivity {

    private  Bitmap bitmap=null;
    private ActivityPlayerSongBinding binding;

    private ArrayList<MySongObject> myListSong;
    private int position;
    boolean fav=false;
    Thread updateSeekbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerSongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.closePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, R.anim.slide_down_out);
            }
        });

        if(MyMediaPlayer.getInstance().isCheckRepeat())
            binding.repeatBtn.setImageResource(R.drawable.ic_current_repeat);
        binding.repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!MyMediaPlayer.getInstance().isCheckRepeat()){
                    binding.repeatBtn.setImageResource(R.drawable.ic_current_repeat);
                    MyMediaPlayer.getInstance().setCheckRepeat(true);
                    Toast.makeText(PlayerSong.this,"Lặp lại hiện tại",Toast.LENGTH_SHORT).show();
                }else {
                    binding.repeatBtn.setImageResource(R.drawable.ic_repeat);
                    MyMediaPlayer.getInstance().setCheckRepeat(false);
                    Toast.makeText(PlayerSong.this,"Lặp lại BẬT",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(MyMediaPlayer.getInstance().isCheckRandom())
            binding.randomBtn.setImageResource(R.drawable.ic_random);
        binding.randomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!MyMediaPlayer.getInstance().isCheckRandom()){
                    binding.randomBtn.setImageResource(R.drawable.ic_random);
                    MyMediaPlayer.getInstance().setCheckRandom(true);
                    Toast.makeText(PlayerSong.this,"Phát ngẫu nhiên BẬT",Toast.LENGTH_SHORT).show();
                }else {
                    binding.randomBtn.setImageResource(R.drawable.ic_shuffle);
                    MyMediaPlayer.getInstance().setCheckRandom(false);
                    Toast.makeText(PlayerSong.this,"Phát ngẫu nhiên TẮT",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(MyMediaPlayer.getInstance().isCheckSongAlbum()){
            int IdAlbum=MyMediaPlayer.getInstance().getIdAlbum();
            MyAlbumObject myAlbumObject=MyAlbumDatabase.getInstance(this).myAlbumDAO().getAlbumById(IdAlbum);
            ArrayList<String> listIdAlbum=new ArrayList<>();
            listIdAlbum=myAlbumObject.getId_song();
            myListSong=new ArrayList<>();
            for(String id:listIdAlbum) {
                myListSong.add(MySongsDatabase.getInstance(this).mySongsDAO().getMySongByID(Integer.valueOf(id)));
            }
            if(myListSong.isEmpty()) return;
            Arrange(myListSong);
            MyMediaPlayer.getInstance().setListPlaySong(myListSong);
        }
        else if(MyMediaPlayer.getInstance().isCheckSongArtist()){
            int IdArtist=MyMediaPlayer.getInstance().getIdArtist();
            MyArtistObject myArtistObject= MyArtistDatabase.getInstance(this).myArtistDAO().getArtistById(IdArtist);
            myListSong=new ArrayList<>();
            myListSong= (ArrayList<MySongObject>) MySongsDatabase.getInstance(this).mySongsDAO().getListSongByArtist(myArtistObject.getNameArtist());
            if(myListSong.isEmpty()) return;
            Arrange(myListSong);
            MyMediaPlayer.getInstance().setListPlaySong(myListSong);
        }
        else if(MyMediaPlayer.getInstance().isCheckFavSong()){
            myListSong=new ArrayList<>();
            List<Integer> IdSong=FavoriteDatabase.getInstance(this).favoriteDAO().getListIdSong();
            for(int id:IdSong){
                myListSong.add(MySongsDatabase.getInstance(this).mySongsDAO().getMySongByID(id));
            }
            Arrange(myListSong);
            MyMediaPlayer.getInstance().setListPlaySong(myListSong);
        }
        else{
            myListSong= (ArrayList<MySongObject>) MySongsDatabase.getInstance(this).mySongsDAO().getListSong();
            Arrange(myListSong);
            MyMediaPlayer.getInstance().setListPlaySong(myListSong);
        }



        position=getIntent().getIntExtra("pos",0);
        binding.songName.setSelected(true);
        binding.songName.setText(myListSong.get(position).getNameSong());
        binding.artistName.setText(myListSong.get(position).getNameArtist());
        bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                    0,myListSong.get(position).getImageSong().length);
        binding.imageSong.setImageBitmap(bitmap);
        favoriteSong();
        binding.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fav){
                    FavoriteObject favoriteObject=FavoriteDatabase.getInstance(PlayerSong.this).favoriteDAO().getMyFavSongByID
                            (myListSong.get(position).getId_song());
                    FavoriteDatabase.getInstance(PlayerSong.this).favoriteDAO().deleteSong(favoriteObject);
                    binding.favBtn.setImageResource(R.drawable.ic_favorite_border);
                    fav=false;
                    Toast.makeText(PlayerSong.this,"Đã xóa khỏi danh sách yêu thích!",Toast.LENGTH_SHORT).show();
                }
                else {
                    String nameSong=myListSong.get(position).getNameSong();
                    String nameArtist=myListSong.get(position).getNameArtist();
                    byte[] imgSong=myListSong.get(position).getImageSong();
                    String uriSong=myListSong.get(position).getLinkSong();
                    int IdSong=myListSong.get(position).getId_song();
                    FavoriteObject favoriteObject;
                    favoriteObject=new FavoriteObject(nameSong,nameArtist,imgSong,uriSong,IdSong);
                    FavoriteDatabase.getInstance(PlayerSong.this).favoriteDAO().insertSong(favoriteObject);
                    binding.favBtn.setImageResource(R.drawable.ic_favorite);
                    fav=true;
                    Toast.makeText(PlayerSong.this,"Đã thêm vào danh sách yêu thích!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        if(MyMediaPlayer.getInstance().getMediaPlayer()==null){
                imageAnimation();
                binding.playBtn.setImageResource(R.drawable.icon_pause);
                MyMediaPlayer.getInstance().playAudioFile(getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        }
        else if(MyMediaPlayer.getInstance().isCheckStopMedia()){
            MyMediaPlayer.getInstance().playAudioFile(getApplicationContext(),myListSong.get(position).getLinkSong(),position);
            MyMediaPlayer.getInstance().setStopMedia();
            imageAnimation();
            binding.playBtn.setImageResource(R.drawable.icon_pause);
        }
        else
        {
            if(MyMediaPlayer.getInstance().chechMedia()){
                binding.playBtn.setImageResource(R.drawable.icon_pause);
                imageAnimation();
            }
            else{
                binding.playBtn.setImageResource(R.drawable.icon_play);
            }
        }


        binding.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyMediaPlayer.getInstance().chechMedia()){
                    binding.playBtn.setImageResource(R.drawable.icon_play);
                    binding.songName.setSelected(false);
                    MyMediaPlayer.getInstance().pauseAudioFile();
                    binding.imageSong.clearAnimation();
                }
                else{
                    binding.playBtn.setImageResource(R.drawable.icon_pause);
                    MyMediaPlayer.getInstance().getMediaPlayer().start();
                    binding.songName.setSelected(true);
                    imageAnimation();
                }
            }
        });
        mediaComplete();

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyMediaPlayer.getInstance().isCheckRepeat()){
                    repeatSong();
                    return;
                }
                else if(MyMediaPlayer.getInstance().isCheckRandom()){
                    RandomPlay();
                    return;
                }
                nextSong();
            }
        });
        binding.prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyMediaPlayer.getInstance().isCheckRepeat()){
                    repeatSong();
                    return;
                }
                else if(MyMediaPlayer.getInstance().isCheckRandom()){
                    RandomPlay();
                    return;
                }
               previousSong();
            }
        });


        updateSeekbar=new Thread(){
            @Override
            public void run() {
                super.run();
                int totalDuration=MyMediaPlayer.getInstance().getMediaPlayer().getDuration();
                int currentPosition=0;
                while (currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition=MyMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition();
                        binding.seekBar.setProgress(currentPosition);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        Seekbar();

        binding.timeEnd.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition()));
        binding.timeEnd.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getDuration()));
    }



    private void favoriteSong(){
        ArrayList<Integer> IdFavSong= (ArrayList<Integer>) FavoriteDatabase.getInstance(this).favoriteDAO().getListIdSong();
        if (!IdFavSong.isEmpty()){
            if(IdFavSong.contains(myListSong.get(position).getId_song())){
                binding.favBtn.setImageResource(R.drawable.ic_favorite);
                fav=true;
            }
            else {
                binding.favBtn.setImageResource(R.drawable.ic_favorite_border);
                fav=false;
            }
        }
    }

    public void imageAnimation(){
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        binding.imageSong.startAnimation(rotateAnimation);

    }
    public String createTime(int duration){
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;
        time=time+min+":";
        if(sec<10){
            time+=0;
        }
        time+=sec;
        return time;
    }


    private void Seekbar(){
        binding.seekBar.setMax(MyMediaPlayer.getInstance().getMediaPlayer().getDuration());
        binding.seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        binding.seekBar.getThumb().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_IN);
        updateSeekbar.start();
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.timeStart.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition()));
                if(b){
                    MyMediaPlayer.getInstance().getMediaPlayer().seekTo(i);
                    seekBar.setProgress(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MyMediaPlayer.getInstance().getMediaPlayer().seekTo(seekBar.getProgress());
            }
        });
    }

    private void nextSong(){
        MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setStopMedia();
        binding.playBtn.setImageResource(R.drawable.icon_pause);
        binding.songName.setSelected(true);
        position=((position+1)%myListSong.size());
        MyMediaPlayer.getInstance().playAudioFile(getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        favoriteSong();
        binding.seekBar.setMax(MyMediaPlayer.getInstance().getMediaPlayer().getDuration());
        mediaComplete();
        binding.timeEnd.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getDuration()));
        binding.songName.setText(myListSong.get(position).getNameSong());
        binding.artistName.setText(myListSong.get(position).getNameArtist());
        bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                0,myListSong.get(position).getImageSong().length);
        binding.imageSong.setImageBitmap(bitmap);
    }
    private void previousSong(){
        MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setStopMedia();
        binding.playBtn.setImageResource(R.drawable.icon_pause);
        position=((position-1)<0?(myListSong.size()-1):position-1);
        MyMediaPlayer.getInstance().playAudioFile(getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        favoriteSong();
        binding.seekBar.setMax(MyMediaPlayer.getInstance().getMediaPlayer().getDuration());
        mediaComplete();
        binding.timeEnd.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getDuration()));
        binding.songName.setSelected(true);
        binding.songName.setText(myListSong.get(position).getNameSong());
        binding.artistName.setText(myListSong.get(position).getNameArtist());
        bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                0,myListSong.get(position).getImageSong().length);
        binding.imageSong.setImageBitmap(bitmap);
    }
    private void repeatSong(){
        MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setStopMedia();
        MyMediaPlayer.getInstance().playAudioFile(getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        favoriteSong();
        binding.seekBar.setMax(MyMediaPlayer.getInstance().getMediaPlayer().getDuration());
        mediaComplete();
        binding.timeEnd.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getDuration()));
        binding.songName.setSelected(true);
    }
    private void RandomPlay(){
        MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setStopMedia();
        Random random=new Random();
        int valueRandom=0;
        do{
            valueRandom=random.nextInt(myListSong.size());
        }while(valueRandom==position);
        position=valueRandom;
        MyMediaPlayer.getInstance().playAudioFile(getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        favoriteSong();
        binding.seekBar.setMax(MyMediaPlayer.getInstance().getMediaPlayer().getDuration());
        mediaComplete();
        binding.timeEnd.setText(createTime(MyMediaPlayer.getInstance().getMediaPlayer().getDuration()));
        binding.songName.setSelected(true);
        binding.songName.setText(myListSong.get(position).getNameSong());
        binding.artistName.setText(myListSong.get(position).getNameArtist());
        bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                0,myListSong.get(position).getImageSong().length);
        binding.imageSong.setImageBitmap(bitmap);
    }
    public void mediaComplete(){
        MyMediaPlayer.getInstance().getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                if(MyMediaPlayer.getInstance().isCheckRepeat()){
                   repeatSong();
                   return;
                }
                else if(MyMediaPlayer.getInstance().isCheckRandom()){
                    RandomPlay();
                    return;
                }
                else binding.nextBtn.performClick();
            }
        });
    }
    public void Arrange(ArrayList<MySongObject> myListSong){
        Collections.sort(myListSong, new Comparator<MySongObject>() {
            @Override
            public int compare(MySongObject mySongObject, MySongObject t1) {
                return mySongObject.getNameSong().compareToIgnoreCase(t1.getNameSong());
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("pos",position);
        setResult(23,intent);
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down_in, R.anim.slide_down_out);
    }
}