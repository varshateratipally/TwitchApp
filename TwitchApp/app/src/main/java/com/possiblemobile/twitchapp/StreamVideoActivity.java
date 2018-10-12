package com.possiblemobile.twitchapp;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.possiblemobile.twitchapp.views.StreamFragment;

public abstract class StreamVideoActivity extends AppCompatActivity implements SensorEventListener {

    private static final int SENSOR_DELAY = 400000;
    private static final int FROM_RADS_TO_DEGS = -57;

    private Sensor mRotationSensor;
    public StreamFragment mStreamFragment;

    protected abstract int getLayoutRessource();

    protected abstract int getVideoContainerRessource();

    protected abstract Bundle getStreamArguments();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRessource());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            // If the Fragment is non-null, then it is currently being
            // retained across a configuration change.
            if (mStreamFragment == null) {
                mStreamFragment = StreamFragment.newInstance(getStreamArguments());
                fm.beginTransaction().replace(getVideoContainerRessource(), mStreamFragment, getString(R.string.stream_fragment_tag)).commit();
            }
        }

        try {
            SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor == mRotationSensor && getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                if (event.values.length > 4) {
                    float[] truncatedRotationVector = new float[4];
                    System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                    update(truncatedRotationVector);
                } else {
                    update(event.values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void update(float[] vectors) {
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;

        float[] rotationMatrix = new float[9];
        float[] adjustedRotationMatrix = new float[9];
        float[] orientation = new float[3];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        float roll = orientation[2] * FROM_RADS_TO_DEGS;

        if (roll > -45 && roll < 45) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    protected void resetStream() {
        FragmentManager fm = getSupportFragmentManager();
        mStreamFragment = StreamFragment.newInstance(getStreamArguments());
        fm.beginTransaction().replace(getVideoContainerRessource(), mStreamFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Eww >(
        if (mStreamFragment != null) {
                super.onBackPressed();
                try {
                    mStreamFragment.backPressed();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                this.overrideTransition();
            } else {
            super.onBackPressed();
            this.overrideTransition();
        }
    }

    private void overrideTransition() {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mStreamFragment == null) {
            return;
        }
        mStreamFragment.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mStreamFragment != null) {
                    if (!mStreamFragment.isVideoInterfaceShowing()) {
                        return false;
                    }
                    super.onBackPressed();
                    mStreamFragment.backPressed();

                    overrideTransition();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public View getMainContentLayout() {
        return findViewById(R.id.main_content);

    }
}
