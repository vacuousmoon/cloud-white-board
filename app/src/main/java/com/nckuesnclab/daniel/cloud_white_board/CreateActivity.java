package com.nckuesnclab.daniel.cloud_white_board;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;


public class CreateActivity extends Activity {

    public static final String MULTICAST_IP = "224.1.1.10";
    private int multicastPort, paintChange = 0, resend = 0;
    private int preX = 0, preY = 0, drawX, drawY;
    private float scaleX, scaleY;
    private boolean enableWrite = true, buttonDisable = false;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        //add drawView
        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        RelativeLayout relativeLayout = (RelativeLayout)this.findViewById(R.id.relativeLayoutCreate);
        relativeLayout.addView(drawView);
        drawView.drawLine(0, 0);

        //get intent
        Intent intent = this.getIntent();
        scaleX = intent.getFloatExtra(MainActivity.SCALE_X, 1);
        scaleY = intent.getFloatExtra(MainActivity.SCALE_Y, 1);
        drawView.setScale(scaleX, scaleY);

        multicastPort = (int)(Math.random()*8999 + 1001);
        //tell user their room number
        new AlertDialog.Builder(CreateActivity.this)
                .setTitle("room number is: " + Integer.toString(multicastPort))
                .setPositiveButton("OK", null)
                .show();

        //initial button
        final Button blackMarker = (Button)this.findViewById(R.id.buttonBlack);
        final Button blueMarker = (Button)this.findViewById(R.id.buttonBlue);
        final Button redMarker = (Button)this.findViewById(R.id.buttonRed);
        final Button eraser = (Button)this.findViewById(R.id.buttonEraser);
        final Button clearAll = (Button)this.findViewById(R.id.buttonClear);
        final Button setAbleWrite = (Button)this.findViewById(R.id.buttonEnableWrite);
        Button information = (Button)this.findViewById(R.id.buttonInformation);

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
                new AlertDialog.Builder(CreateActivity.this)
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

        setAbleWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enableWrite){
                    setAbleWrite.setBackgroundResource(R.drawable.disable_write);
                    enableWrite = false;
                    paintChange = DrawView.DISABLE;
                }else{
                    setAbleWrite.setBackgroundResource(R.drawable.enable_write);
                    enableWrite = true;
                    paintChange = DrawView.ENABLE;
                }
            }
        });

        information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = new ImageView(CreateActivity.this);
                img.setBackgroundResource(R.drawable.introduction_create);
                new AlertDialog.Builder(CreateActivity.this)
                        .setTitle("Your room number: " + multicastPort)
                        .setView(img)
                        .setPositiveButton("OK", null)
                        .show();
                if(buttonDisable){
                    blackMarker.setEnabled(true);
                    blueMarker.setEnabled(true);
                    redMarker.setEnabled(true);
                    eraser.setEnabled(true);
                    clearAll.setEnabled(true);
                    buttonDisable = false;
                }
                drawView.setEnableDraw(true);
            }
        });

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case DrawView.IDLE:
                        drawView.drawLine(drawX, drawY);
                        drawView.setEnableDraw(true);
                        if(buttonDisable){
                            blackMarker.setEnabled(true);
                            blueMarker.setEnabled(true);
                            redMarker.setEnabled(true);
                            eraser.setEnabled(true);
                            clearAll.setEnabled(true);
                            buttonDisable = false;
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
                }
                super.handleMessage(msg);
            }
        };

        Runnable sendRunnable = new Runnable() {
            @Override
            public void run() {
                //set multicastSocket
                try {
                    InetAddress inetAddress= InetAddress.getByName(MULTICAST_IP);
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
                         if(resend<3){
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
                    InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);
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

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);
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
