package android.example.multiplesbitmaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class SimpleDrag extends View {

    private final int INVALID_INDEX = -1; //Int que señala que un index es inválido
    private final int mTotalItems = 5; //Es el total de rectángulos
    private ArrayList<Rect> mItemsCollection; //Rectángulos
    private ArrayList<Point> mActiveDragPoints;
    private ArrayList<Rect>  mActiveRects;

    private Paint mPaint;

    public SimpleDrag(Context context) {
        super(context);
        init();
    }

    public SimpleDrag(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleDrag(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE, PorterDuff.Mode.CLEAR);
        for( Rect rect : mItemsCollection){
            canvas.drawRect(rect, mPaint); //dibuja 5 rectangulitos
        }
    }

    private void init(){
        mActiveRects      = new ArrayList<Rect>(mTotalItems);
        mActiveDragPoints = new ArrayList<Point>(mTotalItems);
        mItemsCollection  = new ArrayList<Rect>();
        for( int i = 0; i < mTotalItems; i++){
            Rect rect = new Rect(i * 100, i * 100, (i + 1) * 100, (i + 1) * 100); //Creo 5 rectángulo
            mItemsCollection.add(rect);//Añado esos rectángulos en la lista
        }
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action  = event.getActionMasked();
        final int pointer = event.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN :
                Point touchDown = new Point((int)event.getX(), (int)event.getY());
                lookForIntersection(touchDown);
                break;
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                mActiveDragPoints.removeAll(mActiveDragPoints);
                mActiveRects.removeAll(mActiveRects);
                break;
            case MotionEvent.ACTION_MOVE :
                int count = 0;
                for(Rect rect : mActiveRects)
                {
                    Point curretPoint = new Point((int)event.getX(count), (int)event.getY(count));
                    moveRect(curretPoint, mActiveDragPoints.get(count), rect);
                    count++;
                }
                Log.d(getClass().getName(), "Active Rects" + mActiveRects.size());
                Log.d(getClass().getName(), "Active Points" + mActiveDragPoints.size());
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN :
                touchDown = new Point((int)event.getX(pointer), (int)event.getY(pointer));
                lookForIntersection(touchDown);

                break;
            case MotionEvent.ACTION_POINTER_UP :
                int index = getIntersectionRectIndex(new Point((int)event.getX(pointer), (int)event.getY(pointer)));
                if( index != INVALID_INDEX )
                {
                    Rect rect = mItemsCollection.get(index);
                    mActiveDragPoints.remove(mActiveRects.indexOf(rect));
                    mActiveRects.remove(rect);
                }

                break;

            default:
                break;
        }
        return true;
    }


    private void lookForIntersection(Point touchDown){
        final int index = getIntersectionRectIndex(touchDown); //Toma la posición del objeto

        if(index != INVALID_INDEX ){ //Si la función anterior no entregó un index inválido entonces:
            final Rect rect = mItemsCollection.get(index); //Toma el rectángulo
            if( mActiveRects.indexOf(rect) == INVALID_INDEX ){
                mActiveDragPoints.add(touchDown); //Añade los puntos que hicieron touchdown a la lista mActiveDragPoints
                mActiveRects.add(mItemsCollection.get(index));//Añade el rectángulo acivo a la lista de rectángulos activos
            }
        }
        Log.d(getClass().getName(), "Active Rects" + mActiveRects.size());
        Log.d(getClass().getName(), "Active Points" + mActiveDragPoints.size());

    }

    private int getIntersectionRectIndex(final Point point){
        int index = INVALID_INDEX;
        for(Rect rect : mItemsCollection){
            if( rect.contains(point.x, point.y) ){ //rect.contains(x,y) significa que si (x,y) pertenece dentro de las dimensiones del ractángulo retorna true, sino false
                index = mItemsCollection.indexOf(rect); //index toma la posición donde se encuentra ese rectángulo
                break;
            }
        }
        return index; //retorna la posición del rectángulo
        //TODO bitmap no tendrá contains, pero puedes hacer if...
    }

    private void moveRect(Point currentPoint, Point prevPoint, final Rect rect)
    {
        int xMoved = currentPoint.x - prevPoint.x;
        int yMoved = currentPoint.y - prevPoint.y;
        rect.set(rect.left + xMoved, rect.top + yMoved, rect.right + xMoved, rect.bottom + yMoved);
        mActiveDragPoints.set(mActiveDragPoints.indexOf(prevPoint), currentPoint);
    }

}