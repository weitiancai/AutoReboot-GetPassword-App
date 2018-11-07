package com.example.getpassword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {
    private String TAG = "SmsReceiver";
    MainActivity activity;
    private static final String queryString = "尊敬的";//"2018";
    private static final String phoneNumber = "106593005";//"10086";
    @Override
    public void onReceive(Context context, Intent intent) {

//        Toast.makeText( "222我接收到了", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "weimeng onReceive: 广播接收器接收到了");
        Bundle bundle = intent.getExtras();
        SmsMessage msg;
        if(null!=bundle)
        {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                String messageBody = msg.getMessageBody();
                Log.d("SmsReceiver", "Received:  number:" + msg.getOriginatingAddress()
                        + " body:" + messageBody + " time:"
                        + receiveTime);

//在这里写自己的逻辑
                if (msg.getOriginatingAddress().equals(phoneNumber) &&
                        messageBody.toLowerCase().startsWith(queryString)) {
                    //是这个号码&& 是这个短信内容
                    Intent serviceIntent = new Intent(context,GetPasswordService.class);
                    serviceIntent.putExtra("body",messageBody);
                    serviceIntent.putExtra("time",receiveTime);

                    context.startService(serviceIntent);//去 GetPasswordService.class
                    //此处context是指 我想搞清楚
                   // Log.d(TAG, "此处context是指 weimeng"+context.toString());
                    //android.app.ReceiverRestrictedContext
                   // Log.d(TAG, "applicationContext weimeng"+context.getApplicationContext().toString());
                    //android.app.Application@4651f82
                }
            }
        }
    }
}

