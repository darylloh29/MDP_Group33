package com.example.mdp_group31;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
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


public class MainActivity extends AppCompatActivity {
    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    @SuppressLint("StaticFieldLeak") private static Context context;
    private static GridMap gridMap;
    @SuppressLint("StaticFieldLeak") static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    @SuppressLint("StaticFieldLeak") static TextView robotStatusTextView, bluetoothStatus, bluetoothDevice;
    @SuppressLint("StaticFieldLeak") static ImageButton upBtn, downBtn, leftBtn, rightBtn;

    BluetoothDevice mBTDevice;
    ProgressDialog myDialog;

    String obstacleID;

    private static final String TAG = "Main Activity";
    public static boolean stopTimerFlag = false;
    public static boolean stopWk9TimerFlag = false;


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
        MainActivity.sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction", "None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        // Toolbar
        ImageButton bluetoothButton = findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(v -> {
            Intent popup = new Intent(MainActivity.this, BluetoothPopUp.class);
            startActivity(popup);
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
                (dialog, which) -> dialog.dismiss()
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

    /**
     * Modular function for sending message over via BT to STM
     * @param message String message to be sent over via BT
     */
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

    /**
     * Resets the robot's direction when user configures it in Map Config fragment
     * @param direction The updated direction
     */
    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction", ""));
    }

    /**
     * Updates the coordinate display whenever robot moves
     */
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

    /**
     * Get SharedPreference, which is a key-value pair that stores important information across activity
     * @param context The current state of the application
     * @return The SharedPreference object
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    /**
     * Handles BT connection
     */
    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            MainActivity.sharedPreferences();

            if (status.equals("connected")) {
                try {
                    myDialog.dismiss();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Device now connected to "
                        + mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
            } else if (status.equals("disconnected")) {
                Toast.makeText(MainActivity.this, "Disconnected from "
                        + mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Disconnected");
                myDialog.show();
            }
            editor.commit();
        }
    };

    /**
     * Handles message sent from RPI
     * Message format:
     * IMG-[obstacle id]-[image id] for image rec
     *  ex: IMG-3-7 for obstacle 3 === image id 7
     * UPDATE-[x-coord]-[y-coord]-<N>[Bearing] - for updating robot coordinates
     *   ex 1: UPDATE-4.5-6-0 for moving robot to [4,6] (no change in direction, so assume is F/B move)
     *   ex 2: UPDATE-6-6-45 for moving robot 45 degrees to the left, and final position is [6,6]
     *   ex 3: UPDATE-6-6-N45 for moving robot 45 degrees to the right, and final position is [6,6]
     * ENDED for signaling Android that task is completed
     */
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            if (message.contains("IMG")) {
                String[] cmd = message.split("-");
                gridMap.updateImageID(cmd[1], cmd[2]);
                obstacleID = cmd[1];
            } else if (message.contains("UPDATE")) {
                String[] cmd = message.split("-");
                int xPos = (int) Float.parseFloat(cmd[1]);
                int yPos = (int) Float.parseFloat(cmd[2]);
                double bearing;
                if (cmd[3].contains("N")) {
                    bearing = (double) -1 * Float.parseFloat(cmd[3].substring(1));
                } else {
                    bearing = Float.parseFloat(cmd[3]);
                }
                gridMap.moveRobot(new int[]{xPos, yPos}, bearing);
            } else if (message.equals("ENDED")) {
                ToggleButton exploreButton = findViewById(R.id.exploreToggleBtn2);
                ToggleButton fastestButton = findViewById(R.id.fastestToggleBtn2);

                if (exploreButton.isChecked()) {
                    stopTimerFlag = true;
                    exploreButton.setChecked(false);
                    robotStatusTextView.setText(R.string.image_rec_end);
                    ControlFragment.timerHandler.removeCallbacks(ControlFragment.timerRunnableExplore);
                } else if (fastestButton.isChecked()) {
                    stopTimerFlag = true;
                    fastestButton.setChecked(false);
                    robotStatusTextView.setText(R.string.fastest_car_end);
                    ControlFragment.timerHandler.removeCallbacks(ControlFragment.timerRunnableFastest);
                }
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                mBTDevice = data.getExtras().getParcelable("mBTDevice");
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}