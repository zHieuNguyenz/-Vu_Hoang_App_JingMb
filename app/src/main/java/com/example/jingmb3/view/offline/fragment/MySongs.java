package com.example.jingmb3.view.offline.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.FragmentMySongsBinding;
import com.example.jingmb3.model.offline.FavoriteDatabase;
import com.example.jingmb3.model.offline.FavoriteObject;
import com.example.jingmb3.model.offline.MyAlbumDatabase;
import com.example.jingmb3.model.offline.MyAlbumObject;
import com.example.jingmb3.model.offline.MyArtistDatabase;
import com.example.jingmb3.model.offline.MyMediaPlayer;
import com.example.jingmb3.model.offline.MySongObject;
import com.example.jingmb3.model.offline.MySongsDatabase;
import com.example.jingmb3.view.activity.MainActivity;
import com.example.jingmb3.view.offline.activity.AddMySong;
import com.example.jingmb3.view.offline.activity.EditMySong;
import com.example.jingmb3.view.offline.activity.PlayerSong;
import com.example.jingmb3.view.offline.activity.SelectAlbumToAddSong;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MySongs extends Fragment  {


    private static final int REQUEST_ADD_SONG = 80;
    FragmentMySongsBinding binding;
    private ArrayList<MySongObject> myListSong;
    private MySongAdapter mySongAdapter;
    private int reqestAddSongtoAlbum=100;
    private int reqestEditSong=99;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentMySongsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), AddMySong.class);
                startActivityForResult(intent,REQUEST_ADD_SONG);
                getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
            }
        });
        myListSong=new ArrayList<MySongObject>();
        mySongAdapter=new MySongAdapter(getContext());
        mySongAdapter.IClickItemListener(new IClickItemListener() {
            @Override
            public void OnClickItemSongs(int position) {
                OnClickToPlay(position);
            }
        });
        mySongAdapter.setClickMoreOption(new MySongAdapter.ClickMoreOption() {
            @Override
            public void clickMoreOption(int position) {
                openDialogOption(position);
            }
        });
        mySongAdapter.setData(myListSong);
        binding.rvMysong.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMysong.setAdapter(mySongAdapter);
        loadData();
    }


    private void OnClickToPlay(int position) {
        if(!MyMediaPlayer.getInstance().isCheckStopMedia()) MyMediaPlayer.getInstance().stopAudioFile();
        MyMediaPlayer.getInstance().setCheckSongAlbum(false);
        MyMediaPlayer.getInstance().setCheckSongArtist(false);
        MyMediaPlayer.getInstance().setCheckFavSong(false);
        Intent intent=new Intent(getActivity(), PlayerSong.class);
        startActivity(intent.putExtra("pos",position));
        getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_ADD_SONG==requestCode && resultCode==Activity.RESULT_OK){
            loadData();
            MyMusic myMusic= (MyMusic) getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_content);
            myMusic.loadMiniPlayer(MyMediaPlayer.getInstance().getPosition());
            if(!MyMediaPlayer.getInstance().isCheckFavSong() && !MyMediaPlayer.getInstance().isCheckSongAlbum() &&
            !MyMediaPlayer.getInstance().isCheckSongArtist()) MyMediaPlayer.getInstance().setListPlaySong(myListSong);
            Toast.makeText(getContext(),"Đã thêm bài hát mới!",Toast.LENGTH_SHORT).show();
        }
        if(reqestAddSongtoAlbum==requestCode && resultCode==Activity.RESULT_OK){
            Toast.makeText(getContext(),"Đã thêm bài hát vào Album "+data.getStringExtra("name album"),Toast.LENGTH_SHORT).show();
        }
        if(reqestEditSong==requestCode && resultCode==Activity.RESULT_OK){
            Toast.makeText(getContext(),"Đã chỉnh sửa bài hát!",Toast.LENGTH_SHORT).show();
            loadData();
        }

    }

    public void Arrange(ArrayList<MySongObject> myListSong){
        Collections.sort(myListSong, new Comparator<MySongObject>() {
            @Override
            public int compare(MySongObject mySongObject, MySongObject t1) {
                return mySongObject.getNameSong().compareToIgnoreCase(t1.getNameSong());
            }
        });
    }


    public void loadData(){
        myListSong= (ArrayList<MySongObject>) MySongsDatabase.getInstance(getContext()).mySongsDAO().getListSong();
        Arrange(myListSong);
        binding.countSongs.setText(myListSong.size()+" Bài hát");
        mySongAdapter.setData(myListSong);
    }

    public ArrayList<MySongObject> getMyListSong() {
        return myListSong;
    }

    public void setMyListSong(ArrayList<MySongObject> myListSong) {
        this.myListSong = myListSong;
    }

    public MySongAdapter getMySongAdapter() {
        return mySongAdapter;
    }

    public void setMySongAdapter(MySongAdapter mySongAdapter) {
        this.mySongAdapter = mySongAdapter;
    }



    public void openDialogOption(final int position){
        final Dialog dialog=new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_more_option);
        Window window=dialog.getWindow();
        if(window==null) return;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes=window.getAttributes();
        windowAttributes.gravity= Gravity.CENTER;
        window.setAttributes(windowAttributes);
        dialog.setCancelable(true);
        LinearLayout addToAlbum,addListFavorite,Edit,Remove;
        ImageView imgSong,favImg;
        TextView nameSong,nameArtist, favText;
        favImg=dialog.findViewById(R.id.favImg);
        nameSong=dialog.findViewById(R.id.nameSongOption);
        nameArtist=dialog.findViewById(R.id.artistOption);
        imgSong=dialog.findViewById(R.id.imgSongOption);
        addToAlbum=dialog.findViewById(R.id.AddSongToAlbum);
        addListFavorite=dialog.findViewById(R.id.AddListFavorite);
        favText=dialog.findViewById(R.id.favText);
        Edit=dialog.findViewById(R.id.EditSong);
        Remove=dialog.findViewById(R.id.DeleteSong);

        ArrayList<Integer> IdFavSong=new ArrayList<>();
        IdFavSong= (ArrayList<Integer>) FavoriteDatabase.getInstance(getContext()).favoriteDAO().getListIdSong();
        if (!IdFavSong.isEmpty()){
            if(IdFavSong.contains(myListSong.get(position).getId_song())){
                favText.setText("Xóa khỏi danh sách yêu thích");
                favImg.setImageResource(R.drawable.ic_heart_broken);
            }
            else {
                favText.setText("Thêm vào danh sách yêu thích");
                favImg.setImageResource(R.drawable.ic_favorite);
            }
        }
        Bitmap bitmap=BitmapFactory.decodeByteArray(myListSong.get(position).getImageSong(),0,
                myListSong.get(position).getImageSong().length);
        imgSong.setImageBitmap(bitmap);
        nameArtist.setText(myListSong.get(position).getNameArtist());
        nameSong.setText(myListSong.get(position).getNameSong());
        addListFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(favText.getText().equals("Thêm vào danh sách yêu thích")){
                    String nameSong=myListSong.get(position).getNameSong();
                    String nameArtist=myListSong.get(position).getNameArtist();
                    byte[] imgSong=myListSong.get(position).getImageSong();
                    String uriSong=myListSong.get(position).getLinkSong();
                    int IdSong=myListSong.get(position).getId_song();
                    FavoriteObject favoriteObject;
                    favoriteObject=new FavoriteObject(nameSong,nameArtist,imgSong,uriSong,IdSong);
                    FavoriteDatabase.getInstance(getContext()).favoriteDAO().insertSong(favoriteObject);
                    Toast.makeText(getContext(),"Đã thêm vào danh sách yêu thích!",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else{
                    FavoriteObject favoriteObject=FavoriteDatabase.getInstance(getContext()).favoriteDAO().getMyFavSongByID
                            (myListSong.get(position).getId_song());
                    FavoriteDatabase.getInstance(getContext()).favoriteDAO().deleteSong(favoriteObject);
                    Toast.makeText(getContext(),"Đã xóa khỏi danh sách yêu thích!",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        addToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), SelectAlbumToAddSong.class);
                intent.putExtra("idSong",myListSong.get(position).getId_song());
                startActivityForResult(intent,reqestAddSongtoAlbum);
                getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
                dialog.dismiss();
            }
        });
        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), EditMySong.class);
                intent.putExtra("idSong",myListSong.get(position).getId_song());
                startActivityForResult(intent,reqestEditSong);
                getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
                dialog.dismiss();
            }
        });
        Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveSong(position);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void RemoveSong(final int postion){
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa bài hát này?")
                .setMessage("Bạn có chắc chắn muốn xóa bài hát "+myListSong.get(postion).getNameSong()
                +" ra khỏi ứng dụng?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        int IdSong=myListSong.get(postion).getId_song();
                        String nameArtist=myListSong.get(postion).getNameArtist();
                        ArrayList<MyAlbumObject> myAlbumList=new ArrayList<>();
                        myAlbumList= (ArrayList<MyAlbumObject>) MyAlbumDatabase.getInstance(getContext()).myAlbumDAO().getMyAlbum();
                        for(MyAlbumObject myAlbumObject:myAlbumList){
                            if(myAlbumObject.getId_song()!=null) {
                                if (myAlbumObject.getId_song().contains(String.valueOf(IdSong))) {
                                    ArrayList<String> idSongList = myAlbumObject.getId_song();
                                    idSongList.remove(String.valueOf(IdSong));
                                    myAlbumObject.setId_song(idSongList);
                                    MyAlbumDatabase.getInstance(getContext()).myAlbumDAO().editAlbum(myAlbumObject);
                                }
                            }
                        }
                        MySongsDatabase.getInstance(getContext()).mySongsDAO().deleteSong(myListSong.get(postion));
                        Toast.makeText(getContext(),"Đã xóa bài hát",Toast.LENGTH_SHORT).show();

                        List<Integer> IdFavSong=FavoriteDatabase.getInstance(getContext()).favoriteDAO().getListIdSong();
                        if(IdFavSong.contains(IdSong)){
                            FavoriteDatabase.getInstance(getContext()).favoriteDAO().deleteSong(FavoriteDatabase
                                    .getInstance(getContext()).favoriteDAO().getMyFavSongByID(IdSong));
                        }

                        List<String> listArtist=MySongsDatabase.getInstance(getContext()).mySongsDAO().getListArtist();
                        if(!listArtist.contains(nameArtist)){
                            MyArtistDatabase.getInstance(getContext()).myArtistDAO().deleteArtist(MyArtistDatabase.
                                    getInstance(getContext()).myArtistDAO().getArtistByName(nameArtist));
                        }

                        List<MySongObject> listSong=new ArrayList<>();
                        listSong=MyMediaPlayer.getInstance().getListPlaySong();
                        loadData();
                        if(myListSong.isEmpty()){
                            MyMediaPlayer.getInstance().stopAudioFile();
                            MyMusic myMusic= (MyMusic) getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_content);
                            myMusic.HideMiniPlayer();
                            return;
                        }
                        if(!listSong.isEmpty()) {
                            if(listSong.get(MyMediaPlayer.getInstance().getPosition()).getId_song()==IdSong){
                                MyMediaPlayer.getInstance().stopAudioFile();
                                MyMusic myMusic= (MyMusic) getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_content);
                                MyMediaPlayer.getInstance().setListPlaySong(myListSong);
                                myMusic.loadMiniPlayer(0);
                            }
                            else MyMediaPlayer.getInstance().setListPlaySong(myListSong);
                        }
                    }
                })
                .setNegativeButton("Hủy",null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}