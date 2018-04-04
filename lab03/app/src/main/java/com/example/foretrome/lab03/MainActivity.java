package com.example.foretrome.lab03;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Date;

/**
 * Sensor activity
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private ImageView blackSquare;
    private ImageView ball;

    private int horizontalBorder;
    private int verticalBorder;

    private static long prevHapticTime;
    private Vibrator haptic;
    private Ringtone audio;

    /**
     * Function ran when the activity is created, some elements likely not initialized
     * @param savedInstanceState android default
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ImageViews
        blackSquare = findViewById(R.id.background);
        ball = findViewById(R.id.ball);

        // Initialize border margins
        ViewGroup.MarginLayoutParams margins =
                (ViewGroup.MarginLayoutParams) blackSquare.getLayoutParams();
        horizontalBorder = margins.leftMargin;
        verticalBorder = margins.topMargin;

        // Initialize sensor
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor;
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }

        this.haptic = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        Uri notification = RingtoneManager.getActualDefaultRingtoneUri(getBaseContext(), RingtoneManager.TYPE_NOTIFICATION);
        this.audio = RingtoneManager.getRingtone(getBaseContext(), notification);

        Toast toast = Toast.makeText(getBaseContext(), "Welcome to my ball game", Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Function ran whenever the sensors change (all the time)
     * @param sensorEvent event with sensor data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // If the ball isn't initialized, don't move.
        if ((ball.getX() == 0.0f) || (ball.getY() == 0.0f)) {
            return;
        }

        // Initialize local variables
        float radiusOfBall = ball.getWidth() / 2.0f;
        float leftCoordinate = ball.getX() - horizontalBorder;
        float rightCoordinate = ball.getX() + horizontalBorder + radiusOfBall;
        float topCoordinate = ball.getY() - verticalBorder;
        float bottomCoordinate = ball.getY() + verticalBorder + radiusOfBall;

        // Checks if left side is hit
        if (leftCoordinate < 0.0F) {
            ball.setX(horizontalBorder);
            runCollisionFeedback();
        }

        // Checks if right side is hit
        if (rightCoordinate > blackSquare.getWidth()) {
            ball.setX(blackSquare.getWidth() - horizontalBorder - radiusOfBall);
            runCollisionFeedback();
        }

        // Checks if top is hit
        if (topCoordinate < 0.0F) {
            ball.setY(verticalBorder);
            runCollisionFeedback();
        }

        // Checks if bottom is hit
        if (bottomCoordinate > blackSquare.getHeight()) {
            ball.setY(blackSquare.getHeight() - verticalBorder - radiusOfBall);
            runCollisionFeedback();
        }

        // Set ball locations based on event
        ball.setX(ball.getX() + sensorEvent.values[1]);
        ball.setY(ball.getY() + sensorEvent.values[0]);
    }

    /**
     * Not doing anything. Just have to be here for SensorEventListener.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    private void runCollisionFeedback() {

        long now = new Date().getTime();

        // Limit by time to get cleaner haptics/audio
        if(this.haptic.hasVibrator() && now - prevHapticTime > 250) {

            this.haptic.vibrate(50);

            prevHapticTime = now;

            this.audio.play();
        }

    }
}
