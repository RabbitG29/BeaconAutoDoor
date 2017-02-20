package com.mfics.kdh.beaconautodoor;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

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
    final static String SERV_URL = "http://211.222.232.176:3001";// server URL 상수 선언

    /* 최초 실행 검사 Service, 앱 실행시 인증을 통해 검사방식으로 대체 */
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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //CheckAppFirstExecute();//Bluetooth 연결 chk
        if(bluetooth.getDeviceState()) // 블루투스 기기의 지원여부가 true 일때
        {
            bluetooth.firstEnableBluetooth();  //블루투스 활성화 시작.
        }

        chkUuidBackgroundTask task = new chkUuidBackgroundTask();//앱 실행시 Verify
        task.execute();

        //1번 버튼
        connBluetoothButton = (Button) findViewById(R.id.bluetoothbutton);
        connBluetoothButton.setOnClickListener(bluetooth.mClickListener);
        if (bluetoothService_obj == null) {
            bluetoothService_obj = new bluetooth(this, new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            });
        }

        //2번 버튼
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
                if(inside.isChecked()) where = -1;  //안이면 -1
                else if(outside.isChecked()) where = 1;//밖이면 1

                //생성시 인자로 정보들을 넘겨줌
                addUserBackgroundTask task = new addUserBackgroundTask(houseNum.getText().toString(), password.getText().toString(), where);
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

    /*Android <-> Node.js 통신을 위한 AsyncTask*/
    /* Verify UUID */
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
            }else {
                verifyText.setText("Server closed");
            }
            reqAddress = "";
        }
    }
    /* Add User, Dialog에서 집호수, 비밀번호, 현재 밖인지 안인지에 대한 정보를 받아 비밀번호 검사 후 UUID 등록 */
    class addUserBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        String address = SERV_URL;
        String response = "";
        String houseNum, password, whereis;

        //Dialog로부터 정보를 받아서 시작
        public addUserBackgroundTask(String house_num, String passwd, int where){
            houseNum = house_num;
            password = passwd;
            whereis = Integer.toString(where);
        }

        protected void onPreExecute() { //URL에 집호수, password, 위치정보, UUID 추가
            String useruuid = GetDevicesUUID(getBaseContext());
            address = address + "/addUser?housenum=" + houseNum + "&passwd=" + password +"&uuid=" + useruuid + "&whereis="+whereis;
        }
        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            Log.e("response", "Add user address : "+address);
            response = request(address);
            return null;
        }
        protected void onPostExecute(Integer a) {
            TextView verifyText = (TextView) findViewById(R.id.verifyText);
            if (response.compareTo("true") == 1) {
                verifyText.setText("인증됨!!");
                Toast.makeText(MainActivity.this, "집 호수 : " +
                        houseNum + "\n비밀번호 : " +
                        password +"\n인증되었습니다.", Toast.LENGTH_LONG).show();
            } else if (response.compareTo("invalid") == 1) {
                Toast.makeText(MainActivity.this, "집 호수 : " +
                        houseNum + "\n비밀번호 : " +
                        password +"\n인증 실패", Toast.LENGTH_LONG).show();
                verifyDialog();  //집 정보 재입력 및 인증
            }else{
                verifyText.setText("Server closed");
                Log.e("response", response);
            }
            address = SERV_URL;
        }
    }

    /* BackgroundTask 기본 소스
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        protected void onPreExecute() {

        }
        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        protected void onPostExecute(Integer a) {
        }
    }
    */
}
