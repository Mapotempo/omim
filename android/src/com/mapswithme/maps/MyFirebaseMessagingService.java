package com.mapswithme.maps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

  private static final String TAG = "MyFirebaseMsgService";

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage)
  {
    //Displaying data in log
    //It is optional
     Log.d(TAG, "From: " + remoteMessage.getFrom());
    Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

    Map<String, String> data = remoteMessage.getData();
    String url = data.get("Data");

    //Calling method to generate notification
    sendNotification(remoteMessage.getNotification().getBody(), url);
  }

  //This method is only generating push notification
  //It is same as we did in earlier posts
  private void sendNotification(String messageBody, final String url)
  {
    Intent intent = new Intent(this, SplashActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    Uri myUri = Uri.parse(url);
    intent.setData(myUri);
    getBaseContext().startActivity(intent);

//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle("Firebase Push Notification")
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0, notificationBuilder.build());
  }
}
