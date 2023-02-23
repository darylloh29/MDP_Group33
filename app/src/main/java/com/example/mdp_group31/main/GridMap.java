package com.example.mdp_group31.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import com.example.mdp_group31.MainActivity;
import com.example.mdp_group31.R;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GridMap extends View {
    private class Cell {
        protected final float startX, startY, endX, endY;
        protected Paint paint;
        private String type;
        private int id = -1;

        private Cell(float startX, float startY, float endX, float endY, Paint paint, String type) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.paint = paint;
            this.type = type;
        }

        public void setType(String type) {
            this.type = type;
            switch (type) {
                case "obstacle":
                    this.paint = obstacleColor;
                    break;
                case "end":
                    this.paint = endColor;
                    break;
                case "start":
                    this.paint = startColor;
                    break;
                case "waypoint":
                    this.paint = waypointColor;
                    break;
                case "unexplored":
                    this.paint = unexploredColor;
                    break;
                case "explored":
                    this.paint = exploredColor;
                    break;
                case "arrow":
                    this.paint = arrowColor;
                    break;
                case "fastestPath":
                    this.paint = fastestPathColor;
                    break;
                case "image":
                    this.paint = obstacleColor;
                default:
                    Logd("setType default: " + type);
                    break;
            }
        }

        public String getType() {
            return this.type;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    SharedPreferences sharedPreferences;
    private final Paint blackPaint = new Paint();
    private final Paint whitePaint = new Paint();
    private final Paint greenPaint = new Paint();
    private final Paint obstacleColor = new Paint();
    private final Paint robotColor = new Paint();
    private final Paint endColor = new Paint();
    private final Paint startColor = new Paint();
    private final Paint waypointColor = new Paint();
    private final Paint unexploredColor = new Paint();
    private final Paint exploredColor = new Paint();
    private final Paint arrowColor = new Paint();
    private final Paint fastestPathColor = new Paint();
    private static String robotDirection = "None";
    private static int[] startCoord = new int[]{-1, -1};
    private static int[] curCoord = new int[]{-1, -1};
    private static int[] oldCoord = new int[]{-1, -1};
    private static ArrayList<int[]> obstacleCoord = new ArrayList<>();
    private static boolean canDrawRobot = false;
    private static boolean startCoordStatus = false;
    private static boolean setObstacleStatus = false;
    private static final boolean unSetCellStatus = false;
    private static final boolean setExploredStatus = false;
    private static boolean validPosition = false;
    private static final String TAG = "GridMap";
    private static final int COL = 20;
    private static final int ROW = 20;
    private static float cellSize;
    private static Cell[][] cells;
    private boolean mapDrawn = false;
    public String[][] OBSTACLE_LIST = new String[20][20];
    public String[][] IMAGE_LIST = new String[20][20];
    public static String[][] IMAGE_BEARING = new String[20][20];
    static ClipData clipData;
    static Object localState;
    int initialColumn, initialRow;

    public GridMap(Context c) {
        super(c);
        initMap();
    }

    public GridMap(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initMap();
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(15);
        whitePaint.setTextAlign(Paint.Align.CENTER);
        greenPaint.setColor(getResources().getColor(R.color.grassColor));
        greenPaint.setStrokeWidth(8);
        obstacleColor.setColor(getResources().getColor(R.color.rockColor));
        robotColor.setColor(getResources().getColor(R.color.light_blue));
        robotColor.setStrokeWidth(2);
        endColor.setColor(Color.RED);
        startColor.setColor(Color.CYAN);
        waypointColor.setColor(Color.GREEN);
        unexploredColor.setColor(getResources().getColor(R.color.lightBlue));
        exploredColor.setColor(getResources().getColor(R.color.exploredColor2));
        arrowColor.setColor(Color.BLACK);
        fastestPathColor.setColor(Color.MAGENTA);
        Paint newpaint = new Paint();
        newpaint.setColor(Color.TRANSPARENT);
        // get shared preferences
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);
    }

    private void initMap() {
        /* initialize 3 2d arrays
            1. OBSTACLE_LIST: 2d array of cells. If a cell has obstacle, the id is found here
            2. IMAGE_LIST: 2d array of cells. If a cell has obstacle and identified, the image id is found here
            3. IMAGE_BEARING: 2d array of cells. If a cell has obstacle, the facing direction is found here.
        */
        for (int outter = 0; outter < this.OBSTACLE_LIST.length; outter++) {
            String[] row = new String[this.OBSTACLE_LIST[outter].length];
            Arrays.fill(row, "");
            this.OBSTACLE_LIST[outter] = row;
            this.IMAGE_LIST[outter] = row;
            IMAGE_BEARING[outter] = row;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mapDrawn) {
            this.createCell();
            mapDrawn = true;
        }

        this.drawIndividualCell(canvas);
        this.drawGridLines(canvas);
        this.drawGridNumber(canvas);
        if (getCanDrawRobot())
            drawRobot(canvas, curCoord);
        drawObstacles(canvas);
    }

    private void createCell() {
        cells = new Cell[COL + 1][ROW + 1];     // COL is horizontal; ROW is vertical
        cellSize = (float) getWidth() / (COL + 1);

        for (int x = 0; x <= COL; x++)
            for (int y = 0; y <= ROW; y++)
                cells[x][y] = new Cell(
                        x * cellSize + (cellSize / 30),
                        y * cellSize + (cellSize / 30),
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                        unexploredColor,
                        "unexplored"
                );
    }

    private void drawIndividualCell(Canvas canvas) {
        // y starts from 1 since 1st column is used for index labels
        for (int x = 1; x <= COL; x++)
            for (int y = 0; y < ROW; y++)
                if (!cells[x][y].getType().equals("image") && cells[x][y].getId() == -1) {
                    canvas.drawRect(
                            cells[x][y].startX,
                            cells[x][y].startY,
                            cells[x][y].endX,
                            cells[x][y].endY,
                            cells[x][y].paint
                    );
                } else {
                    Paint textPaint = new Paint();
                    textPaint.setTextSize(25);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawRect(
                            cells[x][y].startX,
                            cells[x][y].startY,
                            cells[x][y].endX,
                            cells[x][y].endY,
                            cells[x][y].paint
                    );
                    canvas.drawText(
                            String.valueOf(cells[x][y].getId()),
                            (cells[x][y].startX + cells[x][y].endX) / 2,
                            cells[x][y].endY + (cells[x][y].startY - cells[x][y].endY) / 4,
                            textPaint
                    );
                }
    }

    private void drawGridLines(Canvas canvas) {
        for (int x = 0; x < ROW; x ++)
            canvas.drawLine(
                    cells[x][0].startX - (cellSize / 30) + cellSize,
                    cells[x][0].startY + (cellSize / 30),
                    cells[x][20].startX - (cellSize / 30) + cellSize,
                    cells[x][20].startY + (cellSize / 30),
                    whitePaint
            );

        for (int y = 0; y < COL; y ++)
            canvas.drawLine(
                    cells[0][y].startX - (cellSize / 30) + cellSize,
                    cells[0][y].startY + (cellSize / 30),
                    cells[20][y].startX - (cellSize / 30) + cellSize,
                    cells[20][y].startY + (cellSize / 30),
                    whitePaint
            );
    }

    private void drawGridNumber(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(17);
        textPaint.setColor(Color.BLACK);

        for (int x = 1; x <= COL; x++) {
            if (x >= 10)
                canvas.drawText(
                        Integer.toString(x),
                        cells[x][20].startX + (cellSize / 5),
                        cells[x][20].startY + (cellSize / 1.5f),
                        textPaint
                );
            else
                canvas.drawText(
                        Integer.toString(x),
                        cells[x][20].startX + (cellSize / 2.5f),
                        cells[x][20].startY + (cellSize / 1.5f),
                        textPaint
                );
        }

        for (int y = 0; y < ROW; y++) {
            if ((20 - y) >= 10)
                canvas.drawText(
                        Integer.toString(ROW - y),
                        cells[0][y].startX + (cellSize / 5),
                        cells[0][y].startY + (cellSize / 1.5f),
                        textPaint
                );
            else
                canvas.drawText(
                        Integer.toString(ROW - y),
                        cells[0][y].startX + (cellSize / 2.5f),
                        cells[0][y].startY + (cellSize / 1.5f),
                        textPaint
                );
        }
    }

    public boolean getCanDrawRobot() {
        return canDrawRobot;
    }

    public void setCanDrawRobot(boolean canDrawRobot) {
        GridMap.canDrawRobot = canDrawRobot;
    }

    private void drawRobot(Canvas canvas, int[] curCoord) {
        float xCoord, yCoord;
        BitmapFactory.Options op = new BitmapFactory.Options();
        Bitmap bm, mapscalable;
        int robotX = curCoord[0];
        int robotY = curCoord[1];

        if (! (robotX == -1 || robotY == -1)) {
            op.inMutable = true;
            switch (this.getRobotDirection()) {
                case "up":
                    xCoord = cells[robotX][20 - robotY].startX;
                    yCoord = cells[robotX][20 - robotY].startY;
                    bm = BitmapFactory.decodeResource(getResources(),R.drawable.car_face_up, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;

                case "down":
                    xCoord = cells[robotX - 1][20 - (robotY + 1)].startX;
                    yCoord = cells[robotX - 1][20 - (robotY + 1)].startY;
                    bm = BitmapFactory.decodeResource(getResources(),R.drawable.car_face_down, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;

                case "right":
                    xCoord = cells[robotX - 1][20 - robotY].startX;
                    yCoord = cells[robotX - 1][20 - robotY].startY;
                    bm = BitmapFactory.decodeResource(getResources(),R.drawable.car_face_right, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;

                case "left":
                    xCoord = cells[robotX][20 - (robotY - 1)].startX;
                    yCoord = cells[robotX][20 - (robotY + 1)].startY;
                    bm = BitmapFactory.decodeResource(getResources(),R.drawable.car_face_left, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;

                default:
                    Toast.makeText(
                            this.getContext(),
                            "Error with drawing robot (unknown direction)",
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
            }
        }
    }

    private void drawObstacles(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(11);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                // draw image id
                canvas.drawText(
                        OBSTACLE_LIST[19 - i][j],
                        cells[j + 1][19 - i].startX + ((cells[1][1].endX - cells[1][1].startX) / 2),
                        cells[j + 1][i].startY + ((cells[1][1].endY - cells[1][1].startY) / 2) + 10,
                        textPaint
                );

                // color the face direction
                switch (IMAGE_BEARING[19 - i][j]) {
                    case "North":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].startY,
                                cells[j + 1][20 - i].endX,
                                cells[j + 1][i].startY,
                                greenPaint
                        );
                        break;
                    case "South":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].startY + cellSize,
                                cells[j + 1][20 - i].endX,
                                cells[j + 1][i].startY + cellSize,
                                greenPaint
                        );
                        break;
                    case "East":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX + cellSize,
                                cells[j + 1][i].startY,
                                cells[j + 1][20 - i].startX + cellSize,
                                cells[j + 1][i].endY,
                                greenPaint
                        );
                        break;
                    case "West":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].startY,
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].endY,
                                greenPaint
                        );
                        break;
                }
            }
        }
    }

    public String getRobotDirection() {
        return robotDirection;
    }

    private void setValidPosition(boolean status) {
        validPosition = status;
    }

    public boolean getValidPosition() {
        return validPosition;
    }

    public void setSetObstacleStatus(boolean status) {
        setObstacleStatus = status;
    }

    public boolean getSetObstacleStatus() {
        return setObstacleStatus;
    }

    public void setStartCoordStatus(boolean status) {
        startCoordStatus = status;
    }

    private boolean getStartCoordStatus() {
        return startCoordStatus;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initialColumn = (int) (event.getX() / cellSize);
            initialRow = this.convertRow((int) (event.getY() / cellSize));
            String obstacleID;
            String obstacleBearing;
            ToggleButton setStartPointToggleBtn = ((Activity)this.getContext())
                    .findViewById(R.id.startpointToggleBtn);

            if (MapTabFragment.dragStatus) {
                // if the drag location has no obstacles, do nothing
                if (OBSTACLE_LIST[initialRow - 1][initialColumn - 1].equals("")) {
                    return false;
                }
                DragShadowBuilder dragShadowBuilder = new MyDragShadowBuilder(this);
                this.startDragAndDrop(null, dragShadowBuilder, null, 0);
            }

            // start change obstacle
            if (MapTabFragment.changeObstacleStatus) {
                // if touch on empty cell, do nothing
                if (OBSTACLE_LIST[initialRow - 1][initialColumn - 1].equals("")) {
                    return false;
                } else {
                    obstacleID = OBSTACLE_LIST[initialRow - 1][initialColumn - 1];
                    obstacleBearing = IMAGE_BEARING[initialRow - 1][initialColumn - 1];
                    final int tRow = initialRow;
                    final int tCol = initialColumn;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getContext());
                    View mView = ((Activity) this.getContext()).getLayoutInflater()
                            .inflate(R.layout.activity_dialog_change_obstacle,
                                    null);
                    mBuilder.setTitle("Change Existing Obstacle ID/Bearing");
                    final Spinner mIDSpinner = mView.findViewById(R.id.imageIDSpinner2);
                    final Spinner mBearingSpinner = mView.findViewById(R.id.bearingSpinner2);

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            this.getContext(), R.array.imageID_array,
                            android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mIDSpinner.setAdapter(adapter);
                    ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                            this.getContext(), R.array.imageBearing_array,
                            android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mBearingSpinner.setAdapter(adapter2);
                    mIDSpinner.setSelection(Integer.parseInt(obstacleID.substring(2)));

                    switch (obstacleBearing) {
                        case "North": mBearingSpinner.setSelection(0);
                            break;
                        case "South": mBearingSpinner.setSelection(1);
                            break;
                        case "East": mBearingSpinner.setSelection(2);
                            break;
                        case "West": mBearingSpinner.setSelection(3);
                    }

                    // do what when user presses ok
                    final String oldID = obstacleID;
                    mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newID = mIDSpinner.getSelectedItem().toString();
                            String newBearing = mBearingSpinner.getSelectedItem().toString();

                            dropObstacle(oldID, initialColumn, initialRow);
                            setObstacleCoord(tCol, tRow, newID);

                            OBSTACLE_LIST[tRow - 1][tCol - 1] = newID;
                            IMAGE_BEARING[tRow - 1][tCol - 1] = newBearing;

                            String sentText = "ID|" + oldID + "-" + newID + "-" + newBearing;
                            MainActivity.printMessage(sentText);
                            callInvalidate();
                        }
                    });

                    // dismiss
                    mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    mBuilder.setView(mView);
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    Window window =  dialog.getWindow();
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.width = 150;
                    window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }

            if (startCoordStatus) {
                String direction = getRobotDirection();
                boolean flag = false;
                if (canDrawRobot) {
                    if (direction.equals("None")) {
                        direction = "up";
                    }

                    switch (direction) {
                        case "up":
                            if (initialColumn > 0 && initialColumn < 20 && initialRow > 1 && initialRow <= 20) {
                                flag = true;
                            }
                            break;

                        case "left":
                            if (initialColumn > 0 && initialColumn < 20 && initialRow >= 1 && initialRow < 20) {
                                flag = true;
                            }
                            break;

                        case "right":
                            if (initialColumn > 1 && initialColumn <= 20 && initialRow > 1 && initialRow <= 20) {
                                flag = true;
                            }
                            break;

                        case "down":
                            if (initialColumn > 1 && initialColumn <= 20 && initialRow > 0 && initialRow < 20) {
                                flag = true;
                            }
                            break;
                    }

                    for (int i = 1; i < COL; i ++) {
                        for (int j = 1; j < ROW; j ++) {
                            if (cells[i][j].type.equals("robot")) {
                                cells[i][j].setType("explored");
                            }
                        }
                    }
                } else {
                    canDrawRobot = true;
                }

                if (flag) {
                    this.setStartCoord(initialColumn, initialRow);
                    startCoordStatus = false;
                    this.updateRobotAxis(initialColumn, initialRow, direction);
                    if (setStartPointToggleBtn.isChecked())
                        setStartPointToggleBtn.toggle();
                }

                this.invalidate();
                return true;
            }

            // add id and the image bearing, popup to ask for user input
            if (setObstacleStatus) {
                if (initialRow <= 20 && initialColumn <= 20) {
                    OBSTACLE_LIST[initialRow - 1][initialColumn - 1] = "OB0";
                    IMAGE_BEARING[initialRow - 1][initialColumn - 1] = "North";
                    this.setObstacleCoord(initialColumn, initialRow, "OB0");
                }
                this.invalidate();
                return true;
            }
            if (setExploredStatus) {
                cells[initialColumn][20 - initialRow].setType("explored");
                this.invalidate();
                return true;
            }
        }
        return false;
    }

    public void setStartCoord(int col, int row) {
        String direction = getRobotDirection();
        if (direction.equals("None")) {
            direction = "up";
        }
        switch (direction) {
            case "up":
                if (col > 0 && col < 20 && row > 1 && row <= 20) {
                    startCoord[0] = col;
                    startCoord[1] = row;
                } else {
                    return;
                }
                break;

            case "left":
                if (col > 0 && col < 20 && row >= 1 && row < 20) {
                    startCoord[0] = col;
                    startCoord[1] = row;
                } else {
                    return;
                }
                break;

            case "right":
                if (col > 1 && col <= 20 && row > 1 && row <= 20) {
                    startCoord[0] = col;
                    startCoord[1] = row;
                } else {
                    return;
                }
                break;

            case "down":
                if (col > 1 && col <= 20 && row > 0 && row < 20) {
                    startCoord[0] = col;
                    startCoord[1] = row;
                } else {
                    return;
                }
                break;
        }

        if (this.getStartCoordStatus())
            this.setCurCoord(col, row, direction);
    }

    public void setCurCoord(int col, int row, String direction) {
        Logd("Current coordinate is: " + col + " " + row);
        if (col < 1 || col > 20 || row < 1 || row > 20) {
            return;
        }

        curCoord[0] = col;
        curCoord[1] = row;
        this.setRobotDirection(direction);
        this.updateRobotAxis(col, row, direction);

        row = this.convertRow(row);
        cells[col][row].setType("explored");

        switch (direction) {
            case "up":
                cells[col + 1][row + 1].setType("explored");
                cells[col + 1][row].setType("explored");
                cells[col][row + 1].setType("explored");
                break;

            case "down":
                cells[col - 1][row - 1].setType("explored");
                cells[col - 1][row].setType("explored");
                cells[col][row - 1].setType("explored");
                break;

            case "left":
                cells[col + 1][row - 1].setType("explored");
                cells[col + 1][row].setType("explored");
                cells[col][row - 1].setType("explored");
                break;

            case "right":
                cells[col - 1][row + 1].setType("explored");
                cells[col - 1][row].setType("explored");
                cells[col][row + 1].setType("explored");
                break;
        }
    }

    public int[] getCurCoord() {
        return curCoord;
    }

    private int convertRow(int row) {
        return (20 - row);
    }

    public void setRobotDirection(String direction) {
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        robotDirection = direction;
        editor.putString("direction", direction);
        editor.apply();
        this.invalidate();
    }

    private void updateRobotAxis(int col, int row, String direction) {
        TextView xAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.xAxisTextView);
        TextView yAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.yAxisTextView);
        TextView directionAxisTextView =  ((Activity)this.getContext())
                .findViewById(R.id.directionAxisTextView);

        xAxisTextView.setText(String.format(Integer.toString(col)));
        yAxisTextView.setText(String.format(Integer.toString(row)));
        directionAxisTextView.setText(direction);
    }

    public void setObstacleCoord(int col, int row, String obstacleID) {
        int parsedID = Integer.parseInt(obstacleID.substring(2));
        int[] obstacleCoord = new int[]{col, row, parsedID};
        GridMap.obstacleCoord.add(obstacleCoord);
        OBSTACLE_LIST[row - 1][col - 1] = obstacleID;
        row = this.convertRow(row);
        cells[col][row].setType("obstacle");
    }

    private ArrayList<int[]> getObstacleCoord() {
        return obstacleCoord;
    }

    private static void Logd(String message) {
        Log.d(TAG, message);
    }

    @Override
    public boolean onDragEvent(DragEvent dragEvent) {
        clipData = dragEvent.getClipData();
        localState = dragEvent.getLocalState();

        String tempID, tempBearing;
        int endColumn, endRow;
        String obstacleID = OBSTACLE_LIST[initialRow - 1][initialColumn - 1];

        // if the currently dragged cell is empty, do nothing
        if (obstacleID.equals("")) {
            return false;
        }

        // drop outside of map entirely (anywhere on the screen)
        if (!dragEvent.getResult() && dragEvent.getAction() == DragEvent.ACTION_DRAG_ENDED) {
            this.dropObstacle(obstacleID, initialColumn, initialRow);
        }

        // drop on the row and column indices
        if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
            endColumn = (int) (dragEvent.getX() / cellSize);
            endRow = this.convertRow((int) (dragEvent.getY() / cellSize));

            // if dropped within mapview but outside drawn grids, remove obstacle from lists
            if (endColumn <= 0 || endRow <= 0) {
                Logd("Dropped on indices row");
                this.dropObstacle(obstacleID, initialColumn, initialRow);
            }

            // if dropped within gridmap, shift it to new position unless already got existing
            else if (1 <= initialColumn && initialColumn <= 20 && 1 <= initialRow && initialRow <= 20
                    && endColumn <= 20 && endRow <= 20) {
                Logd("Dropped anywhere on the map");
                tempID = OBSTACLE_LIST[initialRow - 1][initialColumn - 1];
                tempBearing = IMAGE_BEARING[initialRow - 1][initialColumn - 1];

                // check if got existing obstacle at drop location
                if (!OBSTACLE_LIST[endRow - 1][endColumn - 1].equals("")) {
                    Logd("An obstacle is already at drop location");
                } else {
                    this.dropObstacle(obstacleID, initialColumn, initialRow);
                    setObstacleCoord(endColumn, endRow, tempID);
                    IMAGE_BEARING[endRow - 1][endColumn - 1] = tempBearing;
                }
            } else {
                Logd("Drag event failed.");
            }

            String sentText = "LOC|" + obstacleID + "-" + initialRow + "-" + initialColumn + "-" + endRow + "-" + endColumn;
            MainActivity.printMessage(sentText);
        }
        this.invalidate();
        return true;
    }

    public void callInvalidate() {
        Logd("Entering callinvalidate");
        this.invalidate();
    }

    public void dropObstacle(String obstacleID, int x, int y) {
        int obstacleX, obstacleY;
        for (int i = 0; i < obstacleCoord.size(); i ++) {
            if (Arrays.equals(obstacleCoord.get(i), new int[]{x, y, Integer.parseInt(obstacleID.substring(2))})) {
                obstacleX = obstacleCoord.get(i)[0];
                obstacleY = obstacleCoord.get(i)[1];
                OBSTACLE_LIST[obstacleY - 1][obstacleX - 1] = "";
                IMAGE_BEARING[obstacleY - 1][obstacleX - 1] = "";
                cells[obstacleX][20 - obstacleY].setType("unexplored");
                obstacleCoord.remove(obstacleCoord.get(i));
                return;
            }
        }
    }

    public void toggleCheckedBtn(String buttonName) {
        ToggleButton setStartPointToggleBtn = ((Activity)this.getContext())
                .findViewById(R.id.startpointToggleBtn);
        ImageButton obstacleImageBtn = ((Activity)this.getContext())
                .findViewById(R.id.addObstacleBtn);

        if (!buttonName.equals("setStartPointToggleBtn"))
            if (setStartPointToggleBtn.isChecked()) {
                this.setStartCoordStatus(false);
                setStartPointToggleBtn.toggle();
            }
        if (!buttonName.equals("obstacleImageBtn"))
            if (obstacleImageBtn.isEnabled())
                this.setSetObstacleStatus(false);
    }


    public void resetMap() {
        Logd("Entering resetMap");
        TextView robotStatusTextView =  ((Activity)this.getContext())
                .findViewById(R.id.robotStatus);
        updateRobotAxis(0, 0, "None");
        robotStatusTextView.setText("Not Available");


        this.toggleCheckedBtn("None");

        startCoord = new int[]{-1, -1};
        curCoord = new int[]{-1, -1};
        oldCoord = new int[]{-1, -1};
        robotDirection = "None";
        obstacleCoord = new ArrayList<>();
        mapDrawn = false;
        canDrawRobot = false;
        validPosition = false;

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                OBSTACLE_LIST[i][j] = "";
                IMAGE_BEARING[i][j] = "";
            }
        }
        Logd("Exiting resetMap");
        this.invalidate();
    }

    // e.g obstacle is on right side of 2x2 and can turn left and vice versa
    public void moveRobot(String direction) {
        Logd("Entering moveRobot");
        setValidPosition(false);
        int[] curCoord = this.getCurCoord();
        ArrayList<int[]> obstacleCoord = this.getObstacleCoord();
        String robotDirection = getRobotDirection();
        String backupDirection = robotDirection;

        // check if got obstacle when moving one grid up before turning in each case
        switch (robotDirection) {
            case "up":
                switch (direction) {
                    case "forward":
                        curCoord[1] += 1;
                        if (curCoord[1] <= 20) {
                            validPosition = true;
                        } else {
                            curCoord[1] -= 1;
                            validPosition = false;
                        }
                        break;
                    case "right":
                        curCoord[1] += 2;
                        curCoord[0] += 3;
                        if ((1 < curCoord[1] && curCoord[1] <= 20)
                                && (1 < curCoord[0] && curCoord[0] <= 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "right")) {
                                validPosition = false;
                                curCoord[1] -= 2;
                                curCoord[0] -= 3;
                            } else {
                                robotDirection = "right";
                                validPosition = true;
                            }
                        } else {
                            curCoord[1] -= 2;
                            curCoord[0] -= 3;
                        }
                        break;
                    case "back":
                        curCoord[1] -= 1;
                        if (curCoord[1] > 1) {
                            validPosition = true;
                        } else {
                            curCoord[1] += 1;
                            validPosition = false;
                        }
                        break;
                    case "left":
                        curCoord[1] += 1;
                        curCoord[0] -= 2;
                        if ((1 <= curCoord[1] && curCoord[1] <= 19)
                                && (1 <= curCoord[0] && curCoord[0] < 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "left")) {
                                validPosition = false;
                                curCoord[1] -= 1;
                                curCoord[0] += 2;
                            } else {
                                robotDirection = "left";
                                validPosition = true;
                            }
                        } else {
                            curCoord[1] -= 1;
                            curCoord[0] += 2;
                        }
                        break;
                    default:
                        robotDirection = "error up";
                        break;
                }
                break;
            case "right":
                switch (direction) {
                    case "forward":
                        curCoord[0] += 1;
                        if (1 < curCoord[0] && curCoord[0] <= 20) {
                            validPosition = true;
                        } else {
                            curCoord[0] -= 1;
                            validPosition = false;
                        }
                        break;
                    case "right":
                        curCoord[0] += 2;
                        curCoord[1] -= 3;
                        if ((1 <= curCoord[1] && curCoord[1] < 20)
                                && (1 < curCoord[0] && curCoord[0] <= 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "down")) {
                                validPosition = false;
                                curCoord[0] -= 2;
                                curCoord[1] += 3;
                            } else {
                                robotDirection = "down";
                                validPosition = true;
                            }
                        } else {
                            curCoord[0] -= 2;
                            curCoord[1] += 3;
                        }
                        break;
                    case "back":
                        curCoord[0] -= 1;
                        if (curCoord[0] > 1) {
                            validPosition = true;
                        } else {
                            curCoord[0] += 1;
                            validPosition = false;
                        }
                        break;
                    case "left":
                        curCoord[0] += 1;
                        curCoord[1] += 2;
                        if ((1 < curCoord[1] && curCoord[1] <= 20)
                                && (1 <= curCoord[0] && curCoord[0] < 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "up")) {
                                validPosition = false;
                                curCoord[0] -= 1;
                                curCoord[1] -= 2;
                            } else {
                                robotDirection = "up";
                                validPosition = true;
                            }
                        } else {
                            curCoord[0] -= 1;
                            curCoord[1] -= 2;
                        }
                        break;
                    default:
                        robotDirection = "error right";
                }
                break;
            case "down":
                switch (direction) {
                    case "forward":
                        curCoord[1] -= 1;
                        if (curCoord[1] >= 1) {
                            validPosition = true;
                        } else {
                            curCoord[1] += 1;
                            validPosition = false;
                        }
                        break;
                    case "right":
                        curCoord[1] -= 2;
                        curCoord[0] -= 3;
                        if ((1 <= curCoord[1] && curCoord[1] <= 19)
                                && (1 <= curCoord[0] && curCoord[0] < 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "left")) {
                                validPosition = false;
                                curCoord[1] += 2;
                                curCoord[0] += 3;
                            } else {
                                robotDirection = "left";
                                validPosition = true;
                            }
                        }
                        else {
                            curCoord[1] += 2;
                            curCoord[0] += 3;
                        }
                        break;
                    case "back":
                        curCoord[1] += 1;
                        if (curCoord[1] < 20) {
                            validPosition = true;
                        } else {
                            curCoord[1] -= 1;
                            validPosition = false;
                        }
                        break;
                    case "left":
                        curCoord[1] -= 1;
                        curCoord[0] += 2;
                        if ((1 < curCoord[1] && curCoord[1] <= 20)
                                && (1 < curCoord[0] && curCoord[0] <= 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "right")) {
                                validPosition = false;
                                curCoord[1] += 1;
                                curCoord[0] -= 2;
                            } else {
                                robotDirection = "right";
                                validPosition = true;
                            }
                        } else {
                            curCoord[1] += 1;
                            curCoord[0] -= 2;
                        }
                        break;
                    default:
                        robotDirection = "error down";
                }
                break;
            case "left":
                switch (direction) {
                    case "forward":
                        curCoord[0] -= 1;
                        if (curCoord[0] >= 1) {
                            validPosition = true;
                        } else {
                            curCoord[0] += 1;
                            validPosition = false;
                        }
                        break;
                    case "right":
                        curCoord[0] -= 2;
                        curCoord[1] += 3;
                        if ((1 < curCoord[1] && curCoord[1] <= 20)
                                && (1 <= curCoord[0] && curCoord[0] < 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "up")) {
                                validPosition = false;
                                curCoord[0] += 2;
                                curCoord[1] -= 3;
                            } else {
                                robotDirection = "up";
                                validPosition = true;
                            }
                        } else {
                            curCoord[0] += 2;
                            curCoord[1] -= 3;
                        }
                        break;
                    case "back":
                        curCoord[0] += 1;
                        if (1 <= curCoord[0] && curCoord[0] < 20) {
                            validPosition = true;
                        } else {
                            curCoord[0] -= 1;
                            validPosition = false;
                        }
                        break;
                    case "left":
                        curCoord[0] -= 1;
                        curCoord[1] -= 2;
                        if ((1 <= curCoord[1] && curCoord[1] < 20)
                                && (1 < curCoord[0] && curCoord[0] <= 20)) {
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord, "down")) {
                                validPosition = false;
                                curCoord[0] += 1;
                                curCoord[1] += 2;
                            } else {
                                robotDirection = "down";
                                validPosition = true;
                            }
                        } else {
                            curCoord[0] += 1;
                            curCoord[1] += 2;
                        }
                        break;
                    default:
                        robotDirection = "error left";
                }
                break;
            default:
                robotDirection = "error moveCurCoord";
                break;
        }

        Logd("Enter checking for obstacles in destination 2x2 grid");
        /*if (getValidPosition())
            // check obstacle for new position
            for (int x = curCoord[0] - 1; x <= curCoord[0]; x++) {
                for (int y = curCoord[1] - 1; y <= curCoord[1]; y++) {
                    for (int i = 0; i < obstacleCoord.size(); i++) {
                        Logd("x-1 = " + (x-1) + ", y = " + y);
                        Logd("obstacleCoord.get(" + i + ")[0] = " + obstacleCoord.get(i)[0]
                                + ", obstacleCoord.get(" + i + ")[1] = " + obstacleCoord.get(i)[1]);
                        if (obstacleCoord.get(i)[0] == (x-1) && obstacleCoord.get(i)[1] == y) { // HERE x
                            setValidPosition(false);
                            robotDirection = backupDirection;
                            break;
                        }
                    }
                    if (!getValidPosition())
                        break;
                }
                if (!getValidPosition())
                    break;
            }*/

        Logd("Exit checking for obstacles in destination 2x2 grid");
        if (getValidPosition())
            this.setCurCoord(curCoord[0], curCoord[1], robotDirection);
        else {
            if (direction.equals("forward") || direction.equals("back"))
                robotDirection = backupDirection;
            this.setCurCoord(oldCoord[0], oldCoord[1], robotDirection);
        }
        this.invalidate();
        Logd("Exiting moveRobot");
    }

    public boolean checkObstaclesRightInFront(int[] coord, ArrayList<int[]> obstacles, String direction) {
        for (int i = 0; i < obstacles.size(); i ++) {
            int[] curObstacle = obstacles.get(i);
            switch (direction) {
                case "up":
                    for (int j = coord[0]; j <= coord[0] + 1; j ++) {
                        for (int k = coord[1] - 1; k <= coord[1]; k ++) {
                            if (curObstacle[0] == j && curObstacle[1] == k) {
                                Logd("Robot has hit an obstacle!!!");
                                return true;
                            }
                        }
                    }
                    break;
                case "down":
                    for (int j = coord[0] - 1; j <= coord[0]; j ++) {
                        for (int k = coord[1]; k <= coord[1] + 1; k ++) {
                            if (curObstacle[0] == j && curObstacle[1] == k) {
                                Logd("Robot has hit an obstacle!!!");
                                return true;
                            }
                        }
                    }
                    break;
                case "left":
                    for (int j = coord[0]; j <= coord[0] + 1; j ++) {
                        for (int k = coord[1]; k <= coord[1] + 1; k ++) {
                            if (curObstacle[0] == j && curObstacle[1] == k) {
                                Logd("Robot has hit an obstacle!!!");
                                return true;
                            }
                        }
                    }
                    break;
                case "right":
                    for (int j = coord[0] - 1; j <= coord[0]; j ++) {
                        for (int k = coord[1] - 1; k <= coord[1]; k ++) {
                            if (curObstacle[0] == j && curObstacle[1] == k) {
                                Logd("Robot has hit an obstacle!!!");
                                return true;
                            }
                        }
                    }
            }
        }
        return false;   // false means no obstacles
    }

    private static class MyDragShadowBuilder extends DragShadowBuilder {
        private Point mScaleFactor;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {
            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            int width;
            int height;

            // Sets the width of the shadow to half the width of the original View
            width = (int) (cells[1][1].endX - cells[1][1].startX);

            // Sets the height of the shadow to half the height of the original View
            height = (int) (cells[1][1].endY - cells[1][1].startY);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);
            // Sets size parameter to member that will be used for scaling shadow image.
            mScaleFactor = size;

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            // Draws the ColorDrawable in the Canvas passed in from the system.
            canvas.scale(mScaleFactor.x/(float)getView().getWidth(),
                    mScaleFactor.y/(float)getView().getHeight());
            getView().draw(canvas);
        }

    }

    // week 8 req to update robot pos when alg sends updates
    public void performAlgoCommand(int x, int y, String direction) {
        Logd("Enter performAlgoCommand");
        Logd("x = " + x + "\n" + "y = " + y);
        if ((x > 1 && x < 21) && (y > -1 && y < 20)) {
            Logd("within grid");
            robotDirection = (robotDirection.equals("None")) ? "up" : robotDirection;
            switch (direction) {
                case "N":
                    robotDirection = "up";
                    break;
                case "S":
                    robotDirection = "down";
                    break;
                case "E":
                    robotDirection = "right";
                    break;
                case "W":
                    robotDirection = "left";
                    break;
            }
        }
        if ((x > 1 && x < 21) && (y > -1 && y < 20)) {
            Logd("within grid");
            setCurCoord(x, y, robotDirection);    // set new coords and direction
            canDrawRobot = true;
        }
        // if robot goes out of frame
        else {
            Logd("set canDrawRobot to false");
            canDrawRobot = false;
            curCoord[0] = -1;
            curCoord[1] = -1;
        }
        this.invalidate();
        Logd("Exit performAlgoCommand");
    }

    public static String saveObstacleList(){
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < obstacleCoord.size(); i ++) {
            Logd("" + obstacleCoord.get(i)[0] + obstacleCoord.get(i)[1]);
            message.append(obstacleCoord.get(i)[0]).append(",").append(obstacleCoord.get(i)[1])
                    .append(",").append(IMAGE_BEARING[obstacleCoord.get(i)[1] - 1][obstacleCoord.get(i)[0] - 1]
                            .charAt(0)).append(",").append(obstacleCoord.get(i)[2]).append("|");
        }
        return message.toString();
    }

    // wk 8 task
    public void updateIDFromRpi(String obstacleID, String imageID) {
        Logd("starting updateIDFromRpi");
        int x = -999;
        int y = -999;
        for (int i = 0; i < obstacleCoord.size(); i ++) {
            if (Integer.parseInt(obstacleID) == obstacleCoord.get(i)[2]) {
                x = obstacleCoord.get(i)[0];
                y = obstacleCoord.get(i)[1];
            }
        }
        IMAGE_LIST[y - 1][x - 1] = imageID;
        this.invalidate();
    }
}