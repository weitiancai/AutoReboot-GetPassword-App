package com.example.getpassword;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String letPasswordBroadcast = "action.getPasswordBroadcast";//让密码广播！
    public static final String rebootBroadcast = "action.rebootActivity";

    private Button send;
    private Button read;
    private Button get;
//    private Button cancelButton,continueButton;
    private Switch mSwitch;
    public TextView tv;

    private String TAG = "MainActivity";
    //final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String destinationAddress = "106593005";
//    public static final String destinationAddress ="10086";
    public static final String MESSAGE = "mm";
   // public static final String MESSAGE ="查询余额";
    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

    private sendReceiver mSendReceiver;//有没有发送成功
    private oppositeReceiver mOppositeReceiver;//对方有没有接收到
    private myReceiver mMyReceiver;
    //private rebootReceiver mRebootReceiver;//改静态注册

    //private IncomingSMSReceiver incomingSMSReceiver;//收到选短信的广播接收器

    private static PendingIntent sentPI, deliverPI;//,receiverPI;

    private List<String> permissionList;

    private Uri SMS_INBOX = Uri.parse("content://sms/");

    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "weimeng MainActivity onCreate: 主界面创建 ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
//            actionBar.hide();//不隐藏了
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        send = (Button) findViewById(R.id.send);
        read = (Button) findViewById(R.id.read);
        get = (Button) findViewById(R.id.get);
        tv = (TextView) findViewById(R.id.tv);
        mSwitch = (Switch) findViewById(R.id.switchToSend);
        send.setOnClickListener(this);
        read.setOnClickListener(this);
        get.setOnClickListener(this);
//        cancelButton.setOnClickListener(this);
//        continueButton.setOnClickListener(this);
        //注册发送短信的状态 的广播接收器

        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, sentIntent, 0);
        mSendReceiver = new sendReceiver();
        registerReceiver(mSendReceiver, new IntentFilter(SENT_SMS_ACTION));
        //注册对方接收到短信状态的 广播接收器
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        mOppositeReceiver = new oppositeReceiver();
        registerReceiver(mOppositeReceiver, new IntentFilter(DELIVERED_SMS_ACTION));
        //注册incomingSMSReceiver
        //Intent incomingIntent = new Intent(SMS_RECEIVED);
        //receiverPI = PendingIntent.getBroadcast(this,0,incomingIntent,0);

        //incomingSMSReceiver = new IncomingSMSReceiver();
        //registerReceiver(incomingSMSReceiver,new IntentFilter(SMS_RECEIVED));
        //弃用动态注册的广播接收器，使用静态注册，因为要求产品在后台可以接收到广播

//        IntentFilter filter = new IntentFilter(SMS_RECEIVED);
//
//        BroadcastReceiver receiver = new IncomingSMSReceiver();
//        registerReceiver(receiver,filter);
        mMyReceiver = new myReceiver();//用于接受返回的密码等内容
        registerReceiver(mMyReceiver,new IntentFilter(letPasswordBroadcast));

        //mRebootReceiver = new rebootReceiver();
        //registerReceiver(mRebootReceiver,new IntentFilter(rebootBroadcast));
        //权限表
        permissionList = new ArrayList<>();

        askPermissions();//        运行时权限 (动态权限申请)
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);//list-->String
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }

        //rebootBroadcastReceiver 会发来Intent
        Intent rebootIntent = getIntent();
        String name = rebootIntent.getStringExtra("name");
        if(name!=null&&name.equals("rebootIntent")){//短路机制，前一个条件不满足自动跳过if
            Log.d(TAG, "收到重启广播 的 命令并已重启，将发送短信");
            sendMessage(destinationAddress,MESSAGE);
        }
        else{
            Log.d(TAG,"没有收到rebootIntent");
        }
        /*处理switch功能*/
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//switch 改变时记录状态
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putBoolean("ContinueSend",isChecked);//存储选中的状态。
                editor.apply();//这句又忘了。。牢记牢记
                Log.d(TAG, "weimeng 保存状态continueSend="+isChecked);
            }
        });

        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        boolean flag = pref.getBoolean("ContinueSend",true);//默认重复发送
        //为了保存switch的状态
        mSwitch.setChecked(flag);
        //之后在sendMessageService里取出 data文件里的数据值赋给 continueSend;

        //显示之前保存的密码和那个时间
        showText();



    }
    //显示通知的通用函数组件
    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title, String content){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setContentText(content);
        return builder.build();

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
//                doSendSMSTO(destinationAddress, "mm");

//                sendMessage(destinationAddress,MESSAGE);//发送短信
                Intent intent = new Intent(MainActivity.this,sendMessageService.class);
                startService(intent);
                break;
            case R.id.read:
                getSmsFromPhone();
                break;
            case R.id.get:
                showText();
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();


        switch(item.getItemId()){
            case android.R.id.home:
                //这个键永远叫 home 内置的
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.explain:
                tv.setText(R.string.explainText);
            default:
                break;
        }
        editor.apply();
        return true;
    }

    public void doReadSMS(View view){
        Cursor cursor = null;
        cursor = managedQuery(Uri.parse("content://sms/inbox"),new String[]{
                "_id","address","body","read"
        },"address=? and read=?",new String[]{destinationAddress,"1"},"data desc");
        if(cursor!=null){
            cursor.moveToFirst();
            if(cursor.moveToFirst()){

            }
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();//toolbar要显示选项按钮
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.set_menu,menu);
        return true;
    }


    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[] { "body" };//"_id", "address", "person",, "date", "type
        String where = "address = '106593005'";
