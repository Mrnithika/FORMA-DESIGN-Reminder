
package lk.formadesign.reminder;

import android.app.*;
import android.content.*;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.*;
import android.graphics.Color;

public class AlarmService extends Service {

    private Ringtone ringtone;

    // New channel ID because Android keeps old channel settings
    private static final String CHANNEL = "forma_alarm_channel_v2";

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager nm =
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {

            Uri soundUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            );

            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                );
            }

            AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel ch = new NotificationChannel(
                CHANNEL,
                "FORMA Reminder Alarms",
                NotificationManager.IMPORTANCE_HIGH
            );

            ch.setDescription("FORMA DESIGN Alarm Notifications");
            ch.enableVibration(true);
            ch.setLightColor(Color.YELLOW);
            ch.setSound(soundUri, audioAttributes);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            nm.createNotificationChannel(ch);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && "STOP_ALARM".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String title = intent == null ? "Reminder" :
            intent.getStringExtra("title");

        String note = intent == null ? "" :
            intent.getStringExtra("note");

        if (title == null || title.trim().isEmpty()) {
            title = "Reminder";
        }

        Intent stop = new Intent(this, AlarmService.class);
        stop.setAction("STOP_ALARM");

        PendingIntent stopPi = PendingIntent.getService(
            this, 2, stop,
            PendingIntent.FLAG_UPDATE_CURRENT |
            PendingIntent.FLAG_IMMUTABLE
        );

        Intent open = new Intent(this, MainActivity.class);

        PendingIntent openPi = PendingIntent.getActivity(
            this, 3, open,
            PendingIntent.FLAG_UPDATE_CURRENT |
            PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder b =
            Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);

        Notification n = b
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("FORMA DESIGN • " + title)
            .setContentText(
                note.isEmpty()
                    ? "It's time for your reminder"
                    : note
            )
            .setStyle(new Notification.BigTextStyle().bigText(
                note.isEmpty()
                    ? "It's time for your reminder"
                    : note
            ))
            .setContentIntent(openPi)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "STOP",
                stopPi
            )
            .build();

        startForeground(77, n);

        try {
            Uri uri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            );

            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                );
            }

            if (uri != null) {
                ringtone = RingtoneManager.getRingtone(this, uri);

                if (ringtone != null) {
                    AudioAttributes attributes =
                        new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();

                    ringtone.setAudioAttributes(attributes);

                    if (Build.VERSION.SDK_INT >= 28) {
                        ringtone.setLooping(true);
                    }

                    ringtone.play();
                }
            }

        } catch (Exception e) {
            android.util.Log.e("FORMA_ALARM",
                "Unable to play alarm", e);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
