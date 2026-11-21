package lk.formadesign.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        Intent service=new Intent(context, AlarmService.class);
        service.putExtra("id",intent.getLongExtra("id",0));
        service.putExtra("title",intent.getStringExtra("title"));
        service.putExtra("note",intent.getStringExtra("note"));
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
        else context.startService(service);
    }
}
