package com.example.ass.wifiscaner;

import android.app.Activity;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity {

    ListView wifiListView;
    Button button;
    WifiManager wifi;
    WifiScanReceiver wifiReceiver;
    boolean uniwideFilter = false;
    final static int WIFI_SHOW_COUNT = 20;
    List<ScanResult> wifiInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiListView = (ListView) findViewById(R.id.wifiList);
        button = (Button) findViewById(R.id.scan);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiScanReceiver();
        if (wifi.isWifiEnabled()) wifi.setWifiEnabled(true);

        Button scanButton = (Button) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wifi.startScan();
            }
        });

        Button infoButton = (Button) findViewById(R.id.info);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setProtocol();
            }
        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uniwideFilter = true;
                    // The toggle is enabled
                } else {
                    uniwideFilter = false;
                    // The toggle is disabled
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.mobility:
                Intent i = new Intent(MainActivity.this, MobilityActivity.class);
                startActivity(i);
                break;
            case R.id.main:
                break;
            default:
                break;
        }
        return true;
    }

    public String ipFormat(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
    }

    public void setProtocol() {
        TextView textView = (TextView) findViewById(R.id.wifiInfo);
        WifiInfo info = wifi.getConnectionInfo();
        int speed = info.getLinkSpeed();
        textView.setText(checkProtocol(speed) + " " + speed + "Mbps " + ipFormat(info.getIpAddress()) + " " + info.getBSSID());
    }

    public String checkProtocol(int speed) {
        if (speed > 800) {
            return "802.11ac";
        } else if (speed > 200) {
            return "802.11n";
        } else if (speed > 54) {
            return "802.11g";
        } else {
            return "802.11b";
        }
    }

    protected void onPause() {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    private class WifiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return (lhs.level < rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
                }
            };
            ArrayList<ScanResult> wifiScanList = (ArrayList<ScanResult>) wifi.getScanResults();
            Collections.sort(wifiScanList, comparator);

            List<String> wifiList = new ArrayList<>();
            Set<String> wifiSet = new HashSet<String>();

            int count = 0;
            for (int i = 0; i < wifiScanList.size(); i++) {
                if (count >= WIFI_SHOW_COUNT) break;
                if (uniwideFilter && !wifiScanList.get(i).SSID.equals("uniwide")) continue;
                if (!uniwideFilter && wifiSet.contains(wifiScanList.get(i).SSID)) continue;
                String info = (count + 1) + "." + wifiScanList.get(i).SSID + " " + wifiScanList.get(i).level + " " + wifiScanList.get(i).BSSID;
                wifiList.add(info);
                wifiInfo.add(wifiScanList.get(i));
                wifiSet.add(wifiScanList.get(i).SSID);
                count++;
            }

            wifiListView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.test_list_item, wifiList));
//            wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Toast.makeText(getApplicationContext(),
//                            "Click ListItem Number " + position, Toast.LENGTH_LONG)
//                            .show();
//                    connectWifi(position);
//                }
//            });
        }

    }
}
