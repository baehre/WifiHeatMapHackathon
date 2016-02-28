package mycompany.com.wifiheatmap;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //from tartakynov
    private ScalarKalmanFilter mFiltersCascade[] = new ScalarKalmanFilter[3];

    private SensorManager sensorManager;
    //grabbing compass azimuth
    private float[] gravity;
    private float[] geoMagnetic;
    private float[] linearGravity;

    private float azimuth;
    protected boolean walking;
    protected boolean walkingY;
    protected Queue queue;

    //x and y coordinate of person
    protected int x;
    protected int y;
    private float walkBuffer;
    protected int[][] matrix;
    private int[][] finalMatrix;
    private int size;

    //like a clock north is 0. includes non cardinals (i.e. NE)
    private int direction;

    private TextView angleText;
    private TextView utilityText;
    private TextView queueTextData;
    private Button startButton;


    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize values
        walkBuffer = 0;
        direction = 0;
        walking = false;
        walkingY = true;
        size = 100;
        //assume user starts in the middle;
        x = (size-1)/2;
        y = (size-1)/2;

        matrix = new int[size][size];
        finalMatrix = new int[size][size];

        angleText = (TextView)findViewById(R.id.angle);
        utilityText = (TextView)findViewById(R.id.utility);
        startButton = (Button)findViewById(R.id.startButton);
        queueTextData = (TextView)findViewById(R.id.queue_data);
        queue = new LinkedList();

        //magic from tartakynov
        mFiltersCascade[0] = new ScalarKalmanFilter(1, 1, 0.01f, 0.0025f);
        mFiltersCascade[1] = new ScalarKalmanFilter(1, 1, 0.01f, 0.0025f);
        mFiltersCascade[2] = new ScalarKalmanFilter(1, 1, 0.01f, 0.0025f);


        timerTask = new TimerTask();

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (walking == false) {
                    initListening();
                    startButton.setText("Stop");
                    walking = true;
                    timerTask.execute();
                } else if (walking == true) {
                    stopListening();
                    startButton.setText("Start");
                    queue.clear();
                    queueTextData.setText("");
                    walking = false;
                    timerTask.cancel(true);
                    timerTask = new TimerTask();
                    for(int i = 0; i < size; i++){
                        for(int j = 0; j < size; j++){
                            finalMatrix[i][j] = wifiStrengthColor(matrix[i][j]);
                        }
                    }
                    matrix = new int[size][size];
                    x = (size-1)/2;
                    y = (size-1)/2;
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_25:
                if (checked)
                    size = 25;
                    finalMatrix = new int[size][size];
                    matrix = new int[size][size];
                    x = (size-1)/2;
                    y = (size-1)/2;
                    break;
            case R.id.radio_50:
                if (checked)
                    size = 50;
                    finalMatrix = new int[size][size];
                    matrix = new int[size][size];
                    x = (size-1)/2;
                    y = (size-1)/2;
                    break;
            case R.id.radio_100:
                if (checked)
                    size = 100;
                    finalMatrix = new int[size][size];
                    matrix = new int[size][size];
                    x = (size-1)/2;
                    y = (size-1)/2;
                    break;
        }
    }

    private int wifiStrengthColor(int p) {
        if(p <= -100) {
            return ContextCompat.getColor(this, R.color.C0);
        }
        else if(p <= -80) {
            return ContextCompat.getColor(this, R.color.C15);
        }
        else if (p <= -70) {
            return ContextCompat.getColor(this, R.color.C14);
        }
        else if (p <= -67) {
            return ContextCompat.getColor(this, R.color.C12);
        }
        else if (p <= -64) {
            return ContextCompat.getColor(this, R.color.C10);
        }
        else if (p <= -61) {
            return ContextCompat.getColor(this, R.color.C8);
        }
        else if (p<=-58) {
            return ContextCompat.getColor(this, R.color.C6);
        }
        else if (p<=-55) {
            return ContextCompat.getColor(this, R.color.C5);
        }
        else if (p<=-52) {
            return ContextCompat.getColor(this, R.color.C3);
        }
        else if (p<=-50) {
            return ContextCompat.getColor(this, R.color.C1);
        }
        else {
            return ContextCompat.getColor(this, R.color.C16);
        }
    }

    //update the user in the matrix every second based on direction
    private void updateLocation(){
        int prevY = y;
        int prevX = x;
        if(direction == 0){
            y = y-1;
        }
        else if(direction == 1){
            x = x-1;
            y = y-1;
        }
        else if(direction == 2){
            x = x-1;
        }
        else if(direction == 3){
            x=x-1;
            y=y+1;
        }
        else if(direction == 4){
            y=y+1;
        }
        else if(direction == 5){
            x = x+1;
            y=y+1;
        }
        else if(direction == 6){
            x=x+1;
        }
        else if(direction == 7){
            x=x+1;
            y=y-1;
        }
        if(x<0 || x>size-1){
            x = prevX;
        }
        if(y<0 || y>size-1){
            y = prevY;
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
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopListening(){
        sensorManager.unregisterListener(this);
    }

    protected int getWifiStrength(WifiManager wifiManager){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int rssi = wifiInfo.getRssi();
        return rssi;
    }


    //from tartakynov
    private float filter(float measurement){
        float f1 = mFiltersCascade[0].correct(measurement);
        float f2 = mFiltersCascade[1].correct(f1);
        float f3 = mFiltersCascade[2].correct(f2);
        return f3;
    }

    private boolean checkQueue(){
        Iterator iter = queue.iterator();
        for(int i = 0; i < queue.size(); i++){
            if((float)iter.next() > 1.5){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearGravity = event.values;
                float accelY = filter((float)Math.sqrt(linearGravity[0] * linearGravity[0] + linearGravity[1] * linearGravity[1] + linearGravity[2] * linearGravity[2]));

                if(queue.size() > 3) {
                    queue.remove();
                    queue.add(accelY);
                }
                else {
                    queue.add(accelY);
                }
                boolean check = checkQueue();
                if(check){
                    walkingY = true;
                }
                else{
                    walkingY = false;
                }
                utilityText.setText("Walking: " + walkingY);
                queueTextData.setText(queue.toString());
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geoMagnetic = event.values;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values;
                //utilityText.setText("LINEAR: " + Math.sqrt(linearGravity[0] * linearGravity[0] + linearGravity[1] * linearGravity[1] + linearGravity[2] * linearGravity[2]));
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
                float outputAzimuth = azimuth;
                if (azimuth < 0) {
                    outputAzimuth = azimuth + 360;
                }
                String output = String.format("%.2f", outputAzimuth);
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
        angleText.setText(angleText.getText() + " : DIRECTION: " + direction);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //makes the implements happy
    }

    private class TimerTask extends AsyncTask<Void, Void, Void>{

        private int wifiStrength;

        protected Void doInBackground(Void... eh){
            startWalk();
            return null;
        }

        //user has pressed the start button. they are walking around and ish
        private void startWalk(){
            long last = System.currentTimeMillis();
            while (walking) {
                if(walkingY) {
                    long current = System.currentTimeMillis();
                    if (current - last >= 1000) {
                        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wifiStrength = getWifiStrength(wifi);
                        updateLocation();
                        if (matrix[x][y] == 0) {
                            matrix[x][y] = wifiStrength;
                        }
                        last = current;
                    }
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.heatMap)
        {
            Intent launchNewIntent =
                    new Intent(MainActivity.this, HeatMap.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("MAIN_FINAL_MATRIX", finalMatrix);
            bundle.putSerializable("SIZE", size);
            launchNewIntent.putExtras(bundle);
            startActivity(launchNewIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
