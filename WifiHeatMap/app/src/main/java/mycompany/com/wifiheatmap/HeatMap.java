package mycompany.com.wifiheatmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

public class HeatMap extends AppCompatActivity {

    private CanvasView canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        Intent intent = getIntent();
        Bundle bundle = getIntent().getExtras();
        int[][] colorMatrix = (int[][])bundle.getSerializable("MAIN_FINAL_MATRIX");
        canvas = (CanvasView)findViewById(R.id.canvas);
        canvas.setMatrix(colorMatrix);
        canvas.postInvalidate();
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
        if (id == R.id.mainActivity)
        {
            Intent launchNewIntent =
                    new Intent(HeatMap.this, MainActivity.class);
            startActivityForResult(launchNewIntent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
