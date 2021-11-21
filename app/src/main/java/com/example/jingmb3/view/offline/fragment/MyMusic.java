package com.example.jingmb3.view.offline.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.FragmentMyMusicBinding;
import com.example.jingmb3.model.offline.MyMediaPlayer;
import com.example.jingmb3.model.offline.MySongObject;
import com.example.jingmb3.model.offline.MySongsDatabase;
import com.example.jingmb3.view.activity.MainActivity;
import com.example.jingmb3.view.offline.activity.PlayerSong;
import com.example.jingmb3.view.offline.activity.Search;
import com.example.jingmb3.view.offline.fragment.MyMusicViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class MyMusic extends Fragment {

    private FragmentMyMusicBinding binding;
    private int position=0;
    private ArrayList<MySongObject> myListSong;
    private MySongObject mySongObject;
    private Bitmap bitmap=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentMyMusicBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyMusicViewPagerAdapter myMusicViewPagerAdapter=new MyMusicViewPagerAdapter(this);
        binding.viewpagerMyMusic.setAdapter(myMusicViewPagerAdapter);
        binding.viewpagerMyMusic.setOffscreenPageLimit(3);
        new TabLayoutMediator(binding.tablayoutMyMS, binding.viewpagerMyMusic, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Bài hát");
                    tab.setIcon(R.drawable.ic_music_search);
                    break;
                case 1:
                    tab.setText("Album");
                    tab.setIcon(R.drawable.ic_album_search);
                    break;
                case 2:
                    tab.setText("Nghệ sĩ");
                    tab.setIcon(R.drawable.ic_artist_search);
                    break;
                case 3:
                    tab.setText("Danh sách yêu thích");
                    tab.setIcon(R.drawable.ic_favorite);
                    break;
            }
        }).attach();
        binding.miniSName.setSelected(true);
        binding.playBtnMini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyMediaPlayer.getInstance().isCheckStopMedia()){
                    binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
                    MyMediaPlayer.getInstance().playAudioFile(getActivity().getApplicationContext(),
                            mySongObject.getLinkSong(),position);
                    MyMediaPlayer.getInstance().setStopMedia();
                }
                else if(MyMediaPlayer.getInstance().chechMedia()){
                    binding.playBtnMini.setImageResource(R.drawable.ic_play_mini);
                    MyMediaPlayer.getInstance().pauseAudioFile();
                    binding.imgSongMini.clearAnimation();
                }
                else{
                    if(MyMediaPlayer.getInstance().getMediaPlayer()!=null){
                        binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
                        MyMediaPlayer.getInstance().getMediaPlayer().start();
                        imageAnimation();
                    }
                    else{
                        binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
                        MyMediaPlayer.getInstance().playAudioFile(getActivity().getApplicationContext(),
                                mySongObject.getLinkSong(),position);
                        imageAnimation();
                    }
                }
            }
        });

        binding.nextBtnMini.setOnClickListener(new View.OnClickListener() {
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
                MyMediaPlayer.getInstance().stopAudioFile();
                MyMediaPlayer.getInstance().setStopMedia();
                imageAnimation();
                loadData();
                binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
                position=((position+1)%myListSong.size());
                mySongObject=myListSong.get(position);
                MyMediaPlayer.getInstance().playAudioFile(getActivity().getApplicationContext(),myListSong.get(position).getLinkSong(),position);
                mediaComplete();
                binding.miniSName.setText(mySongObject.getNameSong());
                binding.miniArtist.setText(mySongObject.getNameArtist());
                bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                        0,myListSong.get(position).getImageSong().length);
                binding.imgSongMini.setImageBitmap(bitmap);
            }
        });

        binding.prevBtnMini.setOnClickListener(new View.OnClickListener() {
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
                MyMediaPlayer.getInstance().stopAudioFile();
                MyMediaPlayer.getInstance().setStopMedia();
                imageAnimation();
                binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
                loadData();
                position=((position-1)<0?(myListSong.size()-1):position-1);
                mySongObject=myListSong.get(position);
                MyMediaPlayer.getInstance().playAudioFile(getActivity().getApplicationContext(),myListSong.get(position).getLinkSong(),position);
                mediaComplete();
                binding.miniSName.setText(mySongObject.getNameSong());
                binding.miniArtist.setText(mySongObject.getNameArtist());
                bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                        0,myListSong.get(position).getImageSong().length);
                binding.imgSongMini.setImageBitmap(bitmap);
            }
        });

        binding.miniPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), PlayerSong.class);
                intent.putExtra("pos",position);
                startActivityForResult(intent,23);
                getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
            }
        });

        loadData();
        if(myListSong.isEmpty()){
            HideMiniPlayer();
            return;
        } else binding.miniPlay.setVisibility(View.VISIBLE);

        loadMiniPlayer(MyMediaPlayer.getInstance().getPosition());

    }
    public void loadMiniPlayer(int position){
        if(!MyMediaPlayer.getInstance().getListPlaySong().isEmpty()){
            loadData();;
        }
        if(myListSong.isEmpty()){
            HideMiniPlayer();
            return;
        } else binding.miniPlay.setVisibility(View.VISIBLE);
        this.position=position;
        mySongObject=myListSong.get(position);
        bitmap= BitmapFactory.decodeByteArray(mySongObject.getImageSong(),0,mySongObject.getImageSong().length);
        binding.imgSongMini.setImageBitmap(bitmap);
        binding.miniSName.setText(mySongObject.getNameSong());
        binding.miniArtist.setText(mySongObject.getNameArtist());
        if(MyMediaPlayer.getInstance().isCheckStopMedia()){
            binding.playBtnMini.setImageResource(R.drawable.ic_play_mini);
            binding.imgSongMini.clearAnimation();
            return;
        }
        if(MyMediaPlayer.getInstance().isPlayAlbum()){
            MyMediaPlayer.getInstance().setPlayAlbum(false);
            imageAnimation();
            binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
            return;
        }
        if(MyMediaPlayer.getInstance().chechMedia())
        {
            imageAnimation();
            binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
        }
        else {
            binding.playBtnMini.setImageResource(R.drawable.ic_play_mini);
            binding.imgSongMini.clearAnimation();
        }
    }

    public void Arrange(){
        Collections.sort(myListSong, new Comparator<MySongObject>() {
            @Override
            public int compare(MySongObject mySongObject, MySongObject t1) {
                return mySongObject.getNameSong().compareToIgnoreCase(t1.getNameSong());
            }
        });
    }

    public void loadData(){
        myListSong=new ArrayList<>();
        if(MyMediaPlayer.getInstance().getListPlaySong().isEmpty())
            MyMediaPlayer.getInstance().setListPlaySong((ArrayList<MySongObject>)
                    MySongsDatabase.getInstance(getContext()).mySongsDAO().getListSong());
        myListSong= MyMediaPlayer.getInstance().getListPlaySong();
        Arrange();
    }

    private void repeatSong(){
        MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setStopMedia();
        loadData();
        MyMediaPlayer.getInstance().playAudioFile(getActivity().getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        mediaComplete();
        binding.miniSName.setText(myListSong.get(position).getNameSong());
        binding.miniArtist.setText(myListSong.get(position).getNameArtist());
        bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                0,myListSong.get(position).getImageSong().length);
        binding.imgSongMini.setImageBitmap(bitmap);
    }
    private void RandomPlay(){
        MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setStopMedia();
        loadData();
        Random random=new Random();
        int valueRandom=0;
        do{
            valueRandom=random.nextInt(myListSong.size());
        }while(valueRandom==position);
        position=valueRandom;
        MyMediaPlayer.getInstance().playAudioFile(getActivity().getApplicationContext(),myListSong.get(position).getLinkSong(),position);
        mediaComplete();
        binding.miniSName.setText(myListSong.get(position).getNameSong());
        binding.miniArtist.setText(myListSong.get(position).getNameArtist());
        bitmap = BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),
                0,myListSong.get(position).getImageSong().length);
        binding.imgSongMini.setImageBitmap(bitmap);
    }

    public void HideMiniPlayer(){
        binding.miniPlay.setVisibility(View.INVISIBLE);
    }

    public void mediaComplete(){
        MyMediaPlayer.getInstance().getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                loadData();
                if(MyMediaPlayer.getInstance().isCheckRepeat()){
                    repeatSong();
                    return;
                }
                else if(MyMediaPlayer.getInstance().isCheckRandom()){
                    RandomPlay();
                    return;
                }
                binding.nextBtnMini.performClick();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(23==requestCode && resultCode==23){
            position=data.getIntExtra("pos",0);
            mySongObject=myListSong.get(position);
            bitmap= BitmapFactory.decodeByteArray(mySongObject.getImageSong(),0,mySongObject.getImageSong().length);
            binding.imgSongMini.setImageBitmap(bitmap);
            binding.miniSName.setText(mySongObject.getNameSong());
            binding.miniArtist.setText(mySongObject.getNameArtist());
            if(MyMediaPlayer.getInstance().chechMedia()){
                binding.playBtnMini.setImageResource(R.drawable.ic_pause_mini);
                imageAnimation();
            }
            else {
                binding.playBtnMini.setImageResource(R.drawable.ic_play_mini);
                binding.imgSongMini.clearAnimation();
            }
            mediaComplete();
        }
    }
    public void imageAnimation(){
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        binding.imgSongMini.startAnimation(rotateAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMiniPlayer(MyMediaPlayer.getInstance().getPosition());
        if(MyMediaPlayer.getInstance().getMediaPlayer()!=null)
        mediaComplete();
    }
}