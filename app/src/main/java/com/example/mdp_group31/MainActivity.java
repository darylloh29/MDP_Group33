package com.example.mdp_group31;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.mdp_group31.main.BluetoothChatFragment;
import com.example.mdp_group31.main.BluetoothConnectionService;
import com.example.mdp_group31.main.BluetoothPopUp;
import com.example.mdp_group31.main.ControlFragment;
import com.example.mdp_group31.main.GridMap;
import com.example.mdp_group31.main.MapTabFragment;
import com.example.mdp_group31.main.SectionsPagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView, bluetoothStatus, bluetoothDevice;
    static ImageButton upBtn, downBtn, leftBtn, rightBtn;

    BluetoothDevice mBTDevice;
    ProgressDialog myDialog;

    String obstacleID;

    private static final String TAG = "Main Activity";
    public static boolean stopTimerFlag = false;
    public static boolean stopWk9TimerFlag = false;

    private int g_coordX;
    private int g_coordY;
    private static UUID myUUID;

    /**
     * onCreate is called when the app runs
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // remember to always call the super method
        super.onCreate(savedInstanceState);

        // choose which layout to be displayed, in this case the activity_main layout
        setContentView(R.layout.activity_main);

        // SectionsPagerAdapter extends from FragmentPagerAdapter
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        // adds different fragments that comes into view when clicked
        // CHAT is for sending and receiving BT message to and from STM
        // MAP CONFIG is for configuring the map layout
        // CHALLENGE provides quick access to execute the algo for img recognition & fastest path
        sectionsPagerAdapter.addFragment(new BluetoothChatFragment(), "CHAT");
        sectionsPagerAdapter.addFragment(new MapTabFragment(), "MAP CONFIG");
        sectionsPagerAdapter.addFragment(new ControlFragment(), "CHALLENGE");

        // TODO
        // dont know what this section does, best to not touch
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction", "None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        // Toolbar
        ImageButton bluetoothButton = findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothPopUp.class);
                startActivity(popup);
            }
        });

        // Bluetooth Status
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        bluetoothDevice = findViewById(R.id.bluetoothConnectedDevice);

        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        // initialize OBSTACLE_LIST and IMAGE_BEARING strings
        // TODO
        // to understand what OBSTACLE_LIST is (ArrayList of strings)
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                gridMap.OBSTACLE_LIST[i][j] = "";
                gridMap.IMAGE_BEARING[i][j] = "";
            }
        }

        // Controller to manually control robot movement
        upBtn = findViewById(R.id.upBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatus);

        // pops up when BT is disconnected
        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
    }

    /**
     * Getter function for getting {@link GridMap} object
     * Used in {@link ControlFragment} and {@link MapTabFragment}
     * @return {@link GridMap}
     */
    public static GridMap getGridMap() {
        return gridMap;
    }

    /**
     * Getter function for getting the status of the robot
     * Used in {@link ControlFragment}
     * @return "Auto Movement/ImageRecog Stopped" or "Week 9 Stopped"
     */
    public static TextView getRobotStatusTextView() {
        return robotStatusTextView;
    }

    /**
     * Getter function for getting the manual control UP button
     * Used in {@link ControlFragment}
     * @return The manual control UP button
     */
    public static ImageButton getUpBtn() {
        return upBtn;
    }

    /**
     * Getter function for getting the manual control DOWN button
     * Used in {@link ControlFragment}
     * @return The manual control DOWN button
     */
    public static ImageButton getDownBtn() {
        return downBtn;
    }

    /**
     * Getter function for getting the manual control LEFT button
     * Used in {@link ControlFragment}
     * @return The manual control LEFT button
     */
    public static ImageButton getLeftBtn() {
        return leftBtn;
    }

    /**
     * Getter function for getting the manual control RIGHT button
     * Used in {@link ControlFragment}
     * @return The manual control RIGHT button
     */
    public static ImageButton getRightBtn() {
        return rightBtn;
    }

    /**
     * Getter function for getting the bluetooth status
     * Used in {@link BluetoothConnectionService}
     * @return "Disconnected" or "Connected"
     */
    public static TextView getBluetoothStatus() {
        return bluetoothStatus;
    }

    /**
     * Getter function for getting the connecteed bluetooth device name
     * Used in {@link BluetoothConnectionService}
     * @return DEVICE_NAME
     */
    public static TextView getConnectedDevice() {
        return bluetoothDevice;
    }

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    // Send Coordinates to ALG
    // TODO
    // understand why is this used
    public static void printCoords(String message) {
        showLog("Displaying Coords untranslated and translated");
        String[] strArr = message.split("-", 2);

        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            //sends untranslated coordinates.
            byte[] bytes = strArr[0].getBytes(Charset.defaultCharset());

            BluetoothConnectionService.write(bytes);
        }
        refreshMessageReceivedNS("Untranslated Coordinates: " + strArr[0] + "\n");
        refreshMessageReceivedNS("Translated Coordinates: " + strArr[1]);
        showLog("Exiting printCoords");
    }

    /**
     * Modular function for sending message over via BT to STM
     * @param message String message to be sent over via BT
     */
    // TODO
    // change the obstacle update section to use this function instead!
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog(message);
        refreshMessageReceivedNS(message);
        showLog("Exiting printMessage");
    }

    /**
     * Basically adds a new line after the message is sent, so that the next message will appear on new line
     * @param message Last message that was sent over via BT
     */
    public static void refreshMessageReceivedNS(String message) {
        BluetoothChatFragment.getMessageReceivedTextView().append(message + "\n");
    }

    // TODO
    // understand what this function and the function below means
    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction", ""));
        printMessage("Direction is set to " + direction);
    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]));
        directionAxisTextView.setText(sharedPreferences.getString("direction", ""));
    }

    /**
     * Debugging function to show TAG + message, where TAG is the java file the message was logged
     * @param message the message to be logged
     */
    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    // TODO
    // understand what shared preference is
    // STOPPED HERE
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if (status.equals("connected")) {
                try {
                    myDialog.dismiss();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to " + mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "
                        + mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
            } else if (status.equals("disconnected")) {
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from " + mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "
                        + mDevice.getName(), Toast.LENGTH_SHORT).show();

                editor.putString("connStatus", "Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    // message handler
    // alg sends x,y,robotDirection,movementAction
    // alg sends ALG,<obstacle id>
    // rpi sends RPI,<image id>
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);
            int[] global_store = gridMap.getCurCoord();
            g_coordX = global_store[0];
            g_coordY = global_store[1];
            ArrayList<String> mapCoord = new ArrayList<>();
            //image format from RPI is "IMG-Obstacle ID-ImageID" eg IMG-3-7
            if (message.contains("IMG")) {
                String[] cmd = message.split("-");
                gridMap.updateImageID(cmd[1], cmd[2]);
                obstacleID = cmd[1];
            }
            // need to code a function to change robot position
            //UPDATE-X_Position-Y_Position-Direction
            // parse UPDATE-[4.75, 8.5, '4.13234890238423']
            else if (message.contains("UPDATE")) {

                String[] cmd1 = message.split("-");
                String l = cmd1[1];
                String str1 = l.replace("[", "");
                String str2 = str1.replace("]", "");
                String str3 = str2.replace(" ", "");
                String str4 = str3.replace("'", "");
                String str5 = str4.replace("\'", "");
                String[] cmd = str5.split(",");
                double xPosD = Float.parseFloat(cmd[0]) + 1.00;
                double yPosD = Float.parseFloat(cmd[1]);
                int xPos = (int) xPosD;
                int yPos = (int) yPosD;
                String direction;
                double radiansF = Float.parseFloat(cmd[2]);
                if (0.78 < radiansF && radiansF < 2.35) {
                    direction = "N";
                } else if (2.35 < radiansF && radiansF < 3.93) {
                    direction = "W";
                } else if (3.93 < radiansF && radiansF < 5.50) {
                    direction = "S";
                } else {
                    direction = "E";
                }
                //cmd[3] == N,S,E,W
                //xPos and yPos is updated grid of RIGHT WHEEL
                if (direction.equals("S")) {
                    xPos = xPos + 1;
                    yPos = yPos + 1;

                }
                ;
                if (direction.equals("E")) {
                    yPos = yPos + 1;
                }
                ;
                if (direction.equals("W")) {
                    xPos = xPos + 1;
                }
                ;
                gridMap.performAlgoCommand(xPos, yPos, direction);

            } else if (message.equals("ENDED")) {
                // if wk 8 btn is checked, means running wk 8 challenge and likewise for wk 9
                // end the corresponding timer
                ToggleButton exploreButton = findViewById(R.id.exploreToggleBtn2);
                ToggleButton fastestButton = findViewById(R.id.fastestToggleBtn2);

                if (exploreButton.isChecked()) {
                    showLog("explorebutton is checked");
                    stopTimerFlag = true;
                    exploreButton.setChecked(false);
                    robotStatusTextView.setText("Auto Movement/ImageRecog Stopped");
                    ControlFragment.timerHandler.removeCallbacks(ControlFragment.timerRunnableExplore);
                } else if (fastestButton.isChecked()) {
                    showLog("fastestbutton is checked");
                    stopTimerFlag = true;
                    fastestButton.setChecked(false);
                    robotStatusTextView.setText("Week 9 Stopped");
                    ControlFragment.timerHandler.removeCallbacks(ControlFragment.timerRunnableFastest);
                }
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    mBTDevice = data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}