package com.example.doran;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
                                                //FirebaseMessagingService도 서비스클래스이며 푸시 메세지를 전달받는 역할을 담당한다.
    private static final String TAG = "FMS";

    String msg,title;
    public MyFirebaseMessagingService() {
    }


    @Override
    public void onNewToken(String token){
        /* 새로운 토큰을 확인했을 때 호출되는 메서드
        * - 이 앱이 Firebase서버에 등록되었을때 호출됨
        * - 파라미터의 등록정보는 이 앱의 등록id를 의미함*/
        super.onNewToken(token);
        Log.d(TAG, "onNewToken 호출됨: "+token);

    }

    public void onMessageReceived(RemoteMessage remoteMessage){
        /*새로운 메세지를 받았을 때 호출되는 메소드
        * 클라우드 서버에서 보내오는 메세지는 FirebaseMessagingService에서 받고
        * onMessageReceived() 메서드가 자동으로 호출된다.
        * 여기서 받아온 메세지를 됨*/
        Log.d(TAG, "onMessageReceived() 호출됨 ㅇㅇ: ");

        title = remoteMessage.getNotification().getTitle();
        msg = remoteMessage.getNotification().getBody();

        Intent intent = new Intent(this,ChattingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //노피티케이션 눌렀을때
        PendingIntent contentIntent = PendingIntent.getActivity(this,0
                ,new Intent(intent),PendingIntent.FLAG_ONE_SHOT); //원래는 0



        //노티피케이션 설정
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = null;
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        // 오레오 이상 버전 채널 추가
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(TAG, "onMessageReceived에서  오레오 이상 버전 코드 실행되나");
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
//
//            // Configure the notification channel.
//            notificationChannel.setDescription("Channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            notificationChannel.enableVibration(true);
//            notificationManager.createNotificationChannel(notificationChannel); //채널생성
//
//            builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());


//====================================================================
            String channelId = "default_channel_id";
            String channelDescription = "Default Channel";
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelDescription, importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            builder = new NotificationCompat.Builder(this, channelId);



        }else{
            Log.d(TAG, "onMessageReceived에서  오레오 해당안됨");
            builder = new NotificationCompat.Builder(getApplicationContext());
        }


        //기존
//        NotificationCompat.Builder
//                builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher)
        builder = builder
                .setContentTitle(title) //푸시알림떴을때의 제목
                .setContentText(msg) // 내용
                .setAutoCancel(true) //자동닫기
                .setSmallIcon(R.mipmap.ic_launcher) //이미지 아이콘 필수로 들어가야함
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) //알림소리
                .setVibrate(new long[]{1,1000}); //1초동안 진동 울려라
        Log.d(TAG, "onMessageReceived에서 builder생성?");

        notificationManager.notify(0,builder.build());

        builder.setContentIntent( contentIntent); //아까만든 팬딩인텐트
        Log.d(TAG, "onMessageReceived() 호출종료 ");






    }

}
