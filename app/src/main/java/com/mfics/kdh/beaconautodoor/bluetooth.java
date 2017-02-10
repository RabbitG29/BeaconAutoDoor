package com.mfics.kdh.beaconautodoor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.util.UUID;

/**
 * Created by KDH on 2017-01-26, modified by KDH on 2017-02-10
 */

public class bluetooth {
    private static final int REQUEST_ENABLE_BT=2;
    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothAdapter btAdapter;
    private static Activity mActivity;
    private static Handler mHandler;

    public bluetooth(Activity activity, Handler handler)
    {
        mActivity = activity;
        mHandler = handler;

        //bluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static boolean getDeviceState()
    {
        Log.d(TAG, "Check the Bluetooth support");

        if(btAdapter==null)
        {
            Log.d(TAG, "Bluetooth is not available");
            return false;
        }

        else
        {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }

    public static void enableBluetooth()
    {
        Log.i(TAG, "Check the enable Bluetooth");

        if(btAdapter.isEnabled())
        {
            //기기의 블루투스 상태가 On일 경우..
            Log.d(TAG, "Bluetooth Enable Now");
            btAdapter.disable();


        }
        else
        {
            //기기의 블루투스 상태가 off일 경우
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult" + resultCode);
        // TODO Auto-generated method stub

        switch(requestCode)
        {

            case REQUEST_ENABLE_BT:
                //When the request to enable Bluetooth returns
                if(resultCode != Activity.RESULT_OK)  //취소를 눌렀을 때
                {
                    Log.d(TAG, "Bluetooth is not enable");
                }
                break;
        }
    }

    public static View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //분기.
            switch ( view.getId() ){

                case R.id.bluetoothbutton :  //모든 블루투스의 활성화는 블루투스 서비스 객체를 통해 접근한다.

                    if(getDeviceState()) // 블루투스 기기의 지원여부가 true 일때
                    {
                        enableBluetooth();  //블루투스 활성화 시작.
                    }

                    break ;

                default: break ;

            }//switch
        }
    };


}
