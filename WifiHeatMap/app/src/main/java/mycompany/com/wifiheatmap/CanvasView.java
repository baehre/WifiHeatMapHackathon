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
    private int size;

    public CanvasView(Context c, AttributeSet attrs){
        super(c, attrs);
        context = c;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point sizeA = new Point();
        display.getSize(sizeA);
        int width = sizeA.x;
        int height = sizeA.y;
        size = 100;
        tempWidth = width/size;
        tempHeight = height/size;
        rectWidth = (float)tempWidth;
        rectHeight = (float)tempHeight;
        paint = new Paint();
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                int color = matrix[x][y];
                paint.setColor(color);
                canvas.drawRect(x * rectWidth, y * rectHeight, (x * rectWidth) + rectWidth, (y * rectHeight) + rectHeight, paint);
            }
        }
    }

    public void setMatrix(int[][] m){
        matrix = m;
    }
    public void setSize(int s){
        size = s;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point sizeA = new Point();
        display.getSize(sizeA);
        int width = sizeA.x;
        int height = sizeA.y;
        tempWidth = width/size;
        tempHeight = height/size;
        rectWidth = (float)tempWidth;
        rectHeight = (float)tempHeight;

    }
}
