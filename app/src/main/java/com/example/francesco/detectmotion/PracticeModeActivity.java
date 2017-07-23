package com.example.francesco.detectmotion;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PracticeModeActivity extends AppCompatActivity implements SensorEventListener {

    TextView tvCommand, tvCheck;
    ImageView ivCheck, ivPlayerState, ivOpponentState;
    int playerOrientation, playerState, command;
    final int INVALID = -1, HIGH_STAND = 0, LOW_STAND = 1, HIGH_ATTACK = 2, LOW_ATTACK = 3;
    final int vibrationLenght = 100;
    Vibrator vibrator;
    SensorManager sensorManager;
    Sensor sensorLinearAcc;
    SensorFusion sensorFusion;
    int SENSOR_DELAY;

    private long attackMinLength = 350000000; //nanoseconds
    private float sogliaY = 5.0f;
    private int sogliaRoll = 55, pitch_upbound = -60, pitch_midbound = 0, pitch_lowbound = 60;
    int state;
    float yPrevious, dy, pos_spike, neg_spike;
    boolean start;
    long startingAttackTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_mode);
        //handle the activity switch
        Intent i = getIntent();
        String mode = i.getStringExtra("game_mode");
        Toast.makeText(getApplicationContext(), "Modalità: "+ mode, Toast.LENGTH_LONG).show();

        state = 0;
        start = true;
        startingAttackTime = 0;
        SENSOR_DELAY  = SensorManager.SENSOR_DELAY_GAME;

        tvCommand = (TextView)findViewById(R.id.tvCommand);
        tvCheck = (TextView)findViewById(R.id.tvCheck);
        ivCheck = (ImageView)findViewById(R.id.ivCheck);
        ivPlayerState = (ImageView)findViewById(R.id.ivPlayerState);
        ivOpponentState = (ImageView)findViewById(R.id.ivOpponentState);
        //mirror the opponent img
        //ivOpponentState.setScaleX(-1);
        //make the opponent img red
        ivOpponentState.setColorFilter(Color.RED);

        ivOpponentState.setImageResource(R.mipmap.fency_high_stand);

        ivCheck.setVisibility(View.INVISIBLE);
        tvCommand.setText("");
        tvCheck.setText("");


        // Get Vibrator instance
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        // Get SensorManager instance
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        // Get LINEAR ACCELERATION sensor
        sensorLinearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //Get SENSOR FUSION sensor
        sensorFusion = new SensorFusion();
        sensorFusion.setMode(SensorFusion.Mode.GYRO);

        registerSensorManagerListeners();

        playerOrientation = INVALID;
        playerState = HIGH_STAND;

        command = generateCommandInt();
        tvCommand.setText(generateCommandString(command));

    }

    public void registerSensorManagerListeners() {
        // Register 3 sensors for SensorFusion
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

    private void changePlayerState(int s){
        if(s != playerState){
            ivPlayerState.setImageAlpha(255);
            playerState = s;
            //change player img
            switch (s){
                case HIGH_STAND:
                    ivPlayerState.setImageResource(R.mipmap.fency_high_stand);
                    break;
                case LOW_STAND:
                    ivPlayerState.setImageResource(R.mipmap.fency_low_stand);
                    break;
                case HIGH_ATTACK:
                    ivPlayerState.setImageResource(R.mipmap.fency_high_attack);
                    break;
                case LOW_ATTACK:
                    ivPlayerState.setImageResource(R.mipmap.fency_low_attack);
                    break;
                case INVALID:
                    ivPlayerState.setImageAlpha(100);
                    break;
            }
        }
    }

    private int generateCommandInt(){
        int i;
        do i = (int)(Math.random()*10)%4;
        while(i == playerState);

        return i;
    }
    private String generateCommandString(int cmd) {
        String str = "";
        switch (cmd){
            case HIGH_STAND:
                str = "Para un attacco alto!";
                break;
            case LOW_STAND:
                str = "Para un attacco basso!";
                break;
            case HIGH_ATTACK:
                str = "Fai un attacco alto!";
                break;
            case LOW_ATTACK:
                str = "Fai un attacco basso!";
                break;
        }
        return str;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        int id = event.sensor.getType();
        //handle SensorFusion cases
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

        if (id == Sensor.TYPE_LINEAR_ACCELERATION) {

            float yCurrent = event.values[1];
            double pitch = sensorFusion.getPitch();
            double roll = sensorFusion.getRoll();

            if (roll > sogliaRoll || roll < -sogliaRoll) {
                //tv1.setText("Unstable");
                playerOrientation = INVALID;
            } else if (pitch > pitch_upbound && pitch < pitch_midbound) {
                //tv1.setText("High Guard");
                playerOrientation = HIGH_STAND;
            } else if (pitch >= pitch_midbound && pitch < pitch_lowbound) {
                //tv1.setText("Low Guard");
                playerOrientation = LOW_STAND;
            } else {
                //tv1.setText("else");
                playerOrientation = INVALID;
            }
            changePlayerState(playerOrientation);

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
                        else if (yCurrent > sogliaY && dy > 0
                                && playerOrientation != INVALID) {
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
                            if (playerOrientation != INVALID && (pos_spike + spike2 < 0) && (event.timestamp- startingAttackTime > attackMinLength)) {
                                //AFFONDO!
                                changePlayerState(playerOrientation +2);
                                vibrator.vibrate(vibrationLenght);
                            } else {
                                //falso allarme
                            }
                            startingAttackTime = 0;
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
                            }
                            else {
                                //e niente
                            }
                            neg_spike = 0;
                            //back to state 0 (idle)
                            state = 0;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
