package com.example.kdh.beacontest;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private long lastTimeBackPressed;
    private Button btn_Connect;
    private Button modifybutton;
    private bluetooth bluetoothService_obj = null;
    private Button verifybutton;
    private static final String TAG = "MAIN";

    String response, serv_addr, address; //연결시 사용할 주소와 response

    private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    public boolean CheckAppFirstExecute(){
        SharedPreferences pref = getSharedPreferences("IsFirst" , Activity.MODE_PRIVATE);
        boolean isFirst = pref.getBoolean("isFirst", false);
        if(!isFirst){ //최초 실행시 true 저장
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirst", true);
            editor.commit();
            showAlertDialog();
        }

        return !isFirst;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serv_addr = "http://211.222.232.176:3001";// Node.js 연결
        address = "";
        response = "";

        CheckAppFirstExecute();

        //1번 버튼
        btn_Connect = (Button) findViewById(R.id.bluetoothbutton);
        btn_Connect.setOnClickListener(bluetooth.mClickListener);
        if (bluetoothService_obj == null) {
            bluetoothService_obj = new bluetooth(this, mHandler);
        }

        //2번 버튼
        verifybutton = (Button) findViewById(R.id.verifybutton);
        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                showAlertDialog();  //집 정보 받기, 추후에 사용


                //통신
            }
        });

        //final List selectedItem = new ArrayList();

        //3번 버튼
        modifybutton = (Button) findViewById(R.id.modifybutton);
        modifybutton.setOnClickListener(new View.OnClickListener() {
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
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        protected void onPreExecute() {
            String useruuid = null;     //uuid 받아오기
            useruuid = GetDevicesUUID(getBaseContext());
            address = serv_addr + "/verify?uuid=" + useruuid;
            TextView print_uuid = (TextView) findViewById(R.id.uuid);
            print_uuid.setText(useruuid);
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            response = request(address);
            Log.e("response", "get response data");
            Log.e("response", response);
            return null;
        }

        protected void onPostExecute(Integer a) {
            TextView btn_verify = (TextView) findViewById(R.id.verifybutton);
            Log.e("response", "before compareTo : " + response);
            if (response.compareTo("true") == 1) {
                btn_verify.setText("Verified!!");
                btn_verify.setEnabled(false);
            } else if (response.compareTo("false") == 1) {
                btn_verify.setText("Fail to Verify, Retry");
            }
            address = "";
        }
    }

    /* request 요청 및 response를 받아 반환*/
    private String request(String urlStr) {
        Log.e("response", "request pass");
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                Log.e("response", "connected!!");
                //conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //conn.setDoOutput(true);   //방식이 post로 바뀌게 됨

                Log.e("response", "HTTP_OK : " + Integer.toString(HttpURLConnection.HTTP_OK));
                Log.e("response", "address : " + address);
                int resCode = conn.getResponseCode();       //문제점......
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

    private void showAlertDialog() {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout loginLayout = (LinearLayout) vi.inflate(R.layout.dialog_verify, null);

        final EditText input_house_number = (EditText) loginLayout.findViewById(R.id.input_house_num);
        final EditText input_house_psw = (EditText) loginLayout.findViewById(R.id.input_house_psw);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        Log.e("response", "showAlertDialog pass");

        adb.setTitle("인증");
        adb.setView(loginLayout);
        adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { //button 클릭시 connect


                BackgroundTask task = new BackgroundTask();
                task.execute();

                Toast.makeText(MainActivity.this, "집 호수 : " +
                        input_house_number.getText().toString() + "\n비밀번호 : " +
                        input_house_psw.getText().toString(), Toast.LENGTH_LONG).show();

            }
            /*
            private String DownloadHtml(String addr) {
                StringBuilder html = new StringBuilder();
                try {
                    //인터넷상의 자원이나 서비스 주소값을 URL 객체로 생성합니다.
                    URL url = new URL(addr);

                    //해당 UTL로 접속합니다.
                    //접속에 성공하면 양방향 통신이 가능한 연결 객체(HttpURLConnection)가 리턴됩니다.
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    if (conn != null) {
                        //연결 제한 시간을 1/1000 초 단위로 지정합니다.
                        //0이면 무한 대기입니다.
                        conn.setConnectTimeout(10000);

                        //읽기 제한 시간을 지정합니다. 0이면 무한 대기합니다.
                        //conn.setReadTimeout(0);

                        //캐쉬 사용여부를 지정합니다.
                        conn.setUseCaches(false);

                        //http 연결의 경우 요청방식을 지정할수 있습니다.
                        //지정하지 않으면 디폴트인 GET 방식이 적용됩니다.
                        //conn.setRequestMethod("GET" | "POST");

                        //서버에 요청을 보내가 응답 결과를 받아옵니다.
                        int resCode = conn.getResponseCode();

                        //요청이 정상적으로 전달되엇으면 HTTP_OK(200)이 리턴됩니다.
                        //URL이 발견되지 않으면 HTTP_NOT_FOUND(404)가 리턴됩니다.
                        //인증에 실패하면 HTTP_UNAUTHORIZED(401)가 리턴됩니다.
                        if (resCode == HttpURLConnection.HTTP_OK) {

                            //요청에 성공했으면 getInputStream 메서드로 입력 스트림을 얻어 서버로부터 전송된 결과를 읽습니다.
                            InputStreamReader isr = new InputStreamReader(conn.getInputStream());

                            //스트림을 직접읽으면 느리고 비효율 적이므로 버퍼를 지원하는 BufferedReader 객체를 사용합니다.

                            BufferedReader br = new BufferedReader(isr);
                            for (; ; ) {
                                String line = br.readLine();
                                if (line == null) break;
                                html.append(line + "\n");
                            }
                            br.close();
                        }
                    }
                    conn.disconnect();
                } catch (Exception e) {
                }
                //html 이 리턴값
                return html.toString();
            }*/
        }).show();

    }

}