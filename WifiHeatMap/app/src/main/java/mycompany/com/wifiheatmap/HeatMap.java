package mycompany.com.wifiheatmap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class HeatMap extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    //grabbing compass azimuth
    private float[] gravity;
    private float[] geoMagnetic;

    private float azimuth;
    private int feet;
    private int inches;
    private int height;
    private double stepLength;
    private boolean walking;

    //x and y coordinate of person
    private int x;
    private int y;

    //like a clock north is 0. includes non cardinals (i.e. NE)
    private int direction;

    private TextView angleText;
    private Button startButton;
    private Spinner feetSpinner;
    private Spinner inchesSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        //initialize values
        direction = 0;
        walking = false;

        angleText = (TextView)findViewById(R.id.angle);
        startButton = (Button)findViewById(R.id.startButton);
        feetSpinner = (Spinner)findViewById(R.id.feet);
        inchesSpinner = (Spinner)findViewById(R.id.inches);

        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(startButton.getText() == "Start"){
                    startButton.setText("Stop");
                    feet = (int)feetSpinner.getSelectedItem();
                    inches = (int)inchesSpinner.getSelectedItem();
                    height = feet * 12 + inches;
                    stepLength = 0.415 * height;
                    walking = true;
                    startWalk();
                }
                else if(startButton.getText() == "Stop"){
                    startButton.setText("Start");
                    walking = false;
                }
            }
        });
        initListening();
    }

    private void startWalk(){
        long last = System.currentTimeMillis();
        while (walking) {
            long current = System.currentTimeMillis();
            if(current-last>=1000){
                WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                int wifiStrength = getWifiStrength(wifi);
                last = current;
            }
        }
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

    private int getWifiStrength(WifiManager wifiManager){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = wifiInfo.getRssi();
        return level;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values;
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
                azimuth = -azimuth*360/(2*3.14159265358979323f);
                String output = String.format("%.2f", azimuth);
                angleText.setText("Angle(Degrees): " + output);
                checkDirection();
            }
        }
    }

    public void checkDirection(){
        float tempAzimuth;
        if(azimuth < 0){
            tempAzimuth = azimuth + 360;
        }
        else{
            tempAzimuth = azimuth;
        }
        if(tempAzimuth<=22.5 || tempAzimuth>337.5){
            direction = 0;
        }
        else if(tempAzimuth<=67.5 && tempAzimuth>22.5){
            direction = 1;
        }
        else if(tempAzimuth<=112.5 && tempAzimuth>67.5){
            direction = 2;
        }
        else if(tempAzimuth<=157.5 && tempAzimuth>112.5){
            direction = 3;
        }
        else if(tempAzimuth<=202.5 && tempAzimuth>157.5){
            direction = 4;
        }
        else if(tempAzimuth<=247.5 && tempAzimuth>202.5){
            direction = 5;
        }
        else if(tempAzimuth<=292.5 && tempAzimuth>247.5){
            direction = 6;
        }
        else if(tempAzimuth<=337.5 && tempAzimuth>292.5){
            direction = 7;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //makes the implements happy
    }
}
