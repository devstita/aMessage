package kr.devta.amessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.ArrayList;

public class MainBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        // throw new UnsupportedOperationException("Not yet implemented");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (!Manager.isServiceRunning(MainService.class)) context.startService(new Intent(context, MainService.class));
        } else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle datas = intent.getExtras();
            String str = "";
            if (datas != null) {
                Object[] pdus = (Object[]) datas.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];

                for (int i = 0; i < messages.length; i++) messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String senderPhone = messages[0].getOriginatingAddress();
                String message = messages[0].getMessageBody();

                FriendInfo friendInfo = null;
                ArrayList<FriendInfo> friendInfos = Manager.readChatList();
                for (FriendInfo curFriendInfo : friendInfos) {
                    if (curFriendInfo.getPhone().equals(senderPhone)) {
                        friendInfo = curFriendInfo;
                        break;
                    }
                }

                if (friendInfo != null) {
                }
            }
        }
    }
}
