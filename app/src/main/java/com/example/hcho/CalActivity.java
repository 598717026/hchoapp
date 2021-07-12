package com.example.hcho;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class CalActivity extends Activity {

    private static final String TAG = "Hcho Cal";

    private static final String HCHO_SERVICE = "xtchcho";

    public static final int ERROR_NO_ERROR = 0;//成功
    public static final int ERROR_BTDEVICE_EXC_FAIL = 1;// 蓝牙锁端执行错误
    public static final int ERROR_PSD_ERROR = 2; // 钥匙错误
    public static final int ERROR_CONNECT_FAIL = 3;
    public static final int ERROR_DEFAULT = 4;
    public static final int ERROR_NOT_FOUND_DEVICE = 5;// 未扫描到指定编码的锁
    public static final int ERROR_HAVE_CONNECTED = 6;//上次连接未执行完毕
    public static final int ERROR_NO_DEVICE = 7;//设备没有初始化
    public static final int ERROR_CREATE_FAIL = 8;

    public static final String BLE_DEVICE_NAME = "HCHO CAL";

    private static final int STOP_LESCAN = 1;
    public BluetoothManager btManager;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic = null;

    Object hchoService = null;

    private int m_AdcMax = 0;
    private int m_AdcMin = 0;
    private int m_ValMax = 0;
    private int m_ValMin = 0;
    private MySqliteOpenHelper sqliteOpenHelper;

    Timer timerGetAdc = null;
    TimerTask taskGetAdc = null;
    Timer timerUpdateSetting = null;
    TimerTask taskUpdateSetting = null;

    Timer timer = null;
    TimerTask task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestPermissions");
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestPermissions");
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, 1);
            }
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestPermissions");
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 1);
            }
        }
        sqliteOpenHelper = new MySqliteOpenHelper(this, Constant.DB_NAME, 1);

        m_AdcMax = sqliteOpenHelper.getAdcMax();
        m_AdcMin = sqliteOpenHelper.getAdcMin();
        m_ValMax = sqliteOpenHelper.getValueMax();
        m_ValMin = sqliteOpenHelper.getValueMin();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Message message = new Message();

                message.what = 3;

                mHandler.sendMessage(message);
            }
        }).start();

        btManager= (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = btManager.getAdapter();
        if (mBluetoothAdapter != null && (!mBluetoothAdapter.isEnabled())){
            mBluetoothAdapter.enable();//打开蓝牙
        }

        Button button = findViewById(R.id.connect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         *要执行的操作
                         */
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }, 10000);
            }
        });

        hchoService = this.getSystemService(HCHO_SERVICE);


    }

    @Override
    protected void onPause() {
        super.onPause();

//        timerGetAdc.cancel();
//        timerUpdateSetting.cancel();

        timer.cancel();

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTimerTask();
    }

    private void initTimerTask()
    {
//        // 每秒对采样一次
//        timerGetAdc = new Timer();
//        taskGetAdc = new TimerTask() {
//            @Override
//            public void run() {
//
//                int adc = getHchoAdc();
//                Log.d(getClass().getName(), "xtchcho：" + adc);
//                int val = sampleToPpb(adc);
//                postValue2Ble(adc, val);
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Message message = new Message();
//
//                        message.what = 2;
//                        message.arg1 = adc;
//                        message.arg2 = val;
//
//                        mHandler.sendMessage(message);
//                    }
//                }).start();
//            }
//        };
//
//        // 每3秒读一次更新一次配置
//        timerUpdateSetting = new Timer();
//        taskUpdateSetting = new TimerTask() {
//            @Override
//            public void run() {
//                getSetting();
//            }
//        };
//
//        timerGetAdc.schedule(taskGetAdc,5000,1000);
//        timerUpdateSetting.schedule(taskUpdateSetting,6000,3000);


        // 每秒对采样一次
        timer = new Timer();
        task = new TimerTask() {
            int counter = 0;
            @Override
            public void run() {

                int adc = getHchoAdc();
                Log.d(getClass().getName(), "xtchcho：" + adc);
                int val = sampleToPpb(adc);

                counter += 1;
                if (counter % 5 == 0)
                {
                    getSetting();
                }
                else {
                    postValue2Ble(adc, val);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Message message = new Message();

                        message.what = 2;
                        message.arg1 = adc;
                        message.arg2 = val;

                        mHandler.sendMessage(message);
                    }
                }).start();
            }
        };
        timer.schedule(task,5000,1000);

    }

    int sampleToPpb(int adc)
    {
        if (m_AdcMax != 0) {
            return  ((adc - m_AdcMin) * (m_ValMax - m_ValMin)) / (m_AdcMax - m_AdcMin) + m_ValMin;
        }
        else
        {
            return adc;
        }
    }

    public int getHchoAdc()
    {
//        if (hchoService == null)
//        {
//            return 0;
//        }
//
//        Object invoke = 0;
//        try {
//            invoke = hchoService.getClass().getDeclaredMethods()[0].invoke(hchoService, null);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        return (int) invoke;
        return sqliteOpenHelper.getAdc();
    }

    private Context mContext = null;

    private Handler mHandler = new Handler() {

        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage");
            TextView textView = findViewById(R.id.statusView);

            switch (msg.what) {

                case 1:
                    textView.setText("connected");
                    postSetting(m_AdcMax, m_AdcMin, m_ValMax, m_ValMin);
                    break;
                case 2:
                    TextView tvadc = findViewById(R.id.adcView);
                    tvadc.setText("ADC:" + msg.arg1);
                    TextView tvval = findViewById(R.id.ppbView);
                    tvval.setText("PPB:" + msg.arg2);
                    break;

                case 3:
                    TextView tvadcmax = findViewById(R.id.adcMaxView);
                    tvadcmax.setText("ADCMAX:" + m_AdcMax);
                    TextView tvadcmin = findViewById(R.id.adcMinView);
                    tvadcmin.setText("ADCMIN:" + m_AdcMin);
                    TextView tvvalmax = findViewById(R.id.valMaxView);
                    tvvalmax.setText("PPBMAX:" + m_ValMax);
                    TextView tvvalmin = findViewById(R.id.valMinView);
                    tvvalmin.setText("PPBMIN:" + m_ValMin);
                    break;

                default:
                    textView.setText("disconnected");

                    mBluetoothAdapter = btManager.getAdapter();
                    if (mBluetoothAdapter != null && (!mBluetoothAdapter.isEnabled())){
                        mBluetoothAdapter.enable();//打开蓝牙
                        Log.d(TAG, "(mBluetoothAdapter != null && (!mBluetoothAdapter.isEnabled()))");
                        try{

                            Thread.sleep(4000);

                        }catch (Exception e ){



                        }

                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 *要执行的操作
                                 */
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            }
                        }, 10000);
                    }
                    break;

            }

        }

    };

    public void postValue2Ble(int adc, int value)
    {
        if (writeCharacteristic == null)
        {
            return;
        }

        byte[] s = new byte[4];

        s[0] = (byte)(adc & 0xff);
        s[1] = (byte)((adc >> 8) & 0xff);
        s[2] = (byte)(value & 0xff);
        s[3] = (byte)((value >> 8) & 0xff);
        writeCharacteristic.setValue(s);
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    public void postSetting(int adc_max, int adc_min, int val_max, int val_min)
    {
        Log.d(TAG, "postSetting");
        if (writeCharacteristic == null)
        {
            return;
        }
        Log.d(TAG, "postSetting start");

        byte[] s = new byte[14];

        s[0] = (byte)((val_min >> 8) & 0xff);
        s[1] = (byte)(val_min & 0xff);
        s[2] = (byte)((val_max >> 8) & 0xff);
        s[3] = (byte)(val_max & 0xff);
        s[4] = (byte)((adc_min >> 8) & 0xff);
        s[5] = (byte)(adc_min & 0xff);
        s[6] = (byte)((adc_max >> 8) & 0xff);
        s[7] = (byte)(adc_max & 0xff);
        s[8] = 0;
        s[9] = 0;
        s[10] = 0;
        s[11] = 0;
        s[12] = 0;
        s[13] = 0;
        writeCharacteristic.setValue(s);
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    public void getSetting()
    {
        Log.d(TAG, "getSetting");
        if (writeCharacteristic == null)
        {
            return;
        }

        Log.d(TAG, "getSetting start");
        mBluetoothGatt.readCharacteristic(writeCharacteristic);
    }

    private int getSettingAdcMax(byte[] s)
    {
        return ((int)s[2]) * 256 + s[3];
    }

    private int getSettingAdcMin(byte[] s)
    {
        return ((int)s[0]) * 256 + s[1];
    }

    private int getSettingValMax(byte[] s)
    {
        return ((int)s[4]) * 256 + s[5];
    }

    private int getSettingValMin(byte[] s)
    {
        return ((int)s[6]) * 256 + s[7];
    }

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i("LeScanCallback", "onLeScan() DeviceName------>" + device.getName()); // 在这里可通过device这个对象来获取到搜索到的ble设备名称和一些相关信息
            if (device.getName() != null) {
                if (device.getName().equals(BLE_DEVICE_NAME)) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothDevice = device;
                    connect();
                }
            }
        }
    };

    public int connect() {
        if (mBluetoothDevice == null) {
            Log.i(TAG, "BluetoothDevice is null.");
            return ERROR_NO_DEVICE;
        }

        // 两个设备通过BLE通信，首先需要建立GATT连接。这里我们讲的是Android设备作为client端，连接GATT Server

        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false,
                mGattCallback); // mGattCallback为回调接口

        if (mBluetoothGatt != null) {

            if (mBluetoothGatt.connect()) {
                Log.d(TAG, "Connect succeed.");
                return ERROR_NO_ERROR;
            } else {
                Log.d(TAG, "Connect fail.");
                mBluetoothGatt = null;
                return ERROR_CONNECT_FAIL;
            }
        } else {
            Log.d(TAG, "BluetoothGatt null.");
            return ERROR_CREATE_FAIL;
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices(); // 执行到这里其实蓝牙已经连接成功了
                Log.i(TAG, "Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothProfile.STATE_DISCONNECTED --release()");
                writeCharacteristic = null;
                mBluetoothGatt = null;

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Message message = new Message();

                        message.what = 0;

                        mHandler.sendMessage(message);
                    }
                }).start();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered");
                List<BluetoothGattService> services = gatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    Log.i(TAG, "[Service " + i + "] uuid:"
                            + services.get(i).getUuid());
                    if (services.get(i).getUuid().equals(UUID.fromString("0000fe40-cc7a-482a-984a-7f2ed5b3e58f"))) {
                        List<BluetoothGattCharacteristic> characteristics = services
                                .get(i).getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            Log.i(TAG, "[Characteristic]"
                                    + characteristics.get(j).getUuid());
                            if (characteristics.get(j).getUuid()
                                    .equals(UUID.fromString("0000fe41-8e22-4541-9d4c-21edae82ed19"))) {

                                writeCharacteristic = characteristics.get(j);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Message message = new Message();

                                        message.what = 1;

                                        mHandler.sendMessage(message);
                                    }
                                }).start();

                                /*byte[] value = new byte[] { 0x01, 0x00,
                                        0x00, 0x01 };
                                characteristics.get(j).setValue(value);
                                characteristics.get(j)
                                        .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                mBluetoothGatt
                                        .writeCharacteristic(characteristics
                                                .get(j));*/
                                break;
                            }
                        }
                        characteristics = null;
                    }
                }

                /*for (int i = 0; i < services.size(); i++) {
                    Log.i(TAG, "[Service " + i + "] uuid:"
                            + services.get(i).getUuid());
                    if (services.get(i).getUuid().equals(Value.uuid)) {
                        List<BluetoothGattCharacteristic> characteristics = services
                                .get(i).getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            Log.i(TAG, "[Characteristic]"
                                    + characteristics.get(j).getUuid());
                            if (characteristics.get(j).getUuid()
                                    .equals(Value.CHARACTERISTIC_WRITE)) {
                                writeCharacteristic = characteristics.get(j);
                                final int charaProp = characteristics.get(j)
                                        .getProperties();
                                // 如果该char可写
                                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                    byte[] value = new byte[] { 0x01, 0x00,
                                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                            0x00, 0x00, 0x00 };
                                    characteristics.get(j).setValue(value);
                                    writeCharacteristic
                                            .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                    mBluetoothGatt
                                            .writeCharacteristic(characteristics
                                                    .get(j));
                                    value = null;
                                }
                                break;
                            }
                        }
                        characteristics = null;
                    }
                }*/
                services = null;
            } else {
                Log.i(TAG, "onServicesDiscovered status------>" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG,
                    "onCharacteristicRead------>"
                            + characteristic.getValue().length);
            Log.i(TAG,
                    "status------>"
                            + status);

            for (int i = 0;i < characteristic.getValue().length; i++)
            {
                Log.i(TAG,
                        "onCharacteristicRead------>"
                                + characteristic.getValue()[i]);
            }

            if (characteristic.getValue().length == 14)
            {
                Log.i(TAG,
                        "setting");
                int AdcMax = getSettingAdcMax(characteristic.getValue());
                int AdcMin = getSettingAdcMin(characteristic.getValue());
                int ValMax = getSettingValMax(characteristic.getValue());
                int ValMin = getSettingValMin(characteristic.getValue());
                if (AdcMax != m_AdcMax || AdcMin != m_AdcMin || ValMax != m_ValMax || ValMin != m_ValMin)
                {
                    sqliteOpenHelper.updateSetting(AdcMax, AdcMin, ValMax, ValMin);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Message message = new Message();

                            message.what = 0;

                            mHandler.sendMessage(message);
                        }
                    }).start();
                }
                m_AdcMax = AdcMax;
                m_AdcMin = AdcMin;
                m_ValMax = ValMax;
                m_ValMin = ValMin;
            }
        }



        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG,
                    "onCharacteristicChanged------>"
                            + characteristic.getValue());
            Log.i(TAG, "UUID------>" + characteristic.getUuid().toString());

        }

        // 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发onCharacteristicWrite
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG,
                    "onCharacteristicWrite status = " + status
                            + ",onCharacteristicWrite------>"
                            + characteristic.getValue());
            if (status != 0) {
//                disconnect(true);
            }
        }
    };

}