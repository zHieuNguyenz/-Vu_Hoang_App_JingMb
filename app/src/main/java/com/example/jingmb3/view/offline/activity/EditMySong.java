package com.example.jingmb3.view.offline.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.ActivityEditMySongBinding;
import com.example.jingmb3.model.offline.FavoriteDatabase;
import com.example.jingmb3.model.offline.FavoriteObject;
import com.example.jingmb3.model.offline.MyArtistDatabase;
import com.example.jingmb3.model.offline.MyArtistObject;
import com.example.jingmb3.model.offline.MySongObject;
import com.example.jingmb3.model.offline.MySongsDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditMySong extends AppCompatActivity {
    private int REQUEST_CAMERA=2;
    private int REQUEST_GALLERY=3;
    private ArrayList<MySongObject> myListSong;
    ActivityEditMySongBinding binding;
    private int IdSong;
    private MySongObject mySongObject=null;
    private String NameArtist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEditMySongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       binding.cancelEditSong.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               finish();
               overridePendingTransition(R.anim.slide_down_in,R.anim.slide_down_out);
           }
       });
       binding.DoneEditSong.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               EditSong();
           }
       });
       binding.cameraEdit.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Camera();
           }
       });
       binding.galleryImgEdit.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               SelectImage();
           }
       });
       LoadUI();
    }

    public void LoadUI(){
        IdSong=getIntent().getIntExtra("idSong",0);
        myListSong=new ArrayList<>();
        myListSong= (ArrayList<MySongObject>) MySongsDatabase.getInstance(this).mySongsDAO().getListSong();
        for(MySongObject mySongObject:myListSong){
            if (mySongObject.getId_song()==IdSong){
                this.mySongObject=mySongObject;
                break;
            }
        }
        Bitmap bitmap= BitmapFactory.decodeByteArray(mySongObject.getImageSong(),0,
                mySongObject.getImageSong().length);
        binding.imgSongEdit.setImageBitmap(bitmap);
        binding.EditNameSong.setText(mySongObject.getNameSong());
        binding.EditArtist.setText(mySongObject.getNameArtist());
        NameArtist=mySongObject.getNameArtist();
    }

    public void EditSong(){
        String nameSong=binding.EditNameSong.getText().toString().trim();
        String nameArtist=binding.EditArtist.getText().toString().trim();
        if(nameSong.isEmpty()||nameArtist.isEmpty()){
            Toast.makeText(this,"Hãy nhập đủ thông tin!",Toast.LENGTH_SHORT).show();
            return;
        }
        FavoriteObject favoriteObject=FavoriteDatabase.getInstance(this).favoriteDAO().
                getMyFavSongByID(IdSong);
        favoriteObject.setNameSong(nameSong);
        favoriteObject.setNameArtist(nameArtist);
        favoriteObject.setImageSong(ImageView_to_Byte());
        FavoriteDatabase.getInstance(this).favoriteDAO().editSong(favoriteObject);
        mySongObject.setNameSong(nameSong);
        mySongObject.setNameArtist(nameArtist);
        mySongObject.setImageSong(ImageView_to_Byte());
        MySongsDatabase.getInstance(this).mySongsDAO().editSong(mySongObject);
        if(NameArtist!=nameArtist){
            List<String> ListNameArtist= MyArtistDatabase.getInstance(this).myArtistDAO()
                    .getListNameArtist();
            if(!ListNameArtist.contains(nameArtist)){
                MyArtistObject myArtistObject=new MyArtistObject();
                myArtistObject.setNameArtist(nameArtist);
                MyArtistDatabase.getInstance(this).myArtistDAO().insertArtist(myArtistObject);
            }
            else{
                ListNameArtist= MySongsDatabase.getInstance(this).mySongsDAO()
                        .getListArtist();
                if(!ListNameArtist.contains(NameArtist)){
                    MyArtistObject myArtistObject=new MyArtistObject();
                    myArtistObject=MyArtistDatabase.getInstance(this).myArtistDAO().getArtistByName(NameArtist);
                    MyArtistDatabase.getInstance(this).myArtistDAO().deleteArtist(myArtistObject);
                }
            }
        }
        setResult(Activity.RESULT_OK);
        finish();
        overridePendingTransition(R.anim.slide_down_in,R.anim.slide_down_out);
    }

    private void Camera(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_CAMERA);
    }

    private void SelectImage(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,REQUEST_GALLERY);
    }

    public byte[] ImageView_to_Byte(){
        binding.imgSongEdit.setDrawingCacheEnabled(true);
        binding.imgSongEdit.buildDrawingCache();
        Bitmap bmp=Bitmap.createBitmap(binding.imgSongEdit.getDrawingCache());
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[]  byteArray=stream.toByteArray();
        return byteArray;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_CAMERA && resultCode==AddMySong.RESULT_OK && data!=null){
            Bitmap bitmap= (Bitmap) data.getExtras().get("data");
            binding.imgSongEdit.setImageBitmap(bitmap);
        }
        if (requestCode==REQUEST_GALLERY && resultCode==AddMySong.RESULT_OK && data!=null){
            Uri uri =data.getData();
            Bitmap bitmapImg= null;
            try {
                bitmapImg = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            binding.imgSongEdit.setImageBitmap(bitmapImg);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down_in,R.anim.slide_down_out);
    }
}