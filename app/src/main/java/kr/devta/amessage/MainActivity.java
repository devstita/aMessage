package kr.devta.amessage;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    ListView chatListView;
    FloatingActionButton mainFloatingActionButton;

    ChatListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Manager.showActivityName(this);

        chatListView = findViewById(R.id.main_ChatListView);
        mainFloatingActionButton = findViewById(R.id.main_MainFloatingActionButton);

        adapter = new ChatListViewAdapter(getApplicationContext());

        chatListView.setAdapter(adapter);
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendInfo friendInfo = (FriendInfo) adapter.getItem(position);
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra("FriendInfo", friendInfo);
                startActivityForResult(chatIntent, Manager.REQUEST_CODE_CHAT);
            }
        });

        mainFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), AddChatActivity.class), Manager.REQUEST_CODE_ADD_FRIEND);
            }
        });

        ArrayList<FriendInfo> previousFriendArrayList = Manager.readChatList();
        Manager.print("PreviousFriendArrayList.size(): " + previousFriendArrayList.size());
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
        }
    }
}
