package lk.formadesign.reminder;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;

public class MainActivity extends Activity {
    int gold=Color.rgb(214,168,75), bg=Color.rgb(16,16,16), panel=Color.rgb(31,31,31), white=Color.WHITE, muted=Color.rgb(180,180,180);
    EditText titleInput,noteInput;
    TextView dateButton,timeButton;
    Calendar selected=Calendar.getInstance();
    LinearLayout listBox;
    ArrayList<Reminder> reminders;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        reminders=ReminderStore.load(this);
        requestPermissionsIfNeeded();
        buildUi();
    }
    void requestPermissionsIfNeeded() {
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},10);
        if(Build.VERSION.SDK_INT>=31) {
            AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
            if(!am.canScheduleExactAlarms()) {
                try { startActivity(new Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM").setData(android.net.Uri.parse("package:"+getPackageName()))); } catch(Exception ignored){}
            }
        }
    }
    TextView text(String s,int size,int color) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t;
    }
    GradientDrawable shape(int color,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    void buildUi() {
        ScrollView scroll=new ScrollView(this); scroll.setBackgroundColor(bg);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(22,28,22,24); scroll.addView(root);
        TextView brand=text("FORMA DESIGN",22,gold); brand.setTypeface(null,Typeface.BOLD); root.addView(brand);
        TextView sub=text("SMART REMINDER",12,muted); sub.setLetterSpacing(0.18f); root.addView(sub);
        TextView heading=text("Plan your day.",28,white); heading.setTypeface(null,Typeface.BOLD); LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2); hp.topMargin=28; root.addView(heading,hp);
        TextView hint=text("Never miss what matters.",14,muted); root.addView(hint);
        LinearLayout card=new LinearLayout(this); card.setOrientation(1); card.setPadding(16,16,16,16); card.setBackground(shape(panel,20));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.topMargin=22; root.addView(card,cp);
        TextView formTitle=text("NEW REMINDER",13,gold); formTitle.setTypeface(null,Typeface.BOLD); card.addView(formTitle);
        titleInput=edit("What do you need to do?"); card.addView(titleInput);
        noteInput=edit("Add a reminder message (optional)"); card.addView(noteInput);
        dateButton=pickerButton(""); timeButton=pickerButton("");
        updatePickerLabels();
        LinearLayout row=new LinearLayout(this); row.setOrientation(0); row.addView(dateButton,new LinearLayout.LayoutParams(0,52,1)); 
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,52,1); tp.leftMargin=8; row.addView(timeButton,tp); card.addView(row);
        dateButton.setOnClickListener(v->new DatePickerDialog(this,(view,y,m,d)->{selected.set(y,m,d);updatePickerLabels();},selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH)).show());
        timeButton.setOnClickListener(v->new TimePickerDialog(this,(view,h,m)->{selected.set(Calendar.HOUR_OF_DAY,h);selected.set(Calendar.MINUTE,m);selected.set(Calendar.SECOND,0);updatePickerLabels();},selected.get(Calendar.HOUR_OF_DAY),selected.get(Calendar.MINUTE),false).show());
        Button save=new Button(this); save.setText("＋  SAVE REMINDER"); save.setTextColor(Color.BLACK); save.setBackgroundTintList(android.content.res.ColorStateList.valueOf(gold)); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,52); sp.topMargin=12; card.addView(save,sp); save.setOnClickListener(v->addReminder());
        TextView upcoming=text("YOUR REMINDERS",13,gold); upcoming.setTypeface(null,Typeface.BOLD); LinearLayout.LayoutParams up=new LinearLayout.LayoutParams(-1,-2); up.topMargin=28; root.addView(upcoming,up);
        listBox=new LinearLayout(this); listBox.setOrientation(1); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=12; root.addView(listBox,lp);
        setContentView(scroll); renderList();
    }
    EditText edit(String hint) {
        EditText e=new EditText(this); e.setSingleLine(true); e.setTextColor(white); e.setHintTextColor(muted); e.setHint(hint); e.setTextSize(15);
        e.setPadding(12,8,12,8); e.setBackground(shape(Color.rgb(43,43,43),12));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,50); p.topMargin=12; e.setLayoutParams(p); return e;
    }
    TextView pickerButton(String s) {
        TextView t=text(s,14,white); t.setGravity(Gravity.CENTER); t.setBackground(shape(Color.rgb(43,43,43),12)); return t;
    }
    void updatePickerLabels() {
        dateButton.setText("▣  "+android.text.format.DateFormat.format("EEE, dd MMM yyyy",selected));
        timeButton.setText("◷  "+String.format(Locale.getDefault(),"%02d:%02d",selected.get(Calendar.HOUR_OF_DAY),selected.get(Calendar.MINUTE)));
    }
    void addReminder() {
        String title=titleInput.getText().toString().trim();
        if(title.isEmpty()){titleInput.setError("Enter a task name");return;}
        if(selected.getTimeInMillis()<=System.currentTimeMillis()){new AlertDialog.Builder(this).setMessage("Please choose a future date and time.").setPositiveButton("OK",null).show();return;}
        Reminder r=new Reminder(System.currentTimeMillis(),title,noteInput.getText().toString().trim(),selected.getTimeInMillis(),false);
        reminders.add(r); ReminderStore.save(this,reminders); schedule(this,r);
        titleInput.setText(""); noteInput.setText(""); selected=Calendar.getInstance(); updatePickerLabels(); renderList();
        Toast.makeText(this,"Reminder saved",Toast.LENGTH_SHORT).show();
    }
    static void schedule(Context c,Reminder r) {
        Intent i=new Intent(c,AlarmReceiver.class); i.putExtra("id",r.id); i.putExtra("title",r.title); i.putExtra("note",r.note);
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)(r.id & 0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am=(AlarmManager)c.getSystemService(ALARM_SERVICE);
        try {
            if(Build.VERSION.SDK_INT>=23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,r.time,pi);
            else am.setExact(AlarmManager.RTC_WAKEUP,r.time,pi);
        } catch(SecurityException ex) { am.set(AlarmManager.RTC_WAKEUP,r.time,pi); }
    }
    void renderList() {
        listBox.removeAllViews();
        Collections.sort(reminders,(a,b)->Long.compare(a.time,b.time));
        if(reminders.isEmpty()){TextView empty=text("No reminders yet. Add your first task above.",14,muted); empty.setPadding(4,12,4,12); listBox.addView(empty);return;}
        for(Reminder r:reminders) {
            LinearLayout item=new LinearLayout(this); item.setOrientation(1); item.setPadding(15,12,15,12); item.setBackground(shape(panel,16));
            LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2); ip.bottomMargin=10; listBox.addView(item,ip);
            TextView name=text((r.done?"✓  ":"")+" "+r.title,17,r.done?muted:white); name.setTypeface(null,Typeface.BOLD); item.addView(name);
            item.addView(text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(new Date(r.time)),12,gold));
            if(!r.note.isEmpty()) item.addView(text(r.note,13,muted));
            LinearLayout actions=new LinearLayout(this); actions.setOrientation(0);
            Button done=new Button(this); done.setText(r.done?"UNDO":"DONE"); done.setTextSize(11); done.setOnClickListener(v->{r.done=!r.done; if(r.done) cancelAlarm(this,r); else if(r.time>System.currentTimeMillis()) schedule(this,r); ReminderStore.save(this,reminders);renderList();});
            Button del=new Button(this); del.setText("DELETE"); del.setTextSize(11); del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Delete this reminder?").setNegativeButton("CANCEL",null).setPositiveButton("DELETE",(d,w)->{cancelAlarm(this,r);reminders.remove(r);ReminderStore.save(this,reminders);renderList();}).show());
            actions.addView(done); actions.addView(del); item.addView(actions);
        }
    }
    static void cancelAlarm(Context c,Reminder r) {
        Intent i=new Intent(c,AlarmReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)(r.id & 0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        ((AlarmManager)c.getSystemService(ALARM_SERVICE)).cancel(pi);
    }
}
