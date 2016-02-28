package mycompany.com.wifiheatmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Andrew on 2/27/2016.
 */
public class CanvasView extends View {

    Context context;
    private double tempWidth;
    private double tempHeight;
    private float rectWidth;
    private float rectHeight;
    private Paint paint;
    private int[][] matrix;

    public CanvasView(Context c, AttributeSet attrs){
        super(c, attrs);
        context = c;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        tempWidth = width/100.0;
        tempHeight = height/100.0;
        rectWidth = (float)tempWidth;
        rectHeight = (float)tempHeight;
        paint = new Paint();
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        for(int x = 0; x < 100; x++){
            for(int y = 0; y < 100; y++){
                if(matrix == null){
                    return;
                }
                int color = matrix[x][y];
                paint.setColor(color);
                canvas.drawRect(x * rectWidth, y * rectHeight, (x * rectWidth) + rectWidth, (y * rectHeight) + rectHeight, paint);
            }
        }
    }

    public void setMatrix(int[][] m){
        matrix = m;
    }
}
