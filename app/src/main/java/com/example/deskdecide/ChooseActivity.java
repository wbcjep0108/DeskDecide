package com.example.deskdecide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        ImageView btnOrg = findViewById(R.id.imageView2);
        ImageView btnVoter = findViewById(R.id.imageView3);

        btnOrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseActivity.this, OrganizerActivity.class));
            }
        });

        btnVoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseActivity.this, VoterActivity.class));
            }
        });
    }
}