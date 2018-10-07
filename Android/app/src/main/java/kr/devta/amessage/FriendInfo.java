package kr.devta.amessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FriendInfo implements Serializable {
    private String name;
    private String phone;

    public FriendInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
    public FriendInfo setName(String name) {
        this.name = name;
        return this;
    }
    public FriendInfo setPhone(String phone) {
        this.phone = phone;
        return this;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public Map<String, String> getAll() {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("Name", name);
        ret.put("Phone", phone);
        return ret;
    }
}
