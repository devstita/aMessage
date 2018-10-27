package kr.devta.amessage;

import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LockScreenActivity extends AppCompatActivity {
    boolean useFingerprint;
    FingerprintManagerCompat fingerprintManager;

    ConstraintLayout rootLayout;
    TextView pinTextView;
    Button[] keypadButtons;
    Button clearButton, backspaceButton;
    ImageView fingerprintStatusImageView;

    String curPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        Manager.getInstance().initActivity(this);
        setResult(RESULT_CANCELED);

        useFingerprint = true;
        fingerprintManager = FingerprintManagerCompat.from(getApplicationContext());

        rootLayout = findViewById(R.id.lockScreen_RootLayout);
        pinTextView = findViewById(R.id.lockScreen_PinTextView);
        keypadButtons = new Button[10];
        keypadButtons[0] = findViewById(R.id.lockScreen_Number0Button);
        keypadButtons[1] = findViewById(R.id.lockScreen_Number1Button);
        keypadButtons[2] = findViewById(R.id.lockScreen_Number2Button);
        keypadButtons[3] = findViewById(R.id.lockScreen_Number3Button);
        keypadButtons[4] = findViewById(R.id.lockScreen_Number4Button);
        keypadButtons[5] = findViewById(R.id.lockScreen_Number5Button);
        keypadButtons[6] = findViewById(R.id.lockScreen_Number6Button);
        keypadButtons[7] = findViewById(R.id.lockScreen_Number7Button);
        keypadButtons[8] = findViewById(R.id.lockScreen_Number8Button);
        keypadButtons[9] = findViewById(R.id.lockScreen_Number9Button);
        clearButton = findViewById(R.id.lockScreen_ClearButton);
        backspaceButton = findViewById(R.id.lockScreen_BackspaceButton);
        fingerprintStatusImageView = findViewById(R.id.lockScreen_FingerprintStatusImageView);

        curPin = "● ● ● ●";

        if (Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).getBoolean(Manager.getInstance().KEY_USE_FINGERPRINT, false)) {
            if (!fingerprintManager.isHardwareDetected() || Build.VERSION.SDK_INT < 23) {
                useFingerprint = false;
                fingerprintStatusImageView.setVisibility(View.GONE);
            }
        } else {
            useFingerprint = false;
            fingerprintStatusImageView.setVisibility(View.GONE);
        }

        FingerprintManagerCompat.AuthenticationCallback callback = new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (useFingerprint) authSucceed();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                fingerprintFail();
            }

            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                disableFingerprint();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
            }
        };
        fingerprintManager.authenticate(null, 0, null, callback, null);

        for (Button curKeypadButton : keypadButtons)
            curKeypadButton.setOnClickListener(v -> {
                int number = Integer.valueOf(((Button) v).getText().toString());
                Manager.print("You Touched " + number);
                curPin = curPin.replaceFirst("●", String.valueOf(number));
                Manager.print("CurPin: " + curPin);
                pinTextView.setText(curPin);

                Manager.print("curPin.trim().replaceAll(\"[^0-9]\", \"\").length(): " + curPin.trim().replaceAll("[^0-9]", "").length());
                if (curPin.trim().replaceAll("[^0-9]", "").length() >= 4) {
                    String pin = Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).getString(Manager.getInstance().KEY_PIN, Manager.getInstance().NONE);
                    if (Manager.getInstance().encrypt(curPin).equals(pin)) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        pinTextView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
                        curPin = "● ● ● ●";
                        pinTextView.setText(curPin);
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
                Manager.print("CurPin: " + curPin);
                pinTextView.setText(curPin);
                break;
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
//        super.onBackPressed();
    }

    private void authSucceed() {
        setResult(RESULT_OK);
        finish();
    }

    private void fingerprintFail() {
        Manager.print("Fingerprint Fail");
        fingerprintStatusImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
    }

    private void disableFingerprint() {
        useFingerprint = false;
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fingerprintStatusImageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fingerprintStatusImageView.startAnimation(fadeOutAnimation);
    }
}
