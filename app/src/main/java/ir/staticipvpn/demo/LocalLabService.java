package ir.staticipvpn.demo;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.VpnService;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class LocalLabService extends VpnService {
    private volatile boolean cancelled;
    private Thread worker;
    private static final byte[] PROBE="VEPEN-LOCAL-LAB".getBytes(StandardCharsets.US_ASCII);
    @Override public int onStartCommand(Intent intent,int flags,int id){
        if(intent==null){stopSelf(id);return START_NOT_STICKY;}
        ResultReceiver receiver=intent.getParcelableExtra("receiver");
        if(receiver==null){stopSelf(id);return START_NOT_STICKY;}
        if(worker!=null)return START_NOT_STICKY;
        cancelled=false;
        worker=new Thread(()->runLab(receiver,id),"vepen-local-lab");
        worker.start();
        return START_NOT_STICKY;
    }
    private void runLab(ResultReceiver receiver,int id){
        String message="آزمایش محلی ناموفق بود.";
        try(ParcelFileDescriptor tun=new Builder().setSession("VEPEN Local Lab")
                .addAddress("192.0.2.1",32).addRoute("192.0.2.2",32)
                .addAllowedApplication(getPackageName()).setMtu(1280).setBlocking(false).establish()){
            if(tun==null)throw new IllegalStateException();
            ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network vpn=null;
            long waitUntil=SystemClock.elapsedRealtime()+2500;
            while(!cancelled && vpn==null && SystemClock.elapsedRealtime()<waitUntil){
                for(Network n:cm.getAllNetworks()){
                    NetworkCapabilities c=cm.getNetworkCapabilities(n);
                    if(c!=null && c.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){vpn=n;break;}
                }
                if(vpn==null)Thread.sleep(100);
            }
            if(cancelled || vpn==null)throw new IllegalStateException();
            try(DatagramSocket socket=new DatagramSocket()){
                vpn.bindSocket(socket);
                socket.send(new DatagramPacket(PROBE,PROBE.length,InetAddress.getByAddress(new byte[]{(byte)192,0,2,2}),33444));
                byte[] packet=new byte[1600];
                StructPollfd p=new StructPollfd();p.fd=tun.getFileDescriptor();p.events=(short)OsConstants.POLLIN;
                long until=SystemClock.elapsedRealtime()+3000;
                while(!cancelled && SystemClock.elapsedRealtime()<until){
                    if(Os.poll(new StructPollfd[]{p},200)<=0)continue;
                    if((p.revents & OsConstants.POLLIN)==0)continue;
                    int len=Os.read(tun.getFileDescriptor(),packet,0,packet.length);
                    if(matches(packet,len)){
                        message="آزمایش موفق: بسته آزمایشی در رابط محلی VPN دریافت شد. هیچ بسته‌ای به سرور خارجی ارسال نشد.";
                        break;
                    }
                }
            }
        }catch(Exception e){message="آزمایش محلی تکمیل نشد: "+e.getClass().getSimpleName();}
        if(!cancelled){Bundle data=new Bundle();data.putString("message",message);receiver.send(0,data);}
        stopSelf(id);
    }
    private static boolean matches(byte[] b,int len){
        if(len<28 || (b[0]&0xf0)!=0x40 || (b[9]&255)!=17)return false;
        if((b[16]&255)!=192 || b[17]!=0 || b[18]!=2 || b[19]!=2)return false;
        int h=(b[0]&15)*4;
        if(h<20 || len<h+8+PROBE.length)return false;
        if((((b[h+2]&255)<<8)|(b[h+3]&255))!=33444)return false;
        for(int i=0;i<PROBE.length;i++)if(b[h+8+i]!=PROBE[i])return false;
        return true;
    }
    @Override public void onRevoke(){cancelled=true;stopSelf();super.onRevoke();}
    @Override public void onDestroy(){cancelled=true;if(worker!=null)worker.interrupt();super.onDestroy();}
}
