package com.cyber.myapplication;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static String mac = "0C:61:CF:2B:B9:0C";
    static String uuid_service = "00001001-0000-1000-8000-00805f9b34fb";
    static String uuid_notify = "00001002-0000-1000-8000-00805f9b34fb";
    static String uuid_send = "00001001-0000-1000-8000-00805f9b34fb";
    Button connect,send;
    AppCompatTextView text;
    BleManager bleManager = BleManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        connect = findViewById(R.id.connect);
        send = findViewById(R.id.send);
        text = findViewById(R.id.text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect:
                bleManager.connect(mac, CallBack.getConnectSuccessCallback());
                break;
            case R.id.send:
                BleDevice cur = bleManager.getAllConnectedDevice().get(0);
                //1.设置蓝牙监听第一次发送
                BleManager.getInstance().notify(cur,uuid_service,uuid_notify,CallBack.getNotifyCallback_1());

        }
    }
    public static  int  cnt =0;
    public static byte[] getInputBytes() {
        String s1 = "201801340003274A";
        String s2 = "201801340002E68A";
//        return HexString.hexToBytes(writeInput.getText().toString());
        int meow = cnt/2;cnt++;
        return meow % 2 == 0 ? hexToByteArray(s1) : hexToByteArray(s2);
    }
    public static byte[] hexToByteArray(String var0) {
        if (var0 != null && var0.length() != 0) {
            if (var0.length() % 2 == 1) {
                var0 = "0" + var0;
            }

            String[] var1;
            byte[] var2 = new byte[(var1 = new String[var0.length() / 2]).length];

            for (int var3 = 0; var3 < var1.length; ++var3) {
                var1[var3] = var0.substring(2 * var3, 2 * (var3 + 1));
                var2[var3] = (byte) Integer.parseInt(var1[var3], 16);
            }

            return var2;
        } else {
            return null;
        }
    }
    public static String byteArrayToHex(byte[] var0) {
        if (var0 != null && var0.length != 0) {
            StringBuilder var1 = new StringBuilder(var0.length);

            for(int var2 = 0; var2 < var0.length; ++var2) {
                var1.append(String.format("%02X", var0[var2]));
                if (var2 < var0.length - 1) {
                    var1.append(' ');
                }
            }

            return var1.toString();
        } else {
            return "";
        }
    }
    static final class CallBack{
        BleManager bleManager = BleManager.getInstance();
        static BleNotifyCallback getNotifyCallback_1() {
            return new BleNotifyCallback() {
                private static final String TAG = "NotifyCallback_1";
                //2.如果监听设置成功,则发送第一次发送的信息
                @Override
                public void onNotifySuccess() {
                    Log.d(TAG, "onNotifySuccess: "+Thread.currentThread().getName());
                    BleDevice cur = BleManager.getInstance().getAllConnectedDevice().get(0);
                    BleManager.getInstance().write(cur,uuid_service,uuid_send,getInputBytes(),getBleSendCallback_1());
                }

                @Override
                public void onNotifyFailure(BleException exception) {
                    Log.d(TAG, "onNotifyFailure: "+Thread.currentThread().getName());
                }

                @Override
                public void onCharacteristicChanged(byte[] data) {
                    Log.d(TAG, "onCharacteristicChanged: "+Thread.currentThread().getName()+" "+byteArrayToHex(data));
                    BleManager bleManager = BleManager.getInstance();
                    bleManager.stopNotify(getConnectedBleDevice(), uuid_service, uuid_notify);
                    bleManager.notify(getConnectedBleDevice(), uuid_service, uuid_notify,getNotifyCallback_2());
                }
            };
        }
        static BleNotifyCallback getNotifyCallback_2() {
            return new BleNotifyCallback() {
                private static final String TAG = "getNotifyCallback_2";

                @Override
                public void onNotifySuccess() {
                    Log.d(TAG, "onNotifySuccess: "+Thread.currentThread().getName());
                    getBleManager().write(getConnectedBleDevice(),uuid_service,uuid_send,getInputBytes(),getBleSendCallback_2());
                }

                @Override
                public void onNotifyFailure(BleException exception) {
                    Log.d(TAG, "onNotifyFailure: "+Thread.currentThread().getName());
                }

                @Override
                public void onCharacteristicChanged(byte[] data) {
                    Log.d(TAG, "onCharacteristicChanged: "+Thread.currentThread().getName()+" "+byteArrayToHex(data));
                    BleManager bleManager = BleManager.getInstance();
                    bleManager.stopNotify(getConnectedBleDevice(), uuid_service, uuid_notify);

                }
            };
        }
        static BleWriteCallback getBleSendCallback_1() {
            return new BleWriteCallback() {
                private static final String TAG = "getBleSendCallback_1";
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                    Log.d(TAG, "onWriteSuccess: " + byteArrayToHex(justWrite)+" "+Thread.currentThread().getName());
                }

                @Override
                public void onWriteFailure(BleException exception) {
                    Log.d(TAG, "onWriteFailure: " + exception.toString()+" "+Thread.currentThread().getName());
                }
            };
        }
        static BleWriteCallback getBleSendCallback_2() {
            return new BleWriteCallback() {
                private static final String TAG = "getBleSendCallback_2";
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                    Log.d(TAG, "onWriteSuccess: " + byteArrayToHex(justWrite)+" "+Thread.currentThread().getName());
                }

                @Override
                public void onWriteFailure(BleException exception) {
                    Log.d(TAG, "onWriteFailure: " + exception.toString()+" "+Thread.currentThread().getName());
                }
            };
        }
        static BleGattCallback getConnectSuccessCallback() {
            return new BleGattCallback() {
                private static final String TAG = "ConnectSuccessCallback";

                @Override
                public void onStartConnect() {
                    Log.d(TAG, "onStartConnect: "+" "+Thread.currentThread().getName());
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.d(TAG, "onConnectFail: "+" "+Thread.currentThread().getName());
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Log.d(TAG, "onConnectSuccess: "+" "+Thread.currentThread().getName());
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    Log.d(TAG, "onDisConnected: "+" "+Thread.currentThread().getName());
                }
            };
        }

        static BleManager getBleManager() {
            return BleManager.getInstance();
        }

        static BleDevice getConnectedBleDevice() {
            return getBleManager().getAllConnectedDevice().get(0);
        }

    }
}
