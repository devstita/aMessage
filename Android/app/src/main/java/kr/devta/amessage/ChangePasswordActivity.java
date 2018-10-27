package kr.devta.amessage;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePasswordActivity extends AppCompatActivity {
    boolean isConfirm;
    String curPin;

    TextView pinTextView;
    Button[] keypadButtons;
    Button clearButton, backspaceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Manager.getInstance().initActivity(this);
        setResult(RESULT_CANCELED);

        isConfirm = getIntent().getBooleanExtra("isConfirm", false);
        Manager.print("Confirm: " + isConfirm);
        if (!isConfirm) {
            Toast.makeText(getApplicationContext(), "PIN 을 입력하세요 (1단계)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "PIN 을 입력하세요 (2단계)", Toast.LENGTH_SHORT).show();
        }

        curPin = "● ● ● ●";

        pinTextView = findViewById(R.id.changePassword_PinTextView);
        keypadButtons = new Button[10];
        keypadButtons[0] = findViewById(R.id.changePassword_Number0Button);
        keypadButtons[1] = findViewById(R.id.changePassword_Number1Button);
        keypadButtons[2] = findViewById(R.id.changePassword_Number2Button);
        keypadButtons[3] = findViewById(R.id.changePassword_Number3Button);
        keypadButtons[4] = findViewById(R.id.changePassword_Number4Button);
        keypadButtons[5] = findViewById(R.id.changePassword_Number5Button);
        keypadButtons[6] = findViewById(R.id.changePassword_Number6Button);
        keypadButtons[7] = findViewById(R.id.changePassword_Number7Button);
        keypadButtons[8] = findViewById(R.id.changePassword_Number8Button);
        keypadButtons[9] = findViewById(R.id.changePassword_Number9Button);
        clearButton = findViewById(R.id.changePassword_ClearButton);
        backspaceButton = findViewById(R.id.changePassword_BackspaceButton);

        for (Button curKeypadButton : keypadButtons)
            curKeypadButton.setOnClickListener(v -> {
                int number = Integer.valueOf(((Button) v).getText().toString());
//                Manager.print("You Touched " + number);
                curPin = curPin.replaceFirst("●", String.valueOf(number));
//                Manager.print("CurPin: " + curPin);
                pinTextView.setText(curPin);

//                Manager.print("curPin.trim().replaceAll(\"[^0-9]\", \"\").length(): " + curPin.trim().replaceAll("[^0-9]", "").length());
                if (curPin.trim().replaceAll("[^0-9]", "").length() >= 4) {
                    if (!isConfirm) {
                        Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                        intent.putExtra("isConfirm", true);
                        startActivityForResult(intent, Manager.getInstance().REQUEST_CODE_CHANGE_PASSWORD);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra(Manager.getInstance().KEY_PIN, curPin.trim());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });

        clearButton.setOnClickListener(v -> {
            curPin = "● ● ● ●";
            pinTextView.setText(curPin);
        });

        backspaceButton.setOnClickListener(v -> {
            char[] curPinChars = curPin.toCharArray();

            for (int i = curPinChars.length - 1; i >= 0; i--) if (curPinChars[i] != '●' && curPinChars[i] != ' ') {
                curPinChars[i] = '●';
                curPin = String.valueOf(curPinChars);
//                Manager.print("CurPin: " + curPin);
                pinTextView.setText(curPin);
                break;
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.getInstance().REQUEST_CODE_CHANGE_PASSWORD) {
            if (resultCode == RESULT_OK) {
                if (data.getStringExtra(Manager.getInstance().KEY_PIN).equals(curPin)) {
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    curPin = "● ● ● ●";
                    pinTextView.setText(curPin);
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다. ", Toast.LENGTH_SHORT).show();
                }
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }
}
