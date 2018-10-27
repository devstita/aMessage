package kr.devta.amessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Manager.getInstance().updateNetworkStatus(context);
            if (ChatActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) ChatActivity.updateUI();
        }
    }
}
