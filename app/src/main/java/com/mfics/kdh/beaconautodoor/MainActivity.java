package com.mfics.kdh.beaconautodoor;

// modified by KDH on 2017-02-10

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.w3c.dom.Text;

import java.util.List;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private long lastTimeBackPressed;
    private Button connBluetoothButton;
    private Button modifyInOrOutButton;
    private bluetooth bluetoothService_obj = null;
    private static final String TAG = "MAIN";
    final static String SERV_URL = "http://211.222.232.176:3001";// server URL 상수 선언
    private BeaconManager beaconManager;
    private Region region;
    private TextView beaconText;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    /* 최초 실행 검사 */
    /*
    public boolean CheckAppFirstExecute(){
        SharedPreferences pref = getSharedPreferences("IsFirst" , Activity.MODE_PRIVATE);
        boolean isFirst = pref.getBoolean("isFirst", false);
        if(!isFirst){ //최초 실행시 true 저장
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirst", true);
            editor.commit();
            //verifyDialog();
        }
        return !isFirst;
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //비콘연결
        beaconText=(TextView) findViewById(R.id.beaconText);
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener()
        {
            @Override
            public void onBeaconsDiscovered(Region region, List list)
            {
                if(!list.isEmpty())
                {
                    Log.d("Airdport", "Nearest place:"+list.get(0));
                    beaconText.setText(list.get(0)+"");
                }
            }
        });

        region = new Region("Range region",UUID.fromString("23"), null, null);

        //CheckAppFirstExecute();//Bluetooth 연결 chk
        if(bluetooth.getDeviceState()) // 블루투스 기기의 지원여부가 true 일때
        {
            bluetooth.firstEnableBluetooth();  //블루투스 활성화 시작.
        }

        chkUuidBackgroundTask task = new chkUuidBackgroundTask();//verify chk
        task.execute();

        //1번 버튼
        connBluetoothButton = (Button) findViewById(R.id.bluetoothbutton);
        connBluetoothButton.setOnClickListener(bluetooth.mClickListener);
        if (bluetoothService_obj == null) {
            bluetoothService_obj = new bluetooth(this, mHandler);
        }

        //3번 버튼
        modifyInOrOutButton = (Button) findViewById(R.id.modifybutton);
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
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, items[selectedIndex[0]], Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
            }

        });
    }

    /* 오른쪽 상단 Menu 설정 */
    /*
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
    */

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


    /*Android <-> Node.js 통신을 위한 AsyncTask*/
    class chkUuidBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
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
            Log.e("response", "reqAddress : " + reqAddress);
            response = request(reqAddress);
            Log.e("response", "get response data");
            Log.e("response", response);
            return null;
        }

        protected void onPostExecute(Integer a) {
            TextView verifyText = (TextView) findViewById(R.id.verifyText);
            Log.e("response", "before compareTo : " + response);
            if (response.compareTo("true") == 1) {
                verifyText.setText("인증됨!!");
            } else if (response.compareTo("false") == 1) {
                verifyText.setText("인증실패, 재시도");
                verifyDialog();  //집 정보 입력 및 인증
            }
            reqAddress = "";
        }
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
                Log.e("response", "resCode : " + Integer.toString(resCode));

                if (resCode == HttpURLConnection.HTTP_OK) {
                    Log.e("response", "HTTP_OK!!");
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
            Log.e("SampleHTTP", "Exception in processing response.", ex);
            ex.printStackTrace();
        }
        Log.e("response", "request finish");
        return output.toString();
    }


    private void verifyDialog() {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout loginLayout = (LinearLayout) vi.inflate(R.layout.dialog_verify, null);

        final EditText input_house_number = (EditText) loginLayout.findViewById(R.id.input_house_num);
        final EditText input_house_psw = (EditText) loginLayout.findViewById(R.id.input_house_psw);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle("인증");
        adb.setView(loginLayout);
        adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { //button 클릭시 connect

                chkUuidBackgroundTask task = new chkUuidBackgroundTask();//verify chk
                task.execute();

                Toast.makeText(MainActivity.this, "집 호수 : " +
                        input_house_number.getText().toString() + "\n비밀번호 : " +
                        input_house_psw.getText().toString(), Toast.LENGTH_LONG).show();

            }
        }).show();

    }

}