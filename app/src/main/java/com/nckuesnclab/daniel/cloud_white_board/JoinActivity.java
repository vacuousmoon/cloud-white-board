package com.nckuesnclab.daniel.cloud_white_board;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class JoinActivity extends Activity {

    private int multicastPort, paintChange = 0, resend = 0;
    private int preX = 0, preY = 0, drawX, drawY;
    private float scaleX, scaleY;
    private boolean localReset = true, buttonDisable = false;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //add drawView
        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        RelativeLayout relativeLayout = (RelativeLayout)this.findViewById(R.id.relativeLayoutJoin);
        relativeLayout.addView(drawView);
        drawView.drawLine(0, 0);

        //get intent
        Intent intent = this.getIntent();
        scaleX = intent.getFloatExtra(MainActivity.SCALE_X, 1);
        scaleY = intent.getFloatExtra(MainActivity.SCALE_Y, 1);
        drawView.setScale(scaleX, scaleY);
        multicastPort = intent.getIntExtra(MainActivity.ROOM_NUMBER, 1000);

        //initial button
        final Button blackMarker = (Button)this.findViewById(R.id.buttonBlackJoin);
        final Button blueMarker = (Button)this.findViewById(R.id.buttonBlueJoin);
        final Button redMarker = (Button)this.findViewById(R.id.buttonRedJoin);
        final Button eraser = (Button)this.findViewById(R.id.buttonEraserJoin);
        final Button clearAll = (Button)this.findViewById(R.id.buttonClearJoin);
        final Button screenShot = (Button)this.findViewById(R.id.buttonScreenShotJoin);
        Button information = (Button)this.findViewById(R.id.buttonInformationJoin);

        blackMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintChange = DrawView.BLACK;
            }
        });

        redMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintChange = DrawView.RED;
            }
        });

        blueMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintChange = DrawView.BLUE;
            }
        });

        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintChange = DrawView.ERASER;
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remind user
                new AlertDialog.Builder(JoinActivity.this)
                        .setTitle("Clear All?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        paintChange = DrawView.CLEAR_ALL;
                                    }
                                })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = new ImageView(JoinActivity.this);
                img.setBackgroundResource(R.drawable.introduction_join);
                new AlertDialog.Builder(JoinActivity.this)
                        .setTitle("Your room number: " + multicastPort)
                        .setView(img)
                        .setPositiveButton("OK", null)
                        .show();
                if(localReset){
                    drawView.resetAbleDraw();
                    drawView.drawLine(0, 0);
                    if(buttonDisable){
                        blackMarker.setEnabled(true);
                        blueMarker.setEnabled(true);
                        redMarker.setEnabled(true);
                        eraser.setEnabled(true);
                        clearAll.setEnabled(true);
                        screenShot.setEnabled(true);
                        buttonDisable = false;
                    }
                }
            }
        });

        //screenShot
        final Runnable screenShotRunnable = new Runnable(){
            @Override
            public void run() {
                drawView.setDrawingCacheEnabled(true);
                Bitmap bitmap = drawView.getDrawingCache();
                saveScreen(bitmap);
                drawView.setDrawingCacheEnabled(false);
            }
        };

        screenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread screenShotThread = new Thread(screenShotRunnable);
                screenShotThread.start();
                Toast.makeText(JoinActivity.this, "screenshot finish", Toast.LENGTH_SHORT).show();
            }
        });

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case DrawView.IDLE:
                        drawView.drawLine(drawX, drawY);
                        if(localReset){
                            drawView.setEnableDraw(true);
                            if(buttonDisable){
                                blackMarker.setEnabled(true);
                                blueMarker.setEnabled(true);
                                redMarker.setEnabled(true);
                                eraser.setEnabled(true);
                                clearAll.setEnabled(true);
                                screenShot.setEnabled(true);
                                buttonDisable = false;
                            }
                        }
                        break;
                    case DrawView.USED:
                        drawView.drawLine(drawX, drawY);
                        drawView.setEnableDraw(false);
                        if(!buttonDisable){
                            blackMarker.setEnabled(false);
                            blueMarker.setEnabled(false);
                            redMarker.setEnabled(false);
                            eraser.setEnabled(false);
                            clearAll.setEnabled(false);
                            screenShot.setEnabled(false);
                            buttonDisable = true;
                        }
                        break;
                    case DrawView.BLACK:
                        drawView.changeColor(DrawView.BLACK);
                        break;
                    case DrawView.BLUE:
                        drawView.changeColor(DrawView.BLUE);
                        break;
                    case DrawView.RED:
                        drawView.changeColor(DrawView.RED);
                        break;
                    case DrawView.ERASER:
                        drawView.changeColor(DrawView.ERASER);
                        break;
                    case DrawView.CLEAR_ALL:
                        drawView.changeColor(DrawView.CLEAR_ALL);
                        break;
                    case DrawView.ENABLE:
                        drawView.setEnableDraw(true);
                        localReset = true;
                        if(buttonDisable){
                            blackMarker.setEnabled(true);
                            blueMarker.setEnabled(true);
                            redMarker.setEnabled(true);
                            eraser.setEnabled(true);
                            clearAll.setEnabled(true);
                            screenShot.setEnabled(true);
                            buttonDisable = false;
                        }
                        break;
                    case DrawView.DISABLE:
                        drawView.setEnableDraw(false);
                        localReset = false;
                        if(!buttonDisable){
                            blackMarker.setEnabled(false);
                            blueMarker.setEnabled(false);
                            redMarker.setEnabled(false);
                            eraser.setEnabled(false);
                            clearAll.setEnabled(false);
                            screenShot.setEnabled(false);
                            buttonDisable = true;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        Runnable sendRunnable = new Runnable() {
            @Override
            public void run() {
                //set multicastSocket
                try {
                    InetAddress inetAddress= InetAddress.getByName(CreateActivity.MULTICAST_IP);
                    MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
                    multicastSocket.setTimeToLive(255);
                    multicastSocket.joinGroup(inetAddress);
                    //send data
                    DatagramPacket datagramPacket;
                    byte[] data;
                    while(true){
                        if(preX != drawView.touchX || preY != drawView.touchY){
                            data = drawView.sendXY.getBytes();
                            datagramPacket = new DatagramPacket(data, data.length, inetAddress, multicastPort);
                            multicastSocket.send(datagramPacket);
                            preX = drawView.touchX;
                            preY = drawView.touchY;
                            resend = 0;
                        }else if(paintChange != DrawView.IDLE){
                            String temp = "00000000" + Integer.toString(paintChange);
                            data = temp.getBytes();
                            datagramPacket = new DatagramPacket(data, data.length, inetAddress, multicastPort);
                            multicastSocket.send(datagramPacket);
                            paintChange = DrawView.IDLE;
                        }else if(drawView.sendXY.equals("000000000")){
                            if(resend < 3){
                                data = drawView.sendXY.getBytes();
                                datagramPacket = new DatagramPacket(data, data.length, inetAddress, multicastPort);
                                multicastSocket.send(datagramPacket);
                                resend++;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };

        Runnable receiveRunnable = new Runnable(){
            @Override
            public void run() {
                try {
                    //set multicastSocket
                    InetAddress inetAddress = InetAddress.getByName(CreateActivity.MULTICAST_IP);
                    MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
                    multicastSocket.setTimeToLive(255);
                    multicastSocket.joinGroup(inetAddress);
                    //receive data
                    DatagramPacket datagramPacket;
                    while(true){
                        byte[] data = new byte[9];
                        datagramPacket = new DatagramPacket(data, data.length, inetAddress, multicastPort);
                        multicastSocket.receive(datagramPacket);
                        String getXY = new String(data);
                        drawX = Integer.valueOf(getXY.substring(0, 4));
                        drawX = (int)(drawX*scaleX);
                        drawY = Integer.valueOf(getXY.substring(4, 8));
                        drawY = (int)(drawY*scaleY);
                        int controlNumber = Integer.valueOf(getXY.substring(8));
                        handler.obtainMessage(controlNumber).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread sendThread = new Thread(sendRunnable);
        Thread receiveThread = new Thread(receiveRunnable);
        sendThread.start();
        receiveThread.start();

    }

    private void saveScreen(Bitmap bitmap){

        //make dir
        File folder = new File(Environment.getExternalStorageDirectory() + "/CloudWhiteBoard/");
        if(!folder.exists()){
            folder.mkdir();
        }

        //number of picture int the dir
        int i = 0;
        File screenImage = new File(Environment.getExternalStorageDirectory() + "/CloudWhiteBoard/screen_shot_" + i + ".png");
        while(screenImage.exists()){
            i++;
            screenImage = new File(Environment.getExternalStorageDirectory() + "/CloudWhiteBoard/screen_shot_" + i + ".png");
        }

        //save screen
        try {
            FileOutputStream out = new FileOutputStream(screenImage);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
