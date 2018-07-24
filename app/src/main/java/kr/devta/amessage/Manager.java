package kr.devta.amessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

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

//    SharedPreferences
    public static final String NAME_TUTORIAL = "Name_Tutorial";
    public static final String KEY_SAW_TUTORIAL = "Key_SawTutorial";

    public static final String NAME_CHAT = "Name_Chat";
    public static final String KEY_CHAT_LIST_STRING_SET = "Key_ChatListStringSet";

    public static SharedPreferences getSharedPreferences(String name) {

        return Manager.context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

//    Chat Management
    public static void addChatList(FriendInfo friendInfo) {
        Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT).getStringSet(Manager.KEY_CHAT_LIST_STRING_SET, null);
        if (previousList == null) previousList = new HashSet<>();

        previousList.add(Manager.serializableToString(friendInfo));

        Manager.getSharedPreferences(Manager.NAME_CHAT).edit().putStringSet(Manager.KEY_CHAT_LIST_STRING_SET, previousList).apply();
    }

    public static void addChat(FriendInfo friendInfo, ChatInfo chatInfo) {
        Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT).getStringSet(friendInfo.getPhone(), null);
        if (previousList == null) previousList = new HashSet<>();

        previousList.add(Manager.serializableToString(chatInfo));

        Manager.getSharedPreferences(Manager.NAME_CHAT).edit().putStringSet(friendInfo.getPhone(), previousList).apply();
    }

    public static ArrayList<FriendInfo> readChatList() {
        Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT).getStringSet(Manager.KEY_CHAT_LIST_STRING_SET, null);
        ArrayList<FriendInfo> ret = new ArrayList<>();

        if (previousList != null && !(previousList.isEmpty())) {
            Iterator<String> iterator = previousList.iterator();
            while (iterator.hasNext()) ret.add((FriendInfo) Manager.stringToSerializable(iterator.next()));

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

        return ret;
    }

    public static ArrayList<ChatInfo> readChat(FriendInfo friendInfo) {
        Set<String> previousList = Manager.getSharedPreferences(Manager.NAME_CHAT).getStringSet(friendInfo.getPhone(), null);
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
        }

        return ret;
    }

    public static ChatInfo readLastChat(FriendInfo friendInfo) {
        ArrayList<ChatInfo> chats = readChat(friendInfo);
        if (chats.size() <= 0) return new ChatInfo(Manager.NONE);
        return chats.get(chats.size() - 1);
    }

//    Utility Method
    public static String serializableToString(Object obj) {
        String ret = Manager.NONE;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            ret = baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static Object stringToSerializable(String obj) {
        Object ret = null;

        try {
            byte bytes[] = obj.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            ret = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

//    Etc Method And Variable
    public static final String NONE = "[$ NONE $]";

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
