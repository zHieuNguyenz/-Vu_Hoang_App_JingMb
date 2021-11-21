package com.example.jingmb3.view.offline.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.FragmentSearchMyAlbumBinding;
import com.example.jingmb3.databinding.FragmentSearchMyArtistBinding;
import com.example.jingmb3.model.offline.MyAlbumDatabase;
import com.example.jingmb3.model.offline.MyAlbumObject;
import com.example.jingmb3.model.offline.MyArtistDatabase;
import com.example.jingmb3.model.offline.MyArtistObject;
import com.example.jingmb3.view.offline.activity.Search;
import com.example.jingmb3.view.offline.activity.SongOfMyArtist;
import com.example.jingmb3.view.offline.activity.SongsOfAlbum;
import com.example.jingmb3.view.offline.adapter.SearchMyAlbumAdapter;
import com.example.jingmb3.view.offline.adapter.SearchMyArtistAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchMyArtist extends Fragment {
    public static SearchMyArtist Instance;
    public static SearchMyArtist  getInstance(){
        if(Instance==null){
            return Instance=new SearchMyArtist();
        }
        return Instance;
    }

    FragmentSearchMyArtistBinding binding;
    private ArrayList<MyArtistObject> listArtist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentSearchMyArtistBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listArtist=new ArrayList<>();
        binding.rvListArtistSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvListArtistSearch.setAdapter(SearchMyArtistAdapter.getInstance());
        loadData();
        SearchMyArtistAdapter.getInstance().ClickItem(new SearchMyArtistAdapter.ClickItem() {
            @Override
            public void clickItem(int position) {
                Intent intent=new Intent(getActivity(), SongOfMyArtist.class);
                intent.putExtra("Id",SearchMyArtistAdapter.getInstance().getListArtists().get(position).getId_artist());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up_in,R.anim.slide_up_out);
            }
        });
    }

    public void setSearchQuery(String query){
        SearchMyArtistAdapter.getInstance().getFilter().filter(query);
    }

    public void loadData(){
        listArtist= (ArrayList<MyArtistObject>) MyArtistDatabase.getInstance(getContext()).myArtistDAO().getMyArtist();
        Arrange();
        SearchMyArtistAdapter.getInstance().setData(listArtist,getContext());
    }

    public void Arrange(){
        Collections.sort(listArtist, new Comparator<MyArtistObject>() {
            @Override
            public int compare(MyArtistObject myArtistObject, MyArtistObject t1) {
                return myArtistObject.getNameArtist().compareToIgnoreCase(t1.getNameArtist());
            }
        });
    }
}