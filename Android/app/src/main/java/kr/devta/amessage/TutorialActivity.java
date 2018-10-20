package kr.devta.amessage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TutorialActivity extends AppCompatActivity {
    Button skipTutorialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Manager.initActivity(this);

        skipTutorialButton = findViewById(R.id.tutorial_SkipTutorialButton);

        skipTutorialButton.setOnClickListener(v -> {
            Manager.getSharedPreferences(Manager.NAME_TUTORIAL).edit().putBoolean(Manager.KEY_SAW_TUTORIAL, true).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        if (Manager.getSharedPreferences(Manager.NAME_TUTORIAL).getBoolean(Manager.KEY_SAW_TUTORIAL, false)) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}
