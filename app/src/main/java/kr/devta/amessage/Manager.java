package kr.devta.amessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Manager {
//    Init Method And Variable
    private static Context context;

    public static void init(Context context) {
//        Manager.print("Manager.init()");
        Manager.context = context;
//        checkNetworkMessage();
    }

//    Intent Request Code
    public static final int REQUEST_CODE_FIREBASE_LOGIN = 1000;
    public static final int REQUEST_CODE_ADD_FRIEND = 1001;
    public static final int REQUEST_CODE_CONTACT_INTENT = 1002;
    public static final int REQUEST_CODE_CHAT = 1003;
    public static final int REQUEST_CODE_CHAT_SETTING = 1004;

//    SharedPreferences
    public static final String NAME_TUTORIAL = "Name_Tutorial";
    public static final String KEY_SAW_TUTORIAL = "Key_SawTutorial";

    public static final String NAME_CHAT_LIST = "Name_ChatList";

    public static final String NAME_CHAT_SEPARATOR = "[$ NAME_CHAT $]";
    public static final String KEY_CHAT_SENDER_SEPARATOR = "$"; // $: Expression Symbol, \\$: String Symbol ( = java.util.regex.Pattern.quote("$")

    public static SharedPreferences getSharedPreferences(String name) {
        return Manager.context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void removeSharedPreferencesToKey(String name, String key) {
        Manager.getSharedPreferences(name).edit().remove(key).apply();
    }

    public static void removeSharedPreferences(String name) {
        Set<String> keys = Manager.getSharedPreferences(name).getAll().keySet();
        for (String curKey : keys) removeSharedPreferencesToKey(name, curKey);
    }

//    Chat Management
    public static void addChatList(FriendInfo friendInfo) {
        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().putString(friendInfo.getPhone(), friendInfo.getName()).apply();
    }

    public static void addChat(int sender, FriendInfo friendInfo, ChatInfo chatInfo, boolean actived) { // sender -> 1: Me, -1: Friend
        Manager.getSharedPreferences((Manager.NAME_CHAT_SEPARATOR) + friendInfo.getPhone()).edit().putString(
                ((sender == 1) ? Manager.getMyPhone() : friendInfo.getPhone()) + (Manager.KEY_CHAT_SENDER_SEPARATOR) + String.valueOf(chatInfo.getDateToLong())
                , chatInfo.getMessage()).apply();
        if (sender == -1 && !actived) Manager.makeNotification(friendInfo, chatInfo);
    }

    public static void changeFriendName(FriendInfo friendInfo, String name) {
        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().putString(friendInfo.getPhone(), name).apply();
    }

    public static FriendInfo getUpdatedFriendInfo(FriendInfo friendInfo) {
        String name = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getString(friendInfo.getPhone(), friendInfo.getName());
        return (new FriendInfo(name, friendInfo.getPhone()));
    }

    public static void removeChat(FriendInfo friendInfo) {
        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().remove(friendInfo.getPhone()).apply();
        Manager.removeSharedPreferences((Manager.NAME_CHAT_SEPARATOR) + friendInfo.getPhone());
    }

    public static ArrayList<FriendInfo> readChatList() {
        ArrayList<FriendInfo> ret = new ArrayList<>();

        Map<String, ?> allDatas = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getAll();
        Set<String> keys = allDatas.keySet();

        for (String curKey : keys) {
            ret.add(new FriendInfo(allDatas.get(curKey).toString(), curKey));
        }

        Collections.sort(ret, new Comparator<FriendInfo>() {
            @Override
            public int compare(FriendInfo o1, FriendInfo o2) {
                long o1LastChat = Manager.readLastChat(o1).getDateToLong();
                long o2LastChat = Manager.readLastChat(o2).getDateToLong();

                return Collator.getInstance().compare(String.valueOf(o1LastChat), String.valueOf(o2LastChat));
            }
        });

        return ret;
    }

    public static ArrayList<ChatInfo> readChat(FriendInfo friendInfo) {
        ArrayList<ChatInfo> ret = new ArrayList<>();

        Map<String, ?> allDatas = Manager.getSharedPreferences((Manager.NAME_CHAT_SEPARATOR) + friendInfo.getPhone()).getAll();
        Set<String> keys = allDatas.keySet();

        for (String curKey : keys) {
//            Manager.print("CurKey: " + curKey);
            String[] splitWithSeparator = curKey.split(Pattern.quote(Manager.KEY_CHAT_SENDER_SEPARATOR));
            String sender = (splitWithSeparator[0]);
            int senderInteger = (sender.equals(getMyPhone()) ? 1 : -1);
//            Manager.print("Sender: " + sender + ", SenderInt: " + senderInteger);
            String dateStr = (splitWithSeparator[1]);
            long date = ((senderInteger == 1) ? Math.abs(Long.valueOf(dateStr)) : -Math.abs(Long.valueOf(dateStr)));
//            Manager.print("Add to ret: Message(" + allDatas.get(curKey).toString() + "), Date(" + date + ")");
            ret.add(new ChatInfo(allDatas.get(curKey).toString(), date));
        }

        Collections.sort(ret, new Comparator<ChatInfo>() {
            @Override
            public int compare(ChatInfo o1, ChatInfo o2) {
                long o1Date = Math.abs(o1.getDateToLong());
                long o2Date = Math.abs(o2.getDateToLong());

                return Collator.getInstance().compare(String.valueOf(o1Date), String.valueOf(o2Date));
            }
        });

        return ret;
    }

    public static ChatInfo readLastChat(FriendInfo friendInfo) {
        ArrayList<ChatInfo> chats = readChat(friendInfo);
        if (chats.size() <= 0) return new ChatInfo(Manager.NONE);
        return chats.get(chats.size() - 1);
    }

    public static void makeNotification(FriendInfo friendInfo, ChatInfo chatInfo) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra("FriendInfo", friendInfo);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Manager.REQUEST_CODE_CHAT,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(friendInfo.getName())
                .setContentText(chatInfo.getMessage())
                .setContentIntent(pendingIntent)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setLights(Color.BLUE, 3000, 3000)
                .setAutoCancel(true);
        notificationManager.notify(0, builder.build());
    }

//    Networking And SMS
    private static final int DIFF_OF_NETWORK_TIME = 800;
    public static final String DATE_SEPARATOR = "[$ DATE $]";

    public static void send(final FriendInfo friendInfo, final ChatInfo chatInfo) {
        boolean myNetworkStatus = checkNetworkConnect();
        if (!myNetworkStatus) {
            sendWithSMS(friendInfo, chatInfo);
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootReference = database.getReference();

        DatabaseReference friendStatusReference = rootReference.child("Users").child(friendInfo.getPhone());
        friendStatusReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
//                    Manager.print("Friend is not in Database");
                    return;
                }
                String enableTime = dataSnapshot.getValue().toString();
                long enableTimeToLong = Long.valueOf(enableTime);
                long enableTimeDiff = Math.abs(Manager.getCurrentTimeMills() - enableTimeToLong);

//                Manager.print("EnableTimeDiff: " + enableTimeDiff);
                if (enableTimeDiff <= DIFF_OF_NETWORK_TIME) {
                    sendWithNetwork(friendInfo, chatInfo);
                } else {
                    sendWithSMS(friendInfo, chatInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sendWithSMS(friendInfo, chatInfo);
            }
        });
    }

    private static void sendWithSMS(FriendInfo friendInfo, ChatInfo chatInfo) {
        SmsManager.getDefault().sendTextMessage(friendInfo.getPhone(), null, chatInfo.getMessage(), null, null);
    }

    private static void sendWithNetwork(final FriendInfo friendInfo, final ChatInfo chatInfo) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootReference = database.getReference();

        final DatabaseReference destReference = rootReference.child("Chats").child(friendInfo.getPhone());
        destReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int maxIdx = -1;
                for (DataSnapshot curSnapshot : dataSnapshot.getChildren()) {
                    String key = curSnapshot.getKey();
                    char[] keyToChar = key.toCharArray();
                    int curIdx = keyToChar[keyToChar.length - 1];
                    if (curIdx > maxIdx) maxIdx = curIdx;
                }

                String curKey = Manager.getMyPhone() + (Manager.SEPARATOR) + String.valueOf((((maxIdx + 1) <= 0) ? 1 : maxIdx + 1));
                String date = String.valueOf(Math.abs(chatInfo.getDateToLong()));
                String message = chatInfo.getMessage();

                destReference.child(curKey).setValue(date + (Manager.DATE_SEPARATOR) + message);
