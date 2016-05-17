package com.example.archiegupta.hotspot_app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.net.wifi.WifiManager.*;

public class MainActivity extends AppCompatActivity {

    TextView textview;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView) findViewById(R.id.textView);
        // long startTime = System.nanoTime();
        WifiManager manager = (WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        final String address = info.getMacAddress();

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Log.d("TAG:","running");
                configure(MainActivity.this, address);
            }

        }, 0, 30000);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    scan();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
    }
    public static void configure(final Context context, String address)
    {
       Log.d("CONFIGURE", "HERE");
        ParseQuery query = new ParseQuery("DeviceConfig");
        query.whereEqualTo("MACAddress", address);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects,
                             ParseException e) {
                if (e == null) {
                    Log.d("Config", "Retrieved " + objects.size() + " phones ");
                    for (ParseObject o : objects) {
                        Log.d("Content:", "Here," + o.get("MACAddress") + o.get("Type"));
                        String type = o.get("Type").toString();
                        if (type.equals("hotspot")) {

                            turnOnOffHotspot(context, true);

                        } else if (type.equals("client")) {
                            Toast.makeText(context, "Its a client!!", Toast.LENGTH_LONG).show();
                            String hotspot_connect = o.get("Hotspot_to_connect").toString();
                            Log.d("TAG::", "caliing func");
                            connect_client(context, hotspot_connect);

                        }


                    }

                }

            }
        });
       // long estimatedTime = (System.nanoTime() - startTime)/(10^9);
       // double seconds = (double)estimatedTime / 1000000000.0;
       // Log.d("Time:"," "+seconds);
       // Toast.makeText(MainActivity.this,"time:"+ seconds, Toast.LENGTH_LONG).show();
    }
//**************************************************************************************************************************

    private static void connect_client(Context context, String hotspot_connect)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //////////////////////////////////////////////////////////////////////////////////////
        String currentSSID = wifiManager.getConnectionInfo().getSSID();
        if(currentSSID.equals(hotspot_connect))
            return;
        ////////////////////////////////////////////////////////////////////////////////////////
        if (!wifiManager.isWifiEnabled()) {
            turnOnOffHotspot(context,false);
            Log.d("TAG!!!!!!!!!!!!", "enabling wifi");
            wifiManager.setWifiEnabled(true);

            }

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + hotspot_connect + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); // open network

        int nd=wifiManager.addNetwork(conf);
        /*wifiManager.disconnect();
        boolean check = wifiManager.enableNetwork(nd, true);
        Log.d("enable", check + "\n");
        wifiManager.reconnect();*/
        //List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        List<ScanResult> mScanResults = wifiManager.getScanResults();
        boolean flag=false;

        Log.d("TAG::::::",hotspot_connect);
        for( ScanResult i : mScanResults ) {
            Log.d("TAG%%%%% ", i.SSID);
            if (i.SSID != null && i.SSID.equals(hotspot_connect)) {

                Log.d("TAG::::::", "Hereeeee I am");
                flag = true;
                break;
            }
        }
            if(flag==true)
            {
                try {
                    wifiManager.disconnect();
                    boolean check = wifiManager.enableNetwork(nd, true);
                    Log.d("enable", check + "\n");
                    wifiManager.reconnect();
                    Toast.makeText(context, "connected to Hotspot.", Toast.LENGTH_LONG).show();
                }

                 catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        else if(flag==false)
                Toast.makeText(context, "Hotspot not available.", Toast.LENGTH_LONG).show();
        }




    private void scan() {

        WifiApControl.getClientList(MainActivity.this, false, new FinishScanListener() {

            @Override
            public void onFinishScan(final ArrayList<ClientScanResult> clients) {

                // textview.setText("WifiApState: " + wifiApManager.getWifiApState() + "\n\n");
                textview.append("Clients: \n");
                for (ClientScanResult clientScanResult : clients) {
                    textview.append("-------------------------------\n");
                    textview.append("Ip Address: " + clientScanResult.getIpAddr() + "\n");
                    //textview.append("Device: " + clientScanResult.getDevice() + "\n");
                    textview.append("MAC Address: " + clientScanResult.getHWAddr() + "\n");
                    //textview.append("isReachable: " + clientScanResult.isReachable() + "\n");
                }
            }
        });
    }
    public static void turnOnOffHotspot(Context context, boolean TurnToOn) {
        Log.d("Tag", "@@@@@@@@@@@@@@@@@@@@@@@@");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiApControl apControl = WifiApControl.getApControl(wifiManager);
        if (apControl != null) {

            // TURN OFF YOUR WIFI BEFORE ENABLE HOTSPOT
            if (wifiManager.isWifiEnabled() && TurnToOn) {
                //turnOnOffWifi(context, false);
                wifiManager.setWifiEnabled(false);
            }

            mobiledata(context,TurnToOn);

            boolean res= apControl.setWifiApEnabled(apControl.getWifiApConfiguration(),TurnToOn);
            if(res==true)
            {
                Toast.makeText(context, "Hotspot enabled!!", Toast.LENGTH_LONG).show();
            }
            else if(res==false)
            {
                Toast.makeText(context, "Turning off Hotspot!!", Toast.LENGTH_LONG).show();
            }
        }
    }
    public static void mobiledata(Context context,boolean enabled)
    {
        ConnectivityManager dataManager;
        dataManager  = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Method dataMtd = null;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        dataMtd.setAccessible(true);
        try {
            dataMtd.invoke(dataManager, enabled);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
