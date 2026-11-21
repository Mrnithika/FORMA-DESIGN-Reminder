package lk.formadesign.reminder;

import org.json.JSONObject;

public class Reminder {
    public long id, time;
    public String title, note;
    public boolean done;
    public Reminder(long id, String title, String note, long time, boolean done) {
        this.id=id; this.title=title; this.note=note; this.time=time; this.done=done;
    }
    JSONObject toJson() {
        JSONObject j=new JSONObject();
        try { j.put("id",id); j.put("title",title); j.put("note",note); j.put("time",time); j.put("done",done); } catch(Exception ignored){}
        return j;
    }
    static Reminder fromJson(JSONObject j) {
        return new Reminder(j.optLong("id"),j.optString("title"),j.optString("note"),j.optLong("time"),j.optBoolean("done"));
    }
}
