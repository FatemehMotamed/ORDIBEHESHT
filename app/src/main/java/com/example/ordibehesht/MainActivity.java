package com.example.ordibehesht;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    BluetoothSPP bt;
    TextView textStatus;

    TextView out_humidity;
    TextView out_light;
    TextView out_fan;
    TextView out_pomp;

    Menu menu;

    private NumberPicker fan_picker;
    private NumberPicker light_picker;
    private String[] pickerVals;

    int valuePickerFan;
    int valuePickerLight;

    int counter = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fan_picker = findViewById(R.id.fan);
        fan_picker.setMaxValue(9);
        fan_picker.setMinValue(0);
        pickerVals  = new String[] {"Off", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        fan_picker.setDisplayedValues(pickerVals);

        fan_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {

            }
        });

        light_picker = findViewById(R.id.light);
        light_picker.setMaxValue(9);
        light_picker.setMinValue(0);
        pickerVals  = new String[] {"Off", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        light_picker.setDisplayedValues(pickerVals);

        light_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {

            }
        });

        textStatus = findViewById(R.id.textStatus);

        out_humidity= findViewById(R.id.out_humidity);
        out_light= findViewById(R.id.out_light);
        out_fan= findViewById(R.id.out_fan);
        out_pomp= findViewById(R.id.out_pomp);

        out_humidity.setEnabled(false);
        out_light.setEnabled(false);
        out_fan.setEnabled(false);
        out_pomp.setEnabled(false);
        fan_picker.setEnabled(false);
        light_picker.setEnabled(false);


        bt = new BluetoothSPP(this);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                textStatus.setText("Connected to " + name);
                out_humidity.setEnabled(true);
                out_light.setEnabled(true);
                out_fan.setEnabled(true);
                out_pomp.setEnabled(true);
                fan_picker.setEnabled(true);
                light_picker.setEnabled(true);

            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                textStatus.setText("Not connected");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                textStatus.setText("Connection failed");
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
        if(id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }
    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);

        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    public void setup(){

        out_pomp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter == 1){
                    bt.send("pomp-on", true);
                    out_pomp.setText("Pomp\n" + "On");
                    out_pomp.setBackgroundColor(Color.parseColor("#673ab7"));
                    counter = 2;
                }else if(counter == 2){
                    bt.send("pomp-off", true);
                    out_pomp.setText("Pomp\n" + "Off");
                    out_pomp.setBackgroundColor(Color.parseColor("#F48FB1"));
                    counter = 1;
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
                Toast.makeText(getApplicationContext(), "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}