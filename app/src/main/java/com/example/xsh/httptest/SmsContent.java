package com.example.xsh.httptest;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xsh on 16-9-23.
 */
public class SmsContent extends ContentObserver {


    public static final String SMS_URI_INBOX = "content://sms/inbox";
    private Activity activity = null;
    private String smsContent = "";
    private EditText verifyText = null;
    private String mMmsNum = "1069095599";
    public String sms = null;

    public SmsContent(Activity activity, Handler handler) {
        super(handler);
        this.activity = activity;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;// 光标
        // 读取收件箱中指定号码的短信
        Log.d("xush", "smsbody======================= onchange");
        cursor = activity.managedQuery(Uri.parse(SMS_URI_INBOX), new String[] { "_id", "address", "body", "read" }, "address=? and read=?",
                new String[] { "1069095599", "0" }, "date desc");

        if (cursor != null) {// 如果短信为未读模式
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {

                String smsbody = cursor.getString(cursor.getColumnIndex("body"));
                Log.d("xush", "smsbody=======================" + smsbody);
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(smsbody.toString());
                smsContent = m.replaceAll("").trim().toString();
                sms = smsContent;
                synchronized (activity) {
                    activity.notify();
                }
            }

        }

    }

}