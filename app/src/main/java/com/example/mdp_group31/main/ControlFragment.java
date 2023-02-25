package com.example.mdp_group31.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mdp_group31.MainActivity;
import com.example.mdp_group31.R;

import java.util.Arrays;


public class ControlFragment extends Fragment {
    private static final String TAG = "ControlFragment";

    SharedPreferences sharedPreferences;

    // Control Button
    ImageButton moveForwardImageBtn, turnRightImageBtn, moveBackImageBtn, turnLeftImageBtn;
    ImageButton exploreResetButton, fastestResetButton;
    private static long exploreTimer, fastestTimer;
    public static ToggleButton exploreButton, fastestButton;
    public static TextView exploreTimeTextView, fastestTimeTextView, robotStatusTextView;
    private static GridMap gridMap;

    // Timer
    public static Handler timerHandler = new Handler();

    public static Runnable timerRunnableExplore = new Runnable() {
        @Override
        public void run() {
            long millisExplore = System.currentTimeMillis() - exploreTimer;
            int secondsExplore = (int) (millisExplore / 1000);
            int minutesExplore = secondsExplore / 60;
            secondsExplore = secondsExplore % 60;

            if (!MainActivity.stopTimerFlag) {
                exploreTimeTextView.setText(String.format("%02d:%02d", minutesExplore,
                        secondsExplore));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    public static Runnable timerRunnableFastest = new Runnable() {
        @Override
        public void run() {
            long millisFastest = System.currentTimeMillis() - fastestTimer;
            int secondsFastest = (int) (millisFastest / 1000);
            int minutesFastest = secondsFastest / 60;
            secondsFastest = secondsFastest % 60;

            if (!MainActivity.stopWk9TimerFlag) {
                fastestTimeTextView.setText(String.format("%02d:%02d", minutesFastest,
                        secondsFastest));
                timerHandler.postDelayed(this, 500);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate
        View root = inflater.inflate(R.layout.activity_control, container, false);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);

        // variable initialization
        moveForwardImageBtn = MainActivity.getUpBtn();
        turnRightImageBtn = MainActivity.getRightBtn();
        moveBackImageBtn = MainActivity.getDownBtn();
        turnLeftImageBtn = MainActivity.getLeftBtn();
        exploreTimeTextView = root.findViewById(R.id.exploreTimeTextView2);
        fastestTimeTextView = root.findViewById(R.id.fastestTimeTextView2);
        exploreButton = root.findViewById(R.id.exploreToggleBtn2);
        fastestButton = root.findViewById(R.id.fastestToggleBtn2);
        exploreResetButton = root.findViewById(R.id.exploreResetImageBtn2);
        fastestResetButton = root.findViewById(R.id.fastestResetImageBtn2);
        robotStatusTextView = MainActivity.getRobotStatusTextView();
        fastestTimer = 0;
        exploreTimer = 0;

        gridMap = MainActivity.getGridMap();

        // Button Listener
        moveForwardImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gridMap.getCanDrawRobot()) {
                    int[] curCoord = gridMap.getCurCoord();
                    String direction = gridMap.getRobotDirection();
                    switch (direction) {
                        case "up":
                            gridMap.moveRobot(new int[]{curCoord[0], curCoord[1] + 1}, 0);
                            break;
                        case "left":
                            gridMap.moveRobot(new int[]{curCoord[0] - 1, curCoord[1]}, 0);
                            break;
                        case "down":
                            gridMap.moveRobot(new int[]{curCoord[0], curCoord[1] - 1}, 0);
                            break;
                        case "right":
                            gridMap.moveRobot(new int[]{curCoord[0] + 1, curCoord[1]}, 0);
                            break;
                    }

                    MainActivity.refreshLabel();
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
            }
        });

        turnRightImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gridMap.getCanDrawRobot()) {
                    int[] curCoord = gridMap.getCurCoord();
                    String direction = gridMap.getRobotDirection();
                    switch (direction) {
                        case "up":
                            gridMap.moveRobot(new int[]{curCoord[0] + 3, curCoord[1] + 2}, -90);
                            break;
                        case "left":
                            gridMap.moveRobot(new int[]{curCoord[0] - 2, curCoord[1] + 3}, -90);
                            break;
                        case "down":
                            gridMap.moveRobot(new int[]{curCoord[0] - 3, curCoord[1] - 2}, -90);
                            break;
                        case "right":
                            gridMap.moveRobot(new int[]{curCoord[0] + 2, curCoord[1] - 3}, -90);
                            break;
                    }

                    MainActivity.refreshLabel();
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
            }
        });

        moveBackImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gridMap.getCanDrawRobot()) {
                    int[] curCoord = gridMap.getCurCoord();
                    String direction = gridMap.getRobotDirection();
                    switch (direction) {
                        case "up":
                            gridMap.moveRobot(new int[]{curCoord[0], curCoord[1] - 1}, 0);
                            break;
                        case "left":
                            gridMap.moveRobot(new int[]{curCoord[0] + 1, curCoord[1]}, 0);
                            break;
                        case "down":
                            gridMap.moveRobot(new int[]{curCoord[0], curCoord[1] + 1}, 0);
                            break;
                        case "right":
                            gridMap.moveRobot(new int[]{curCoord[0] - 1, curCoord[1]}, 0);
                            break;
                    }
                    MainActivity.refreshLabel();
                }
                else {
                    updateStatus("Please press 'STARTING POINT'");
                }
            }
        });

        turnLeftImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gridMap.getCanDrawRobot()) {
                    int[] curCoord = gridMap.getCurCoord();
                    String direction = gridMap.getRobotDirection();
                    switch (direction) {
                        case "up":
                            gridMap.moveRobot(new int[]{curCoord[0] - 2, curCoord[1] + 1}, 90);
                            break;
                        case "left":
                            gridMap.moveRobot(new int[]{curCoord[0] - 1, curCoord[1] - 2}, 90);
                            break;
                        case "down":
                            gridMap.moveRobot(new int[]{curCoord[0] + 2, curCoord[1] - 1}, 90);
                            break;
                        case "right":
                            gridMap.moveRobot(new int[]{curCoord[0] + 1, curCoord[1] + 2}, 90);
                            break;
                    }
                    MainActivity.refreshLabel();
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
            }
        });

        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreToggleBtn");
                ToggleButton exploreToggleBtn = (ToggleButton) v;

                if (exploreToggleBtn.getText().equals("WK8 START")) {
                    showToast("Auto Movement/ImageRecog timer stop!");
                    robotStatusTextView.setText("Auto Movement Stopped");
                    timerHandler.removeCallbacks(timerRunnableExplore);
                }
                else if (exploreToggleBtn.getText().equals("STOP")) {
                    String msg = gridMap.getAllObstacles();
                    MainActivity.printMessage(msg);
                    MainActivity.stopTimerFlag = false;
                    showToast("Auto Movement/ImageRecog timer start!");
                    robotStatusTextView.setText("Auto Movement Started");
                    exploreTimer = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableExplore, 0);
                }
                else {
                    showToast("Else statement: " + exploreToggleBtn.getText());
                }
                showLog("Exiting exploreToggleBtn");
            }
        });

        fastestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestToggleBtn");
                ToggleButton fastestToggleBtn = (ToggleButton) v;
                if (fastestToggleBtn.getText().equals("WK9 START")) {
                    showToast("Fastest car timer stop!");
                    robotStatusTextView.setText("Fastest Car Stopped");
                    timerHandler.removeCallbacks(timerRunnableFastest);
                }
                else if (fastestToggleBtn.getText().equals("STOP")) {
                    showToast("Fastest car timer start!");
                    try {
                        MainActivity.printMessage("STM|Start");
                    } catch (Exception e) {
                        showLog(e.getMessage());
                    }
                    MainActivity.stopWk9TimerFlag = false;
                    robotStatusTextView.setText("Fastest Car Started");
                    fastestTimer = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableFastest, 0);
                }
                else
                    showToast(fastestToggleBtn.getText().toString());
                showLog("Exiting fastestToggleBtn");
            }
        });

        exploreResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreResetImageBtn");
                showToast("Resetting exploration time...");
                exploreTimeTextView.setText("00:00");
                robotStatusTextView.setText("Not Available");
                if(exploreButton.isChecked())
                    exploreButton.toggle();
                timerHandler.removeCallbacks(timerRunnableExplore);
                showLog("Exiting exploreResetImageBtn");
            }
        });

        fastestResetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showLog("Clicjed fatestResetImgBtn");
                showToast("Resetting Fastest Time...");
                fastestTimeTextView.setText("00:00");
                robotStatusTextView.setText("Fastest Car Finished");
                if(fastestButton.isChecked()){
                    fastestButton.toggle();
                }
                timerHandler.removeCallbacks(timerRunnableFastest);
                showLog("Exiting fastestResetImgBtn");
            }
        });

        return root;
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void updateStatus(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0, 0);
        toast.show();
    }
}