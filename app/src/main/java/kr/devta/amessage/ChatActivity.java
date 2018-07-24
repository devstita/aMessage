package kr.devta.amessage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    ListView chatingListView;
    EditText messageEditText;
    Button sendButton;

    FriendInfo friendInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatingListView = findViewById(R.id.chat_ChatingListView);
        messageEditText = findViewById(R.id.chat_MessageEditText);
        sendButton = findViewById(R.id.chat_SendButton);

        friendInfo = (FriendInfo) getIntent().getSerializableExtra("FriendInfo");

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) sendButton.setEnabled(false);
                else sendButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString();
            }
        });

        ArrayList<ChatInfo> chats = Manager.readChat(friendInfo);
        for (ChatInfo chat : chats)
    }
}