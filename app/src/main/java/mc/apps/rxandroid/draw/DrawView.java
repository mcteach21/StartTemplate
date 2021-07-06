package mc.apps.rxandroid.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawView extends View {
    Paint paint;
    public DrawView(Context context) { //, @Nullable AttributeSet attrs
        super(context);
        init();
    }
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(30, 50, 400, 350, paint);
    }

//    @Override
//    public void draw(Canvas canvas) {
//
//        paint.setColor(Color.BLACK);
//        paint.setStrokeWidth(3);
//        canvas.drawRect(30, 30, 80, 80, paint);
//        paint.setStrokeWidth(0);
//        paint.setColor(Color.CYAN);
//        canvas.drawRect(33, 60, 77, 77, paint);
//        paint.setColor(Color.YELLOW);
//        canvas.drawRect(33, 33, 77, 60, paint);
//
//        super.draw(canvas);
//    }
}
