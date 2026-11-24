package ir.staticipvpn.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LabActivity extends Activity {
    private TextView result;
    private Button start;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable deadline = () -> finishTest("پاسخی دریافت نشد؛ آزمایش متوقف شد.");
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,96,28,28);
        root.setBackgroundColor(0xFF0F172A);
        TextView title=new TextView(this); title.setText("آزمایش محلی VPN"); title.setTextColor(Color.WHITE); title.setTextSize(24);
        root.addView(title);
        result=new TextView(this); result.setTextColor(Color.WHITE); result.setTextSize(17); result.setPadding(0,24,0,24);
        result.setText("فقط یک بسته مصنوعی در خود گوشی آزمایش می‌شود. اینترنت یا IP عمومی تغییر نمی‌کند. اگر VPN دیگری فعال باشد، آزمایش اجرا نمی‌شود.");
        root.addView(result);
        start=new Button(this); start.setText("شروع آزمایش"); start.setOnClickListener(v->requestTest()); root.addView(start);
        Button back=new Button(this); back.setText("بازگشت"); back.setOnClickListener(v->finish()); root.addView(back);
        setContentView(root);
    }
    private void requestTest() {
        ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities nc=cm==null?null:cm.getNetworkCapabilities(cm.getActiveNetwork());
        if(nc!=null && nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
            result.setText("VPN دیگری فعال است؛ آزمایش اجرا نشد."); return;
        }
        Intent consent=VpnService.prepare(this);
        if(consent!=null) startActivityForResult(consent,301); else begin();
    }
    @Override protected void onActivityResult(int request,int code,Intent data) {
        super.onActivityResult(request,code,data);
        if(request!=301)return;
        if(code==RESULT_OK)begin(); else result.setText("مجوز VPN داده نشد.");
    }
    private void begin(){
        if(running)return;
        running=true;start.setEnabled(false);result.setText("در حال آزمایش محلی...");
        ResultReceiver receiver=new ResultReceiver(handler){
            @Override protected void onReceiveResult(int code,Bundle data){
                if(running)finishTest(data.getString("message","آزمایش پایان یافت."));
            }
        };
        Intent i=new Intent(this,LocalLabService.class);
        i.putExtra("receiver",receiver);
        try{startService(i);handler.postDelayed(deadline,10000);}
        catch(RuntimeException e){finishTest("شروع سرویس آزمایشی ممکن نشد.");}
    }
    private void finishTest(String text){
        handler.removeCallbacks(deadline);running=false;stopService(new Intent(this,LocalLabService.class));start.setEnabled(true);result.setText(text);
    }
    @Override protected void onStop(){
        if(running)finishTest("آزمایش به دلیل خروج از صفحه متوقف شد.");
        super.onStop();
    }
}
