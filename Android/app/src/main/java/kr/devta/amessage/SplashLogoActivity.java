package kr.devta.amessage;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashLogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_logo);
        Manager.init(getApplicationContext());
        Manager.initActivity(this, status -> {
            if (status) {
                checkPermission();
            }
        });
    }

    private void checkPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Manager.checkUpdate((status) -> {
                    if (status) {
                        login();
                    } else {
                        finish();
                    }
                });
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "앱을 종료합니다..", Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        TedPermission.Builder tedPermission = TedPermission.with(getApplicationContext());
        tedPermission.setPermissionListener(permissionListener);

        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS,
                    Manifest.permission.FOREGROUND_SERVICE};
        } else {
            permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS};
        }

        tedPermission.setPermissions(permissions);
        tedPermission.setRationaleMessage("전화번호를 읽고, 문자 송수신을 위해서 이 권한이 필요합니다. ");
        tedPermission.setDeniedMessage("권한을 거부하시면 앱을 사용할 수 없습니다. [ 설정 ] -> [ 권한 ] 에서 다시 허용할 수 있습니다. ");
        tedPermission.check();
    }

    private void login() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
//            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), Manager.REQUEST_CODE_FIREBASE_LOGIN);
//        } else done();
        // TEMP : Test Mode (Without Real Device)
        done();
    }

    private void done() {
        if (!Manager.isServiceRunning(MainService.class)) {
            Manager.print("MainService is not running");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(getApplicationContext(), MainService.class));
            } else {
                startService(new Intent(getApplicationContext(), MainService.class));
            }
        }

//        startActivity(new Intent(getApplicationContext(), TutorialActivity.class));
        // TEMP: Test Mode (Without Tutorial)
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.REQUEST_CODE_FIREBASE_LOGIN) {
//            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(getApplicationContext(), "UID: " + user.getUid(), Toast.LENGTH_SHORT).show();
                done();
            } else {
                Toast.makeText(getApplicationContext(), "Login Error: Exit Application..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
