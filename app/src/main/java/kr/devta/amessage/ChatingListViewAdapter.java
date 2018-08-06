package kr.devta.amessage;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatingListViewAdapter extends BaseAdapter {
    Context context;
    FriendInfo friendInfo;
    ArrayList<ChatInfo> items;

    public ChatingListViewAdapter(Context context, FriendInfo friendInfo) {
        this.context = context;
        this.friendInfo = friendInfo;
        items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.item_chatinglistview, null, true);

        TextView chatTextView = layout.findViewById(R.id.chatingListViewItem_ChatTextView);
        TextView dateTextView = layout.findViewById(R.id.chatingListViewItem_DateTextView);
        int sender = ((items.get(position).getDateToLong() > 0) ? 1 : -1);

        chatTextView.setText(((sender == 1) ? "Me" : "You") + ": " + items.get(position).getMessage());
        Linkify.addLinks(chatTextView, Linkify.ALL);
        chatTextView.setLinksClickable(true);
        dateTextView.setText(items.get(position).getDateWithFormat());

        return layout;
    }

    public ChatingListViewAdapter addItem(ChatInfo item) {
        items.add(item);
        return this;
    }
    public ChatingListViewAdapter setFriendInfo(FriendInfo friendInfo) {
        this.friendInfo = friendInfo;
        return this;
    }
    public FriendInfo getFriendInfo() {
        return friendInfo;
    }
    public ChatingListViewAdapter removeItem(int position) {
        items.remove(position);
        return this;
    }
    public ChatingListViewAdapter removeItem(ChatInfo item) {
        items.remove(item);
        return this;
    }
    public ChatingListViewAdapter clear() {
        items.clear();
        return this;
    }
    public ChatingListViewAdapter refresh() {
        notifyDataSetChanged();
        return this;
    }
}
