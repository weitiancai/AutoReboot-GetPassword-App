package com.example.getpassword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class rebootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intentToStartMainActivity = new Intent(context,MainActivity.class);
        intentToStartMainActivity.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        intentToStartMainActivity.putExtra("name","rebootIntent");
        context.startActivity(intentToStartMainActivity);//启动MainActivity
    }
}
