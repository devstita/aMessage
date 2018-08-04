package kr.devta.amessage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChatSettingActivity extends AppCompatActivity {
    EditText changeFriendNameEditText;
    Button removeChatingButton;
    Button changeFriendNameButton;

    FriendInfo friendInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_setting);

        changeFriendNameEditText = findViewById(R.id.chatSetting_ChangeFriendNameEditText);
        removeChatingButton = findViewById(R.id.chatSetting_RemoveChatingButton);
        changeFriendNameButton = findViewById(R.id.chatSetting_ChangeFriendNameButton);

        friendInfo = (FriendInfo) getIntent().getSerializableExtra("FriendInfo");

        changeFriendNameEditText.setText(friendInfo.getName());
        changeFriendNameEditText.setHint(friendInfo.getPhone());

        removeChatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manager.removeChat(friendInfo);
                Intent result = new Intent();
                result.putExtra("Action", "Remove");
                setResult(RESULT_OK, result);
                finish();
            }
        });

        changeFriendNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = changeFriendNameEditText.getText().toString();

                if (name.isEmpty()) name = friendInfo.getPhone();

                Manager.changeFriendName(friendInfo, name);
                Intent result = new Intent();
                result.putExtra("Action", "ChangeName");
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}