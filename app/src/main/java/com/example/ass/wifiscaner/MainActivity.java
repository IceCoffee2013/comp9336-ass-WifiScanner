package com.example.ass.wifiscaner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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

    public enum SecurityMode {
        OPEN, WEP, WPA, WPA2
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiListView = (ListView) findViewById(R.id.wifiList);
        button = (Button) findViewById(R.id.scan);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiScanReceiver();
        if (wifi.isWifiEnabled()) wifi.setWifiEnabled(true);

        Button clickMeBtn = (Button) findViewById(R.id.scan);
        clickMeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wifi.startScan();
            }
        });

        Button b2 = (Button) findViewById(R.id.button);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myClick(v);
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

//        checkWifiFreq();

    }

    public boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public String ipFormat(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
    }

    public void checkWifiFreq() {
        List<ScanResult> results = wifi.getScanResults();
        boolean is5G = false;
        for (ScanResult r : results) {
            int freq = r.frequency;
            if (is5GHz(freq)) {
                is5G = true;
                break;
            }
        }

        TextView textView = (TextView) findViewById(R.id.textView2);
        if (is5G) {
            textView.setText("Support 5G");
        } else {
            textView.setText("Do not support 5G");
        }

    }

    public void myClick(View v) {
        // Write your own code
        TextView txCounter = (TextView) findViewById(R.id.textView);
        WifiInfo info = wifi.getConnectionInfo();
        int ip = info.getIpAddress();
        String ipAddress = ipFormat(ip);
        txCounter.setText("IP: " + ipAddress + " | SSID: " + info.getSSID());
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

        private String ipFormat(int ip) {
            return (ip & 0xFF) + "." +
                    ((ip >> 8) & 0xFF) + "." +
                    ((ip >> 16) & 0xFF) + "." +
                    ((ip >> 24) & 0xFF);
        }

        private void connectWifi(final int position) {
            String networkSSID = wifiInfo.get(position).SSID;
            String networkBSSID = wifiInfo.get(position).BSSID;

            String userName = "z5046341";
            String passWord = "Ryj12345.";

            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = networkSSID;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
            enterpriseConfig.setIdentity(userName);
            enterpriseConfig.setPassword(passWord);
            enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
            wifiConfig.enterpriseConfig = enterpriseConfig;

            int netId = wifi.addNetwork(wifiConfig);
            wifi.saveConfiguration();
            wifi.disconnect();
            wifi.enableNetwork(netId, true);
            wifi.reconnect();
            int ipAdress = wifi.getConnectionInfo().getIpAddress();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Connecting...")
                    .setMessage("ssid:" + networkSSID + "\n" + "bssid:" + networkBSSID + "\n" + "ip:" + ipFormat(ipAdress))
                    .setPositiveButton("Confirm", null)
                    .create().show();
        }
    }
}
