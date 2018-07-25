package kr.devta.amessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Manager {
    //    Init Method And Variable
    private static Context context;
    public static void init(Context context) {
        Manager.context = context;
    }

    //    Intent Request Code
    public static final int REQUEST_CODE_FIREBASE_LOGIN = 1000;
    public static final int REQUEST_CODE_ADD_FRIEND = 1001;
    public static final int REQUEST_CODE_CONTACT_INTENT = 1002;
    public static final int REQUEST_CODE_CHAT = 1003;

    //    SharedPreferences
    public static final String NAME_TUTORIAL = "Name_Tutorial";
    public static final String KEY_SAW_TUTORIAL = "Key_SawTutorial";

    public static final String NAME_CHAT_LIST = "Name_ChatList";

    public static final String NAME_CHAT_SEPARATOR = "[$ NAME_CHAT $]";
    public static final String KEY_CHAT_SENDER_SEPARATOR = "$";

    public static SharedPreferences getSharedPreferences(String name) {

        return Manager.context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    //    Chat Management
    public static void addChatList(FriendInfo friendInfo) {
        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().putString(friendInfo.getPhone(), friendInfo.getName()).apply();
    }

    public static void addChat(int sender, FriendInfo friendInfo, ChatInfo chatInfo) { // sender -> 1: Me, -1: Friend
        Manager.getSharedPreferences(Manager.NAME_CHAT_SEPARATOR + friendInfo.getPhone()).edit().putString(
                ((sender == 1) ? Manager.getMyPhone() : friendInfo.getPhone()) + Manager.KEY_CHAT_SENDER_SEPARATOR + String.valueOf(chatInfo.getDateToLong())
                , chatInfo.getMessage()).apply();
    }

    public static ArrayList<FriendInfo> readChatList() {
        ArrayList<FriendInfo> ret = new ArrayList<>();

        Map<String, ?> allDatas = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getAll();
        Set<String> keys = allDatas.keySet();

        Iterator<String> keysIterator = keys.iterator();
        while (keysIterator.hasNext()) {
            String curKey = keysIterator.next();
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

        Map<String, ?> allDatas = Manager.getSharedPreferences(Manager.NAME_CHAT_SEPARATOR + friendInfo.getPhone()).getAll();
        Set<String> keys = allDatas.keySet();

        Iterator<String> keysIterator = keys.iterator();
        while (keysIterator.hasNext()) {
            String curKey = keysIterator.next();
            String sender = (curKey.split(Manager.KEY_CHAT_SENDER_SEPARATOR)[0]);
            int senderInteger = (sender.equals(getMyPhone()) ? 1 : -1);
            String date = (curKey.split(Manager.KEY_CHAT_SENDER_SEPARATOR)[1]);
            ret.add(new ChatInfo(allDatas.get(keys).toString(), (Long.valueOf(date) * senderInteger)));
        }

        Collections.sort(ret, new Comparator<ChatInfo>() {
            @Override
            public int compare(ChatInfo o1, ChatInfo o2) {
                long o1Date = o1.getDateToLong();
                long o2Date = o2.getDateToLong();

                return Collator.getInstance().compare(o1Date, o2Date);
            }
        });

        return ret;
    }

    public static ChatInfo readLastChat(FriendInfo friendInfo) {
        ArrayList<ChatInfo> chats = readChat(friendInfo);
        if (chats.size() <= 0) return new ChatInfo(Manager.NONE);
        return chats.get(chats.size() - 1);
    }

//    Networking And SMS
    public static void send(FriendInfo friendInfo, ChatInfo chatInfo) {
        boolean myNetworkStatus = checkNetworkStatus();
        final boolean[] friendNetworkStatus = {false};

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootReference = database.getReference();

        DatabaseReference friendStatusReference = rootReference.child("Users").child(friendInfo.getPhone());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String enableTime = dataSnapshot.getValue().toString();
                if (Math.abs(Manager.getCurrentTimeMills() - Long.valueOf(enableTime)) <= 250) friendNetworkStatus[0] = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        friendStatusReference.addValueEventListener(valueEventListener);
        friendStatusReference.removeEventListener(valueEventListener);

        if (myNetworkStatus && friendNetworkStatus[0]) { // Network Message
            DatabaseReference destReference = rootReference.child("Sender");
        } else { // SMS Message
            SmsManager.getDefault().sendTextMessage(friendInfo.getPhone(), null, chatInfo.getMessage(), null, null);
        }
    }

    public static boolean checkNetworkStatus() {
        boolean ret = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null)
            ret = networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

        return ret;
    }

//    Utility Method
    public static String serializableToString(@NonNull Object obj) {
        String ret = Manager.NONE;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new Base64OutputStream(baos, Base64.NO_PADDING | Base64.NO_WRAP));
            oos.writeObject(obj);
            oos.flush();
            ret = baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static Object stringToSerializable(@NonNull String obj) {
        Object ret = null;

        try {
            ret = new ObjectInputStream(new Base64InputStream(new ByteArrayInputStream(obj.getBytes()), Base64.NO_PADDING | Base64.NO_WRAP)).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static boolean isServiceRunning(Class<?> sc) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
            if (sc.getName().equals(serviceInfo.service.getClassName())) return true;
        return false;
    }

    @SuppressLint("MissingPermission")
    public static String getMyPhone() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phone = Manager.NONE;

        if (telephonyManager.getLine1Number() != null) phone = telephonyManager.getLine1Number();
        else if (telephonyManager.getSimSerialNumber() != null) phone = telephonyManager.getSimSerialNumber();

        phone.replace("+82", String.valueOf(0));
        if (phone.startsWith("82")) phone.replace("82", "");

        return phone;
    }

//        Etc Method And Variable
    public static final String NONE = "[$ NONE $]";
    public static final String DATE = "[$ DATE $]";

    public static void print(String m) {
        Log.d("AMESSAGE_DEBUG", m);
    }

    public static void showActivityName(Activity activity) {
        print("Activity Created: " + activity.getClass().getSimpleName());
    }

    public static long getCurrentTimeMills() {
        long ret = 0L;
//        LocationManager locationManager = (LocationManager) Manager.context.getSystemService(Manager.context.LOCATION_SERVICE);
//        @SuppressLint("MissingPermission") ret = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime();
        ret = System.currentTimeMillis();
        return ret;
    }
}
