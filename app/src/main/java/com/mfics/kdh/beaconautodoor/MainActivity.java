package com.mfics.kdh.beaconautodoor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.Object;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    final static String SERV_PORT = "3002";
    final static String SERV_URL = "http://211.222.232.176:" + SERV_PORT;// server URL 상수 선언
    final String BEACON_UUID = "0e96badb-348a-4395-a5c2-a2a76381e6f2";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private long lastTimeBackPressed;
    private bluetooth bluetoothService = null;
    private BeaconManager beaconManager;
    int beaconState = 0;

    //TextView mTxtOrient; //방향 sensor
    //SensorManager mSm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("response", "App Start!!!");
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);




        //mSm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //mTxtOrient = (TextView) findViewById(R.id.orient_sensor);

        //앱 실행시 Verify
        VerifyUUIDBackgroundTask task = new VerifyUUIDBackgroundTask();
        task.execute();

        if (bluetoothService == null) {//Bluetooth 객체 생성 및 블루투스 ON
            bluetoothService = new bluetooth(this, new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            });
            if (bluetoothService.getDeviceState()) {
                bluetoothService.firstEnableBluetooth();
            }
        }


        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        //1번 버튼
        Button connBluetoothButton= (Button) findViewById(R.id.bluetoothbutton);
        connBluetoothButton.setOnClickListener(bluetoothService.mClickListener);

        //2번 버튼
        Button modifyInOrOutButton = (Button) findViewById(R.id.modifybutton);
        modifyInOrOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{"실내(집 안)", "실외(집 밖)"};
                final int[] selectedIndex = {0};

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("현재 위치를 설정해 주세요")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedIndex[0] = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {    //실 내/외 설정 변경 (selectedindex[0] = 0(실내), se...dex[1] = 1(실외)
                                ModifyBackgroundTask task = new ModifyBackgroundTask(selectedIndex[0], items[selectedIndex[0]]);
                                task.execute();
                            }
                        }).create().show();
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
/*
    @Override
    public void onBeaconServiceConnect() {


        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            TextView beaconText = (TextView) findViewById(R.id.beaconText);
            @Override
            public void didEnterRegion(Region region) {
                Log.e("response", "I just saw an beacon for the first time!");
                beaconText.setText("인식됨");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.e("response", "I no longer see an beacon");
                beaconText.setText("인식되지 않음");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.e("response", "I have just switched from seeing/not seeing beacons: "+state);
                beaconState = state;
            }
        });
        beaconManager.addRangeNotifier(new RangeNotifier() {


            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.e("response", "Rssi : "+ beacons.iterator().next().getRssi());
                    Log.e("response", "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");

                }else{
                    Log.e("reponse", "no beacon");

                }
            }
        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region(BEACON_UUID, null, null, null));
        } catch (RemoteException e) {    }
    }
*/

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.e("response", "Rssi : "+ beacons.iterator().next().getRssi());
                    Log.e("response", "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                    BeaconBackgroundTask btask = new BeaconBackgroundTask(beacons.iterator().next().getDistance());
                    btask.execute();


                }
                else {
                    Log.e("reponse", "no beacon");

                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    /* UUiD 받아오기 */
    private String GetDevicesUUID(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    /* 뒤로가기 두번 누르면 종료 */
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
            finish();
            return;
        }
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    }

    private void verifyDialog() {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout addUserLayout = (LinearLayout) vi.inflate(R.layout.dialog_verify, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("주민 인증 및 등록");
        adb.setView(addUserLayout);

        adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int where = 1;
                //addUserLayout이라 작명한 Layout을 지정해야 id로부터 VIew를 받아 올 수 있으며, 지정해주지 않으면 nullpointerException 오류 발생
                EditText houseNum = (EditText) addUserLayout.findViewById(R.id.input_house_num);
                EditText password = (EditText) addUserLayout.findViewById(R.id.input_house_psw);
                RadioButton inside = (RadioButton) addUserLayout.findViewById(R.id.radiobtn_in);
                RadioButton outside = (RadioButton) addUserLayout.findViewById(R.id.radiobtn_out);
                if (inside.isChecked()) where = 0;  //안이면 0
                else if (outside.isChecked()) where = 1;//밖이면 1

                //생성시 인자로 정보들을 넘겨줌
                AddUserBackgroundTask task = new AddUserBackgroundTask(houseNum.getText().toString(), password.getText().toString(), where);
                task.execute();

                houseNum.setText("");
                password.setText("");
            }
        }).show();
    }

    /* request 요청 및 response를 받아 반환*/
    private String request(String urlStr) {
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                //conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //conn.setDoOutput(true);   //방식이 post로 바뀌게 됨

                int resCode = conn.getResponseCode();
                //Log.e("response", "resCode : " + Integer.toString(resCode));

                if (resCode == HttpURLConnection.HTTP_OK) {
                    //Log.e("response", "HTTP_OK!!");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while (true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        output.append(line + "\n");
                    }

                    reader.close();
                    conn.disconnect();
                }
            }
        } catch (Exception ex) {
            //Log.e("SampleHTTP", "Exception in processing response.", ex);
            ex.printStackTrace();
        }
        //Log.e("response", "request finish");
        return output.toString();
    }



    /*Android <-> Node.js 통신을 위한 AsyncTask*/
    /* Verify UUID */
    class VerifyUUIDBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        String reqAddress = "";
        String response = "";

        protected void onPreExecute() {
            String useruuid = null;     //uuid 받아오기
            useruuid = GetDevicesUUID(getBaseContext());
            reqAddress = SERV_URL + "/verify?uuid=" + useruuid;
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            //Log.e("response", "reqAddress : " + reqAddress);
            response = request(reqAddress);
            //Log.e("response", "get response data");
            //Log.e("response", response);
            return null;
        }

        protected void onPostExecute(Integer a) {
            TextView verifyText = (TextView) findViewById(R.id.verifyText);
            //Log.e("response", "before compareTo : " + response);
            if (response.compareTo("true") == 1) {
                verifyText.setText("인증됨!!");
                AccessBackgroundTask chkAccessTask = new AccessBackgroundTask();
                chkAccessTask.execute();
            } else if (response.compareTo("false") == 1) {
                verifyText.setText("인증실패, 재시도");
                verifyDialog();  //집 정보 입력 및 인증
            } else {
                verifyText.setText("Server closed");
            }
            reqAddress = "";
        }
    }

    /* Add User, Dialog에서 집호수, 비밀번호, 현재 밖인지 안인지에 대한 정보를 받아 비밀번호 검사 후 UUID 등록 */
    class AddUserBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        String address = SERV_URL;
        String response = "";
        String houseNum, password, whereis;

        //Dialog로부터 정보를 받아서 시작
        public AddUserBackgroundTask(String house_num, String passwd, int where) {
            houseNum = house_num;
            password = passwd;
            whereis = Integer.toString(where);
        }

        protected void onPreExecute() { //URL에 집호수, password, 위치정보, UUID 추가
            String useruuid = GetDevicesUUID(getBaseContext());
            address = address + "/addUser?housenum=" + houseNum + "&passwd=" + password + "&uuid=" + useruuid + "&whereis=" + whereis;
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            //Log.e("response", "Add user address : " + address);
            response = request(address);
            return null;
        }

        protected void onPostExecute(Integer a) {
            TextView verifyText = (TextView) findViewById(R.id.verifyText);
            if (response.compareTo("true") == 1) {
                verifyText.setText("인증됨!!");
                Toast.makeText(MainActivity.this, "집 호수 : " +
                        houseNum + "\n비밀번호 : " +
                        password + "\n인증되었습니다.", Toast.LENGTH_LONG).show();
                AccessBackgroundTask chkAccessTask = new AccessBackgroundTask();
                chkAccessTask.execute();
            } else if (response.compareTo("invalid") == 1) {
                Toast.makeText(MainActivity.this, "집 호수 : " +
                        houseNum + "\n비밀번호 : " +
                        password + "\n인증 실패", Toast.LENGTH_LONG).show();
                verifyDialog();  //집 정보 재입력 및 인증
            } else {
                verifyText.setText("Server closed");
                //Log.e("response", response);
            }
            address = SERV_URL;
        }
    }

    /* 실 내/외 설정 변경 */
    class ModifyBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        String address = SERV_URL;
        String response = "";
        String whereis, selected;

        public ModifyBackgroundTask(int where, String select) {
            whereis = Integer.toString(where);
            selected = select;
        }

        protected void onPreExecute() {
            String useruuid = GetDevicesUUID(getBaseContext());
            address = address + "/modifyWhereis?whereis=" + whereis + "&uuid=" + useruuid;
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            //Log.e("response", "Modify address : " + address);
            response = request(address);
            return null;
        }

        protected void onPostExecute(Integer a) {
            if (response.compareTo("true") == 1) {
                Toast.makeText(MainActivity.this, selected + "로 변경되었습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    class AccessBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        float[] mR = new float[9];
        float[] mI = new float[9];
        float[] mV = new float[9];
        float[] mGravity = null;
        float[] mGeometric = null;
        String response = "";
        String address = SERV_URL;

        protected void onPreExecute() {
            //mSm.registerListener(mSeonsorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
            //mSm.registerListener(mSeonsorListener, mSm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            //SystemClock.sleep(500);
            //address = address + "/chkAccess?rotMatrix=" + columnMatrix(mR);
            //Log.e("response", address);
            //response = request(address);
            return null;
        }

        protected void onPostExecute(Integer a) {
        }

/*
    SensorEventListener mSeonsorListener = new SensorEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mGravity = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mGeometric = event.values.clone();
                    break;
            }

            if (mGravity != null && mGeometric != null) {
                SensorManager.getRotationMatrix(mR, mI, mGravity, mGeometric);
                //float inclination = SensorManager.getInclination(mI);
                SensorManager.getOrientation(mR, mV);

                mTxtOrient.setText(
                        //"Gra : " + dumpValues(mGravity)
                        //+ "\nMag : " + dumpValues(mGeometric)
                        //"R : \n" + dumpMatrix(mR)
                                    + "\nI : \n" + dumpMatrix(mI)
                                    + "\ninclination : " + inclination
                                    + "\n\nRot : \n" + dumpMatrix(mV)
                                    + "\n\nTop : "
                                    "x : " + String.format("%.3f", Radian2Deegree(Math.cos(mV[0])*Math.cos(mV[1])))
                                    + "\ny : " + String.format("%.3f", Radian2Deegree(Math.sin(mV[0])*Math.cos(mV[1])))
                                    + "\nz : " + String.format("%.3f", Radian2Deegree(-Math.cos(mV[1]-Math.PI/2)))
                                    + "\nmv[0] : " +String.format("%.3f", mV[0])
                                    + "\nmv[1] : " +String.format("%.3f", mV[1])
                                    + "\nmv[2] : " +String.format("%.3f", mV[2])
                                    /*+ "\n\nLeft : "
                                    + "\nx : " + String.format("%.3f", -Math.cos(mV[0])*Math.sin(mV[1])*Math.sin(mV[2]) + Math.sin(mV[0])*Math.cos(mV[2]))
                                    + "\ny : " + String.format("%.3f", -Math.sin(mV[0])*Math.sin(mV[1])*Math.sin(mV[2]) - Math.cos(mV[0])*Math.cos(mV[2]))
                                    + "\nz : " + String.format("%.3f", Math.cos(mV[1])*Math.sin(mV[2]))
                                    + "\n\nBack : "
                                    + "\nx : " + String.format("%.3f", -Math.cos(mV[0])*Math.sin(mV[1])*Math.cos(mV[2]) + Math.sin(mV[0])*Math.sin(mV[2]))
                                    + "\ny : " + String.format("%.3f", -Math.sin(mV[0])*Math.sin(mV[1])*Math.cos(mV[2]) - Math.cos(mV[0])*Math.sin(mV[2]))
                                    + "\nz : " + String.format("%.3f", Math.cos(mV[1])*Math.sin(mV[2]-Math.PI/2))
                );
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    float Radian2Deegree(double radian) {
        return (float)(radian*180/(float)Math.PI);
    }

    String dumpMatrix(float[] m) {
        return String.format("%.2f, %.2f, %.2f\n%.2f, %.2f, %.2f\n%.2f, %.2f, %.2f\n", m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
    }

    String columnMatrix(float[] m) {
        return String.format("%.2f_%.2f_%.2f", m[0], m[1], m[2]);
    }
   */
    }


    //BackgroundTask 기본 소스
    class BeaconBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        String address = SERV_URL;
        String response;
        double distanceBeacon;
        String whereis, selected;
        public BeaconBackgroundTask(double distance){
            distanceBeacon=distance;
        }
        protected void onPreExecute() {
            address = address+"인자";
        }
        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            response = request(address);
            return null;
        }
        protected void onPostExecute(Integer a) {
            TextView beaconText = (TextView) findViewById(R.id.beaconText);
            beaconText.setText("인식됨, 비콘과의 거리 : "+distanceBeacon+"m");
            if(distanceBeacon<3)
            {
                beaconText.setText("문열림, 비콘과의 거리 : "+distanceBeacon+"m");
                //ModifyBackgroundTask modifyBackgroundTask = new ModifyBackgroundTask(whereis, selected);
                //modifyBackgroundTask.execute();
            }

            //통신후 작업
        }
    }

}