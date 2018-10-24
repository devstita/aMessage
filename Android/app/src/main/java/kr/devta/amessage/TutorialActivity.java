package kr.devta.amessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

// aTODO: Design Tutorial
public class TutorialActivity extends AppCompatActivity {
    Button skipTutorialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Manager.getInstance().initActivity(this);

        skipTutorialButton = findViewById(R.id.tutorial_SkipTutorialButton);

        skipTutorialButton.setOnClickListener(v -> {
            Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_TUTORIAL).edit().putBoolean(Manager.getInstance().KEY_SAW_TUTORIAL, true).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        if (Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_TUTORIAL).getBoolean(Manager.getInstance().KEY_SAW_TUTORIAL, false)) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}
