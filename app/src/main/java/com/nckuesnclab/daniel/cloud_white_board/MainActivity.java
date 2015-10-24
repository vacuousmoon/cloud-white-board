package com.nckuesnclab.daniel.cloud_white_board;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {

    public final static String SCALE_X = "scaleX";
    public final static String SCALE_Y = "scaleY";
    public final static String ROOM_NUMBER = "multicastPort";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        final float scaleX = width/1280;
        final float scaleY = height/720;

        //initial button
        Button create = (Button)this.findViewById(R.id.buttonCreate);
        Button join = (Button)this.findViewById(R.id.buttonJoin);
        Button help = (Button)this.findViewById(R.id.buttonHelp);

        //setOnClickListener
        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                intent.putExtra(SCALE_X, scaleX);
                intent.putExtra(SCALE_Y, scaleY);
                startActivity(intent);
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set a new alert dialog to enter room number
                final EditText editText  = new EditText(MainActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Room number")
                        .setView(editText)
                        .setPositiveButton("Enter",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final int roomNumber = Integer.valueOf(editText.getText().toString());
                                        Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                                        intent.putExtra(SCALE_X, scaleX);
                                        intent.putExtra(SCALE_Y, scaleY);
                                        intent.putExtra(ROOM_NUMBER, roomNumber);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton("Cancel",null)
                        .show();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("how to use")
                        .setMessage("App only works under same WIFI access point.\n\n"
                                + "Create: creating a white board\n\n"
                                + "Join: joining an existing white board")
                        .setPositiveButton("ok", null)
                        .show();
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
