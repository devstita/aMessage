package kr.devta.amessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Manager {
    //    PART: Singleton
    private static Manager instance = new Manager();
    private Manager() { }
    public static Manager getInstance() {
        return instance;
    }

    //    PART: Init Method And Variable
    private Context context;

    public void init(Context context) {
//       print("Manager.getInstance().init()");
       this.context = context;
    }

    //    PART: Intent Request Code
    public final int REQUEST_CODE_FIREBASE_LOGIN = 1000;
    public final int REQUEST_CODE_ADD_FRIEND = 1001;
    public final int REQUEST_CODE_CONTACT_INTENT = 1002;
    public final int REQUEST_CODE_CHAT = 1003;
    public final int REQUEST_CODE_CHAT_SETTING = 1004;

    //    PART: SharedPreferences
    public final String NAME_TUTORIAL = "Name_Tutorial";
    public final String KEY_SAW_TUTORIAL = "Key_SawTutorial";

    public final String NAME_CHAT_LIST = "Name_ChatList";

    public final String NAME_CHAT_SEPARATOR = "[$ NAME_CHAT $]";
    public final String KEY_CHAT_SENDER_SEPARATOR = "$"; // $: Expression Symbol, \\$: String Symbol ( = java.util.regex.Pattern.quote("$")

    public SharedPreferences getSharedPreferences(String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void removeSharedPreferencesToKey(String name, String key) {
       getSharedPreferences(name).edit().remove(key).apply();
    }

    public void removeSharedPreferences(String name) {
        Set<String> keys =getSharedPreferences(name).getAll().keySet();
        for (String curKey : keys) removeSharedPreferencesToKey(name, curKey);
    }

    //    PART: Chat Management
    public void addChatList(FriendInfo friendInfo) {
       getSharedPreferences(NAME_CHAT_LIST).edit().putString(friendInfo.getPhone(), friendInfo.getName()).apply();
    }

    public void addChat(int sender, FriendInfo friendInfo, ChatInfo chatInfo, boolean actived) { // sender -> 1: Me, -1: Friend
       getSharedPreferences((NAME_CHAT_SEPARATOR) + friendInfo.getPhone()).edit().putString(
                ((sender == 1) ?getMyPhone() : friendInfo.getPhone()) + (KEY_CHAT_SENDER_SEPARATOR) + String.valueOf(chatInfo.getDateToLong())
                , chatInfo.getMessage()).apply();
        if (sender == -1 && !actived)makeNotification(friendInfo, chatInfo);
    }

    public void changeFriendName(FriendInfo friendInfo, String name) {
       getSharedPreferences(NAME_CHAT_LIST).edit().putString(friendInfo.getPhone(), name).apply();
    }

    public FriendInfo getUpdatedFriendInfo(FriendInfo friendInfo) {
        String name =getSharedPreferences(NAME_CHAT_LIST).getString(friendInfo.getPhone(), friendInfo.getName());
        return (new FriendInfo(name, friendInfo.getPhone()));
    }

    public void removeChat(FriendInfo friendInfo) {
       getSharedPreferences(NAME_CHAT_LIST).edit().remove(friendInfo.getPhone()).apply();
       removeSharedPreferences((NAME_CHAT_SEPARATOR) + friendInfo.getPhone());
    }

    public ArrayList<FriendInfo> readChatList() {
        ArrayList<FriendInfo> ret = new ArrayList<>();

        Map<String, ?> allDatas =getSharedPreferences(NAME_CHAT_LIST).getAll();
        Set<String> keys = allDatas.keySet();

        for (String curKey : keys) {
            ret.add(new FriendInfo(allDatas.get(curKey).toString(), curKey));
        }

        Collections.sort(ret, (o1, o2) -> {
            long o1LastChat =readLastChat(o1).getDateToLong();
            long o2LastChat =readLastChat(o2).getDateToLong();

            return Collator.getInstance().compare(String.valueOf(o1LastChat), String.valueOf(o2LastChat));
        });

        return ret;
    }

    public ArrayList<ChatInfo> readChat(FriendInfo friendInfo) {
        ArrayList<ChatInfo> ret = new ArrayList<>();

        Map<String, ?> allDatas =getSharedPreferences((NAME_CHAT_SEPARATOR) + friendInfo.getPhone()).getAll();
        Set<String> keys = allDatas.keySet();

        for (String curKey : keys) {
//           print("CurKey: " + curKey);
            String[] splitWithSeparator = curKey.split(Pattern.quote(KEY_CHAT_SENDER_SEPARATOR));
            String sender = (splitWithSeparator[0]);
            int senderInteger = (sender.equals(getMyPhone()) ? 1 : -1);
//           print("Sender: " + sender + ", SenderInt: " + senderInteger);
            String dateStr = (splitWithSeparator[1]);
            long date = ((senderInteger == 1) ? Math.abs(Long.valueOf(dateStr)) : -Math.abs(Long.valueOf(dateStr)));
//           print("Add to ret: Message(" + allDatas.get(curKey).toString() + "), Date(" + date + ")");
            ret.add(new ChatInfo(allDatas.get(curKey).toString(), date));
        }

        Collections.sort(ret, (o1, o2) -> {
            long o1Date = Math.abs(o1.getDateToLong());
            long o2Date = Math.abs(o2.getDateToLong());

            return Collator.getInstance().compare(String.valueOf(o1Date), String.valueOf(o2Date));
        });

        return ret;
    }

    // TODO: Develop Lighter (Have to Change Saving Algorithm)
    public ChatInfo readLastChat(FriendInfo friendInfo) {
        ArrayList<ChatInfo> chats = readChat(friendInfo);
        if (chats.size() <= 0) return new ChatInfo(NONE);
        return chats.get(chats.size() - 1);
    }

    public void makeNotification(FriendInfo friendInfo, ChatInfo chatInfo) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.putExtra("FriendInfo", friendInfo);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,REQUEST_CODE_CHAT,
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(friendInfo.getName())
                .setContentText(chatInfo.getMessage())
                .setContentIntent(pendingIntent)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification))
                .setVibrate(new long[]{0, 1000}) // [ 진동 전 대기시간, 진동시간 ] 반복
                .setLights(Color.BLUE, 3000, 3000)
                .setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            builder =makeNotificationChannel(builder);

        notificationManager.notify(1000, builder.build());
    }

    //    PART: Networking And SMS
    private final int TYPE_WIFI = 1;
    private final int TYPE_MOBILE = 2;
    private final int TYPE_NOT_CONNECTED = 0;
    private final int NETWORK_STATUS_NOT_CONNECTED = 0;
    private final int NETWORK_STATUS_WIFI = 1;
    private final int NETWORK_STATUS_MOBILE = 2;

    public boolean myNetworkStatus = false;
    public boolean friendNetworkStatus = false;

    private ValueEventListener friendNetworkStatusEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
               print("Friend is NOT in DB");
                friendNetworkStatus = false;
                ChatActivity.updateUI();
            } else {
                friendNetworkStatus = dataSnapshot.getValue().equals("Connected");
                ChatActivity.updateUI();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
           print("Check Friend Network Status Cancelled..");
            friendNetworkStatus = false;
            ChatActivity.updateUI();
        }
    };

    public void send(final FriendInfo friendInfo, final ChatInfo chatInfo) {
        if (myNetworkStatus && friendNetworkStatus) {
            sendWithNetwork(friendInfo, chatInfo);
        } else {
            sendWithSMS(friendInfo, chatInfo);
        }
    }

    private void sendWithSMS(FriendInfo friendInfo, ChatInfo chatInfo) {
        SmsManager.getDefault().sendTextMessage(friendInfo.getPhone(), null, chatInfo.getMessage(), null, null);
    }

    private void sendWithNetwork(final FriendInfo friendInfo, final ChatInfo chatInfo) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootReference = database.getReference();

        final DatabaseReference destReference = rootReference.child("Chats").child(friendInfo.getPhone());
        destReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String curKey = (getMyPhone() + (SEPARATOR) + String.valueOf(Math.abs(chatInfo.getDateToLong())));
                String message = chatInfo.getMessage();

                destReference.child(curKey).setValue(message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sendWithSMS(friendInfo, chatInfo);
            }
        });
    }

    private int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    private int getConnectivityStatusString(Context context) {
        int conn =getConnectivityStatus(context);
        int status = 0;
        if (conn ==TYPE_WIFI) {
            status = NETWORK_STATUS_WIFI;
        } else if (conn ==TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn ==TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }

    private boolean isNetworkConnected(Context context) {
        return (getConnectivityStatusString(context) != NETWORK_STATUS_NOT_CONNECTED);
    }

    public void updateNetworkStatus(Context context) {
        myNetworkStatus = isNetworkConnected(context);
    }

    public void startUpdateFriendNetworkStatus(FriendInfo friendInfo) {
        if (!myNetworkStatus) {
           print("Your Network is Disconnected");
            friendNetworkStatus = false;
        } else {
            FirebaseDatabase.getInstance().getReference().child("Users").child(friendInfo.getPhone()).addValueEventListener(friendNetworkStatusEventListener);
        }
    }

    public void stopUpdateFriendNetworkStatus(FriendInfo friendInfo) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(friendInfo.getPhone()).removeEventListener(friendNetworkStatusEventListener);
    }

    //    PART: Utility Method
    public boolean isServiceRunning(Class<?> sc) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
            if (sc.getName().equals(serviceInfo.service.getClassName())) return true;
        return false;
    }

    public boolean isServiceRunning(Context context, Class<?> sc) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
            if (sc.getName().equals(serviceInfo.service.getClassName())) return true;
        return false;
    }

    @SuppressLint("MissingPermission")
    public String getMyPhone(Context context) {
        String phone;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phone = telephonyManager.getLine1Number();

        phone = phone.replace("+82", "0");
        if (phone.startsWith("82")) phone = phone.replace("82", "");

        return phone;
        // TEMP: Test Mode (Without Real Device)
//        return "01099999999";
    }

    public String getMyPhone() {
        if (context == null) throw getNullPointerException();
        return getMyPhone(context);
    }

    public ArrayList<FriendInfo> getContacts(Context context) {
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
//               print("Contact Added Item: " + name + ", " + phoneNumber);
            } while (contactCursor.moveToNext());
        }

        return ret;
    }

    public Notification.Builder makeNotificationChannel(Notification.Builder builder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getSharedPreferences(NOTIFICATION_CHANNEL_NAME).getBoolean(NOTIFICATION_CHANNEL_ID, false)) {
                assert notificationManager != null;
                notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
               getSharedPreferences(NOTIFICATION_CHANNEL_NAME).edit().putBoolean(NOTIFICATION_CHANNEL_ID, true).apply();
            }
            builder.setChannelId("aMessage Notification ID");
        }
        return builder;
    }

    //    PART: Etc Method And Variable
    public final String NONE = "[$ NONE $]";
    public final String SEPARATOR = "_";

    public final String NOTIFICATION_CHANNEL_ID = "aMessage Notification ID";
    public final String NOTIFICATION_CHANNEL_NAME = "aMessage Notification";

    public void print(String m) {
        Log.d("AMESSAGE_DEBUG", m);
    }

    public void initActivity(Activity activity) {
       init(activity.getApplicationContext());
        print("Activity Init: " + activity.getClass().getSimpleName());

        if (!isServiceRunning(MainService.class)) {
           print("MainService is not running");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.getApplicationContext().startForegroundService(new Intent(activity.getApplicationContext(), MainService.class));
            } else {
                activity.getApplicationContext().startService(new Intent(activity.getApplicationContext(), MainService.class));
            }
        }
    }

    public long getCurrentTimeMills() {
        long ret;
        ret = System.currentTimeMillis();
        return ret;
    }

    public boolean checkNetworkConnectNow() {
        if (context == null) throw getNullPointerException();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return ((wifi.isAvailable() && wifi.isConnectedOrConnecting())
                || (mobile.isAvailable() && mobile.isConnectedOrConnecting()));
    }

    public void updateNetworkConnectNow() {
        myNetworkStatus = checkNetworkConnectNow();
        ChatActivity.updateUI();
    }

    // TODO: If it is NOT last version, stop Application
    public boolean checkUpdate(int versionCode) {
        return (getVersionCode() >= versionCode);
    }

    public String getVersionName() {
        PackageInfo packageInfo;

        try{
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return "NONE";
        }

        return packageInfo.versionName;
    }

    public int getVersionCode() {
        PackageInfo packageInfo;

        try{
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return 0;
        }

        return packageInfo.versionCode;
    }

    public NullPointerException getNullPointerException() {
        return new NullPointerException("Manager.getInstance().context is null");
    }
}
