package com.graphhopper.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView mTextView;
    Button mButton;
    Button mButtonMap;
    Button mButtonPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)

        this.setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.my_text);
        mButton = (Button) findViewById(R.id.my_button);
        mButtonMap = (Button) findViewById(R.id.my_button_map);
        mButtonPath = (Button) findViewById(R.id.my_button_path);
        mTextView.setText("Map the Masses!");
        mButton.setText("Contribute data");
        mButtonMap.setText("View Crowd Density Map");
        mButtonPath.setText("Crowd Sensitive Directions");

        setListeners();
    }

    private void setListeners() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_collection);
                Intent intent = new Intent(MainActivity.this, Collection.class);
                startActivity(intent);
                finish();
            }
        });
        mButtonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_maps);
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mButtonPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_path);
                Intent intent = new Intent(MainActivity.this, MActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
