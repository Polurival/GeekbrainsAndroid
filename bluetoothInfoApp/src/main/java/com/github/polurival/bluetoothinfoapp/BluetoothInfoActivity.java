package com.github.polurival.bluetoothinfoapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Polurival
 * on 02.10.2016.
 * <p/>
 * http://stackoverflow.com/questions/10795424/how-to-get-the-bluetooth-devices-as-a-list
 */
public class BluetoothInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_BLUETOOTH = 0;
    private Button mEnableBluetoothBtn;
    private Button mFindDevicesBtn;
    private Button mStopFindingDevicesBtn;
    private RecyclerView mDevicesList;

    private BluetoothAdapter mBluetoothAdapter;
    private DevReceiver mDevReceiver;

    private List<DeviceEntity> mDeviceEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_info);

        mDeviceEntityList = new ArrayList<>();

        initBluetooth();
        initViews();
        setButtonsBehavior();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mDevReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enableBluetoothBtn:
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, 1);
                } else {
                    Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.findDevicesBtn:
                boolean isScanning = mBluetoothAdapter.startDiscovery();
                Log.d("InfoAboutDiscovery", "" + isScanning);
                mFindDevicesBtn.setEnabled(false);
                mStopFindingDevicesBtn.setEnabled(true);
                break;

            case R.id.stopFindingDevicesBtn:
                mBluetoothAdapter.cancelDiscovery();
                mFindDevicesBtn.setEnabled(true);
                mStopFindingDevicesBtn.setEnabled(false);

            default:
                break;
        }
    }

    private void initViews() {
        mEnableBluetoothBtn = (Button) findViewById(R.id.enableBluetoothBtn);
        mFindDevicesBtn = (Button) findViewById(R.id.findDevicesBtn);
        mStopFindingDevicesBtn = (Button) findViewById(R.id.stopFindingDevicesBtn);
        mStopFindingDevicesBtn.setEnabled(false);
        mDevicesList = (RecyclerView) findViewById(R.id.devicesList);
    }


    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            mEnableBluetoothBtn.setEnabled(false);
        }
        mDevReceiver = new DevReceiver();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDevReceiver, intentFilter);
    }

    private void setButtonsBehavior() {
        mEnableBluetoothBtn.setOnClickListener(this);
        mFindDevicesBtn.setOnClickListener(this);
        mStopFindingDevicesBtn.setOnClickListener(this);
    }

    private class DevReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BT", device.getName() + "\n" + device.getAddress());

                String address = device.getAddress();
                String name = device.getName();
                DeviceEntity deviceEntity = new DeviceEntity(address, name);
                mDeviceEntityList.add(deviceEntity);

                initRecyclerList();

            }
        }
    }

    private void initRecyclerList() {
        mDevicesList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDevicesList.setAdapter(new DeviceListAdapter(getApplicationContext(), mDeviceEntityList));
    }
}
