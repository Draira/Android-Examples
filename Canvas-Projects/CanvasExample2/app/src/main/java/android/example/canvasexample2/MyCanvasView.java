package android.example.canvasexample2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;


public class MyCanvasView extends View {

    private Paint mPaint;
    private Path mPath;
    private int mDrawColor;
    private int mBackgroundColor;
    private Canvas mExtraCanvas;
    private Bitmap mExtraBitmap;
    private Rect mFrame;

    public MyCanvasView(Context context) {
        super(context);
    }

    public MyCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBackgroundColor = ResourcesCompat.getColor(getResources(), R.color.opaque_orange, null);
        mDrawColor = ResourcesCompat.getColor(getResources(), R.color.opaque_yellow, null);

        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(mDrawColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mExtraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mExtraCanvas = new Canvas(mExtraBitmap);
        mExtraCanvas.drawColor(mBackgroundColor);

        int inset = 40;
        mFrame = new Rect (inset, inset, width - inset, height - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //En esencia solo cambia esta parte
        Path clipPath = new Path(); //Se genera un nuevo clipPath
        clipPath.addRoundRect(new RectF(canvas.getClipBounds()), 50,50, Path.Direction.CW); //Y se le da forma, con las esquinas redondeadas
        canvas.clipPath(clipPath);//Luego se le añade esta forma al canvas
        super.onDraw(canvas);

        canvas.drawBitmap(mExtraBitmap, 0, 0, null);
        canvas.drawRect(mFrame, mPaint);
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            mExtraCanvas.drawPath(mPath, mPaint);
        }
    }

    private void touchUp() {
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            default:
                // do nothing
        }
        return true;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
