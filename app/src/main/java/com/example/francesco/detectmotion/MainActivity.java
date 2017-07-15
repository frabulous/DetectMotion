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
    boolean start=false;
    SensorManager sensorManager;
    Sensor sensor;
    float sogliaY;
    long delay = 23;
    float yPrevius=0;
    float distance=0;
    TextView tvSogliaY, timeY, tvTimeSystem, tvdelay;
    long sysTime;
    long startTime= 0;
    SeekBar barraSogliaY, barraDelay;
    TextView y_acceleration;

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

        y_acceleration = (TextView)findViewById(R.id.Yvalue_label);
        timeY = (TextView)findViewById(R.id.time_label);
        tvTimeSystem = (TextView)findViewById(R.id.timeSystem);
        sysTime = System.nanoTime();

        context=this;

        // Get SensorManager instance
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        // Get ACCELEROMETER sensor
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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

        //Log.e("Main", "GX="+String.valueOf(event.values[0])+"\nGY="+String.valueOf(event.values[1])+"\nGZ="+String.valueOf(event.values[2]));
        float yCurrent=event.values[1]; // Get current y
        if(yCurrent > sogliaY && !start) {
            tv1.setText("Acceleration start: "+sysTime+" \ndy = " + yCurrent);
            startTime= sysTime;
            yPrevius= yCurrent;
            start= true;
            distance=0;
        }
        else if(yCurrent < yPrevius && start) {
            tv2.setText("Acceleration end: "+sysTime+" \ndy = " + yCurrent);
            start= false;
            float time= (sysTime-startTime)/10;
            timeY.setText("Distanza: "+ yPrevius*time*time);
        }

        y_acceleration.setText("Y: "+ yCurrent);
        sysTime = SystemClock.currentThreadTimeMillis();
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