package com.example.jingmb3.view.offline.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.FragmentMyArtistsBinding;
import com.example.jingmb3.model.offline.MyAlbumObject;
import com.example.jingmb3.model.offline.MyArtistDatabase;
import com.example.jingmb3.model.offline.MyArtistObject;
import com.example.jingmb3.model.offline.MyMediaPlayer;
import com.example.jingmb3.model.offline.MySongObject;
import com.example.jingmb3.model.offline.MySongsDatabase;
import com.example.jingmb3.view.offline.activity.AddMySong;
import com.example.jingmb3.view.offline.activity.SongOfMyArtist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MyArtists extends Fragment {
    private MyArtistAdapter myArtistAdapter;
    private ArrayList<MyArtistObject> ListMyArtist;
    private FragmentMyArtistsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentMyArtistsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListMyArtist=new ArrayList<MyArtistObject>();
        myArtistAdapter=new MyArtistAdapter(getContext());
        LoadUI();
        binding.rvMyArtist.setLayoutManager(new GridLayoutManager(getContext(),3));
        binding.rvMyArtist.setAdapter(myArtistAdapter);
        myArtistAdapter.ClickItemArtist(new MyArtistAdapter.ClickItemArtist() {
            @Override
            public void clickItemArtist(int IdArtist) {
                Intent intent=new Intent(getActivity(), SongOfMyArtist.class);
                intent.putExtra("Id",IdArtist);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
            }
        });
    }
    private void LoadUI(){
        ListMyArtist= (ArrayList<MyArtistObject>) MyArtistDatabase.getInstance(getContext()).myArtistDAO().getMyArtist();
        if(ListMyArtist.isEmpty())  binding.countArtist.setText("0 Nghệ sĩ");
        else binding.countArtist.setText(ListMyArtist.size()+" Nghệ sĩ");
        Arrange();
        myArtistAdapter.setData(ListMyArtist);
    }
    public void Arrange(){
        Collections.sort(ListMyArtist, new Comparator<MyArtistObject>() {
            @Override
            public int compare(MyArtistObject myArtistObject, MyArtistObject t1) {
                return myArtistObject.getNameArtist().compareToIgnoreCase(t1.getNameArtist());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadUI();
    }
}