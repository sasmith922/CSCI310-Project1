package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;

public class ResultActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultText = findViewById(R.id.resultText);
        Button playAgainButton = findViewById(R.id.playAgainButton);

        boolean won = getIntent().getBooleanExtra("won", false);
        int elapsedSeconds = getIntent().getIntExtra("elapsedSeconds", 0);

        String resultsMsg;

        if(won) {
            resultsMsg = "Used " + elapsedSeconds + " seconds.\nYou won.\nGood job!";
        } else {
            resultsMsg = "Used " + elapsedSeconds + " seconds.\nYou lose.\nTry again.";
        }

        resultText.setText(resultsMsg);

        playAgainButton.setOnClickListener(view -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
