package kr.devta.amessage;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

// TODO: PIN TextView, Setting
// DOING: Finger Print
public class ApplicationSettingsActivity extends AppCompatActivity {
    boolean useFingerprint;
    int fingerprintCount;
    FingerprintManagerCompat fingerprintManager;

    Button[] keypadButtons;
    Button clearButton, backspaceButton;
    ImageView fingerprintStatusImageView;

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_settings);

        useFingerprint = true;
        fingerprintCount = 0;
        fingerprintManager = FingerprintManagerCompat.from(getApplicationContext());

        keypadButtons = new Button[10];
        keypadButtons[0] = findViewById(R.id.applicationSettings_Number0Button);
        keypadButtons[1] = findViewById(R.id.applicationSettings_Number1Button);
        keypadButtons[2] = findViewById(R.id.applicationSettings_Number2Button);
        keypadButtons[3] = findViewById(R.id.applicationSettings_Number3Button);
        keypadButtons[4] = findViewById(R.id.applicationSettings_Number4Button);
        keypadButtons[5] = findViewById(R.id.applicationSettings_Number5Button);
        keypadButtons[6] = findViewById(R.id.applicationSettings_Number6Button);
        keypadButtons[7] = findViewById(R.id.applicationSettings_Number7Button);
        keypadButtons[8] = findViewById(R.id.applicationSettings_Number8Button);
        keypadButtons[9] = findViewById(R.id.applicationSettings_Number9Button);
        clearButton = findViewById(R.id.applicationSettings_ClearButton);
        backspaceButton= findViewById(R.id.applicationSettings_BackspaceButton);
        fingerprintStatusImageView = findViewById(R.id.applicationSettings_FingerprintStatusImageView);

        if (!fingerprintManager.isHardwareDetected() || Build.VERSION.SDK_INT < 23) {
            useFingerprint = false;
            fingerprintStatusImageView.setVisibility(View.GONE);
        }

        FingerprintManagerCompat.AuthenticationCallback callback = new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                done();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                fail();
            }

            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                fail(errString.toString());
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                fail(helpString.toString());
            }
        };
        fingerprintManager.authenticate(null, 0, null, callback, null);
    }

    private void done() {
        Manager.getInstance().print("Fingerprint Succeed");

        setResult(RESULT_OK);
        finish();
    }

    private void fail() {
        fail("None");
    }

    private void fail(String message) {
        fingerprintCount++;
        if (fingerprintCount >= 4) {
            setResult(RESULT_CANCELED);
            finish();
        }
        Manager.getInstance().print("Fingerprint Fail: " + message);
    }
}
