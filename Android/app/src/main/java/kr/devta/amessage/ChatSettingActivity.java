package kr.devta.amessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class ChatSettingActivity extends AppCompatActivity {
    EditText changeFriendNameEditText;
    Button removeChatingButton;
    Button changeFriendNameButton;

    FriendInfo friendInfo;
    String nameWhenEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_setting);
        Manager.getInstance().initActivity(this);

        changeFriendNameEditText = findViewById(R.id.chatSetting_ChangeFriendNameEditText);
        removeChatingButton = findViewById(R.id.chatSetting_RemoveChatingButton);
        changeFriendNameButton = findViewById(R.id.chatSetting_ChangeFriendNameButton);

        friendInfo = (FriendInfo) getIntent().getSerializableExtra("FriendInfo");
        nameWhenEmpty = friendInfo.getPhone();

        changeFriendNameEditText.setText(friendInfo.getName());
        changeFriendNameEditText.setHint(nameWhenEmpty);

        removeChatingButton.setOnClickListener(v -> {
            Manager.getInstance().removeChat(friendInfo);
            Intent result = new Intent();
            result.putExtra("Action", "Remove");
            setResult(RESULT_OK, result);
            finish();
        });

        changeFriendNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = changeFriendNameEditText.getText().toString();

                if (name.isEmpty()) name = nameWhenEmpty;

                Manager.getInstance().changeFriendName(friendInfo, name);
                Intent result = new Intent();
                result.putExtra("Action", "ChangeName");
                setResult(RESULT_OK, result);
                finish();
            }
        });

        ArrayList<FriendInfo> readFromContcts = Manager.getInstance().getContacts(getApplicationContext());
        for (FriendInfo curFriendInfo : readFromContcts) if (friendInfo.getPhone().equals(curFriendInfo.getPhone())) {
            changeFriendNameEditText.setHint(curFriendInfo.getName());
            nameWhenEmpty = curFriendInfo.getName();
            break;
        }
    }
}
