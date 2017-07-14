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

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnClickListener, OnSeekBarChangeListener {

    AppCompatActivity context;
    TextView tvX, tvY;
    Button resetBtn_normal, resetBtn_game, resetBtn_fast;
    int currentDelay;
    boolean start=true;
    float xLast, yLast;
    SensorManager sensorManager;
    Sensor sensor;
    float sogliaX, sogliaY;
    TextView tvSogliaX, tvSogliaY, timeY, tvTimeSystem;
    long sysTime;
    SeekBar barraSogliaX, barraSogliaY;
    TextView x_acceleration, y_acceleration, z_acceleration;
    int col_base = 0xff000000; //black
    int col_selected = 0xffddffdd; //green_

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvX=(TextView)findViewById(R.id.txtviewX);
        tvY=(TextView)findViewById(R.id.txtviewY);
        resetBtn_normal = (Button)findViewById(R.id.resetBtn);
        resetBtn_game = (Button)findViewById(R.id.resetBtn_game);
        resetBtn_fast = (Button)findViewById(R.id.resetBtn_fast);
        resetBtn_normal.setTextColor(col_selected);
        currentDelay = SensorManager.SENSOR_DELAY_NORMAL;
        sogliaX = 1.0f;
        sogliaY = 1.0f;
        tvSogliaX = (TextView)findViewById(R.id.txtviewSogliaX);
        barraSogliaX = (SeekBar)findViewById(R.id.barraSogliaX);
        tvSogliaX.setText("soglia:" + sogliaX);
        barraSogliaX.setOnSeekBarChangeListener(this);
        tvSogliaY = (TextView)findViewById(R.id.txtviewSogliaY);
        barraSogliaY = (SeekBar)findViewById(R.id.barraSogliaY);
        tvSogliaY.setText("soglia:" + sogliaY);
        barraSogliaY.setOnSeekBarChangeListener(this);

        x_acceleration = (TextView)findViewById(R.id.Xvalue_label);
        y_acceleration = (TextView)findViewById(R.id.Yvalue_label);
        z_acceleration = (TextView)findViewById(R.id.Zvalue_label);
        timeY = (TextView)findViewById(R.id.time_label);
        tvTimeSystem = (TextView)findViewById(R.id.timeSystem);
        sysTime = System.nanoTime();

        context=this;

        // Get SensorManager instance
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        // Get ACCELEROMETER sensor
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Set button listeners
        resetBtn_normal.setOnClickListener(this);
        resetBtn_game.setOnClickListener(this);
        resetBtn_fast.setOnClickListener(this);
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
        float xCurrent=event.values[0]; // Get current x
        float yCurrent=event.values[1]; // Get current y
        if(start){
            // Initialize last x and y
            xLast=xCurrent;
            yLast=yCurrent;
            start=false;
        }
        else{
            // Calculate variation between last x and current x, last y and current y
            float xDelta=xLast-xCurrent;
            float yDelta=yLast-yCurrent;
            if(Math.sqrt(xDelta*xDelta/2) > sogliaX)
                tvX.setText("The device is moved horizontally: \ndx = " +xDelta);
            if(Math.sqrt(yDelta*yDelta/2) > sogliaY) {
                tvY.setText("The device is moved vertically: \ndy = " + yDelta);
                timeY.setText("vertical movement at: 0"+ event.timestamp);
            }

            // Update last x and y
            xLast=xCurrent;
            yLast=yCurrent;
        }

        x_acceleration.setText("X: "+ event.values[0]);
        y_acceleration.setText("Y: "+ event.values[1]);
        z_acceleration.setText("Z: "+ event.values[2]);
        sysTime = SystemClock.currentThreadTimeMillis();
        tvTimeSystem.setText("System time: "+ sysTime);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        onPause();
        tvX.setText("");
        tvY.setText("");
        x_acceleration.setText("X: ");
        y_acceleration.setText("Y: ");
        z_acceleration.setText("Z: ");
        start = true;
        resetBtn_normal.setTextColor(col_base);
        resetBtn_game.setTextColor(col_base);
        resetBtn_fast.setTextColor(col_base);

        if(id == resetBtn_normal.getId()) {
            resetBtn_normal.setTextColor(col_selected);
            currentDelay = SensorManager.SENSOR_DELAY_NORMAL;
        }
        else if(id == resetBtn_game.getId()) {
            resetBtn_game.setTextColor(col_selected);
            currentDelay = SensorManager.SENSOR_DELAY_GAME;
        }
        else if(id == resetBtn_fast.getId()) {
            resetBtn_fast.setTextColor(col_selected);
            currentDelay = SensorManager.SENSOR_DELAY_FASTEST;
        }

        onResume();

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int id = seekBar.getId();

        if(id == barraSogliaX.getId()){
            //gestisce la seekbar della soglia e fa reset
            sogliaX = ((float)i)/10 ;
            tvSogliaX.setText("soglia X: "+ sogliaX);
            tvX.setText("");
            tvY.setText("");
            start = true;
        }
        if(id == barraSogliaY.getId()){
            //gestisce la seekbar dellala soglia e fa reset
            sogliaY = ((float)i)/10 ;
            tvSogliaY.setText("soglia Y: "+ sogliaY);
            tvX.setText("");
            tvY.setText("");
            start = true;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}