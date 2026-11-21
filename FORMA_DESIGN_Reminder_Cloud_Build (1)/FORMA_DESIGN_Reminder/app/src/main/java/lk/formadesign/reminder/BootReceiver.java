package lk.formadesign.reminder;

import android.content.*;
import android.app.*;
import android.os.Build;
import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if(!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) return;
        ArrayList<Reminder> list=ReminderStore.load(c);
        for(Reminder r:list) if(!r.done && r.time>System.currentTimeMillis()) MainActivity.schedule(c,r);
    }
}
