package lk.formadesign.reminder;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class ReminderStore {
    static final String PREF="forma_reminders", KEY="items";
    static ArrayList<Reminder> load(Context c) {
        ArrayList<Reminder> out=new ArrayList<>();
        try {
            JSONArray a=new JSONArray(c.getSharedPreferences(PREF,0).getString(KEY,"[]"));
            for(int i=0;i<a.length();i++) out.add(Reminder.fromJson(a.getJSONObject(i)));
        } catch(Exception ignored){}
        return out;
    }
    static void save(Context c, ArrayList<Reminder> list) {
        JSONArray a=new JSONArray();
        for(Reminder r:list) a.put(r.toJson());
        c.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).apply();
    }
}
