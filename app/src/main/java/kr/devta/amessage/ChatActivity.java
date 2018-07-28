package kr.devta.amessage;

import android.support.constraint.ConstraintLayout;
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
    public static ActivityStatus status = null;

    ListView chatingListView;
    EditText messageEditText;
    Button sendButton;

    FriendInfo friendInfo;
    public static ChatingListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        status = ActivityStatus.CREATED;

        chatingListView = findViewById(R.id.chat_ChatingListView);
        messageEditText = findViewById(R.id.chat_MessageEditText);
        sendButton = findViewById(R.id.chat_SendButton);

        friendInfo = (FriendInfo) getIntent().getSerializableExtra("FriendInfo");
        adapter = new ChatingListViewAdapter(getApplicationContext(), friendInfo);

        chatingListView.setAdapter(adapter);

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
                if (message == null || message.isEmpty()) return;

                ChatInfo chatInfo = new ChatInfo(message);

                Manager.send(friendInfo, chatInfo);

                adapter.addItem(chatInfo);
                Manager.addChat(1, friendInfo, chatInfo);
            }
        });

        ArrayList<ChatInfo> chats = Manager.readChat(friendInfo);
        for (ChatInfo chat : chats) {
            String message = chat.getMessage();
            long originDate = chat.getDateToLong();
            long date = Math.abs(originDate);
            int sender = (int) (originDate / Math.abs(originDate));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status = ActivityStatus.RESUMED;
    }

    @Override
    protected void onPause() {
        super.onPause();
        status = ActivityStatus.PAUSED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status = ActivityStatus.DESTROYED;
    }
}
