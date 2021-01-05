package com.tourcoo.smartpark.print_old;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;


import com.newland.aidl.beeper.AidlBeeper;
import com.newland.aidl.bluetoothbase.AidlBluetoothBase;
import com.newland.aidl.deviceInfo.AidlDeviceInfo;
import com.newland.aidl.deviceService.AidlDeviceService;
import com.newland.aidl.guestDisplay.AidlGuestDisplay;
import com.newland.aidl.iccard.AidlCPUCard;
import com.newland.aidl.iccard.AidlICCard;
import com.newland.aidl.led.AidlLED;
import com.newland.aidl.magcardreader.AidlMagCardReader;
import com.newland.aidl.pboc.AidlPBOC;
import com.newland.aidl.pinpad.AidlPinpad;
import com.newland.aidl.printer.AidlPrinter;
import com.newland.aidl.rfcard.AidlRFCard;
import com.newland.aidl.scanner.AidlScanner;
import com.newland.aidl.serialComm.AidlSerialComm;
import com.newland.aidl.terminal.AidlTerminalManage;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * @author :JenkinsZhou
 * @description : 打印相关服务
 * @company :途酷科技
 * @date 2020年12月14日14:18
 * @Email: 971613168@qq.com
 */
public class DeviceService {
    public DeviceConnectListener connectListener;
    private static AidlDeviceService deviceService;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());

    public DeviceService(Context context, DeviceConnectListener connectListener) {
        this.context = context;
        this.connectListener = connectListener;
    }

    public void connect() {
        SDKExecutors.getThreadPoolInstance().submit(new Runnable() {
            @Override
            public void run() {
                if (deviceService == null) {
                    Intent intent = new Intent();
                    intent.setPackage("com.newland.mtype.service"); // 使用服务apk的时候使用
//                    intent.setPackage(context.getPackageName()); //使用arr的时候使用
                    intent.setAction("com.newland.DeviceService");
                    context.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                    setDisconnecting();
                } else {
                    setDisconnectSuccess();
                }
            }
        });
    }

    public void disconnect() {
        SDKExecutors.getThreadPoolInstance().submit(new Runnable() {
            @Override
            public void run() {
                if (deviceService != null) {
                    try {
                        context.unbindService(serviceConnection);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setDisconnected();

                    } finally {
                        deviceService = null;
                        setDisconnected();
                    }
                } else {
                    setNoDisconnect();
                }
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            deviceService = null;
            setDisconnected();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            deviceService = AidlDeviceService.Stub.asInterface(service);
            if (connectListener != null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    connectListener.deviceConnectSuccess();
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectListener.deviceConnectSuccess();
                        }
                    });
                }
            }
        }
    };

    public static AidlBeeper getBeeper() {
        try {
            return AidlBeeper.Stub.asInterface(deviceService.getBeeper());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlLED getLed() {
        try {
            return AidlLED.Stub.asInterface(deviceService.getLed());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlSerialComm getSerialPort() {
        try {
            return AidlSerialComm.Stub.asInterface(deviceService.getSerialComm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlScanner getScanner() {
        try {
            return AidlScanner.Stub.asInterface(deviceService.getScanner());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlMagCardReader getMagCardReader() {
        try {
            return AidlMagCardReader.Stub.asInterface(deviceService.getMagCardReader());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlICCard getInsertCardReader() {
        try {
            return AidlICCard.Stub.asInterface(deviceService.getICCard());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlCPUCard getCPUCardReader() {
        try {
            return AidlCPUCard.Stub.asInterface(deviceService.getCPUCard());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlRFCard getRFCardReader() {
        try {
            return AidlRFCard.Stub.asInterface(deviceService.getRFCard());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlPinpad getPinpad() {
        try {
            return AidlPinpad.Stub.asInterface(deviceService.getPinpad());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlPrinter getPrinter() {
        try {
            return AidlPrinter.Stub.asInterface(deviceService.getPrinter());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlPBOC getPBOC() {
        try {
            return AidlPBOC.Stub.asInterface(deviceService.getPBOC());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlDeviceInfo getDeviceInfo() {
        try {
            return AidlDeviceInfo.Stub.asInterface(deviceService.getDeviceInfo());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlTerminalManage getTerminalManage() {
        try {
            return AidlTerminalManage.Stub.asInterface(deviceService.getTerminal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlGuestDisplay getGuestDisplay() {
        try {
            return AidlGuestDisplay.Stub.asInterface(deviceService.getGuestDisplay());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AidlBluetoothBase getBluetoothBase() {
        try {
            return AidlBluetoothBase.Stub.asInterface(deviceService.getBluetoothBase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void setDisconnected() {
        if (connectListener != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                connectListener.deviceDisConnected();
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(connectListener != null){
                            connectListener.deviceDisConnected();
                        }
                    }
                });
            }
        }
    }


    private void setDisconnectSuccess() {
        if (connectListener != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                connectListener.deviceConnectSuccess();
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectListener.deviceConnectSuccess();
                    }
                });
            }
        }
    }

    private void setDisconnecting() {
        if (connectListener != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                if(connectListener != null){
                    connectListener.deviceConnecting();
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(connectListener != null){
                            connectListener.deviceConnecting();
                        }
                    }
                });
            }
        }
    }


    private void setNoDisconnect() {
        if (connectListener != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                if(connectListener != null){
                    connectListener.deviceNoConnect();
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(connectListener != null){
                            connectListener.deviceNoConnect();
                        }
                    }
                });
            }
        }
    }

    public DeviceConnectListener getConnectListener() {
        return connectListener;
    }


}
