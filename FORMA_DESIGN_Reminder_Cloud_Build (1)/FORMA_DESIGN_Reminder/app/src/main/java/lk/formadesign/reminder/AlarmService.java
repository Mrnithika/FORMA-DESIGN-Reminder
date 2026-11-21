package lk.formadesign.reminder;

import android.app.*;
import android.content.*;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.graphics.Color;

public class AlarmService extends Service {
    private Ringtone ringtone;
    private static final String CHANNEL="forma_alarm_channel";
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel ch=new NotificationChannel(CHANNEL,"FORMA Reminder Alarms",NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Reminder alerts from FORMA DESIGN");
            ch.enableVibration(true); ch.setLightColor(Color.YELLOW);
            nm.createNotificationChannel(ch);
        }
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        long id=intent==null?0:intent.getLongExtra("id",0);
        String title=intent==null?"Reminder":intent.getStringExtra("title");
        String note=intent==null?"":intent.getStringExtra("note");
        if(title==null || title.trim().isEmpty()) title="Reminder";
        Intent stop=new Intent(this,AlarmService.class).setAction("STOP_ALARM");
        PendingIntent stopPi=PendingIntent.getService(this,2,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent openPi=PendingIntent.getActivity(this,3,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        Notification n=b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
          .setContentTitle("FORMA DESIGN • "+title).setContentText(note.isEmpty()?"It's time for your reminder":note)
          .setStyle(new Notification.BigTextStyle().bigText(note.isEmpty()?"It's time for your reminder":note))
          .setContentIntent(openPi).setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX)
          .setOngoing(true).addAction(android.R.drawable.ic_media_pause,"STOP",stopPi).build();
        startForeground(77,n);
        if("STOP_ALARM".equals(intent==null?null:intent.getAction())) { stopSelf(); return START_NOT_STICKY; }
        try {
            Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone=RingtoneManager.getRingtone(this,uri);
            if(ringtone!=null) {
                if(Build.VERSION.SDK_INT>=28) ringtone.setLooping(true);
                ringtone.play();
            }
        } catch(Exception ignored){}
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() {
        if(ringtone!=null && ringtone.isPlaying()) ringtone.stop();
        stopForeground(true); super.onDestroy();
    }
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
