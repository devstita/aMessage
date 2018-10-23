package kr.devta.amessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, MainService.class));
                } else {
                    context.startService(new Intent(context, MainService.class));
                }
            }
        }

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] messages = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessage = new SmsMessage[messages.length];

                for (int i = 0; i < messages.length; i++)
                    smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
                String senderPhone = smsMessage[0].getOriginatingAddress();
                String message = smsMessage[0].getMessageBody();
                long time = smsMessage[0].getTimestampMillis();

                FriendInfo friendInfo = null;
                ArrayList<FriendInfo> friendInfos = Manager.readChatList();
                for (FriendInfo curFriendInfo : friendInfos) {
                    if (curFriendInfo.getPhone().equals(senderPhone)) {
                        friendInfo = curFriendInfo;
                        break;
                    }
                }

                if (friendInfo == null) {
                    String name = senderPhone;
                    ArrayList<FriendInfo> contacts = Manager.getContacts(context);
                    for (FriendInfo curFriendInfo : contacts)
                        if (senderPhone.equals(curFriendInfo.getPhone())) {
                            name = curFriendInfo.getName();
                            break;
                        }

                    friendInfo = new FriendInfo(name, senderPhone);
                    Manager.addChatList(friendInfo);
                }
                ChatInfo chatInfo = new ChatInfo(message, -time);

                boolean actived = false;
                if (ChatActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) {
                    if (ChatActivity.adapter != null) {
                        if (ChatActivity.adapter.getFriendInfo().getPhone().equals(friendInfo.getPhone())) {
                            ChatActivity.adapter.addItem(chatInfo).refresh();
                            actived = true;
                        }
                    }
                } else if (MainActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) {
                    MainActivity.updateUI();
                }
                Manager.addChat(-1, friendInfo, chatInfo, actived);
            }
        }
    }
}
