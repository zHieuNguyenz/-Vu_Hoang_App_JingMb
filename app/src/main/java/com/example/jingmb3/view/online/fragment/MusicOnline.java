package com.example.jingmb3.view.online.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jingmb3.R;
import com.example.jingmb3.databinding.FragmentMusicOnlineBinding;
import com.example.jingmb3.view.offline.fragment.MyMusic;
import com.example.jingmb3.view.online.fragment.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MusicOnline extends Fragment {
    private FragmentMusicOnlineBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentMusicOnlineBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getActivity());
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2);
        new TabLayoutMediator(binding.tablayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
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
                }
            }
        }).attach();
    }
}