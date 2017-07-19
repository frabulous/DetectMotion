package com.example.francesco.detectmotion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnSeekBarChangeListener {

    AppCompatActivity context;
    private DecimalFormat d = new DecimalFormat("#.##");
    TextView tv1, tv2;
    int SENSOR_DELAY;

    SensorManager sensorManager;
    Sensor sensorLinearAcc;
    SensorFusion sensorFusion;

    float sogliaY;
    int sogliaRoll;

    TextView tvSogliaY, tvTempo, tvSogliaRoll, actionLog;

    Button btnClearLog;
    SeekBar  barraSogliaY, barraTempo, barraSogliaRoll;
    TextView tvAzimuth, tvPitch, tvRoll;

    int state;
    float yPrevious, dy, pos_spike, neg_spike;
    boolean start;
    long startingAttackTime;
    private long attackMinLenght;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        d.setMaximumFractionDigits(2);
        d.setMinimumFractionDigits(2);

        tv1=(TextView)findViewById(R.id.tv1);
        tv2=(TextView)findViewById(R.id.tv2);
        SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
        state = 0;
        sogliaRoll = 45;
        sogliaY = 5.0f;
        start = true;
        startingAttackTime = 0;
        attackMinLenght = 350000000; //nanoseconds

        tvSogliaRoll = (TextView)findViewById(R.id.tvSogliaRoll);
        barraSogliaRoll = (SeekBar)findViewById(R.id.barraSogliaRoll);
        barraSogliaRoll.setOnSeekBarChangeListener(this);

        tvSogliaY = (TextView)findViewById(R.id.tvSogliaY);
        barraSogliaY = (SeekBar)findViewById(R.id.barraSogliaY);
        barraSogliaY.setOnSeekBarChangeListener(this);

        tvTempo = (TextView)findViewById(R.id.tvTempo);
        barraTempo = (SeekBar)findViewById(R.id.barraTempo);
        barraTempo.setOnSeekBarChangeListener(this);

        tvAzimuth = (TextView)findViewById(R.id.tvAzimuth);
        tvPitch = (TextView)findViewById(R.id.tvPitch);
        tvRoll = (TextView)findViewById(R.id.tvRoll);

        actionLog = (TextView)findViewById(R.id.actionLog);

        tv2.setText("stato = "+ state);
        tvSogliaRoll.setText("Soglia roll: "+ sogliaRoll +"°");
        tvSogliaY.setText("Soglia Y: "+ sogliaY);
        tvTempo.setText("durata minima attacco (msec): " + "350");

        btnClearLog = (Button)findViewById(R.id.btnClearLog);
        btnClearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionLog.setText("");
            }
        });

        // Get SensorManager instance
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        // Get LINEAR ACCELERATION sensor
        sensorLinearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //Get SENSOR FUSION sensor
        sensorFusion = new SensorFusion();
        sensorFusion.setMode(SensorFusion.Mode.GYRO);

        registerSensorManagerListeners();

    }

    public void registerSensorManagerListeners() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SENSOR_DELAY);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SENSOR_DELAY);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SENSOR_DELAY);

        // Register sensorLinearAcc
        sensorManager.registerListener(this, sensorLinearAcc, SENSOR_DELAY);
    }
    public void updateOrientation() {

        double azimuthValue = sensorFusion.getAzimuth();
        double rollValue =  sensorFusion.getRoll();
        double pitchValue =  sensorFusion.getPitch();

        tvAzimuth.setText("Azimuth: "+String.valueOf(d.format(azimuthValue)));
        tvRoll.setText("Roll: "+String.valueOf(d.format(rollValue)));
        tvPitch.setText("Pitch: "+String.valueOf(d.format(pitchValue)));

    }

    @Override
    protected void onResume(){
        super.onResume();
        // Register sensorLinearAcc
        registerSensorManagerListeners();
        start=true;
    }
    @Override
    protected void onPause(){
        super.onPause();
        // Unregister sensorLinearAcc
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    public void onSensorChanged(SensorEvent event) {
        int id = event.sensor.getType();
        //gestore di SensorFusion
        switch (id) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorFusion.setAccel(event.values);
                sensorFusion.calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                sensorFusion.gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorFusion.setMagnet(event.values);
                break;
        }
        updateOrientation();

        if (id == Sensor.TYPE_LINEAR_ACCELERATION) {

            //float xCurrent = event.values[0];
            float yCurrent = event.values[1];
            //float zCurrent = event.values[2];

            double azimuth = sensorFusion.getAzimuth();
            double pitch = sensorFusion.getPitch();
            double roll = sensorFusion.getRoll();

            if (roll > sogliaRoll || roll < -sogliaRoll) {
                tv1.setText("Unstable");
            } else if (pitch >-60 && pitch < 0) {
                tv1.setText("High Guard");
            } else if (pitch >= 0 && pitch < 60) {
                tv1.setText("Low Guard");
            } else {
                tv1.setText("else");
            }

            tvAzimuth.setText("Azimuth: " + String.valueOf(d.format(azimuth)));
            tvPitch.setText("Pitch: "+ String.valueOf(d.format(pitch)));
            tvRoll.setText("Roll: "+ String.valueOf(d.format(roll)));

            if(start){
                // Initialize last y
                yPrevious = yCurrent;
                start = false;
            }
            else {
                dy = yCurrent - yPrevious;

                switch (state) {
                    case 0:
                        if (yCurrent >= -sogliaY && yCurrent <= sogliaY) {
                            //stay in state 0
                        }
                        else if (yCurrent > sogliaY && dy > 0) {
                            //save the starting time of the (possible) attack
                            startingAttackTime = event.timestamp;
                            //go to state 1
                            state = 1;
                        }
                        else if (yCurrent < -sogliaY && dy < 0){
                            //go to state -1
                            state = -1;
                        }
                        break;
                    case 1:
                        //waiting for positive spike
                        if (dy < 0) {
                            pos_spike = yCurrent;
                            //go to state 2
                            state = 2;
                        }
                        break;
                    case 2:
                        //waiting for negative spike
                        if (dy > 0) {
                            float spike2 = yCurrent;
                            if ((pos_spike + spike2 < 0) && (event.timestamp- startingAttackTime > attackMinLenght)) {
                                double dt = (double)(event.timestamp- startingAttackTime)/1000000000; //seconds
                                //tv1.setText("durata ultimo attacco : "+ dt + " secondi");
                                //AFFONDO!
                                actionLog.append("AFFONDO! ----- durata: "+ dt + " sec\n" +
                                                 "___________----- piccoSu: "+ pos_spike +" ; piccoGiu: "+ neg_spike +"\n");

                            } else {
                                //falso allarme
                                actionLog.append("quasi\n");
                            }
                            pos_spike = 0;
                            //go to state 0
                            state = 0;
                        }
                        break;
                    case -1:
                        //waiting for negative spike
                        if(dy>0){
                            neg_spike = yCurrent;
                            //go to state -2
                            state = -2;
                        }
                        break;
                    case -2:
                        //waiting for positive spike
                        if (dy<0){
                            float spike2 = yCurrent;
                            if (neg_spike + spike2 >= 0){
                                //AUTOAFFONDO!
                                actionLog.append("Ti Sei Ucciso!\n");
                            }
                            else {
                                //e niente, ti sarai sbagliato...
                                actionLog.append("nope\n");
                            }
                            neg_spike = 0;
                            //back to state 0 (idle)
                            state = 0;
                        }
                        break;
                    default:
                        break;
                }
                tv2.setText("stato = " + state);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int id = seekBar.getId();

        if(id == barraSogliaY.getId()){
            //sensorManager.unregisterListener(this);
            sogliaY = ((float)i)/10;
            tvSogliaY.setText("Soglia Y: "+ sogliaY);
            //registerSensorManagerListeners();
        }
        else if(id == barraSogliaRoll.getId()) {
            sogliaRoll = i;
            tvSogliaRoll.setText("Soglia roll: " + sogliaRoll +"°");
        }
        else if(id == barraTempo.getId()) {
            tvTempo.setText("durata minima attacco(msec): " + i);
            attackMinLenght = i* 1000000;
        }
        start = true;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}