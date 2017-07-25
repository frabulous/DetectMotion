import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Francesco on 24/07/2017.
 */

public class BluetoothConnectionService {
    // Debugging
    private static final String TAG = "BluetoothConnectionSer"; //max 23 char!

    private static final String appName = "DETECT";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;

    ProgressBar mProgressBar;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;

    private final BluetoothAdapter BT_ADAPTER;
    Context mContext;

    /**
     * Constructor. Prepares a new BluetoothConnection session.
     *
     * @param context The UI Activity Context
     */
    public BluetoothConnectionService(Context context) {
        mContext = context;
        BT_ADAPTER = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = BT_ADAPTER.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: setting up server using"+ MY_UUID_INSECURE);

            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread running");

            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start...");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.e(TAG, "accept() failed",e);
            }

            // If a connection was accepted
            if (socket != null) {
                //connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel : Cancelling AcceptThread");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel : close of AcceptThread ServerSocket failed. "+ e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectedThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }
        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "run : mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectedThread: trying to create ");
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
