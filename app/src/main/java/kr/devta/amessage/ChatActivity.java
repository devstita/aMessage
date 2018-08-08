package kr.devta.amessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements Runnable {
    public static ActivityStatus status = null;

    Toolbar toolbar;
    ListView chatingListView;
    EditText messageEditText;
    Button sendButton;

    FriendInfo friendInfo;
    public static ChatingListViewAdapter adapter;
    Thread checkFriendNetworkThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        status = ActivityStatus.CREATED;

        toolbar = findViewById(R.id.chat_Toolbar);
        chatingListView = findViewById(R.id.chat_ChatingListView);
        messageEditText = findViewById(R.id.chat_MessageEditText);
        sendButton = findViewById(R.id.chat_SendButton);

        friendInfo = (FriendInfo) getIntent().getSerializableExtra("FriendInfo");
        adapter = new ChatingListViewAdapter(getApplicationContext(), friendInfo);
        checkFriendNetworkThread = new Thread(this);

        toolbar.setTitle(friendInfo.getName());
        toolbar.setSubtitle(" - " + String.valueOf(-1));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                messageEditText.setText("");

                ChatInfo chatInfo = new ChatInfo(message);

                Manager.send(friendInfo, chatInfo);

                adapter.addItem(chatInfo).refresh();
                Manager.addChat(1, friendInfo, chatInfo, true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status = ActivityStatus.RESUMED;
        checkFriendNetworkThread.start();

        adapter.clear();
        ArrayList<ChatInfo> chats = Manager.readChat(friendInfo);
        for (ChatInfo chat : chats) {
            adapter.addItem(chat);
        }
        adapter.refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        status = ActivityStatus.PAUSED;
        checkFriendNetworkThread.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status = ActivityStatus.DESTROYED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.chatMenu_Information:
                Intent intent = new Intent(getApplicationContext(), ChatSettingActivity.class);
                intent.putExtra("FriendInfo", friendInfo);
                startActivityForResult(intent, Manager.REQUEST_CODE_CHAT_SETTING);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.REQUEST_CODE_CHAT_SETTING && resultCode == RESULT_OK) {
            switch (data.getStringExtra("Action")) {
                case "Remove":
                    finish();
                    break;
                case "ChangeName":
                    friendInfo = (Manager.getUpdatedFriendInfo(friendInfo));
                    toolbar.setTitle(friendInfo.getName());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) break;
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference().child("Users").child(friendInfo.getPhone()).child("LastAppOpen").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long myCurrentTime = Manager.getCurrentTimeMills();
                    long friendLastAppOpen = Long.valueOf(dataSnapshot.getValue().toString());
                    long diffMillis = myCurrentTime - friendLastAppOpen;
                    long diff = diffMillis / 1000;
                    String connectingMessage;

                    /*
                     1 second: 1 second
                     1 minute: 60 second
                     1 hour = 3600 second
                     1 day = 43200 second
                     */

                    long day = 0, hour = 0, minute = 0, second = diff;
                    while (second >= 60) {
                        if (second >= 43200) {
                            day++;
                            second -= 43200;
                        } else if (second >= 3600) {
                            hour++;
                            second -= 3600;
                        } else if (second >= 60) {
                            minute++;
                            second -= 60;
                        }
                    }

                    if (day > 0) connectingMessage = (String.valueOf(day) + " 일 전 접속");
                    else if (hour > 0) connectingMessage = (String.valueOf(hour) + " 시간 전 접속");
                    else if (minute > 0) connectingMessage = (String.valueOf(minute) + " 분 전 접속");
                    else connectingMessage = "현재 접속중..";

                    toolbar.setSubtitle(" - " + connectingMessage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
