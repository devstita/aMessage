package kr.devta.amessage;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatInfo implements Serializable {
    private String message;
    private long date;

    public ChatInfo(String message, long date) {
        this.message = message;
        this.date = date;
    }

    public ChatInfo(String message) {
        this.message = message;
        this.date = Manager.getInstance().getCurrentTimeMills();
    }

    public ChatInfo setMessage(String message) {
        this.message = message;
        return this;
    }
    public ChatInfo setDate(Date date) {
        this.date = date.getTime();
        return this;
    }
    public ChatInfo setDate(long date) {
        this.date = date;
        return this;
    }
    public String getMessage() {
        return message;
    }
    public Date getDate() {
        return new Date(((date > 0) ? date : -date));
    }
    public long getDateToLong() {
        return date;
    }
    public String getDateWithFormat() {
        String month = new SimpleDateFormat("M").format(getDate());
        String day = new SimpleDateFormat("d").format(getDate());
        String ampm = new SimpleDateFormat("a", Locale.US).format(getDate());
        String hour = new SimpleDateFormat("hh", Locale.US).format(getDate());
        String minute = new SimpleDateFormat("mm", Locale.US).format(getDate());

        return (month + "월 " + day + "일 " + ampm + " " + hour + ":" + minute);
    }
}
