package kr.devta.amessage;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatInfo implements Serializable {
    private String message;
    private long date;

    public ChatInfo(String message, long date) {
        this.message = message;
        this.date = date;
    }

    public ChatInfo(String message) {
        this.message = message;
        long date = Manager.getCurrentTimeMills();
    }

    public ChatInfo setMessage(String message) {
        this.message = message;
        return this;
    }
    public ChatInfo setDate(Date date) {
        this.date = date;
        return this;
    }
    public String getMessage() {
        return message;
    }
    public Date getDate() {
        return new Date(date);
    }
    public long getDateToLong() {
        return date;
    }
    public String getDateWithFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a h:m");
        return simpleDateFormat.format(getDateToLong());
    }
}
