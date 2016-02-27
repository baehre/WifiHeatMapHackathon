package mycompany.com.wifiheatmap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class HeatMap extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    //for calculating a step
    private float previousZ;
    private float currentZ;
    //how sensitive we are for checking footstep
    private int threshold;
    //for counting steps
    private int numSteps;
    //grabbing accelerometer values
    private float[] gravity;
    private float[] geoMagnetic;

    private float azimuth;
    private double azimuthDouble;

    private TextView stepNum;
    private TextView angle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        //initialize values
        threshold = 5;
        numSteps = 0;
        previousZ = 0;
        currentZ = 0;
        stepNum = (TextView)findViewById(R.id.stepNum);
        angle = (TextView)findViewById(R.id.angle);
        stepNum.setText("Number of Steps: 0");
        initListening();
    }

    private void initListening(){
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values;
                float z = gravity[2];
                currentZ = z;
                if(Math.abs(currentZ - previousZ) > threshold){
                    numSteps++;
                    stepNum.setText("Number of Steps: " + numSteps);
                }
                previousZ = z;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geoMagnetic = event.values;
                break;
        }
        if(geoMagnetic != null && gravity != null){
            float R[] = new float[9];
            float I[] = new float[9];
            boolean works =
                    SensorManager.getRotationMatrix(R, I, gravity, geoMagnetic);
            if (works)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];
                azimuthDouble = Math.toDegrees(azimuth);
                String output = String.format("%.2f", azimuthDouble);
                angle.setText("Angle(Degrees): " + output);
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //makes the implements happy
    }
}
