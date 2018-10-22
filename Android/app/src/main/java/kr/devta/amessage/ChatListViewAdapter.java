package kr.devta.amessage;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatListViewAdapter extends BaseAdapter {
    Context context;
    ArrayList<FriendInfo> items;

    public ChatListViewAdapter(Context context) {
        this.context = context;
        items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public FriendInfo getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView nameTextView = new TextView(context);
        TextView chatTextView = new TextView(context);

        String name = items.get(position).getName();
        String chat = Manager.readLastChat(items.get(position)).getMessage();
        int sender = ((Manager.readLastChat(items.get(position)).getDateToLong() > 0) ? 1 : -1);

        if (chat.equals(Manager.NONE)) chat = "";

        nameTextView.setText(name);
        nameTextView.setTextColor(ContextCompat.getColor(context, R.color.title));
        nameTextView.setTextSize(25);

        chatTextView.setText(((sender == 1) ? "Me" : "You") + ": " + chat);
        chatTextView.setTextColor(ContextCompat.getColor(context, R.color.title));
        chatTextView.setTextSize(18);

        layout.addView(nameTextView);
        layout.addView(chatTextView);

        return layout;
    }

    public ChatListViewAdapter addItem(FriendInfo item) {
        items.add(item);
        return this;
    }

    public ChatListViewAdapter removeItem(int position) {
        items.remove(position);
        return this;
    }

    public ChatListViewAdapter removeItem(FriendInfo item) {
        items.remove(item);
        return this;
    }

    public ChatListViewAdapter clear() {
        items.clear();
        return this;
    }

    public ChatListViewAdapter refresh() {
        notifyDataSetChanged();
        return this;
    }
}
