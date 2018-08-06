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

//        Manager.print("Something is received: " + intent.getAction());
        Manager.init(context);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            if (!Manager.isServiceRunning(MainService.class)) context.startService(new Intent(context, MainService.class));
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
//            Manager.print("He's coming..!!!!");
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

//                Manager.print("Catch SMS: From " + senderPhone + ", " + message);

                if (friendInfo == null) {
                    String name = senderPhone;
                    ArrayList<FriendInfo> contacts = Manager.getContacts(context);
                    for (FriendInfo curFriendInfo : contacts) if (senderPhone.equals(curFriendInfo.getPhone())) {
                        name = curFriendInfo.getName();
                        break;
                    }

                    friendInfo = new FriendInfo(name, senderPhone);
                    Manager.addChatList(friendInfo);
                }
                ChatInfo chatInfo = new ChatInfo(message, -time);

                boolean actived = false;
                if (ChatActivity.status != null && ChatActivity.status.equals(ActivityStatus.RESUMED)) // Activity is Running
                    if (ChatActivity.adapter != null)
                        if (ChatActivity.adapter.getFriendInfo().getPhone().equals(friendInfo.getPhone())) {
                            ChatActivity.adapter.addItem(chatInfo).refresh();
                            actived = true;
                        }
                Manager.addChat(-1, friendInfo, chatInfo, actived);
            }
        }
    }
}
