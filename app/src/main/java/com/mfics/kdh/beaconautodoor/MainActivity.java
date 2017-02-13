package com.mfics.kdh.beaconautodoor;


import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;



import android.app.Activity;
import android.app.ActivityManager;
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

import java.util.List;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.R.id.list;


public class MainActivity extends AppCompatActivity {

    private long lastTimeBackPressed;
    private Button connBluetoothButton;
    private Button modifyInOrOutButton;
    private bluetooth bluetoothService_obj = null;
    //private Button verifybutton;
    private static final String TAG = "MAIN";
<<<<<<< HEAD
    private BeaconManager beaconManager;
    private Region region;
    private TextView BeaconTest;
    String response, serv_addr, address; //연결시 사용할 주소와 response
=======
    final static String SERV_URL = "http://211.222.232.176:3001";// server URL 상수 선언
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }; // bluetooth 기능 실행

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
<<<<<<< HEAD
    } // 앱 최초 실행 여부 판단


=======
    }
    */
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //CheckAppFirstExecute();//Bluetooth 연결 chk

<<<<<<< HEAD
        CheckAppFirstExecute(); // 앱 최초 실행 여부 판단

        //Beacon인식 -> 실패 ㅠㅠ


        //1번 버튼(블루투스 실행)
        btn_Connect = (Button) findViewById(R.id.bluetoothbutton);
        btn_Connect.setOnClickListener(bluetooth.mClickListener);
=======
        chkUuidBackgroundTask task = new chkUuidBackgroundTask();//verify chk
        task.execute();

        //1번 버튼
        connBluetoothButton = (Button) findViewById(R.id.bluetoothbutton);
        connBluetoothButton.setOnClickListener(bluetooth.mClickListener);
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4
        if (bluetoothService_obj == null) {
            bluetoothService_obj = new bluetooth(this, mHandler);
        }

<<<<<<< HEAD
        //2번 버튼(인증)
=======
        //2번 버튼
        /*
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4
        verifybutton = (Button) findViewById(R.id.verifybutton);
        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
<<<<<<< HEAD
                showAlertDialog();  //집 정보 받기, 추후에 사용


                //집 정보를 서버로 전송해야 함
=======
                verifyDialog();  //집 정보 입력 및 인증
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4
            }
        });
        */

<<<<<<< HEAD
        //3번 버튼(현재 위치 설정)
        modifybutton = (Button) findViewById(R.id.modifybutton);
        modifybutton.setOnClickListener(new View.OnClickListener() {
=======
        //3번 버튼
        modifyInOrOutButton = (Button) findViewById(R.id.modifybutton);
        modifyInOrOutButton.setOnClickListener(new View.OnClickListener() {
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4
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
                        }).create().show(); // OK를 눌렀을 경우
            }

        });
    } // MainActivity 실행

    /* 오른쪽 상단 Menu 설정 */
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    } // 옵션 메뉴

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
<<<<<<< HEAD
    } // Settings
=======
    }
    */
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4

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
    } // 기기의 UUID 접근

    /* 뒤로가기 두번 누르면 종료 */
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
            finish();
            return;
        }
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    } // 뒤로가기 버튼 더블클릭할 시에 종료


    /*Android <-> Node.js 통신을 위한 AsyncTask*/
    class chkUuidBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        String reqAddress = "";
        String response = "";
        protected void onPreExecute() {
            String useruuid = null;     //uuid 받아오기
            useruuid = GetDevicesUUID(getBaseContext());
            reqAddress = SERV_URL + "/verify?uuid=" + useruuid;
            TextView print_uuid = (TextView) findViewById(R.id.uuid);
            print_uuid.setText(useruuid);
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
            TextView verifyButton = (TextView) findViewById(R.id.verifybutton);
            Log.e("response", "before compareTo : " + response);
            if (response.compareTo("true") == 1) {
                verifyButton.setText("Verified!!");
                verifyButton.setEnabled(false);
            } else if (response.compareTo("false") == 1) {
                verifyButton.setText("Fail to Verify, Retry");
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


                Toast.makeText(MainActivity.this, "집 호수 : " +
                        input_house_number.getText().toString() + "\n비밀번호 : " +
                        input_house_psw.getText().toString(), Toast.LENGTH_LONG).show();

            }
<<<<<<< HEAD

=======
>>>>>>> 65cd1f13724b4e1f9592aa68fd37282fbafdb5f4
        }).show();

    } // 집 정보 다이얼로그(2번 버튼)

}
