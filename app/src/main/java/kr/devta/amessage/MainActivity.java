package kr.devta.amessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static ActivityStatus status = null;

    Toolbar toolbar;
    ListView chatListView;
    FloatingActionButton mainFloatingActionButton;

    private static ChatListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Manager.showActivityName(this);

        status = ActivityStatus.CREATED;

        toolbar = findViewById(R.id.main_Toolbar);
        chatListView = findViewById(R.id.main_ChatListView);
        mainFloatingActionButton = findViewById(R.id.main_MainFloatingActionButton);

        adapter = new ChatListViewAdapter(getApplicationContext());

        setSupportActionBar(toolbar);

        chatListView.setAdapter(adapter);
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendInfo friendInfo = adapter.getItem(position);
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra("FriendInfo", friendInfo);
                startActivityForResult(chatIntent, Manager.REQUEST_CODE_CHAT);
            }
        });

        chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ChatSettingActivity.class);
                intent.putExtra("FriendInfo", adapter.getItem(position));
                startActivityForResult(intent, Manager.REQUEST_CODE_CHAT_SETTING);
                return false;
            }
        });

        mainFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), AddChatActivity.class), Manager.REQUEST_CODE_ADD_FRIEND);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status = ActivityStatus.RESUMED;
        updateUI();
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

    public static void updateUI() {
        adapter.clear();
        ArrayList<FriendInfo> previousFriendArrayList = Manager.readChatList();
        for (FriendInfo friendInfo : previousFriendArrayList) {
            if (friendInfo != null) adapter.addItem(friendInfo);
            else Manager.print("Item: Null");
        }
        adapter.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.REQUEST_CODE_ADD_FRIEND && resultCode == RESULT_OK) {
            String name = data.getStringExtra("Name");
            String phone = data.getStringExtra("Phone");
            FriendInfo friendInfo = new FriendInfo(name, phone);
            Manager.addChatList(friendInfo);
            adapter.addItem(friendInfo);
            adapter.refresh();
        } else if (requestCode == Manager.REQUEST_CODE_CHAT && resultCode == RESULT_OK) {
            FriendInfo friendInfo = (FriendInfo) data.getSerializableExtra("FriendInfo");
        } else if (requestCode == Manager.REQUEST_CODE_CHAT_SETTING && resultCode == RESULT_OK) {
            switch (data.getStringExtra("Action")) {
                case "Remove":
                case "ChangeName":
                    updateUI();
                    break;
                default:
                    break;
            }
        }
    }
}
