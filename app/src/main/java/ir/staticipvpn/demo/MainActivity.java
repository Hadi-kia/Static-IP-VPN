package ir.staticipvpn.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView stats, status, count;
    private SharedPreferences prefs;
    private final Set<String> selected = new HashSet<>();
    private long startRx, startTx;
    private final Runnable refresh = new Runnable() {
        @Override public void run() { refreshStats(); handler.postDelayed(this, 5000); }
    };
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private TextView label(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER); t.setPadding(dp(8),dp(10),dp(8),dp(10));
        return t;
    }
    private LinearLayout panel(LinearLayout root) {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(12),dp(8),dp(12),dp(8));
        GradientDrawable d = new GradientDrawable(); d.setColor(0xFF1E293B); d.setCornerRadius(dp(16)); p.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(14); root.addView(p,lp);
        return p;
    }
    private void updateCount() {
        count.setText("تعداد برنامه‌های انتخاب‌شده: " + selected.size());
    }
    private String format(long n) {
        if (n < 0) return "ناموجود";
        if (n < 1024) return n + " B";
        if (n < 1048576) return String.format(Locale.ROOT,"%.1f KB",n/1024.0);
        if (n < 1073741824L) return String.format(Locale.ROOT,"%.1f MB",n/1048576.0);
        return String.format(Locale.ROOT,"%.2f GB",n/1073741824.0);
    }
    private long delta(long a,long b) { return a < 0 || b < 0 || a < b ? -1 : a-b; }
    private void refreshStats() {
        stats.setText("دانلود ↓  " + format(delta(TrafficStats.getTotalRxBytes(),startRx)) +
            "\nآپلود ↑  " + format(delta(TrafficStats.getTotalTxBytes(),startTx)));
    }
    private void checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm == null ? null : cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean ok = nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        status.setText((ok ? "اینترنت دستگاه در دسترس است" : "اینترنت دستگاه تأیید نشد") +
            "\nتونل VPN: غیرفعال • سرور: تنظیم نشده");
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=getSharedPreferences("local_settings",MODE_PRIVATE);
        selected.addAll(prefs.getStringSet("selected_apps",new HashSet<>()));
        startRx=TrafficStats.getTotalRxBytes(); startTx=TrafficStats.getTotalTxBytes();
        ScrollView scroll=new ScrollView(this); scroll.setBackgroundColor(0xFF0F172A);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(16),dp(48),dp(16),dp(40)); scroll.addView(root);
        TextView title=label("VEPEN",32,Color.WHITE); title.setTypeface(null,Typeface.BOLD); root.addView(title);
        root.addView(label("نسخه آموزشی ۰.۳.۰ • آزمایش محلی VPN",15,0xFF94A3B8));
        LinearLayout network=panel(root);
        status=label("تونل VPN: غیرفعال",19,0xFF38BDF8); network.addView(status);
        Button check=new Button(this); check.setText("بررسی اینترنت دستگاه"); check.setOnClickListener(v->checkNetwork()); network.addView(check);
        network.addView(label("IP ثابت: موجود نیست\nتغییر خودکار سرور و DNS: غیرفعال",15,Color.WHITE));
        Button lab=new Button(this); lab.setText("آزمایش محلی VPN"); lab.setOnClickListener(v->startActivity(new Intent(this,LabActivity.class))); network.addView(lab);
        LinearLayout traffic=panel(root);
        traffic.addView(label("حجم مصرف اینترنت دستگاه",20,Color.WHITE));
        stats=label("",22,0xFF38BDF8); stats.setTextDirection(View.TEXT_DIRECTION_LTR); traffic.addView(stats);
        traffic.addView(label("از زمان باز شدن صفحه؛ مجموع ترافیک دستگاه، نه VPN یا برنامه‌های انتخاب‌شده.",13,0xFFCBD5E1));
        LinearLayout apps=panel(root); apps.addView(label("انتخاب نرم‌افزارها",20,Color.WHITE));
        count=label("",16,0xFF38BDF8); apps.addView(count);
        apps.addView(label("انتخاب‌ها فقط روی گوشی ذخیره می‌شوند؛ مسیر ترافیک تغییر نمی‌کند.",13,0xFFCBD5E1));
        Intent launcher=new Intent(Intent.ACTION_MAIN); launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        ArrayList<ResolveInfo> list=new ArrayList<>(getPackageManager().queryIntentActivities(launcher,0));
        list.sort(Comparator.comparing(r->r.loadLabel(getPackageManager()).toString(),String.CASE_INSENSITIVE_ORDER));
        Set<String> shown=new HashSet<>();
        for(ResolveInfo r:list) {
            String pkg=r.activityInfo.packageName;
            if(pkg.equals(getPackageName()) || !shown.add(pkg)) continue;
            CheckBox box=new CheckBox(this);
            box.setText(r.loadLabel(getPackageManager()).toString()); box.setTextColor(Color.WHITE);
            box.setMinHeight(dp(48)); box.setChecked(selected.contains(pkg));
            box.setOnCheckedChangeListener((b,checked)->{
                if(checked) selected.add(pkg); else selected.remove(pkg);
                prefs.edit().putStringSet("selected_apps",new HashSet<>(selected)).apply(); updateCount();
            });
            apps.addView(box);
        }
        selected.retainAll(shown);
        prefs.edit().putStringSet("selected_apps",new HashSet<>(selected)).apply();
        updateCount();
        if(shown.isEmpty()) apps.addView(label("برنامه قابل نمایش یافت نشد.",14,Color.WHITE));
        LinearLayout power=panel(root);
        power.addView(label("مصرف انرژی",19,Color.WHITE));
        power.addView(label("بدون سرویس پس‌زمینه؛ آمار فقط هنگام نمایش صفحه هر ۵ ثانیه به‌روز می‌شود.",14,0xFFCBD5E1));
        setContentView(scroll); checkNetwork(); refreshStats();
    }
    @Override protected void onStart() {
        super.onStart(); handler.removeCallbacks(refresh); handler.post(refresh);
    }
    @Override protected void onStop() {
        handler.removeCallbacks(refresh); super.onStop();
    }
}
