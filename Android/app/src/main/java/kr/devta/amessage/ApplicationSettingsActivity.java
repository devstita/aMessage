package kr.devta.amessage;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

public class ApplicationSettingsActivity extends AppCompatActivity {
    Switch enableApplicationLockSwitch;
    Switch useFingerprintSwitch;
    Button changePinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_settings);

        enableApplicationLockSwitch = findViewById(R.id.applicationSettings_EnableApplicationLockSwitch);
        useFingerprintSwitch = findViewById(R.id.applicationSettings_UseFingerprintSwitch);
        changePinButton = findViewById(R.id.applicationSettings_ChangePinButton);

        enableApplicationLockSwitch.setChecked(Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).getBoolean(Manager.getInstance().KEY_APPLICATION_LOCK_ENABLED, false));
        enableApplicationLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Manager.print("Enable Changed:" + isChecked);
            if (isChecked) {
                startActivityForResult(new Intent(getApplicationContext(), ChangePasswordActivity.class), Manager.getInstance().REQUEST_CODE_CREATE_PASSWORD);
            } else {
                Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).edit().putBoolean(Manager.getInstance().KEY_APPLICATION_LOCK_ENABLED, false).apply();
                useFingerprintSwitch.setChecked(false);
                changePinButton.setEnabled(false);
            }
        });

        if (!FingerprintManagerCompat.from(getApplicationContext()).isHardwareDetected()) {
            useFingerprintSwitch.setEnabled(false);
            useFingerprintSwitch.setChecked(false);
            Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).edit().putBoolean(Manager.getInstance().KEY_USE_FINGERPRINT, false).apply();
        } else {
            useFingerprintSwitch.setChecked(Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).getBoolean(Manager.getInstance().KEY_USE_FINGERPRINT, false));
            useFingerprintSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).edit().putBoolean(Manager.getInstance().KEY_USE_FINGERPRINT, isChecked).apply();
                if (isChecked) enableApplicationLockSwitch.setChecked(true);
            }));
        }

        changePinButton.setEnabled(enableApplicationLockSwitch.isChecked());
        changePinButton.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), ChangePasswordActivity.class), Manager.getInstance().REQUEST_CODE_CHANGE_PASSWORD));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.getInstance().REQUEST_CODE_CREATE_PASSWORD) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                String pin = Manager.getInstance().toSHA(data.getStringExtra(Manager.getInstance().KEY_PIN));
                Manager.print("Enable Lock: " + pin);
                Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).edit().putBoolean(Manager.getInstance().KEY_APPLICATION_LOCK_ENABLED, true).apply();
                Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).edit().putString(Manager.getInstance().KEY_PIN, pin).apply();
                changePinButton.setEnabled(true);
            }
            else {
                enableApplicationLockSwitch.setChecked(false);
                useFingerprintSwitch.setChecked(false);
                changePinButton.setEnabled(true);
            }
        } else if (requestCode == Manager.getInstance().REQUEST_CODE_CHANGE_PASSWORD) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                String pin = Manager.getInstance().toSHA(data.getStringExtra(Manager.getInstance().KEY_PIN));
                Manager.print("Change Pin: " + pin);
                Manager.getInstance().getSharedPreferences(Manager.getInstance().NAME_LOCK_APPLICATION).edit().putString(Manager.getInstance().KEY_PIN, pin).apply();
            }
        }
    }
}
