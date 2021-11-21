package com.example.jingmb3.view.online.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.jingmb3.R;

public class SearchOnline extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_online);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}