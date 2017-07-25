package com.example.francesco.detectmotion;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService;

import java.util.UUID;

public class DuelModeActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duel_mode);
        //handle the activity switch
        Intent i = getIntent();
        String mode = i.getStringExtra("game_mode");
        Toast.makeText(getApplicationContext(), "Modalità: " + mode, Toast.LENGTH_LONG).show();

        btnBT = (Button) findViewById(R.id.btnBT);
        btnBT.setOnClickListener(this);

        //BT configuration
        //
    }

    @Override
    public void onClick(View view) {

    }
}