//        String where = " address = '10086' " ;
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
//        showToast(""+cur.toString());
        //此处读取内容指定为 where 失败了， cur.query第三个 selection 参数指定为where  读取不到不会返回null。。。
        if (null == cur) {
            showToast("mei");
            tv.setText("没有收到过来自" + destinationAddress + "的短信！");
            return;
        }
        if(cur.moveToNext()) { // cur初始是-1 moveToNext后为0 另外 moveToFirst也是0 代表数据库第一行
// int number = cur.getInt(cur.getColumnIndex("address"));//手机号
// String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));
            Log.d(TAG, "getSmsFromPhone: "+body);
            tv.setText(body);
//这里我是要获取自己短信服务号码中的验证码~~
            Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                String res = matcher.group().substring(1, 11);
                Log.d(TAG, "getSmsFromPhoneContent: "+res);
            }
        }
    }

    public void askPermissions(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_SMS)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_SMS);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECEIVE_SMS)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECEIVE_SMS);
        }
        //多的同样加进去
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length>0){//多项危险权限动态申请的代码
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            showToast("拒绝权限将无法使用程序");
                            finish();
                            return;
                        }
                    }
                }
                else{
                    showToast("发生未知错误");
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /*
     *调用系统功能发送短信
     * @param phoneNumber
     * @param message
     * 会弹出短信发送页面 这个代码没有解放人的脑子 和手，不好。。
     */
    public void doSendSMSTO(String phoneNumber, String message) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }
    }

    /*
     *调用系统短信接口发送短信
     */
    public static void sendMessage(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, MainActivity.sentPI, MainActivity.deliverPI);
        }
    }


    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    class sendReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    showToast("发送成功");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    showToast("发送失败");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    break;
                default:
                    break;
            }
        }
    }

    class oppositeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showToast("收件人接收成功");
        }
    }
    /*
     *调用系统短信接口发送短信
     */
    // 对短信发送状态  广播接收器
    //private SmsStatusReceiver mSmsStatusReceiver;

    //对短信接受状态  广播接收器
    //private SmsDelliveryStatusReceiver mSmsDeliveryStatusReceiver;
    /*
    * 用于接受password的接收器
    * */
    class myReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String password = intent.getStringExtra("password");
            String time = intent.getStringExtra("time");
            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putString("password",password);
            editor.putString("time",time);
            editor.apply();
            showText();
            getNotificationManager().notify(1,getNotification("闪讯助手密码更新","password:"+password));
        }
    }
    private void showText(){
        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        String password = pref.getString("password","not get yet");
        String time = pref.getString("time","not get yet");
        tv.setText("密码: "+password+"\n上次更新时间："+time);
    }
    /* //思路图里改静态注册一个接收器
    class rebootReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "weimeng 收到rebootReceiver要发送短信了");
            sendMessage(destinationAddress,MESSAGE);
        }
    }*/


/*这个已经弃用
    //接收到短信的广播接收器
    class IncomingSMSReceiver extends BroadcastReceiver

    {

        private static final String queryString = "2018";

        //private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

        public void onReceive(Context _context, Intent _intent) {
            //showToast("111我接收到了！！！");
            if (_intent.getAction().equals(SMS_RECEIVED)) {
                SmsManager sms = SmsManager.getDefault();
                Bundle bundle = _intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++)
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    for (SmsMessage message : messages) {
                        String msg = message.getMessageBody();
                        String to = message.getOriginatingAddress();
                        if (msg.toLowerCase().startsWith(queryString)) {
                            //String out = msg.substring(queryString.length());
                            Log.d(TAG, "这是我写的"+msg);
                            //sms.sendTextMessage(to, null, "mm", null, null);
                        }
                    }
                }
            }
        }
    }*/

    @Override
    protected void onDestroy() {
//        unregisterReceiver(mRebootReceiver);
        unregisterReceiver(mOppositeReceiver);
        unregisterReceiver(mSendReceiver);
        unregisterReceiver(mMyReceiver);
        //取消注册
        super.onDestroy();
    }

}