//                Manager.print("Send with Network: " + message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sendWithSMS(friendInfo, chatInfo);
            }
        });
    }

//    Utility Method
    public static boolean isServiceRunning(Class<?> sc) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
            if (sc.getName().equals(serviceInfo.service.getClassName())) return true;
        return false;
    }

    @SuppressLint("MissingPermission")
    public static String getMyPhone(Context context) {
        String phone;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phone = telephonyManager.getLine1Number();

        phone.replace("+82", "0");
        if (phone.startsWith("82")) phone.replace("82", "");

        return phone;
    }

    public static String getMyPhone() {
        if (context == null) throw getNullPointerException();
        return getMyPhone(context);
    }

    public static ArrayList<FriendInfo> getContacts(Context context) {
        ArrayList<FriendInfo> ret = new ArrayList<>();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor contactCursor = context.getContentResolver().query(uri, projection, null, selectionArgs, sortOrder);

        if (contactCursor.moveToFirst()) {
            do {
                String phoneNumber = contactCursor.getString(1).replaceAll("[^0-9]", "");
                if (phoneNumber.length() != 11) continue;
                String name = contactCursor.getString(2);

                FriendInfo toAdd = new FriendInfo(name, phoneNumber);
                ret.add(toAdd);
//                Manager.print("Contact Added Item: " + name + ", " + phoneNumber);
            } while (contactCursor.moveToNext());
        }

        return ret;
    }

//        Etc Method And Variable
    public static final String NONE = "[$ NONE $]";
    public static final String SEPARATOR = "_";

    public static final int CIPHER_OF_DATE = 13;

    public static void print(String m) {
        Log.d("AMESSAGE_DEBUG", m);
    }

    public static void showActivityName(Activity activity) {
        print("Activity Created: " + activity.getClass().getSimpleName());
    }

    public static long getCurrentTimeMills() {
        long ret;
        ret = System.currentTimeMillis();
        return ret;
    }

    public static boolean checkNetworkConnect() {
        if (context == null) throw getNullPointerException();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return ((wifi.isAvailable() && wifi.isConnectedOrConnecting())
                || (mobile.isAvailable() && mobile.isConnectedOrConnecting()));
    }

    public static NullPointerException getNullPointerException() {
        return new NullPointerException("Manager.context is null");
    }
    public static NullPointerException getNullPointerException(String m) {
        return new NullPointerException(m);
    }
}
