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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.Logger;


/**
 * Created by langley on 3/10/17.
 */

public class MobilityActivity extends Activity {

    WifiManager wifiManager;
    WifiBroadcastReceiver wifiBroadcastReceiver;
    String lastIP = "";
    String currentIP = "";
    String lastAP = "";
    String currentAP = "";
    Long apBegin = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobility);
        Logger.init(MobilityActivity.class.getName());

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiBroadcastReceiver = new WifiBroadcastReceiver();

        Button bt = findViewById(R.id.refresh);
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setConnectionInfo("");
                Logger.e("debug1");
            }
        });

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

            Logger.e("action:" + action);
            // AP
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                if (!(SupplicantState.isValidState(state) && (state == SupplicantState.COMPLETED
                        || state == SupplicantState.ASSOCIATED))) {
                    Logger.e("invalid state: " + state.toString());
                    return;
                }

                Logger.e("AP: " + state.toString());
                setConnectionInfo("AP State: " + state.toString() + "\n");

                StringBuffer apStateBuffer = new StringBuffer();

                apStateBuffer.append("L2 handoff" + "\n");

                if (SupplicantState.isValidState(state)
                        && state == SupplicantState.ASSOCIATED) {
                    apBegin = System.currentTimeMillis();
                    Logger.e("apBegin" + apBegin);
                }

                if (SupplicantState.isValidState(state)
                        && state == SupplicantState.COMPLETED) {
                    if (apBegin != Long.MAX_VALUE) {
                        Long delay = System.currentTimeMillis() - apBegin;
                        apStateBuffer.append("L2 handoff delay: " + delay + "ms\n");
                        Logger.e("L2 handoff delay: " + delay + "ms");
                    }
                    apBegin = Long.MAX_VALUE;
                }

                if (lastAP.equals("")) {
                    boolean connected = checkConnectedToDesiredWifi();
                    apStateBuffer.append("Last AP: NAN \n");
                } else {
                    apStateBuffer.append("Last AP: " + lastAP + "\n");
                }

                if (lastIP.equals("")) {
                    apStateBuffer.append("last IP: NAN \n");
                } else {
                    apStateBuffer.append("last IP: " + lastIP + "\n");
                }

                currentAP = wifiManager.getConnectionInfo().getBSSID();
                currentIP = ipFormat(wifiManager.getConnectionInfo().getIpAddress());
                lastAP = currentAP;
                lastIP = currentIP;

                apStateBuffer.append("AP state: " + state.toString() + "\n");
                apStateBuffer.append("current AP: " + currentAP + "\n");
                apStateBuffer.append("current IP: " + currentIP + "\n");

                TextView apTextView = findViewById(R.id.apState);
                apTextView.setText(apStateBuffer);
//                }
            }


            // IP
//            if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
//                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//
//                Logger.e("IP: " + state.toString());
//                setConnectionInfo("IP State: " + state.toString());
//
////                if (SupplicantState.isValidState(state)
////                        && state == SupplicantState.COMPLETED) {
//
//                if (!lastIP.equals("")) {
//
//                }
//
//                lastAP = wifiManager.getConnectionInfo().getBSSID();
//                lastIP = ipFormat(wifiManager.getConnectionInfo().getIpAddress());
//
//                StringBuffer ipStateBuffer = new StringBuffer();
//                ipStateBuffer.append("IP state: " + state.toString() + "\n");
//                ipStateBuffer.append("L3 handoff" + "\n");
//                ipStateBuffer.append("Last IP: " + lastIP + "\n");
//                ipStateBuffer.append("current AP: " + wifiManager.getConnectionInfo().getBSSID() + "\n");
//                ipStateBuffer.append("current IP: " + ipFormat(wifiManager.getConnectionInfo().getIpAddress()) + "\n");
//                ipStateBuffer.append("L3 handoff delay: " + "\n");
//
//                TextView ipTextView = findViewById(R.id.ipState);
//                ipTextView.setText(ipStateBuffer);
//
////                }
//            }
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
