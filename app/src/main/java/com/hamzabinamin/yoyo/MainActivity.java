package com.hamzabinamin.yoyo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

public class MainActivity extends Activity implements View.OnClickListener {

    Button clearTableButton;
    TextView txtString;
    ListView listView;
    CustomAdapter customAdapter;
    Handler bluetoothIn;
    PlayGifView yoyo;
    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private static final String USER_AGENT = "Mozilla/5.0";
    String yoyoID = "";
    String yoyoValue = "";
    List<Player> playerList = new ArrayList<Player>();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        clearTableButton = (Button) findViewById(R.id.clearTableButton);
        txtString = (TextView) findViewById(R.id.myResultsTextView);
        listView = (ListView) findViewById(R.id.listview);
        customAdapter = new CustomAdapter(getBaseContext(), playerList);
        listView.setAdapter(customAdapter);
        clearTableButton.setOnClickListener(this);

        ImageView imageView = (ImageView) findViewById(R.id.yoyo);
        if (imageView != null)
            Glide.with(this).load("http://www.animated-gifs.eu/kids-yoyo/0010.gif").asGif().into(imageView);

        //Declare the timer
        Timer t = new Timer();
//Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      String url = String.format("http://www.sg-yoyo.com/retrieveTest.php\n");
                                      try {
                                          sendGETforRetrieve(url);
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }
                                  }

                              }, 0, 5000);


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                  //  int endOfLineIndex = recDataString.indexOf("END");                    // determine the end-of-line
                   // if (endOfLineIndex > 0) {                                           // make sure there data before ~
                    //    String dataInPrint = recDataString.substring(0, endOfLineIndex);
                    // extract string
                    if(recDataString.toString().contains("P0C01") || recDataString.toString().contains("C01") || recDataString.toString().contains("P0C02") || recDataString.toString().contains("C02")) {
                        String[] store = recDataString.toString().split("!");
                        String deviceName = store[0];
                        //deviceName = deviceName.substring(deviceName.indexOf("POC"));
                        deviceName = deviceName.replaceAll(".*P","");
                        String number = store[1];
                        txtString.setText(number);
                        if(recDataString.toString().contains("C01")) {
                            yoyoID = "POC01";
                        }
                        else if(recDataString.toString().contains("C02")) {
                            yoyoID = "POC02";
                        }
                        yoyoID = deviceName;
                        yoyoValue = number;
                        String url = String.format("http://www.sg-yoyo.com/insertTest.php?yoyoID=%s&yoyoValue=%s\n", yoyoID, yoyoValue);

                        try {
                            sendGET(yoyoID, yoyoValue, url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        recDataString.delete(0, recDataString.length());                    //clear all string data
                    }
                    }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    public void sendGET(final String paramUsername, final String paramPassword, String paramURL) throws IOException {

        class HttpGetAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                String loginStatus = "";
                try {
                    String paramUsername = strings[0];
                    String paramPassword = strings[1];
                    String paramURL = strings[2];
                    System.out.println("paramUsername: " + paramUsername + " paramPassword is: " + paramPassword + "paramURL: " +paramURL );
                    URL url = new URL(paramURL);
                    System.out.println("URL: "+ url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();

                    String inputString;
                    while ((inputString = bufferedReader.readLine()) != null) {
                        builder.append(inputString);
                    }

                    Log.e("Response 1: ",builder.toString());
                    loginStatus = builder.toString();

                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return loginStatus;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if(result.contains("New record created successfully")){
                    Log.e("Response", "Record Got Saved");
                    Toast.makeText(getApplicationContext(), "YoYo Values got Saved", Toast.LENGTH_SHORT).show();
                }
                else if(result.contains("Error")) {
                    Log.e("Response", "Record Didn't Get Saved");
                    Toast.makeText(getApplicationContext(), "YoYo Values didn't get Saved", Toast.LENGTH_SHORT).show();
                }
            }
        }
        HttpGetAsyncTask httpGetAsyncTask = new HttpGetAsyncTask();
        // Parameter we pass in the execute() method is relate to the first generic type of the AsyncTask
        // We are passing the connectWithHttpGet() method arguments to that
        httpGetAsyncTask.execute(paramUsername, paramPassword, paramURL);
    }

    public void sendGETforRetrieve(String paramURL) throws IOException {

        class HttpGetAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                String loginStatus = "";
                try {
                    String paramURL = strings[0];
                    URL url = new URL(paramURL);
                    System.out.println("URL: "+ url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();

                    String inputString;
                    while ((inputString = bufferedReader.readLine()) != null) {
                        builder.append(inputString);
                    }

                    Log.e("Response 1: ",builder.toString());
                    loginStatus = builder.toString();

                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return loginStatus;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                playerList.clear();
                String[] storeResult = result.split(",");
                String[] storeResult2 = new String[storeResult.length * 2];
                List<String> yoyoScore = new ArrayList<String>();
                List<String> yoyoDevice = new ArrayList<String>();
                List<String> yoyoTimeStamp = new ArrayList<String>();
                Log.e("JSON Result: ", result.toString());

                if (!result.contains("0 results")) {
                    try {
                        JSONArray arr = new JSONArray(result);
                        for (int i = 0; i < arr.length(); i++) {
                            yoyoScore.add(arr.getJSONObject(i).getString("YoYoValue"));
                            yoyoDevice.add(arr.getJSONObject(i).getString("YoYoID"));
                            yoyoTimeStamp.add(arr.getJSONObject(i).getString("YoYoLatUpdate"));
                            playerList.add(new Player(i + 1, yoyoDevice.get(i), yoyoTimeStamp.get(i), Integer.parseInt(yoyoScore.get(i))));
                            int position = listView.getSelectedItemPosition();
                            customAdapter.notifyDataSetChanged();
                            listView.setSelection(position);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.e("YoYo Score: ", yoyoScore.toString());
                    Log.e("YoYo Time Stamp: ", yoyoTimeStamp.toString());
                }
            }
        }
        HttpGetAsyncTask httpGetAsyncTask = new HttpGetAsyncTask();
        // Parameter we pass in the execute() method is relate to the first generic type of the AsyncTask
        // We are passing the connectWithHttpGet() method arguments to that
        httpGetAsyncTask.execute(paramURL);
    }



    public void sendGETforClearTable(String paramURL) throws IOException {

        class HttpGetAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                String loginStatus = "";
                try {
                    String paramURL = strings[0];
                    URL url = new URL(paramURL);
                    System.out.println("URL: "+ url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();

                    String inputString;
                    while ((inputString = bufferedReader.readLine()) != null) {
                        builder.append(inputString);
                    }

                    Log.e("Response 1: ",builder.toString());
                    loginStatus = builder.toString();

                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return loginStatus;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                Log.e("JSON Result: ", result.toString());

                if(result.contains("Table cleared successfully")) {
                    Toast.makeText(getBaseContext(), "Table Cleared", Toast.LENGTH_SHORT).show();
                }

            }
        }
        HttpGetAsyncTask httpGetAsyncTask = new HttpGetAsyncTask();
        // Parameter we pass in the execute() method is relate to the first generic type of the AsyncTask
        // We are passing the connectWithHttpGet() method arguments to that
        httpGetAsyncTask.execute(paramURL);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.clearTableButton:
                String url = String.format("http://sg-yoyo.com/clearTable.php\n");
                try {
                    sendGETforClearTable(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    public void showList() {

        List<Player> playerList = new ArrayList<Player>();


        customAdapter = new CustomAdapter(this, playerList);
        listView.setAdapter(customAdapter);
    }
}