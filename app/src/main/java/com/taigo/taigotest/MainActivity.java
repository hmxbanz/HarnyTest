package com.taigo.taigotest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.data.ScanResult;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.taigo.taigotest.permissionLibrary.PermissionsManager;
import com.taigo.taigotest.permissionLibrary.PermissionsResultAction;
import com.taigo.taigotest.service.BluetoothService;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothService mBluetoothService;
    private ScanResult device;
    public Button btnConnect,btnNotify,btnStarLuck,btnValify,btnAccessory,btnTime;
    private BluetoothGattCharacteristic defaultCharacteristic;
    private TextView txtShow;
    private boolean isConnect;
    private final String key = "910726305003efb0f819537fa6e8f5e3";
    private List<BluetoothGattCharacteristic> characteristics;
    private boolean isShowFirst=true;
    private String MD5, MD5_3, MD5_6, MD5_8, MD5_12,MD5_2,MD5_5,MD5_7,MD5_11;
    private String s;
    private StringBuilder stringBuilder16;
    private String bleAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if (mBluetoothService == null) {
            bindService();
        }
        NLog.setDebug(true);


        int n = 1234;
        byte[] b = new byte[4];
        for(int i=0;i<4;i++){
            b[3-i] = (byte)(n & 0xFF);
            n = n>>>8;
        }

        int a = 0;
        NLog.w("qqqqqq",a);
        NLog.w("qqqqqq",Integer.toBinaryString(Integer.MIN_VALUE));
        int i = 0;
        i = a&0xff;
        NLog.w("qqqqqq",Integer.toBinaryString(a));
        NLog.w("qqqqqq",Integer.toBinaryString(i));
        //NLog.w("",i);

        int aa=2331;
        int bb=43333;
        int cc=0;
        int dd=0;
        cc=aa&bb;
        dd=cc&aa;

        NLog.w("wwwwwww",dd);
        //NLog.w("wwwwwww",bb);
        //NLog.w("wwwwwww",Integer.toBinaryString(bb));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
    }

    public void init() {
        btnConnect= (Button) findViewById(R.id.btnConnect);
        txtShow = (TextView) findViewById(R.id.txtShow);
        btnNotify= (Button) findViewById(R.id.btnNotify);
        btnStarLuck= (Button) findViewById(R.id.btnStarLuck);
        btnValify= (Button) findViewById(R.id.btnValify);
        btnTime= (Button) findViewById(R.id.btnTime);
        btnAccessory= (Button) findViewById(R.id.btnAccessory);

        String[] Permissions=new String[]{Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //权限申请
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                Permissions,
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                    }
                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(MainActivity.this, "获取权限失败，请点击后允许获取", Toast.LENGTH_SHORT).show();
                    }
                }, true);





    }
    //封装Byte[]数组
    private byte[] getByte(String num,String bleAddr) {
        byte a[]=new byte[44];
        a[0]=0x01;
        byte[] keyByte=key.getBytes();
        for (int i=0;i<keyByte.length;i++)
            a[i+1]=keyByte[i];

        byte[] ramNum=num.getBytes();
        for (int i=0;i<ramNum.length;i++)
            a[33+i]=ramNum[i];

        byte[] addr = HexUtil.hexStringToBytes(bleAddr);
        for (int i=0;i<addr.length;i++)
            a[41+i]=addr[i];

        return a;
    }

    private String MD5(byte[] s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            //byte[] bytes = md.digest(s.getBytes("utf-8"));
            byte[] bytes = md.digest(s);
            return toHex(bytes);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    public void bindService() {
        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE);
    }
    public void unbindService() {
        if(mFhrSCon!=null) {
            unbindService(mFhrSCon);
            mFhrSCon=null;
        }
    }
    private ServiceConnection mFhrSCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.BluetoothBinder) service).getService();
            mBluetoothService.setScanCallback(callback);
            //mBluetoothService.scanDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };
    private BluetoothService.Callback callback = new BluetoothService.Callback() {

        @Override
        public void onStartScan() {
            btnConnect.setText("扫描中...");
            btnConnect.setEnabled(false);
        }

        @Override
        public void onScanning(ScanResult result) {
            if(result.getDevice().getName() !=null && (result.getDevice().getName().contains("PET"))) {
                device=result;

                bleAddress=device.getDevice().getName();
                bleAddress = bleAddress.substring(4, bleAddress.length());
            }
        }

        @Override
        public void onScanComplete() {
            if(device!=null){
                        btnConnect.setText("找到一个哈尼蛋");
                        btnConnect.setEnabled(true);
            }
            else
            {
                btnConnect.setText("再次扫描");
                btnConnect.setEnabled(true);
            }

        }

        @Override
        public void onConnecting() {
            btnConnect.setText("正在连接哈尼蛋");
            btnConnect.setEnabled(true);
        }

        @Override
        public void onConnectFail() {
            NToast.shortToast(MainActivity.this, "连接失败。");
            btnConnect.setText("连接失败，重新连接");
            btnConnect.setEnabled(true);
        }

        @Override
        public void onDisConnected() {
            NToast.shortToast(MainActivity.this, "连接已断开。");
            btnConnect.setText("已断开哈尼蛋");
            btnConnect.setEnabled(true);
            btnTime.setEnabled(false);
            btnStarLuck.setEnabled(false);
            btnValify.setEnabled(false);
            btnAccessory.setEnabled(false);
            btnNotify.setEnabled(false);
        }

        @Override
        public void onServicesDiscovered() {
                isConnect=true;
                btnTime.setEnabled(true);
                btnStarLuck.setEnabled(true);
                btnValify.setEnabled(true);
                btnAccessory.setEnabled(true);
                btnNotify.setEnabled(true);
                btnConnect.setText("断开连接");
                btnConnect.setEnabled(true);
                showData();


        }
    };

    public void ConnectHandler(View view) {
        if(isConnect) {
            mBluetoothService.closeConnect();
            isConnect=false;
        }
        else
        {
            if(device!=null)
            {
                mBluetoothService.scanAndConnect5(device.getDevice().getAddress());
                btnConnect.setEnabled(false);
            }
            else{
                mBluetoothService.scanDevice();
                btnConnect.setEnabled(false);
            }

        }
    }
    public void NotifyHandler(View view) {
        this.btnNotify.setEnabled(false);
        mBluetoothService.notify(  new BleCharacterCallback() {
            @Override
            public void onSuccess(final BluetoothGattCharacteristic characteristic) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         s = String.valueOf(HexUtil.encodeHex(characteristic.getValue()));
                                if(isShowFirst){
                                    txtShow.setText("");
                                    isShowFirst=false;
                                }

                                //回复确认道具
                        if("01".equals(s.substring(0,2)))
                        {
                            mBluetoothService.write("11"+ MD5_3 + MD5_6 + MD5_8 + MD5_12 , new BleCharacterCallback() {
                                @Override
                                public void onSuccess(BluetoothGattCharacteristic characteristic) {

                                }

                                @Override
                                public void onFailure(BleException exception) {

                                }

                                @Override
                                public void onInitiatedResult(boolean result) {

                                }
                            });

                        }

                        txtShow.append(s+"\n");
                        NToast.shortToast(MainActivity.this,s);
//                        // For all other profiles, writes the data formatted in HEX.对于所有的文件，写入十六进制格式的文件
//                        //这里读取到数据
                        final byte[] data = characteristic.getValue();

                        if (data != null && data.length > 0) {
                            final StringBuilder stringBuilder = new StringBuilder(data.length);
                            final StringBuilder stringBuilder16 = new StringBuilder(data.length);
                            for (byte byteChar : data) {
                                //以十六进制的形式输出
                                stringBuilder16.append(String.format("%02x ", byteChar));
                                //以原始byte形式输出
                                stringBuilder.append(" "+byteChar);
                            }

                            NLog.w("BLEBLE---原始byte", stringBuilder.toString());
                            NLog.d("BLEBLE---转换成16进制", stringBuilder16.toString());
                        }
//                                byte command = data[1];
//                                int aaaaa=command & 0x01;
//                                NLog.d("aaBLEBLE>>>>>>>>>",Integer.toHexString(170));

                    }
                });
            }

            @Override
            public void onFailure(final BleException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            @Override
            public void onInitiatedResult(boolean result) {

            }

        });
    }
    public void StarLuckHandler(View view) {

        mBluetoothService.write( "03454321323534555511112222", new BleCharacterCallback() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic characteristic) {

            }

            @Override
            public void onFailure(BleException exception) {

            }

            @Override
            public void onInitiatedResult(boolean result) {

            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBluetoothService.write( "04333344442123434531353315", new BleCharacterCallback() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic characteristic) {

            }

            @Override
            public void onFailure(BleException exception) {

            }

            @Override
            public void onInitiatedResult(boolean result) {

            }
        });

        NToast.shortToast(this,"发送星座运势");
    }

    public void ValifyHandler(View view) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("ddHHmmss");
        String ramStr = sdf.format(date);

        MD5=MD5(getByte(ramStr,bleAddress));
        MD5_3 =MD5.substring(6,8);
        MD5_6 =MD5.substring(12,14);
        MD5_8 =MD5.substring(16,18);
        MD5_12 =MD5.substring(24,26);
        MD5_2 =MD5.substring(4,6);
        MD5_5 =MD5.substring(10,12);
        MD5_7 =MD5.substring(14,16);
        MD5_11 =MD5.substring(22,24);

        int i=Integer.valueOf(ramStr);
        String s = Integer.toHexString(i);
        byte[] data = Ntool.Int2Bytes_LE(i);
        stringBuilder16=null;
        if (data != null && data.length > 0) {
            stringBuilder16 = new StringBuilder(data.length);
            for (byte byteChar : data) {
                //以十六进制的形式输出
                stringBuilder16.append(String.format("%02x", byteChar));
            }

            NLog.d("BLEBLE---转换成16进制", stringBuilder16.toString());
        }

        mBluetoothService.write("20"+stringBuilder16, new BleCharacterCallback() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic characteristic) {
            String aa="";
            }

            @Override
            public void onFailure(BleException exception) {

            }

            @Override
            public void onInitiatedResult(boolean result) {

            }
        });
    }
    public void AccessoryHandler(View view) {
        mBluetoothService.write("01"+ MD5_3 + MD5_6 + MD5_8 + MD5_12 +"0101", new BleCharacterCallback() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic characteristic) {

            }

            @Override
            public void onFailure(BleException exception) {

            }

            @Override
            public void onInitiatedResult(boolean result) {

            }
        });

        NToast.shortToast(MainActivity.this,"发送道具");
    }
    public void TimeHandler(View view) {
        mBluetoothService.write("02180223112211", new BleCharacterCallback() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic characteristic) {
                String aa="";
            }

            @Override
            public void onFailure(BleException exception) {

            }

            @Override
            public void onInitiatedResult(boolean result) {

            }
        });


        NToast.shortToast(this,"发送时间");
    }


    public void showData() {
        BluetoothGatt gatt = mBluetoothService.getGatt();
        for (BluetoothGattService servicess : gatt.getServices()) {
            Log.w(TAG, "================== find service: " + servicess.getUuid().toString());
            if (servicess.getUuid().toString().startsWith("0000ffe0")) {//枪发给手机
                mBluetoothService.setService(servicess);
                characteristics = servicess.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {

                    if (characteristic.getUuid().toString().startsWith("0000ffe2"))
                        mBluetoothService.setWriteCharacteristic(characteristic);

                    if (characteristic.getUuid().toString().startsWith("0000ffe1")) {
                        mBluetoothService.setCharacteristic(characteristic);
                        defaultCharacteristic=characteristic;
                    }

                    Log.w(TAG, "================== find characteristics count: " + characteristics.size());
//                            BluetoothGattCharacteristic characteristic = characteristics.get(0);
                    Log.w(TAG, "================== find characteristic: " + characteristic.getUuid().toString());
                    //Log.w(TAG, "================== characteristic value: " + byte2HexStr(characteristic.getValue()));
                    //gatt.setCharacteristicNotification(characteristic, true);
                    Log.w(TAG, "================== Thread : " + Thread.currentThread().getId());


                }
            }
        }

    }
}
