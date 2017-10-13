package com.example.ass.wifiscaner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.util.Date;

import static android.text.format.Formatter.formatIpAddress;


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
    Long ipBegin = Long.MAX_VALUE;

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

            /**
             * detect ap handoff
             */
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

                if (SupplicantState.isValidState(state)
                        && state == SupplicantState.COMPLETED) {
                    lastAP = currentAP;
                    lastIP = currentIP;
                }

                apStateBuffer.append("AP state: " + state.toString() + "\n");
                apStateBuffer.append("current AP: " + currentAP + "\n");
                apStateBuffer.append("current IP: " + currentIP + "\n");

                TextView apTextView = findViewById(R.id.apState);
                apTextView.setText(apStateBuffer);
            }

            /**
             * detect ip handoff
             */
//            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                StringBuffer ipBuffer = new StringBuffer();
//
//                if (info != null) {
//                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
//                        if (info.getType() == ConnectivityManager.TYPE_WIFI
//                                || info.getType() == ConnectivityManager.TYPE_MOBILE) {
//                            long current = System.currentTimeMillis();
//                            String ipAddress = ipFormat(wifiManager.getConnectionInfo().getIpAddress());
//                           Long ipDelay = current - ipBegin;
//
//                            ipBuffer.append("IP delay: " + ipDelay);
//                            ipBuffer.append("current ip: " + ipAddress);
//                            TextView ipTextview = findViewById(R.id.ipState);
//                            ipTextview.setText(ipBuffer);
//
//                        }
//                    }
//                    if (NetworkInfo.State.DISCONNECTED == info.getState()) {
//                        ipBegin = System.currentTimeMillis();
//                    }
//                }
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
