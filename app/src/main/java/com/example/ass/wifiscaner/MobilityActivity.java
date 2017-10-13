package com.example.ass.wifiscaner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by langley on 3/10/17.
 */

public class MobilityActivity extends Activity {

    WifiManager wifiManager;
    WifiBroadcastReceiver wifiBroadcastReceiver;
    String lastIP = "";
    String lastAP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobility);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiBroadcastReceiver = new WifiBroadcastReceiver();

        Button bt = findViewById(R.id.refresh);
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setConnectionInfo("");
            }
        });

        Log.d("Mobility","debug");

        setConnectionInfo("");

    }

    protected void onPause() {
        unregisterReceiver(wifiBroadcastReceiver);
        super.onPause();
    }

    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver, intentFilter);
        super.onResume();
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
                break;
            case R.id.main:
                Intent i = new Intent(MobilityActivity.this, MainActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
        return true;
    }

    private void setConnectionInfo(String payload) {
        TextView textView = findViewById(R.id.connection);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("IP: " + ipFormat(wifiManager.getConnectionInfo().getIpAddress()) + "\n");
        stringBuffer.append("BSSID: " + wifiManager.getConnectionInfo().getBSSID() + "\n");
        stringBuffer.append("SSID: " + wifiManager.getConnectionInfo().getSSID() + "\n");
        stringBuffer.append(payload + "\n");
        textView.setText(stringBuffer);
    }

    public String ipFormat(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d("Mobility", action);
            // AP
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                if (!SupplicantState.isValidState(state) || state != SupplicantState.COMPLETED
                        || state != SupplicantState.COMPLETED) {
                    Log.d("invalid state: ", state.toString());
                    return;
                }

                Log.d("Mobility", "AP: " + state.toString());
                setConnectionInfo("AP State: " + state.toString() + "\n");

                StringBuffer apStateBuffer = new StringBuffer();
                Long apBegin = Long.MAX_VALUE;

                if (!lastAP.equals("")) {
                    boolean connected = checkConnectedToDesiredWifi();
                }

                if (SupplicantState.isValidState(state)
                        && state == SupplicantState.DISCONNECTED) {
                    apBegin = System.currentTimeMillis();
                }

                if (SupplicantState.isValidState(state)
                        && state == SupplicantState.COMPLETED) {
                    if (apBegin != Long.MAX_VALUE) {
                        Long delay = System.currentTimeMillis() - apBegin;
                        apStateBuffer.append("L2 handoff delay: " + delay + "\n");
                        Log.d("Mobility", "L2 handoff delay: " + delay + "");
                    }
                    apBegin = Long.MAX_VALUE;
                }

                if (!lastAP.equals("")) {
                    boolean connected = checkConnectedToDesiredWifi();
                }

                lastAP = wifiManager.getConnectionInfo().getBSSID();
                lastIP = ipFormat(wifiManager.getConnectionInfo().getIpAddress());


                apStateBuffer.append("AP state: " + state.toString() + "\n");
                apStateBuffer.append("L2 handoff" + "\n");
                apStateBuffer.append("Last AP: " + lastAP + "\n");
                apStateBuffer.append("current AP: " + wifiManager.getConnectionInfo().getBSSID() + "\n");
                apStateBuffer.append("current IP: " + ipFormat(wifiManager.getConnectionInfo().getIpAddress()) + "\n");

                TextView apTextView = findViewById(R.id.apState);
                apTextView.setText(apStateBuffer);
//                }
            }
            // IP
            if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                Log.d("Mobility", "IP: " + state.toString());
                setConnectionInfo("IP State: " + state.toString());

//                if (SupplicantState.isValidState(state)
//                        && state == SupplicantState.COMPLETED) {

                if (!lastIP.equals("")) {

                }

                lastAP = wifiManager.getConnectionInfo().getBSSID();
                lastIP = ipFormat(wifiManager.getConnectionInfo().getIpAddress());

                StringBuffer ipStateBuffer = new StringBuffer();
                ipStateBuffer.append("IP state: " + state.toString() + "\n");
                ipStateBuffer.append("L3 handoff" + "\n");
                ipStateBuffer.append("Last IP: " + lastIP + "\n");
                ipStateBuffer.append("current AP: " + wifiManager.getConnectionInfo().getBSSID() + "\n");
                ipStateBuffer.append("current IP: " + ipFormat(wifiManager.getConnectionInfo().getIpAddress()) + "\n");
                ipStateBuffer.append("L3 handoff delay: " + "\n");

                TextView ipTextView = findViewById(R.id.ipState);
                ipTextView.setText(ipStateBuffer);

//                }
            }
        }

        /**
         * Detect you are connected to a specific network.
         */
        private boolean checkConnectedToDesiredWifi() {
            boolean connected = false;

            String desiredMacAddress = lastAP;

            WifiInfo wifi = wifiManager.getConnectionInfo();
            if (wifi != null) {
                // get current router Mac address
                String bssid = wifi.getBSSID();
                connected = desiredMacAddress.equals(bssid);
            }

            return connected;
        }
    }

}
