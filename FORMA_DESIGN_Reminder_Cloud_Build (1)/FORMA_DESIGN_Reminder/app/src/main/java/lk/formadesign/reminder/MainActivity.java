
package lk.formadesign.reminder;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;

public class MainActivity extends Activity {

    int gold = Color.rgb(218, 180, 105);
    int bg = Color.rgb(14, 20, 31);
    int panel = Color.rgb(27, 37, 53);
    int panel2 = Color.rgb(35, 47, 65);
    int white = Color.WHITE;
    int muted = Color.rgb(160, 173, 190);
    int green = Color.rgb(112, 201, 155);

    LinearLayout listBox, statsBox;
    ArrayList<Reminder> reminders;
    AlertDialog addDialog;

    EditText titleInput, noteInput;
    TextView dateButton, timeButton;
    Calendar selected = Calendar.getInstance();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        reminders = ReminderStore.load(this);
        requestPermissionsIfNeeded();
        buildUi();
    }

    void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission("android.permission.POST_NOTIFICATIONS")
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{"android.permission.POST_NOTIFICATIONS"}, 10);
        }

        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am =
                (AlarmManager) getSystemService(ALARM_SERVICE);

            if (!am.canScheduleExactAlarms()) {
                try {
                    startActivity(new Intent(
                        "android.settings.REQUEST_SCHEDULE_EXACT_ALARM"
                    ).setData(android.net.Uri.parse(
                        "package:" + getPackageName())));
                } catch (Exception ignored) {}
            }
        }
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(1);
        return l;
    }

    LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(0);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    void space(LinearLayout parent, int height) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, height));
    }

    TextView button(String value, int color, int textColor) {
        TextView t = text(value, 14, textColor);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(15, 12, 15, 12);
        t.setBackground(shape(color, 14));
        return t;
    }

    void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = column();
        root.setPadding(22, 25, 22, 30);
        scroll.addView(root);

        LinearLayout header = horizontal();

        LinearLayout brandBox = column();
        TextView brand = text("FORMA DESIGN", 21, gold);
        brand.setTypeface(null, Typeface.BOLD);
        brandBox.addView(brand);

        TextView subtitle = text("PERSONAL PRODUCTIVITY", 10, muted);
        subtitle.setLetterSpacing(0.16f);
        brandBox.addView(subtitle);

        header.addView(brandBox,
            new LinearLayout.LayoutParams(0, -2, 1));

        TextView bell = text("◉", 27, gold);
        bell.setGravity(Gravity.CENTER);
        header.addView(bell, new LinearLayout.LayoutParams(45, 45));

        root.addView(header);

        space(root, 35);

        TextView greeting = text("YOUR DAY, YOUR WAY.", 11, gold);
        greeting.setLetterSpacing(0.18f);
        root.addView(greeting);

        TextView heading = text("Stay on Track.", 30, white);
        heading.setTypeface(null, Typeface.BOLD);
        root.addView(heading);

        TextView hint = text(
            "Make every moment count.", 14, muted);
        root.addView(hint);

        space(root, 25);

        statsBox = column();
        LinearLayout statsRow = horizontal();

        statsRow.addView(statCard("TOTAL TASKS", "0", 0),
            new LinearLayout.LayoutParams(0, 105, 1));

        View gap = new View(this);
        statsRow.addView(gap,
            new LinearLayout.LayoutParams(10, 1));

        statsRow.addView(statCard("COMPLETED", "0", 1),
            new LinearLayout.LayoutParams(0, 105, 1));

        statsBox.addView(statsRow);

        space(statsBox, 10);

        statsBox.addView(statCard("PENDING REMINDERS", "0", 2),
            new LinearLayout.LayoutParams(-1, 90));

        root.addView(statsBox);

        space(root, 28);

        LinearLayout titleRow = horizontal();

        TextView upcoming = text("UPCOMING REMINDERS", 13, gold);
        upcoming.setTypeface(null, Typeface.BOLD);
        upcoming.setLetterSpacing(0.08f);
        titleRow.addView(upcoming,
            new LinearLayout.LayoutParams(0, -2, 1));

        TextView count = text("YOUR TASKS", 10, muted);
        titleRow.addView(count);

        root.addView(titleRow);

        listBox = column();
        LinearLayout.LayoutParams listParams =
            new LinearLayout.LayoutParams(-1, -2);
        listParams.topMargin = 14;
        root.addView(listBox, listParams);

        space(root, 20);

        TextView add = button("+   CREATE NEW REMINDER", gold,
            Color.rgb(20, 25, 34));
        add.setTextSize(15);
        add.setPadding(12, 18, 12, 18);
        root.addView(add, new LinearLayout.LayoutParams(-1, -2));

        add.setOnClickListener(v -> showAddDialog());

        space(root, 25);

        TextView footer = text(
            "DESIGNED BY FORMA DESIGN", 10, muted);
        footer.setGravity(Gravity.CENTER);
        footer.setLetterSpacing(0.12f);
        root.addView(footer);

        setContentView(scroll);
        renderList();
    }

    View statCard(String label, String number, int type) {
        LinearLayout card = column();
        card.setPadding(15, 14, 15, 12);
        card.setBackground(shape(panel, 18));

        TextView l = text(label, 10, muted);
        l.setLetterSpacing(0.08f);
        card.addView(l);

        TextView n = text(number, 27,
            type == 1 ? green : gold);
        n.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = 7;
        card.addView(n, p);

        card.setTag(type);
        return card;
    }

    void updateStats() {
        int total = reminders.size();
        int completed = 0;

        for (Reminder r : reminders) {
            if (r.done) completed++;
        }

        int pending = total - completed;

        for (int i = 0; i < statsBox.getChildCount(); i++) {
            View v = statsBox.getChildAt(i);

            if (v instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) v;

                for (int j = 0; j < row.getChildCount(); j++) {
                    View child = row.getChildAt(j);
                    if (child.getTag() instanceof Integer) {
                        int type = (Integer) child.getTag();
                        TextView number =
                            (TextView) ((LinearLayout) child).getChildAt(1);

                        if (type == 0) number.setText("" + total);
                        if (type == 1) number.setText("" + completed);
                        if (type == 2) number.setText("" + pending);
                    }
                }
            }
        }
    }

    void showAddDialog() {
        selected = Calendar.getInstance();
        selected.add(Calendar.HOUR_OF_DAY, 1);
        selected.set(Calendar.SECOND, 0);

        LinearLayout form = column();
        form.setPadding(22, 22, 22, 22);
        form.setBackgroundColor(bg);

        TextView heading = text("Create Reminder", 23, white);
        heading.setTypeface(null, Typeface.BOLD);
        form.addView(heading);

        TextView sub = text(
            "Add a new task to your schedule.", 13, muted);
        form.addView(sub);

        titleInput = edit("Reminder title");
        form.addView(titleInput);

        noteInput = edit("Additional message (optional)");
        form.addView(noteInput);

        dateButton = pickerButton("");
        timeButton = pickerButton("");
        updatePickerLabels();

        LinearLayout row = horizontal();

        row.addView(dateButton,
            new LinearLayout.LayoutParams(0, 52, 1));

        View gap = new View(this);
        row.addView(gap,
            new LinearLayout.LayoutParams(8, 1));

        row.addView(timeButton,
            new LinearLayout.LayoutParams(0, 52, 1));

        LinearLayout.LayoutParams rp =
            new LinearLayout.LayoutParams(-1, -2);
        rp.topMargin = 15;
        form.addView(row, rp);

        dateButton.setOnClickListener(v ->
            new DatePickerDialog(this, (view, y, m, d) -> {
                selected.set(y, m, d);
                updatePickerLabels();
            }, selected.get(Calendar.YEAR),
            selected.get(Calendar.MONTH),
            selected.get(Calendar.DAY_OF_MONTH)).show());

        timeButton.setOnClickListener(v ->
            new TimePickerDialog(this, (view, h, m) -> {
                selected.set(Calendar.HOUR_OF_DAY, h);
                selected.set(Calendar.MINUTE, m);
                selected.set(Calendar.SECOND, 0);
                updatePickerLabels();
            }, selected.get(Calendar.HOUR_OF_DAY),
            selected.get(Calendar.MINUTE), false).show());

        TextView save = button("SAVE REMINDER", gold,
            Color.rgb(20, 25, 34));

        LinearLayout.LayoutParams saveParams =
            new LinearLayout.LayoutParams(-1, 54);
        saveParams.topMargin = 20;
        form.addView(save, saveParams);

        save.setOnClickListener(v -> addReminder());

        addDialog = new AlertDialog.Builder(this)
            .setView(form)
            .setNegativeButton("CLOSE", (d, w) -> d.dismiss())
            .create();

        addDialog.show();
    }

    EditText edit(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setTextColor(white);
        e.setHintTextColor(muted);
        e.setHint(hint);
        e.setTextSize(14);
        e.setPadding(14, 8, 14, 8);
        e.setBackground(shape(panel2, 13));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, 52);
        p.topMargin = 13;
        e.setLayoutParams(p);
        return e;
    }

    TextView pickerButton(String value) {
        TextView t = text(value, 13, white);
        t.setGravity(Gravity.CENTER);
        t.setBackground(shape(panel2, 13));
        return t;
    }

    void updatePickerLabels() {
        dateButton.setText(
            "▣  " + android.text.format.DateFormat.format(
                "EEE, dd MMM", selected));

        timeButton.setText(
            "◷  " + String.format(Locale.getDefault(),
                "%02d:%02d",
                selected.get(Calendar.HOUR_OF_DAY),
                selected.get(Calendar.MINUTE)));
    }

    void addReminder() {
        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Enter a task name");
            return;
        }

        if (selected.getTimeInMillis() <= System.currentTimeMillis()) {
            new AlertDialog.Builder(this)
                .setMessage("Please choose a future date and time.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        Reminder r = new Reminder(
            System.currentTimeMillis(),
            title,
            noteInput.getText().toString().trim(),
            selected.getTimeInMillis(),
            false
        );

        reminders.add(r);
        ReminderStore.save(this, reminders);
        schedule(this, r);

        if (addDialog != null) addDialog.dismiss();

        renderList();

        Toast.makeText(this,
            "Reminder successfully created",
            Toast.LENGTH_SHORT).show();
    }

    static void schedule(Context c, Reminder r) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.putExtra("id", r.id);
        i.putExtra("title", r.title);
        i.putExtra("note", r.note);

        PendingIntent pi = PendingIntent.getBroadcast(
            c,
            (int)(r.id & 0x7fffffff),
            i,
            PendingIntent.FLAG_UPDATE_CURRENT |
            PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am =
            (AlarmManager)c.getSystemService(ALARM_SERVICE);

        try {
            if (Build.VERSION.SDK_INT >= 23)
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, r.time, pi);
            else
                am.setExact(AlarmManager.RTC_WAKEUP, r.time, pi);
        } catch (SecurityException ex) {
            am.set(AlarmManager.RTC_WAKEUP, r.time, pi);
        }
    }

    void renderList() {
        listBox.removeAllViews();

        Collections.sort(reminders,
            (a, b) -> Long.compare(a.time, b.time));

        updateStats();

        if (reminders.isEmpty()) {
            LinearLayout empty = column();
            empty.setPadding(20, 25, 20, 25);
            empty.setGravity(Gravity.CENTER);
            empty.setBackground(shape(panel, 18));

            TextView icon = text("◷", 32, gold);
            icon.setGravity(Gravity.CENTER);
            empty.addView(icon);

            TextView message = text(
                "No reminders yet", 17, white);
            message.setTypeface(null, Typeface.BOLD);
            message.setGravity(Gravity.CENTER);
            empty.addView(message);

            TextView detail = text(
                "Create your first reminder to get started.",
                12, muted);
            detail.setGravity(Gravity.CENTER);
            empty.addView(detail);

            listBox.addView(empty);
            return;
        }

        for (Reminder r : reminders) {
            LinearLayout item = column();
            item.setPadding(17, 16, 17, 15);
            item.setBackground(shape(panel, 18));

            LinearLayout.LayoutParams ip =
                new LinearLayout.LayoutParams(-1, -2);
            ip.bottomMargin = 12;
            listBox.addView(item, ip);

            LinearLayout top = horizontal();

            TextView name = text(
                r.title, 16, r.done ? muted : white);
            name.setTypeface(null, Typeface.BOLD);
            top.addView(name,
                new LinearLayout.LayoutParams(0, -2, 1));

            TextView status = text(
                r.done ? "COMPLETED" : "UPCOMING",
                9, r.done ? green : gold);
            status.setTypeface(null, Typeface.BOLD);
            top.addView(status);

            item.addView(top);

            TextView date = text(
                "◷  " +
                DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
                ).format(new Date(r.time)),
                12, gold);

            LinearLayout.LayoutParams dp =
                new LinearLayout.LayoutParams(-1, -2);
            dp.topMargin = 9;
            item.addView(date, dp);

            if (r.note != null && !r.note.isEmpty()) {
                TextView note = text(r.note, 13, muted);
                LinearLayout.LayoutParams np =
                    new LinearLayout.LayoutParams(-1, -2);
                np.topMargin = 8;
                item.addView(note, np);
            }

            View line = new View(this);
            line.setBackgroundColor(
                Color.rgb(55, 67, 83));

            LinearLayout.LayoutParams lineParams =
                new LinearLayout.LayoutParams(-1, 1);
            lineParams.topMargin = 14;
            lineParams.bottomMargin = 10;
            item.addView(line, lineParams);

            LinearLayout actions = horizontal();

            TextView done = button(
                r.done ? "UNDO" : "✓  COMPLETE",
                r.done ? panel2 : Color.rgb(47, 91, 75),
                r.done ? white : green);

            actions.addView(done,
                new LinearLayout.LayoutParams(0, 42, 1));

            View actionGap = new View(this);
            actions.addView(actionGap,
                new LinearLayout.LayoutParams(8, 1));

            TextView delete = button(
                "DELETE", Color.rgb(76, 43, 48),
                Color.rgb(255, 150, 150));

            actions.addView(delete,
                new LinearLayout.LayoutParams(0, 42, 1));

            item.addView(actions);

            done.setOnClickListener(v -> {
                r.done = !r.done;

                if (r.done) cancelAlarm(this, r);
                else if (r.time > System.currentTimeMillis())
                    schedule(this, r);

                ReminderStore.save(this, reminders);
                renderList();
            });

            delete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setMessage("Delete this reminder?")
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("DELETE", (d, w) -> {
                        cancelAlarm(this, r);
                        reminders.remove(r);
                        ReminderStore.save(this, reminders);
                        renderList();
                    }).show());
        }
    }

    static void cancelAlarm(Context c, Reminder r) {
        Intent i = new Intent(c, AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(
            c,
            (int)(r.id & 0x7fffffff),
            i,
            PendingIntent.FLAG_UPDATE_CURRENT |
            PendingIntent.FLAG_IMMUTABLE
        );

        ((AlarmManager)c.getSystemService(ALARM_SERVICE))
            .cancel(pi);
    }
}
