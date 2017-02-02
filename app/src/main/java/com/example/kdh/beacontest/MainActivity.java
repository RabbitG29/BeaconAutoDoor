package com.example.kdh.beacontest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity {

    private long lastTimeBackPressed;
    private Button btn_Connect;
    private Button modifybutton;
    private bluetooth bluetoothService_obj = null;
    private Button verifybutton;
    private static final String TAG = "MAIN";



    private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        btn_Connect = (Button) findViewById(R.id.bluetoothbutton);
        btn_Connect.setOnClickListener(bluetooth.mClickListener);


        if (bluetoothService_obj==null) {
            bluetoothService_obj = new bluetooth(this, mHandler);
        }

        verifybutton = (Button) findViewById(R.id.verifybutton);
        verifybutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v2)
            {
                String useruuid = null;
                useruuid = GetDevicesUUID(getBaseContext());
            }
        });

        final List selectedItem = new ArrayList();

        modifybutton = (Button) findViewById(R.id.modifybutton);

        modifybutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String[] items = new String[]{"Indoor", "Outdoor"};
                final int[] selectedIndex={0};

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog .setTitle("Setting Your Location")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                selectedIndex[0]=which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Toast.makeText(MainActivity.this, items[selectedIndex[0]], Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
            }

        });
    }

    @Override
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

    private String GetDevicesUUID(Context mContext){
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }
    @Override
    public void onBackPressed()
    {
        if(System.currentTimeMillis()-lastTimeBackPressed<1500)
        {
            finish();
            return;
        }

        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    }

}
