package kr.devta.amessage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements Runnable {
    private static ActivityStatus status = null;
    private Thread checkNetworkThread = null;

    Toolbar toolbar;
    ListView chatingListView;
    private static EditText messageEditText;
    Button sendButton;

    FriendInfo friendInfo;
    public static ChatingListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Manager.initActivity(this);

        status = ActivityStatus.CREATED;
        checkNetworkThread = new Thread(this);

        toolbar = findViewById(R.id.chat_Toolbar);
        chatingListView = findViewById(R.id.chat_ChatingListView);
        messageEditText = findViewById(R.id.chat_MessageEditText);
        sendButton = findViewById(R.id.chat_SendButton);

        friendInfo = (FriendInfo) getIntent().getSerializableExtra("FriendInfo");
        adapter = new ChatingListViewAdapter(getApplicationContext(), friendInfo);

        checkNetworkThread.start();

        toolbar.setTitle(friendInfo.getName());
        toolbar.setSubtitle(" - " + friendInfo.getPhone());

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
                if (messageEditText.getText().toString().contains("\n")) {
                    Manager.print("Enter key Clicked!!");
                    messageEditText.setText(messageEditText.getText().toString().replace("\n", ""));
                    sendButton.performClick();
                } else {
                    if (s.toString().isEmpty()) sendButton.setEnabled(false);
                    else sendButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setEnabled(false);
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (message.isEmpty()) return;
            messageEditText.setText("");

            ChatInfo chatInfo = new ChatInfo(message);

            Manager.send(friendInfo, chatInfo);

            adapter.addItem(chatInfo).refresh();
            Manager.addChat(1, friendInfo, chatInfo, true);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        status = ActivityStatus.RESUMED;
        if (checkNetworkThread == null) checkNetworkThread = new Thread(this);
        Manager.chatActivityCheckNetworkThreadFlag = true;
        if (checkNetworkThread.getState() == Thread.State.NEW) checkNetworkThread.start();

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
        Manager.chatActivityCheckNetworkThreadFlag = false;
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
        while (Manager.chatActivityCheckNetworkThreadFlag) {
            Manager.checkFriendNetwork(friendInfo, ChatActivity::updateUI);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Manager.chatActivityCheckNetworkThreadFlag = true;
    }

    public static void updateUI(boolean status) {
        new Handler(Looper.getMainLooper()).post(() -> messageEditText.setHint((status) ? "aMessage 로 전송" : "SMS 로 전송"));
    }

    @NonNull
    public static ActivityStatus getActivityStatus() {
        try {
            if (status == null) {
                status = ActivityStatus.NULL;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return status;
    }
}
