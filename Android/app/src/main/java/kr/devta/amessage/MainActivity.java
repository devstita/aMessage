package kr.devta.amessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static ActivityStatus status = null;

    Toolbar toolbar;
    ListView chatListView;
    FloatingActionButton mainFloatingActionButton;

    private static ChatListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Manager.getInstance().initActivity(this);

        startActivity(new Intent(getApplicationContext(), LockScreenActivity.class));

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("FriendInfo")) {
            Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
            chatIntent.putExtra("FriendInfo", getIntent().getSerializableExtra("FriendInfo"));
            startActivityForResult(chatIntent, Manager.getInstance().REQUEST_CODE_CHAT);
        }

        status = ActivityStatus.CREATED;

        toolbar = findViewById(R.id.main_Toolbar);
        chatListView = findViewById(R.id.main_ChatListView);
        mainFloatingActionButton = findViewById(R.id.main_MainFloatingActionButton);

        adapter = new ChatListViewAdapter(getApplicationContext());

        setSupportActionBar(toolbar);

        chatListView.setAdapter(adapter);
        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            FriendInfo friendInfo = adapter.getItem(position);
            Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
            chatIntent.putExtra("FriendInfo", friendInfo);
            startActivityForResult(chatIntent, Manager.getInstance().REQUEST_CODE_CHAT);
        });

        chatListView.setOnItemLongClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), ChatSettingActivity.class);
            intent.putExtra("FriendInfo", adapter.getItem(position));
            startActivityForResult(intent, Manager.getInstance().REQUEST_CODE_CHAT_SETTING);
            return false;
        });

        mainFloatingActionButton.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), AddChatActivity.class), Manager.getInstance().REQUEST_CODE_ADD_FRIEND));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainMenu_Settings:
                startActivity(new Intent(getApplicationContext(), ApplicationSettingsActivity.class));
                break;
            case R.id.mainMenu_Lock:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void updateUI() {
        adapter.clear();
        ArrayList<FriendInfo> previousFriendArrayList = Manager.getInstance().readChatList();
        for (FriendInfo friendInfo : previousFriendArrayList) {
            if (friendInfo != null) adapter.addItem(friendInfo);
            else Manager.print("Item: Null");
        }
        adapter.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.getInstance().REQUEST_CODE_ADD_FRIEND && resultCode == RESULT_OK) {
            String name = data.getStringExtra("Name");
            String phone = data.getStringExtra("Phone");
            FriendInfo friendInfo = new FriendInfo(name, phone);
            Manager.getInstance().addChatList(friendInfo);
            adapter.addItem(friendInfo);
            adapter.refresh();
        } else if (requestCode == Manager.getInstance().REQUEST_CODE_CHAT && resultCode == RESULT_OK) {
            FriendInfo friendInfo = (FriendInfo) data.getSerializableExtra("FriendInfo");
        } else if (requestCode == Manager.getInstance().REQUEST_CODE_CHAT_SETTING && resultCode == RESULT_OK) {
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
