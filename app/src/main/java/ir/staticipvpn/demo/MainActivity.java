package ir.staticipvpn.demo;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private TextView label(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER); t.setPadding(dp(10), dp(12), dp(10), dp(12));
        return t;
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(15,23,42));
        getWindow().setNavigationBarColor(Color.rgb(15,23,42));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(15,23,42));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(dp(20),dp(64),dp(20),dp(40));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(layout);
        TextView title = label("Static IP VPN", 30, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        layout.addView(title);
        layout.addView(label("نسخه نمایشی آموزشی • ۰.۱.۰", 16, 0xFF94A3B8));
        TextView status = label("اتصال برقرار نیست", 22, 0xFF38BDF8);
        layout.addView(status);
        Button connect = new Button(this);
        connect.setText("اتصال (غیرفعال در نسخه نمایشی)");
        connect.setEnabled(false);
        layout.addView(connect, new LinearLayout.LayoutParams(-1, dp(64)));
        TextView info = label("سرور: تنظیم نشده\nIP اختصاصی: موجود نیست\nاین برنامه هیچ تونل شبکه‌ای ایجاد نمی‌کند و IP دستگاه را تغییر نمی‌دهد.", 17, Color.WHITE);
        GradientDrawable card = new GradientDrawable();
        card.setColor(0xFF1E293B); card.setCornerRadius(dp(16));
        info.setBackground(card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-2);
        params.topMargin=dp(24);
        layout.addView(info,params);
        setContentView(scroll);
    }
}
