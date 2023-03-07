package com.example.mdp_group31.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import java.util.Locale;


public class ControlFragment extends Fragment {
    private static final String TAG = "ControlFragment";
    private final MainActivity mainActivity;
    private long imgRecTime, fastestCarTime;
    private ToggleButton imgRecBtn, fastestCarBtn;
    private TextView imgRecText, fastestCarText, robotStatusText;
    private GridMap gridMap;
    private int[] curCoord;
    private String direction;

    public ControlFragment(MainActivity main) {
        this.mainActivity = main;
    }

    public static Handler timerHandler = new Handler();

    public Runnable imgRecTimer = new Runnable() {
        @Override
        public void run() {
            long msTime = System.currentTimeMillis() - imgRecTime;
            int sTime = (int) (msTime / 1000);
            int minuteTime = sTime / 60;
            sTime = sTime % 60;

            if (! mainActivity.imgRecTimerFlag) {
                imgRecText.setText(String.format(Locale.US, "%02d:%02d", minuteTime, sTime));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    public Runnable fastestCarTimer = new Runnable() {
        @Override
        public void run() {
            long msTime = System.currentTimeMillis() - fastestCarTime;
            int sTime = (int) (msTime / 1000);
            int minuteTime = sTime / 60;
            sTime = sTime % 60;

            if (!mainActivity.fastestCarTimerFlag) {
                fastestCarText.setText(String.format(Locale.US,"%02d:%02d", minuteTime,
                        sTime));
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
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        // variable initialization
        // Control Button
        ImageButton forwardBtn = this.mainActivity.getUpBtn();
        ImageButton rightBtn = this.mainActivity.getRightBtn();
        ImageButton backBtn = this.mainActivity.getDownBtn();
        ImageButton leftBtn = this.mainActivity.getLeftBtn();
        ImageButton imgRecResetBtn = root.findViewById(R.id.exploreResetImageBtn2);
        ImageButton fastestCarResetBtn = root.findViewById(R.id.fastestResetImageBtn2);
        this.imgRecText = root.findViewById(R.id.exploreTimeTextView2);
        this.fastestCarText = root.findViewById(R.id.fastestTimeTextView2);
        this.imgRecBtn = root.findViewById(R.id.exploreToggleBtn2);
        this.fastestCarBtn = root.findViewById(R.id.fastestToggleBtn2);
        this.robotStatusText = this.mainActivity.getRobotStatusText();
        this.fastestCarTime = 0;
        this.imgRecTime = 0;
        this.gridMap = this.mainActivity.getGridMap();

        // Button Listener
        forwardBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] + 1}, 0);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 1, this.curCoord[1]}, 0);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] - 1}, 0);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 1, this.curCoord[1]}, 0);
                        break;
                }

                this.mainActivity.refreshCoordinate();
            }
            else
                this.showToast("Please place robot on map to begin");
        });

        rightBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 3, this.curCoord[1] + 2}, -90);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 2, this.curCoord[1] + 3}, -90);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 3, this.curCoord[1] - 2}, -90);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 2, this.curCoord[1] - 3}, -90);
                        break;
                }

                this.mainActivity.refreshCoordinate();
            }
            else
                this.showToast("Please place robot on map to begin");
        });

        backBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] - 1}, 0);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 1, this.curCoord[1]}, 0);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] + 1}, 0);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 1, this.curCoord[1]}, 0);
                        break;
                }
                this.mainActivity.refreshCoordinate();
            }
            else {
                this.showToast("Please place robot on map to begin");
            }
        });

        leftBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 2, this.curCoord[1] + 1}, 90);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 1, this.curCoord[1] - 2}, 90);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 2, this.curCoord[1] - 1}, 90);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 1, this.curCoord[1] + 2}, 90);
                        break;
                }
                this.mainActivity.refreshCoordinate();
            }
            else
                this.showToast("Please place robot on map to begin");
        });

        imgRecBtn.setOnClickListener(v -> {
            if (imgRecBtn.getText().equals("START")) {
                this.showToast("Image Recognition Completed!!");
                this.robotStatusText.setText(R.string.img_rec_stop);
                timerHandler.removeCallbacks(this.imgRecTimer);
            }
            else if (imgRecBtn.getText().equals("STOP")) {
                mainActivity.imgRecTimerFlag = false;
                this.showToast("Image Recognition Started!!");
                String getObsPos = this.gridMap.getAllObstacles();
                getObsPos = "OBS|" + getObsPos;
                this.mainActivity.sendMessage(getObsPos);
                this.robotStatusText.setText(R.string.img_rec_start);
                this.imgRecTime = System.currentTimeMillis();
                timerHandler.postDelayed(imgRecTimer, 0);
            }
        });

        fastestCarBtn.setOnClickListener(v -> {
            if (fastestCarBtn.getText().equals("START")) {
                this.showToast("Fastest Car Stopped!");
                this.robotStatusText.setText(R.string.fastest_car_stop);
                timerHandler.removeCallbacks(fastestCarTimer);
            }
            else if (fastestCarBtn.getText().equals("STOP")) {
                this.showToast("Fastest Car started!");
                this.mainActivity.sendMessage("STM|Start");  //TODO change this cmd
                mainActivity.fastestCarTimerFlag = false;
                this.robotStatusText.setText(R.string.fastest_car_start);
                this.fastestCarTime = System.currentTimeMillis();
                timerHandler.postDelayed(fastestCarTimer, 0);
            }
        });

        imgRecResetBtn.setOnClickListener(v -> {
            this.showToast("Resetting image recognition challenge timer...");
            this.imgRecText.setText(R.string.timer_default_val);
            this.robotStatusText.setText(R.string.robot_status_na);
            if (this.imgRecBtn.isChecked())
                this.imgRecBtn.toggle();
            timerHandler.removeCallbacks(imgRecTimer);
        });

        fastestCarResetBtn.setOnClickListener(view -> {
            showToast("Resetting fastest car challenge timer...");
            this.fastestCarText.setText(R.string.timer_default_val);
            this.robotStatusText.setText(R.string.robot_status_na);
            if (this.fastestCarBtn.isChecked()){
                this.fastestCarBtn.toggle();
            }
            timerHandler.removeCallbacks(fastestCarTimer);
        });

        return root;
    }

    private void debugMessage(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}