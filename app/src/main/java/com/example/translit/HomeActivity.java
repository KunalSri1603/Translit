package com.example.translit;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    Button textConvert,startConversation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ConstraintLayout layout=findViewById(R.id.home_layout);
        AlphaAnimation animation =new AlphaAnimation(0.0f,1.0f);
        animation.setFillAfter(true);
        animation.setDuration(2500);
        layout.startAnimation(animation);
        textConvert=findViewById(R.id.textConvert);
        startConversation=findViewById(R.id.conversation);
        textConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(HomeActivity.this,Translation.class);
                startActivity(intent1);
            }
        });
        startConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(HomeActivity.this,ConversationActivity.class);
                startActivity(intent2);
            }
        });
    }
}
