package kr.devta.amessage;

import android.os.Bundle;

import com.github.omadahealth.lollipin.lib.managers.AppLockActivity;

// DOING: Finger Print + PIN
public class IntoAppActivity extends AppLockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_into_app);
    }

    @Override
    public void showForgotDialog() {

    }

    @Override
    public void onPinSuccess(int attempts) {

    }

    @Override
    public void onPinFailure(int attempts) {

    }

    @Override
    public int getPinLength() {
        return 4;
//        return super.getPinLength();
    }
}
