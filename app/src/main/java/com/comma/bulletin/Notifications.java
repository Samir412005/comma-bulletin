package com.comma.bulletin;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import java.util.*;

public final class Notifications {
 public static void create(Context c){if(Build.VERSION.SDK_INT>=26)c.getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel("updates","الإعلانات الجديدة",NotificationManager.IMPORTANCE_DEFAULT));}
 public static void show(Context c,List<Item> items){
  create(c);
  if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  if(!NotificationManagerCompat.from(c).areNotificationsEnabled())return;
  Item first=items.get(0); Intent intent=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent p=PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  String title=items.size()==1?first.title:"لديك "+items.size()+" إعلانات جديدة";
  try{NotificationManagerCompat.from(c).notify(100,new NotificationCompat.Builder(c,"updates").setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(first.title).setStyle(new NotificationCompat.BigTextStyle().bigText(first.body)).setContentIntent(p).setAutoCancel(true).build());}catch(SecurityException ignored){}
 }
}
