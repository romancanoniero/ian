package com.iyr.ian.utils.bluetooth.ble;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_UNKNOWN;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.iyr.ian.BuildConfig;
import com.iyr.ian.app.AppClass;
import com.iyr.ian.utils.bluetooth.ble.BLECentralManagerInterface;
import com.iyr.ian.utils.bluetooth.ble.BLECharacteristic;
import com.iyr.ian.utils.bluetooth.ble.BLEPeripheralInterace;
import com.iyr.ian.utils.bluetooth.ble.BLEPeripheralObservablesInterface;
import com.iyr.ian.utils.bluetooth.ble.BLEPeripheralState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class BLEPeripheralDefault implements BLEPeripheralInterace {
    private static final String LT = BLEPeripheralDefault.class.getName();
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    @NonNull
    private final BluetoothDevice device;
    @NonNull
    private final BLECentralManagerInterface manager;
    private final BluetoothGatt[] gatt = new BluetoothGatt[]{null};
    private static final int RSSI_INTERVAL_MS = 500;
    private BLEService[] services = new BLEService[]{};

    private BLEPeripheralState state = BLEPeripheralState.disconnected;

    private synchronized void setState(BLEPeripheralState state) {
        this.state = state;
    }

    /*
    private synchronized BLEPeripheralState getState() {
        return this.state;
    }
     */

    private final Context context;
    private boolean cached = false;
    private final BLEPeripheralObservables observables = new BLEPeripheralObservables();
    private final RSSIFilter filter = new RSSIFilter();

    final Handler handler = new Handler(Looper.getMainLooper());
    final int CLOSE_DELAY_MS = 200;
    @NonNull
    private final Runnable mRssiRunable = new Runnable() {
        @Override
        public void run() {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "RSSI runnable id=" + identifier() + " isConnected=" + isConnected());
            }
            if (gatt() != null && isConnected()) {
                Log.v(LT, "request RSSI id=" + identifier());
                if (Build.VERSION.SDK_INT >= 31) {
                    // Android 12 (S)
                    if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return ;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        return ;
                    }

                }

                gatt().readRemoteRssi();
            }
            manager.postOperation(this, RSSI_INTERVAL_MS);
        }
    };
    private int lastStatus;
    private final Runnable closeSuccessRunnable = this::waitForClose;

    BLEPeripheralDefault(@NonNull Context context, @NonNull BLECentralManagerInterface manager, @NonNull BluetoothDevice device) {
        this.device = device;
        this.manager = manager;
        this.context = context;

        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12 (S)
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return ;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return ;
            }

        }


        int type = device.getType();
        if (BuildConfig.DEBUG) {
            Log.d(LT, "BLEPeripheralDefault addr=" + device.getAddress() + " name=" + device.getName() + " type=" + type);
        }
        this.cached = type != DEVICE_TYPE_UNKNOWN;
    }

    @NonNull
    public String identifier() {
        return device.getAddress();
    }


    @Override
    public void connect(boolean auto) {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "connect id=" + identifier());
        }
        if (isConnected()) {
            if (gatt() == null) {
                Log.w(LT, "gatt == null but device is connected");
            } else {
                return;
            }
        }
        setState(BLEPeripheralState.connecting);
        BluetoothGatt g;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
     /*
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
*/
            if (Build.VERSION.SDK_INT >= 31) {
                // Android 12 (S)
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return ;
                }
            } else {
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    return ;
                }

            }




            g = device.connectGatt(context, auto, callback, TRANSPORT_LE);
        } else {
            g = device.connectGatt(context, auto, callback);
        }
        if (BuildConfig.DEBUG) {
            Log.d(LT, "init gatt id=" + identifier() + " gatt is null: " + (g == null ? "yes" : "no") + " id=" + identifier());
        }
        if (g == null) {
            Log.w(LT, "will init gatt to null, id=" + identifier());
        }
        // TODO: it aready should be set in onConnectionStateChange but just in case
        if (g != null) {
            setGatt(g);
        }
    }

    private final BluetoothGattCallback callback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(@NonNull BluetoothGatt gatt, int status, int newState) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onConnectionStateChange id=" + identifier() +
                        " addr=" + gatt.getDevice().getAddress() +
                        " status=" + status +
                        " newState=" + newState);
            }
            lastStatus = status;
            if (status == GATT_SUCCESS) {
                setGatt(gatt);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    setState(BLEPeripheralState.connected);
                    observables.channelConnected.broadcast(new BLEPeripheralObservablesInterface.ConnectedEvent());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    waitForClose();
                }
            } else {
  /*
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
*/

                if (Build.VERSION.SDK_INT >= 31) {
                    // Android 12 (S)
                    if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return ;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        return ;
                    }

                }

                gatt.disconnect();
                waitForClose();
            }
        }

        @Override
        public void onServicesDiscovered(@NonNull final BluetoothGatt gatt, int status) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onServicesDiscovered id=" + identifier() +
                        " addr=" + gatt.getDevice().getAddress() +
                        " status=" + status);
            }
            List<BLEService> serviceList = new ArrayList<>();
            for (BluetoothGattService service : gatt.getServices()) {
                serviceList.add(new BLEService(service));
            }
            services = new BLEService[serviceList.size()];
            serviceList.toArray(services);
            setState(BLEPeripheralState.discovered);
            observables.channelDiscoveredServices.broadcast(new BLEPeripheralObservablesInterface.DiscoveredServicesEvent(services, status));
        }

        private int rssi = 0;

        /*
                private ArrayList<Integer> rssis = new ArrayList<>();
                private ArrayList<Integer> rssiVels = new ArrayList<>();

                private double avg = 0;
                private double avgV = 0;

                private void rssiAvg() {
                    long sum = 0;
                    long sumV = 0;
                    long prev = rssis.get(0);
                    for (int rssi : rssis) {
                        sum += rssi;
                        sumV += (rssi - prev);
                        prev = rssi;
                    }
                    avg = (double) sum / rssis.size();
                    avgV = (double) sumV / rssis.size();
                }

                private double sigma = 0;
                private double sigmaV = 0;

                private void rssiSigma() {
                    rssiAvg();
                    long sum = 0;
                    long sumV = 0;
                    long prev = rssis.get(0);
                    for (int rssi : rssis) {
                        double d = avg - rssi;
                        sum += d * d;
                        double v = (rssi - prev);
                        double dv = avgV - v;
                        sumV += dv * dv;
                        prev = rssi;
                    }
                    sigma = Math.sqrt((double) sum / rssis.size());
                    sigmaV = Math.sqrt((double) sumV / rssis.size());
                };
        */

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssiRaw, int status) {
                /*
                rssis.clear();
                rssiVels.clear();
                 */
            filter.add(rssiRaw);
            /*
            rssis.add(rssiRaw);
            rssiSigma();
            Log.d(LT, "rssi=" + rssiRaw + " avg=" + avg + " sigma = " + sigma + " avgV=" + avgV + " sigmaV=" + sigmaV);
             */

            int rssi = filter.get();

            if (rssi != this.rssi) {
                Log.v(LT, "rssi=" + rssi + " raw=" + rssiRaw);
                if (BuildConfig.DEBUG) {
                    Log.v(LT, "onReadRemoteRssi id=" + identifier() + " rssiEnabled=" + rssi + " status=" + status);
                }
                this.rssi = rssi;
                observables.channelRSSI.broadcast(new BLEPeripheralObservablesInterface.RSSIEvent(rssi, status));
            } else {
                if (BuildConfig.DEBUG) {
                    Log.v(LT, "onReadRemoteRssi skipped id=" + identifier() + " rssiEnabled=" + rssi + " status=" + status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onCharacteristicWrite id=" + identifier() +
                        " addr=" + gatt.getDevice().getAddress() +
                        " characteristic=" + characteristic.getUuid() +
                        " status=" + status);
            }
            observables.channelWrite.broadcast(new BLEPeripheralObservablesInterface.CharacteristicEvent(
                    new BLECharacteristic(characteristic),
                    status));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onCharacteristicChanged id=" + identifier() +
                        " addr=" + gatt.getDevice().getAddress() +
                        " characteristic=" + characteristic.getUuid());
            }
            observables.channelNotification.broadcast(new BLECharacteristic(characteristic));
        }
    };

    @Override
    public void discoveryServices() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "discoveryServices id=" + identifier());
        }
        if (isConnected()) {
            setState(BLEPeripheralState.discovering);
            /*
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }*/
            if (Build.VERSION.SDK_INT >= 31) {
                // Android 12 (S)
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return ;
                }
            } else {
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    return ;
                }

            }

            gatt().discoverServices();
        }
    }

    @Override
    public BLEError writeInt8(BLECharacteristic characteristic, int value) {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "writeInt8 id=" + identifier() +
                    " gatt=" + (gatt() == null ? "null" : gatt().getDevice().getAddress()) +
                    " connected=" + manager.connected(device) +
                    " characteristic=" + characteristic.gattCharacteristic.getUuid() +
                    " value=" + value);
        }
        if (gatt() == null) {
            return BLEError.noGatt;
        }
        if (!manager.connected(device)) {
            return BLEError.notConnected;
        }
        setState(BLEPeripheralState.writting);
        characteristic.setInt8(value);
  /*
        if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return BLEError.noPeripheral;
        }
*/

        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12 (S)
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return BLEError.noPeripheral;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return BLEError.noPeripheral;
            }

        }



        gatt().writeCharacteristic(characteristic.gattCharacteristic);
        return BLEError.ok;
    }

    @Override
    public BLEError setNotify(BLECharacteristic characteristic, boolean enable) {
        if (gatt() == null) {
            return BLEError.noGatt;
        }
        if (!manager.connected(device)) {
            return BLEError.notConnected;
        }
        /*
        if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return BLEError.ok;
        }*/

        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12 (S)
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return BLEError.ok;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return BLEError.ok;
            }

        }

        gatt().setCharacteristicNotification(characteristic.gattCharacteristic, enable);
        BluetoothGattDescriptor descriptor = characteristic.gattCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt().writeDescriptor(descriptor);
        }

        return BLEError.ok;
    }

    /*
        @Override
        public BLEPeripheralState state() {
            BluetoothGatt g;
            synchronized (this) {
                g = gatt;
            }
            if (g == null || !manager.connected(device)) {
                return BLEPeripheralState.disconnected;
            }
            return getState();
        }
    */
    @Override
    public BLEPeripheralObservablesInterface observables() {
        return observables;
    }

    private void waitForClose() {
        if (isConnected()) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "post close device " + device.getAddress());
            }
            handler.postDelayed(closeSuccessRunnable, CLOSE_DELAY_MS);
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "close device " + device.getAddress());
            }
            close();
            observables.channelDisconnected.broadcast(new BLEPeripheralObservablesInterface.DisconnectedEvent(lastStatus));
        }
    }

    @Override
    synchronized public void close() {
        setState(BLEPeripheralState.disconnected);
        if (isConnected()) {
            Log.w(LT, "gatt will be closed, but device is connected");
        }
        if (gatt() == null) {
            Log.w(LT, "gatt will be closed, but  gatt is null");
        }
        if (gatt() != null) {
            Log.d(LT, "close");
  /*
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
*/
            if (Build.VERSION.SDK_INT >= 31) {
                // Android 12 (S)
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return ;
                }
            } else {
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    return ;
                }

            }

            gatt().close();
        }
        if (BuildConfig.DEBUG) {
            Log.d(LT, "set gatt null");
        }
        setGatt(null);
    }

    @Override
    public void disconnect() {
        if (BLEPeripheralState.disconnected.equals(state)) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "disconnect id=" + identifier() + ", already disconnected, no action");
            }
        } else {
            BluetoothGatt gatt = gatt();
            boolean connected = isConnected();
            if (connected && gatt != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(LT, "disconnect id=" + identifier() + ", connected, will wait connection change");
                }
                setState(BLEPeripheralState.disconnecting);
  /*
                if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
*/

                if (Build.VERSION.SDK_INT >= 31) {
                    // Android 12 (S)
                    if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return ;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        return ;
                    }

                }

                gatt.disconnect();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(LT, "disconnect id=" + identifier() + ", not connected will simulate connection change");
                }
                setState(BLEPeripheralState.disconnecting);
                if (gatt != null)
                    gatt.disconnect(); // just in case ?
                try {
                    Thread.sleep(400); // 5 times per 2 secs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                close();
                observables.channelDisconnected.broadcast(new BLEPeripheralObservablesInterface.DisconnectedEvent(0));
            }
        }
    }

    private int rssiCount = 0;

    @Override
    public void enableRSSI() {
        /*
        rssiCount++;
        Log.d(LT, "enableRSSI id=" + identifier() + " count=" + rssiCount);
        if (rssiCount == 1) {
            filter.reset();
            manager.postOperation(mRssiRunable);
        }
         */
        Log.d(LT, "enableRSSI id=" + identifier());
        filter.reset();
        rssiCount = 1;
        manager.postOperation(mRssiRunable);
    }

    @Override
    public void disableRSSI() {
        /*
        if (rssiCount > 0) {
            rssiCount--;
            Log.d(LT, "disableRSSI id=" + identifier() + " count=" + rssiCount);
            manager.cancelOperation(mRssiRunable);
        }
         */
        Log.d(LT, "disableRSSI id=" + identifier());
        manager.cancelOperation(mRssiRunable);
        rssiCount = 0;
    }

    public BLEService[] services() {
        return services;
    }

    public String name() {
    /*
        if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "TODO";
        }

     */
        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12 (S)
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "TODO";
            }
        } else {
            if (ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(AppClass.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return "TODO";
            }

        }

        return device.getName();
    }

    public String address() {
        return device.getAddress();
    }

    @Override
    public boolean isConnected() {
        boolean connected = manager.connected(device);
        if (BuildConfig.DEBUG) {
            Log.w(LT, "isConnected, device=" + device.getAddress() + " manager.connected=" + connected);
        }
        BluetoothGatt g = gatt();
        if (g == null && connected) {
            Log.w(LT, "isConnected, but gatt == null, id=" + identifier());
        }
        return connected;
    }

    @Override
    public boolean isDisconnected() {
        boolean notconnected = !manager.connected(device);
        if (gatt() == null && !notconnected) {
            Log.w(LT, "not isDisconnected, but gatt == null, id=" + identifier());
        }
        return notconnected;
    }

    public boolean rssiEnabled() {
        return rssiCount > 0;
    }

    private BluetoothGatt gatt() {
        BluetoothGatt ret;
        synchronized (gatt) {
            ret = gatt[0];
        }
        if (BuildConfig.DEBUG) {
            Log.v(LT, "getGatt null:" + (ret == null ? "yes" : "no") + " id=" + identifier());
        }
        return ret;
    }

    private void setGatt(BluetoothGatt gatt) {
        synchronized (this.gatt) {
            if (BuildConfig.DEBUG) {
                Log.v(LT, "setGatt null:" + (gatt == null ? "yes" : "no") + " id=" + identifier());
            }
            if (gatt != null && this.gatt[0] != null) {
                Log.w(LT, "Overwrite not null gatt with other not null, id=" + identifier());
            }
            this.gatt[0] = gatt;
        }
    }

    @Override
    public boolean cached() {
        return cached;
    }

}
