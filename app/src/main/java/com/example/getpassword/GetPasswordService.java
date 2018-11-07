package com.example.getpassword;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetPasswordService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.getpassword.action.FOO";
    private static final String ACTION_BAZ = "com.example.getpassword.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.getpassword.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.getpassword.extra.PARAM2";

    public GetPasswordService() {
        super("GetPasswordService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GetPasswordService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GetPasswordService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }



    //乱七八糟一堆，有用的就下面这个
    String body="",time="";
    String TAG="GetPassWordService";
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "weimeng 得到密码了");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
        body = intent.getStringExtra("body");
        time = intent.getStringExtra("time");
        Log.d(TAG, "weimeng Get body :"+body);
        Log.d(TAG, "weimeng Get time"+time);
        new Thread(new Runnable() {
            @Override
            public void run() {
            String regEx = "[0-9]{6}";//10086
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(body);
            String send;
            if(matcher.find()){
                send =  matcher.group();
            }else {
                send = "没有找到匹配数字";
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(MainActivity.letPasswordBroadcast);//这里广播的 action 一定要记住，不要搞混了
            sendIntent.putExtra("password",send);
            sendIntent.putExtra("time",time);
                //1 包名 2 接收器类名
//            sendIntent.setComponent(new ComponentName("com.example.getpassword",
//                        "com.example.getpassword.MainActivity.myReceiver"));//android8.0要设置的
            sendBroadcast(sendIntent);//轮了一圈再发回 MainActivity
            }
        }).start();


    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
