package edu.vub.at.wekittens;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class EndScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        String winner = bundle.getString("winner");
        TextView tv = (TextView) findViewById(R.id.end_text);
        tv.setText("Player "+winner+" has won the game !");
    }
}