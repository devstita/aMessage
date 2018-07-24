package kr.devta.amessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    //    public static final String KEY_CHAT_LIST_STRING_SET = "Key_ChatListStringSet";
//
    public static final String NAME_CHAT_SEPARATOR = "[$ NAME_CHAT $]";
    public static final String KEY_CHAT_SENDER_SEPARATOR = "$";

    public static SharedPreferences getSharedPreferences(String name) {

        return Manager.context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    //    Chat Management
    public static void addChatList(FriendInfo friendInfo) {
        /* Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getStringSet(Manager.KEY_CHAT_LIST_STRING_SET, null);
        if (previousList == null) previousList = new HashSet<>();

        String toString = Manager.serializableToString(friendInfo);
        Manager.print("Manager.addChatList: " + friendInfo.getName() + ", " + toString);
        previousList.add(toString);

        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().putStringSet(Manager.KEY_CHAT_LIST_STRING_SET, previousList).apply(); */

        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().putString(friendInfo.getPhone(), friendInfo.getName()).apply();
    }

    public static void addChat(int sender, FriendInfo friendInfo, ChatInfo chatInfo) { // sender -> 1: Me, -1: Friend
        /* Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getStringSet(friendInfo.getPhone(), null);
        if (previousList == null) previousList = new HashSet<>();

        previousList.add(Manager.serializableToString(chatInfo));

        Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).edit().putStringSet(friendInfo.getPhone(), previousList).apply(); */

        Manager.getSharedPreferences(Manager.NAME_CHAT_SEPARATOR + friendInfo.getPhone()).edit().putString(
                ((sender == 1) ? Manager.getMyPhone() : friendInfo.getPhone()) + Manager.KEY_CHAT_SENDER_SEPARATOR + String.valueOf(chatInfo.getDateToLong())
                , chatInfo.getMessage()).apply();
    }

    public static ArrayList<FriendInfo> readChatList() {
        /* Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getStringSet(Manager.KEY_CHAT_LIST_STRING_SET, null);
        ArrayList<FriendInfo> ret = new ArrayList<>();

        if (previousList != null && !(previousList.isEmpty())) {
            Iterator<String> iterator = previousList.iterator();
            while (iterator.hasNext()) {
                String curItemWithString = iterator.next();
                Manager.print("readChatList: " + curItemWithString);
                FriendInfo curItem = (FriendInfo) Manager.stringToSerializable(curItemWithString);
//                Manager.print(curItem.getName() + ", " + curItem.getPhone());
                ret.add(curItem);
            }

            if (ret.size() > 1) {
                Collections.sort(ret, new Comparator<FriendInfo>() {
                    @Override
                    public int compare(FriendInfo o1, FriendInfo o2) {
                        ArrayList<ChatInfo> first = Manager.readChat(o1);
                        ArrayList<ChatInfo> second = Manager.readChat(o2);

                        Collections.sort(first, new Comparator<ChatInfo>() {
                            @Override
                            public int compare(ChatInfo o1, ChatInfo o2) {
                                int compare = o1.getDate().compareTo(o2.getDate());
                                int ret = 0;

                                if (compare > 0) { // o1.getDate 가 o2.getDate() 보다 나중
                                    ret = -1;
                                } else if (compare < 0) { // o1.getDate 가 o2.getDate() 보다 일찍
                                    ret = 1;
                                } else { // o1.getDate 와 o2.getDate() 가 동시
                                    ret = 0;
                                }

                                return ret;
                            }
                        });

                        Collections.sort(second, new Comparator<ChatInfo>() {
                            @Override
                            public int compare(ChatInfo o1, ChatInfo o2) {
                                int compare = o1.getDate().compareTo(o2.getDate());
                                int ret = 0;

                                if (compare > 0) { // o1.getDate 가 o2.getDate() 보다 나중
                                    ret = -1;
                                } else if (compare < 0) { // o1.getDate 가 o2.getDate() 보다 일찍
                                    ret = 1;
                                } else { // o1.getDate 와 o2.getDate() 가 동시
                                    ret = 0;
                                }

                                return ret;
                            }
                        });

                        int compare = first.get(first.size() - 1).getDate().compareTo(second.get(second.size() - 1).getDate());
                        int ret = 0;
                        if (compare > 0) { // first 가 second 보다 나중
                            ret = 1;
                        } else if (compare < 0) { // first 가 second 보다 일찍
                            ret = -1;
                        } else { // first 와 second 가 동시
                            ret = 0;
                        }

                        return ret;
                    }
                });
            }
        } */

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
        /* Map<String, ?> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT_LIST).getAll();
        ArrayList<ChatInfo> ret = new ArrayList<>();

        if (previousList != null && !previousList.isEmpty()) {
            Iterator<String> iterator = previousList.iterator();
            while (iterator.hasNext()) {
                ret.add((ChatInfo) stringToSerializable(iterator.next()));
            }

            Collections.sort(ret, new Comparator<ChatInfo>() {
                @Override
                public int compare(ChatInfo o1, ChatInfo o2) { // ret 1: 앞 원소가 먼저, ret -1: 뒤 원소가 먼저, ret 0: Anyway
                    int compare = o1.getDate().compareTo(o2.getDate());
                    int ret = 0;
                    if (compare > 0) { // first 가 second 보다 나중
                        ret = 1;
                    } else if (compare < 0) { // first 가 second 보다 일찍
                        ret = -1;
                    } else { // first 와 second 가 동시
                        ret = 0;
                    }

                    return ret;
                }
            });
        } */

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
