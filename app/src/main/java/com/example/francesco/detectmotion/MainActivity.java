package com.example.francesco.detectmotion;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnSeekBarChangeListener {

    AppCompatActivity context;
    TextView tv1, tv2;
    int currentDelay;

    SensorManager sensorManager;
    Sensor sensor;
    float sogliaY;
    long delay = 23;

    TextView tvSogliaY, timeY, tvTimeSystem, tvdelay, actionLog;
    long sysTime;

    SeekBar barraSogliaY, barraDelay;
    TextView x_acceleration, y_acceleration, z_acceleration;

    int state= 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1=(TextView)findViewById(R.id.txtview1);
        tv2=(TextView)findViewById(R.id.txtview2);
        currentDelay = SensorManager.SENSOR_DELAY_GAME;
        sogliaY = 4.0f;
        tvSogliaY = (TextView)findViewById(R.id.txtviewSogliaY);
        barraSogliaY = (SeekBar)findViewById(R.id.barraSogliaY);
        barraDelay = (SeekBar)findViewById(R.id.barraDelay);
        tvdelay = (TextView)findViewById(R.id.tvdelay);
        barraSogliaY.setOnSeekBarChangeListener(this);
        barraDelay.setOnSeekBarChangeListener(this);

        x_acceleration = (TextView)findViewById(R.id.Xvalue_label);
        y_acceleration = (TextView)findViewById(R.id.Yvalue_label);
        z_acceleration = (TextView)findViewById(R.id.Zvalue_label);
        timeY = (TextView)findViewById(R.id.time_label);
        tvTimeSystem = (TextView)findViewById(R.id.timeSystem);
        sysTime = System.nanoTime();
        //TimeUnit.NANOSECONDS.toSeconds(1000000000000L);
        actionLog = (TextView)findViewById(R.id.actionLog);

        context=this;

        // Get SensorManager instance
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        // Get ACCELEROMETER sensor
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    protected void onResume(){
        super.onResume();
        // Register sensor
        sensorManager.registerListener(this, sensor, currentDelay);

    }

    protected void onPause(){
        super.onPause();
        // Unregister sensor
        sensorManager.unregisterListener(this, sensor);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    public void onSensorChanged(SensorEvent event) {

        float xCurrent= event.values[0];
        float yCurrent= event.values[1];
        float zCurrent= event.values[2];
        sysTime = SystemClock.currentThreadTimeMillis();


        if(xCurrent > 2 || xCurrent < -2)
            actionLog.setText("Unstable");
        else if(yCurrent>0 && zCurrent>0 ){
            actionLog.setText("High Guard");
        }
        else if(yCurrent< 0 && zCurrent>0 ) {
            actionLog.setText("Low Guard");
        }
        else
            actionLog.setText("");

        x_acceleration.setText("X: "+ event.values[0]);
        y_acceleration.setText("Y: "+ event.values[1]);
        z_acceleration.setText("Z: "+ event.values[2]);
        tvTimeSystem.setText("System time: "+ sysTime);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int id = seekBar.getId();

        if(id == barraSogliaY.getId()){
            //gestisce la seekbar della soglia e fa reset
            sogliaY = ((float)i)/10 ;
            tvSogliaY.setText("soglia Y: "+ sogliaY);
        }
        else if(id == barraDelay.getId()){
            delay = (long)i;
            tvdelay.setText("delay: "+ delay);
        }
        tv1.setText("");
        tv2.setText("");
        timeY.setText("");

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}