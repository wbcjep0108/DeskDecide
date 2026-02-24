package com.example.deskdecide;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RankingActivity extends AppCompatActivity {

    DatabaseHelper db;
    ListView lvRankings;
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        db = new DatabaseHelper(this);
        lvRankings = findViewById(R.id.lv_rankings);
        tvTitle = findViewById(R.id.tv_ranking_title);

        String category = getIntent().getStringExtra("category");
        if (category == null) category = "Presidency";
        
        tvTitle.setText(category + " Rankings");
        loadRankings(category);
    }

    private void loadRankings(String category) {
        Cursor cursor = db.getRankings(category);
        ArrayList<String> list = new ArrayList<>();
        int rank = 1;
        while (cursor.moveToNext()) {
            list.add(rank + ". " + cursor.getString(0) + " - " + cursor.getInt(1) + " votes");
            rank++;
        }
        
        // Custom adapter to set font color to black for each list item
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                return view;
            }
        };
        
        lvRankings.setAdapter(adapter);
    }
}