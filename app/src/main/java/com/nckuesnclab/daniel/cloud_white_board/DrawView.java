package com.nckuesnclab.daniel.cloud_white_board;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

/**
 * Created by Daniel Wang on 2015/10/22.
 */
public class DrawView extends View{

    public static final int IDLE = 0;
    public static final int USED = 1;
    public static final int BLACK = 2;
    public static final int BLUE = 3;
    public static final int RED = 4;
    public static final int ERASER = 5;
    public static final int CLEAR_ALL = 6;
    public static final int ENABLE = 7;
    public static final int DISABLE = 8;

    private boolean reset, enableDraw, drawing;
    private int tempX = 0,tempY = 0, paintNumber;
    private float scaleX = 1, scaleY = 1;
    private Path[] path;
    private Paint[] paint;

    public int touchX, touchY;
    public String sendXY;

    public DrawView(Context context) {
        super(context);
        enableDraw = true;
        drawing = false;
        paintNumber = 1;
        sendXY = "000000000";
        paint = new Paint[1];
        path = new Path[1];
        addPaintPath(paintNumber, DrawView.BLACK);
    }

    public void setScale(float x, float y){
        scaleX = x;
        scaleY = y;
    }

    public void setEnableDraw(boolean b){
        enableDraw = b;
    }

    private void addPaintPath(int number, int color){
        Paint[] tempPaint = new Paint[number];
        tempPaint = Arrays.copyOf(paint, number);
        paint = new Paint[number];
        paint = Arrays.copyOf(tempPaint, number);
        paint[number-1] = new Paint();
        paint[number-1].setAntiAlias(true);
        paint[number-1].setStyle(Paint.Style.STROKE);
        paint[number-1].setStrokeWidth(10);
        switch(color){
            case BLACK:
                paint[number-1].setColor(Color.BLACK);
                break;
            case BLUE:
                paint[number-1].setColor(Color.BLUE);
                break;
            case RED:
                paint[number-1].setColor(Color.RED);
                break;
            case ERASER:
                paint[number-1].setStrokeWidth(100);
                paint[number-1].setColor(Color.WHITE);
                break;
        }
        Path[] tempPath = new Path[number];
        tempPath = Arrays.copyOf(path, number);
        path = new Path[number];
        path = Arrays.copyOf(tempPath, number);
        path[number-1] = new Path();
    }

    public void resetPath(){
        for(int i = 0; i < paintNumber; i++){
            path[i].reset();
        }
        this.invalidate();
    }

    public void changeColor(int color){
        paintNumber++;
        this.addPaintPath(paintNumber, color);
    }

    public void resetAbleDraw(){
        enableDraw = true;
        drawing = false;
    }

    private void drawValue(int x, int y,Path path){
        if(reset && x != 0){
            path.moveTo(x, y);
            reset = false;
        }else{
            if(x == 0 && y == 0){
                reset = true;
            }else{
                int preX = tempX;
                int preY = tempY;
                int distanceX = Math.abs(x-preX);
                int distanceY = Math.abs(y-preY);

                if(distanceX >= 3 || distanceY >= 3){
                    path.lineTo(x, y);
                    tempX = x;
                    tempY = y;
                }
            }
        }
        this.invalidate();
    }

    public void drawLine(int x, int y){
        this.drawValue(x, y, path[paintNumber-1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i = 0; i < paintNumber; i++){
            canvas.drawPath(path[i], paint[i]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int canvasTouchX, canvasTouchY;
        String sendX, sendY;
        if(enableDraw || drawing){
            switch(event.getAction()){
                case MotionEvent.ACTION_UP:
                    sendXY = "000000000";
                    drawing = false;
                    touchX = 0;
                    touchY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    canvasTouchX = (int)event.getX();
                    canvasTouchY = (int)event.getY();
                    //Log.i("test",Integer.toString(canvasTouchX));
                    if(canvasTouchX >= 0 && canvasTouchY >= 0){
                        touchX = canvasTouchX;
                        touchY = canvasTouchY;
                        canvasTouchX = (int)(canvasTouchX/scaleX);
                        canvasTouchY = (int)(canvasTouchY/scaleY);
                        sendX = Integer.toString(canvasTouchX);
                        switch(sendX.length()){
                            case 1:
                                sendX = "000" + sendX;
                                break;
                            case 2:
                                sendX = "00" + sendX;
                                break;
                            case 3:
                                sendX = "0" + sendX;
                                break;
                        }
                        sendY = Integer.toString(canvasTouchY);
                        switch(sendY.length()){
                            case 1:
                                sendY = "000" + sendY;
                                break;
                            case 2:
                                sendY = "00" + sendY;
                                break;
                            case 3:
                                sendY = "0" + sendY;
                                break;
                        }
                        sendXY = sendX + sendY + USED;
                        drawing = true;
                    }
                    break;
            }
        }
        //return super.onTouchEvent(event);
        //use return true
        return true;
    }
}
